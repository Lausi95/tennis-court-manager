package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

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
  val skippedDays: List<String>,
  val links: Map<String, String>
)

data class TrainingCollection(
  val items: List<TrainingModel>,
  val count: Int,
  val daysOfWeek: List<DayOfWeekModel>,
  val links: Map<String, String>
)

@Controller
@RequestMapping("/api/trainings")
class TrainingController(
  private val courtController: CourtController,
  private val slotController: SlotController,
  private val courtRepository: CourtRepository,
  private val trainingRepository: TrainingRepository
) {

  @GetMapping
  fun getTrainings(model: Model): String {
    val courts = courtRepository.findAll()
    val items = trainingRepository.findAll().map { training ->
      val court = courts.find { it.id == training.courtId } ?: error("Could not find court for training.")
      TrainingModel(
        training.id,
        CourtModel(court.id, court.name, mapOf()),
        DAY_OF_WEEK_MODELS.find { it.id == training.dayOfWeek }!!,
        formatFromTime(training.fromSlot),
        formatToTime(training.toSlot),
        training.description,
        training.skippedDates.map { it.format(DateTimeFormatter.ISO_DATE) },
        mapOf(
          "self" to "/api/trainings/${training.id}",
          "delete" to "/api/trainings/${training.id}",
        ),
      )
    }

    val trainingCollection = TrainingCollection(items, items.size, DAY_OF_WEEK_MODELS, mapOf())

    model.addAttribute("trainingCollection", trainingCollection)

    courtController.getCourts(model)
    slotController.getSlots(model)

    return "views/trainings"
  }

  @PostMapping
  fun createTrainig(model: Model, params: PostTrainingParams): String {
    val errors = mutableListOf<String>()

    val training = Training(
      params.dayOfWeek,
      params.courtId,
      params.fromSlot,
      params.toSlot,
      params.description
    )

    val trainings = trainingRepository.findByDayOfWeekAndCourtId(training.dayOfWeek, training.courtId)
    if (trainings.any { it.collidesWith(training) }) {
      errors.add("Zu dem angegebenen Zeitraum ist bereits Training")
    }

    if (errors.isEmpty()) {
      trainingRepository.save(training)
    }

    return getTrainings(model)
  }

  @DeleteMapping("/{trainingId}")
  fun deleteTraining(model: Model, @PathVariable trainingId: String): String {
    trainingRepository.deleteById(trainingId)
    return getTrainings(model)
  }
}
