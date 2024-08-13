package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.IsoDate
import de.lausi.tcm.domain.model.*
import de.lausi.tcm.iso
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BlockModel(
  val type: BlockType,
  val fromTime: String,
  val toTime: String,
  val slots: Int,
  val duration: String,
  val text: String,
  val core: Boolean,

  val links: Map<String, String>
)

data class CourtOccupancyPlanModel(val courtName: String, val blocks: List<BlockModel>)

data class OccupancyPlanModel(
  val items: List<CourtOccupancyPlanModel>,

  val todayDate: String,
  val planDate: String,
  val dayOfWeek: DayOfWeekModel,

  val links: Map<String, String>
)

@Controller
@RequestMapping("/api/occupancy-plan")
class OccupancyPlanController(
  private val courtRepository: CourtRepository,
  private val occupancyPlanService: OccupancyPlanService,
) {

  @GetMapping("/report.csv")
  @ResponseBody
  fun getOccupancyPlanCsv(@RequestParam @IsoDate from: LocalDate?, @RequestParam @IsoDate to: LocalDate?, @RequestParam(defaultValue = "false") dl: Boolean): ResponseEntity<String> {
    val fromDate = from ?: LocalDate.now()
    val toDate = to ?: LocalDate.now()

    var csv = "user,description,slot-start-date,slot-start-time,slot-end-date,slot-end-time,court-number,type\n"

    val courts = courtRepository.findAll()
    val courtIds = courts.map { it.id }

    var dateIt = fromDate;
    do {
      val plan = occupancyPlanService.getOccupancyPlan(dateIt, courtIds)
      courts.forEach {
        val blocks = plan.blocksByCourt[it.id]
        blocks?.forEach { block ->
          val user = if (block.type == BlockType.FREE_PLAY) block.description.split(",")[0] else ""
          val description = if (block.type != BlockType.FREE && block.type != BlockType.FREE_PLAY) block.description else ""
          val slotStartDate = dateIt.iso()
          val slotStartTime = formatFromTimeIso(block.fromSlot)
          val slotEndDate = dateIt.iso()
          val slotEndTime = formatToTimeIso(block.toSlot)
          val courtNumber = it.name
          val type = block.type.toString()
          csv += "\"$user\",\"$description\",$slotStartDate,$slotStartTime,$slotEndDate,$slotEndTime,$courtNumber,$type\n"
        }
      }
      dateIt = dateIt.plusDays(1L)
    } while (dateIt.isBefore(toDate))

    return if (dl) {
      ResponseEntity.ok()
        .header("Content-Type", "text/csv")
        .header("Content-Disposition", "attachment; filename=\"occupancy-plan.csv\"\n")
        .body(csv);
    } else {
      ResponseEntity.ok()
        .header("Content-Type", "text/csv")
        .body(csv);
    }
  }

  @GetMapping
  fun getOccupancyPlan(model: Model, @RequestParam(name = "date") @IsoDate planDate: LocalDate): String {
    val dateFormatter = DateTimeFormatter.ISO_DATE

    val todayDate = LocalDate.now()
    val prevPlanDate = planDate.minusDays(1)
    val nextPlanDate = planDate.plusDays(1)

    val todayDateString = dateFormatter.format(todayDate)
    val planDateString = dateFormatter.format(planDate)
    val prevCourtDateString = dateFormatter.format(prevPlanDate)
    val nextCourtDateString = dateFormatter.format(nextPlanDate)

    val courts = courtRepository.findAll()
    val courtIds = courts.map { it.id }

    val occupancyPlan = occupancyPlanService.getOccupancyPlan(planDate, courtIds)

    val courtModels = courts.map { court ->
      val blockModels = occupancyPlan.render(court.id).map {
        val blockLinks = mutableMapOf<String, String>()
        if (it.type == BlockType.FREE) {
          blockLinks["book"] = "/reservations?date=${planDateString}&slotId=${it.fromSlot}&courtId=${court.id}"
        }

        BlockModel(
          it.type,
          formatFromTime(it.fromSlot),
          formatToTime(it.toSlot),
          slotAmount(it.fromSlot, it.toSlot),
          formatDuration(it.fromSlot, it.toSlot),
          it.description,
          planDate.dayOfWeek.isWeekend() || isCoreTimeSlot(it.fromSlot),
          blockLinks,
        )
      }
      CourtOccupancyPlanModel(court.name, blockModels)
    }

    val links = mutableMapOf(
      "self" to "?date=$planDateString",
      "nextPlan" to "/?date=$nextCourtDateString",
    )
    if (planDate > todayDate) {
      links["prevPlan"] = "/?date=$prevCourtDateString"
    }

    val dayOfWeek = DAY_OF_WEEK_MODELS.find { it.id == planDate.dayOfWeek }!!

    val occupancyPlanModel = OccupancyPlanModel(
      courtModels,
      todayDateString,
      planDateString,
      dayOfWeek,
      links,
    )

    model.addAttribute("occupancyPlan", occupancyPlanModel)

    return "views/occupancy-plan"
  }
}
