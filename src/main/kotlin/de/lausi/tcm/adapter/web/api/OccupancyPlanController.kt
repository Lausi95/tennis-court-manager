package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.IsoDate
import de.lausi.tcm.domain.model.*
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

  val links: Map<String, String>
)

@Controller
@RequestMapping("/api/occupancy-plan")
class OccupancyPlanController(
  private val courtRepository: CourtRepository,
  private val occupancyPlanService: OccupancyPlanService,
) {

  @ResponseBody
  @GetMapping(headers = ["accept=application/json"])
  fun getCourtCollection(@RequestParam(name = "date") @IsoDate planDate: LocalDate): OccupancyPlanModel {
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

    return OccupancyPlanModel(
      courtModels,
      todayDateString,
      planDateString,
      links,
    )
  }

  @GetMapping
  fun getOccupancyPlan(model: Model, @RequestParam @IsoDate date: LocalDate): String {
    model.addAttribute("occupancyPlan", getCourtCollection(date))
    return "views/occupancy-plan"
  }
}
