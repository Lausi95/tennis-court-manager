package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

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
  val courtRepository: CourtRepository,
  val courtController: CourtController,
  val uniqueTrainingRepository: UniqueTrainingRepository,
  private val slotController: SlotController
) {

  @GetMapping
  fun getUniqueTrainings(model: Model): String {
    val items = uniqueTrainingRepository.findByDateGreaterThanEqual(LocalDate.now()).map {
      val court = courtRepository.findById(it.courtId).map { c -> CourtModel(c.id, c.name) }.orElseGet { CourtModel("", "???") }
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
  fun createUniqueTraining(model: Model, request: CreateUniqueTrainingRequest): String {
    val errors = mutableListOf<String>()

    if (!courtRepository.existsById(request.courtId)) {
      errors.add("Der angegebene Platz eixistiert nicht.")
    }

    if (request.toSlotId < request.fromSlotId) {
      errors.add("Die End-Zeit kann nicht vor der Start-Zeit liegen.")
    }

    // TODO collision test

    if (errors.isNotEmpty()) {
      model.addAttribute("errors", errors)
      return getUniqueTrainings(model)
    }

    val uniqueTraining = UniqueTraining(
      UUID.randomUUID().toString(),
      request.date,
      request.courtId,
      request.fromSlotId,
      request.toSlotId,
      request.description
    )

    uniqueTrainingRepository.save(uniqueTraining)

    return getUniqueTrainings(model)
  }

  @DeleteMapping("/{uniqueTrainingId}")
  fun deleteUniqueTraining(model: Model, @PathVariable uniqueTrainingId: String): String {
    uniqueTrainingRepository.deleteById(uniqueTrainingId)
    return getUniqueTrainings(model)
  }
}
