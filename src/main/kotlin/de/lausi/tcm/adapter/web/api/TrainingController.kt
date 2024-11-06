package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.memberId
import de.lausi.tcm.application.CreateTrainingCommand
import de.lausi.tcm.application.TrainingUseCase
import de.lausi.tcm.domain.model.*
import de.lausi.tcm.ger
import de.lausi.tcm.iso

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.DayOfWeek
import java.time.LocalDate

data class DayOfWeekModel(val id: DayOfWeek, val name: String)

val DAY_OF_WEEK_MODELS = listOf(
  DayOfWeekModel(DayOfWeek.MONDAY, "Montag"),
  DayOfWeekModel(DayOfWeek.TUESDAY, "Dienstag"),
  DayOfWeekModel(DayOfWeek.WEDNESDAY, "Mittwoch"),
  DayOfWeekModel(DayOfWeek.THURSDAY, "Donnerstag"),
  DayOfWeekModel(DayOfWeek.FRIDAY, "Freitag"),
  DayOfWeekModel(DayOfWeek.SATURDAY, "Samstag"),
  DayOfWeekModel(DayOfWeek.SUNDAY, "Sonntag"),
)

data class TrainingModel(
  val id: String,
  val court: CourtModel,
  val dayOfWeek: DayOfWeekModel,
  val fromTime: String,
  val toTime: String,
  val description: String,
  val skippedDays: Set<SkippedDateModel>,
  val links: Map<String, String>
)

data class TrainingCollection(
  val items: List<TrainingModel>,
  val count: Int,
  val daysOfWeek: List<DayOfWeekModel>,
  val links: Map<String, String>
)

data class SkippedDateModel(
  val date: String,
  val links: Map<String, String>,
)

data class AddSkippedDateRequest(
  val date: LocalDate,
)

@Controller
@RequestMapping("/api/trainings")
class TrainingController(
  private val trainingUseCase: TrainingUseCase,
  private val courtController: CourtController,
  private val slotController: SlotController,
) {

  @GetMapping
  fun getTrainings(model: Model): String {
    val items = trainingUseCase.getAllTrainings().map { it.toModel() }

    val trainingCollection = TrainingCollection(items, items.size, DAY_OF_WEEK_MODELS, mapOf())

    model.addAttribute("trainingCollection", trainingCollection)

    courtController.getCourts(model)
    slotController.getSlots(model)

    return "views/trainings"
  }

  fun Training.toModel(): TrainingModel {
    val courtModel = with (courtController) {
      return@with trainingUseCase.getCourt(courtId).toModel()
    }

    return TrainingModel(
      id,
      courtModel,
      DAY_OF_WEEK_MODELS.find { it.id == dayOfWeek }!!,
      formatFromTime(fromSlot),
      formatToTime(toSlot),
      description,
      skippedDates.map { it.toModel(id) }.toSet(),
      mapOf(
        "self" to "/trainings/${id}",
        "delete" to "/api/trainings/${id}",
        "skippedDateFrom" to "/api/trainings/$id/add-skipped-date-form"
      ),
    )
  }

  fun LocalDate.toModel(trainingId: String): SkippedDateModel {
    return SkippedDateModel(ger(), mapOf(
      "delete" to "/api/trainings/$trainingId/skipped-dates/${iso()}"
    ))
  }

  @GetMapping("/{trainingId}")
  fun getTraining(model: Model, principal: Principal, @PathVariable trainingId: String): String {
    val training = trainingUseCase.getTraining(trainingId)
    model.addAttribute("training", training.toModel())
    return "entity/training"
  }

  @GetMapping("/{trainingId}/add-skipped-date-form")
  fun getSkippedDateForm(model: Model, @PathVariable trainingId: String): String {
    model.addAttribute("submit", "/api/trainings/$trainingId/skipped-dates")
    return "form/training-add-skipped-date"
  }

  @PostMapping("{trainingId}/skipped-dates")
  fun addSkippedDate(model: Model, @PathVariable trainingId: String, request: AddSkippedDateRequest, principal: Principal): String {
    val training = trainingUseCase.addSkippedDateToTraining(principal.memberId(), trainingId, request.date)
    model.addAttribute("skippedDate", request.date.toModel(training.id))
    return "entity/training-skipped-date"
  }

  @ResponseBody
  @DeleteMapping("{trainingId}/skipped-dates/{date}")
  fun deleteSkippedDate(@PathVariable trainingId: String, @PathVariable date: LocalDate, principal: Principal) {
    trainingUseCase.removeSkippedDateFromTraining(principal.memberId(), trainingId, date)
  }

  @PostMapping
  fun createTraining(model: Model, principal: Principal, params: PostTrainingParams): String {
    val training = trainingUseCase.createTraining(principal.memberId(), CreateTrainingCommand(
      params.dayOfWeek,
      params.courtId,
      params.fromSlot,
      params.toSlot,
      params.description
    ))

    return getTrainings(model)
  }

  @DeleteMapping("/{trainingId}")
  fun deleteTraining(model: Model, principal: Principal, @PathVariable trainingId: String): String {
    trainingUseCase.deleteTraining(principal.memberId(), trainingId)
    return getTrainings(model)
  }
}
