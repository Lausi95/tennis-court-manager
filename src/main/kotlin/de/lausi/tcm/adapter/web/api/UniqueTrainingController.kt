package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class UniqueTrainingModel(
  val id: String,
  val date: String,
  val court: CourtModel,
  val fromTime: String,
  val toTime: String,
  val description: String,
  val links: Map<String, String> = mapOf())

data class UniqueTrainingCollection(
  val items: List<UniqueTrainingModel>,
  val links: Map<String, String> = mapOf())

data class CreateUniqueTrainingRequest(
  val date: LocalDate,
  val courtId: String,
  val fromSlotId: Int,
  val toSlotId: Int,
  val description: String)

@Controller
@RequestMapping("/api/unique-trainings")
class UniqueTrainingController(
  private val courtRepository: CourtRepository,
  private val courtController: CourtController,
  private val uniqueTrainingRepository: UniqueTrainingRepository,
  private val slotController: SlotController,
  private val memberService: MemberService,
  private val uniqueTrainingService: UniqueTrainingService,
  private val occupancyPlanService: OccupancyPlanService,
  private val courtService: CourtService,
) {

  @GetMapping
  fun getUniqueTrainings(model: Model): String {
    val items = uniqueTrainingRepository.findByDateGreaterThanEqual(LocalDate.now()).map {
      val court = with (courtController) { courtService.getCourt(it.courtId)?.toModel() ?: CourtModel.NOT_FOUND }

      UniqueTrainingModel(
        it.id,
        it.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
        court,
        formatFromTime(it.fromSlot),
        formatToTime(it.toSlot),
        it.description,
        mapOf(
          "delete" to "/api/unique-trainings/${it.id}"
        )
      )
    }

    model.addAttribute("uniqueTrainingCollection", UniqueTrainingCollection(items, mapOf(
      "self" to "/api/unique-trainings",
      "create" to "/api/unique-trainings",
    )))

    courtController.getCourts(model)
    slotController.getSlots(model)

    return "views/unique-trainings"
  }

  @PostMapping
  fun createUniqueTraining(model: Model, principal: Principal, request: CreateUniqueTrainingRequest): String {
    memberService.getMember(principal.name).assertRoles(Group.TRAINER)

    val errors = mutableListOf<String>()

    if (!courtRepository.existsById(request.courtId)) {
      errors.add("Der angegebene Platz eixistiert nicht.")
    }

    if (request.toSlotId < request.fromSlotId) {
      errors.add("Die End-Zeit kann nicht vor der Start-Zeit liegen.")
    }

    val uniqueTraining = UniqueTraining(
      UUID.randomUUID().toString(),
      request.date,
      request.courtId,
      request.fromSlotId,
      request.toSlotId,
      request.description
    )

    with(uniqueTrainingService) {
      val reservationBlock = uniqueTraining.toBlock()
      val occupancyPlan = occupancyPlanService.getOccupancyPlan(request.date, listOf(request.courtId))
      if (!occupancyPlan.canPlace(request.courtId, reservationBlock)) {
        errors.add("Der Platz ist zu dem Zeitraum schon belegt.")
      }
    }

    if (errors.isNotEmpty()) {
      model.addAttribute("errors", errors)
      return getUniqueTrainings(model)
    }

    uniqueTrainingRepository.save(uniqueTraining)

    return getUniqueTrainings(model)
  }

  @DeleteMapping("/{uniqueTrainingId}")
  fun deleteUniqueTraining(model: Model, principal: Principal, @PathVariable uniqueTrainingId: String): String {
    memberService.getMember(principal.name).assertRoles(Group.TRAINER)
    uniqueTrainingRepository.deleteById(uniqueTrainingId)
    return getUniqueTrainings(model)
  }
}
