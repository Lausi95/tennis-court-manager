package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.IsoDate
import de.lausi.tcm.domain.model.BlockType
import de.lausi.tcm.domain.model.CourtRepository
import de.lausi.tcm.domain.model.OccupancyPlanService
import de.lausi.tcm.domain.model.Slot
import de.lausi.tcm.iso
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.security.Principal
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
@RequestMapping("/occupancy-plan")
class OccupancyPlanController(
  private val courtRepository: CourtRepository,
  private val occupancyPlanService: OccupancyPlanService,
) {

  data class OccupancyPlanCsvParams(
    @IsoDate val from: LocalDate?,
    @IsoDate val to: LocalDate?,
    val dl: Boolean = false,
  )

  @GetMapping("/report.csv")
  @ResponseBody
  fun getOccupancyPlanCsv(params: OccupancyPlanCsvParams): ResponseEntity<String> {
    val fromDate = params.from ?: LocalDate.now()
    val toDate = params.to ?: LocalDate.now()

    var csv = "user,description,slot-start-date,slot-start-time,slot-end-date,slot-end-time,court-number,type\n"

    val courts = courtRepository.findAll()
    val courtIds = courts.map { it.id }

    var dateIt = fromDate
    do {
      val plan = occupancyPlanService.getOccupancyPlan(dateIt, courtIds)
      courts.forEach {
        val blocks = plan.blocksByCourt[it.id]
        blocks?.forEach { block ->
          val user = if (block.type == BlockType.FREE_PLAY) block.description.split(",")[0] else ""
          val description =
            if (block.type != BlockType.FREE && block.type != BlockType.FREE_PLAY) block.description else ""
          val slotStartDate = dateIt.iso()
          val slotStartTime = block.fromSlot.formatFromTimeIso()
          val slotEndDate = dateIt.iso()
          val slotEndTime = block.toSlot.formatToTimeIso()
          val courtNumber = it.name
          val type = block.type.toString()
          csv += "\"$user\",\"$description\",$slotStartDate,$slotStartTime,$slotEndDate,$slotEndTime,$courtNumber,$type\n"
        }
      }
      dateIt = dateIt.plusDays(1L)
    } while (dateIt.isBefore(toDate))

    return if (params.dl) {
      ResponseEntity.ok()
        .header("Content-Type", "text/csv")
        .header("Content-Disposition", "attachment; filename=\"occupancy-plan.csv\"\n")
        .body(csv)
    } else {
      ResponseEntity.ok()
        .header("Content-Type", "text/csv")
        .body(csv)
    }
  }

  @GetMapping
  fun getOccupancyPlan(
    principal: Principal,
    model: Model,
    @RequestParam(name = "date") @IsoDate planDate: LocalDate
  ): String {
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
          blockLinks["book"] =
            "/reservations?use=create&date=${planDateString}&slotId=${it.fromSlot.index}&courtId=${court.id.value}"
        }

        BlockModel(
          it.type,
          it.fromSlot.formatFromTime(),
          it.toSlot.formatToTime(),
          Slot.distance(it.fromSlot, it.toSlot),
          Slot.formatDuration(it.fromSlot, it.toSlot),
          it.description,
          false, // TODO reinvent core time logic
          blockLinks,
        )
      }
      CourtOccupancyPlanModel(court.name.value, blockModels)
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

    return "views/occupancy-plan/view"
  }
}
