package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.CourtRepository

import de.lausi.tcm.domain.model.TrainingRepository
import de.lausi.tcm.domain.model.formatFromTime
import de.lausi.tcm.domain.model.formatToTime
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
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
  private val courtRepository: CourtRepository,
  private val trainingRepository: TrainingRepository) {

  @GetMapping(headers = ["accept: application/json"])
  @ResponseBody
  fun getTrainings(): TrainingCollection {
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
        mapOf(),
      )
    }

    return TrainingCollection(items, items.size, DAY_OF_WEEK_MODELS, mapOf())
  }

  @GetMapping
  fun getTrainings(model: Model): String {
    model.addAttribute("trainingCollection", getTrainings())
    return "views/training"
  }
}
