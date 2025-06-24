package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Court
import de.lausi.tcm.domain.model.Training
import de.lausi.tcm.domain.model.TrainingId
import de.lausi.tcm.ger
import de.lausi.tcm.iso
import org.springframework.ui.Model
import java.time.DayOfWeek
import java.time.LocalDate

data class DayOfWeekModel(
  val id: DayOfWeek,
  val name: String,
)

val DAY_OF_WEEK_MODELS = listOf(
  DayOfWeekModel(DayOfWeek.MONDAY, "Montag"),
  DayOfWeekModel(DayOfWeek.TUESDAY, "Dienstag"),
  DayOfWeekModel(DayOfWeek.WEDNESDAY, "Mittwoch"),
  DayOfWeekModel(DayOfWeek.THURSDAY, "Donnerstag"),
  DayOfWeekModel(DayOfWeek.FRIDAY, "Freitag"),
  DayOfWeekModel(DayOfWeek.SATURDAY, "Samstag"),
  DayOfWeekModel(DayOfWeek.SUNDAY, "Sonntag"),
)

fun DayOfWeek.toModel() = DAY_OF_WEEK_MODELS.find { this == it.id } ?: error("Thid day of week does not exist.")

fun List<DayOfWeek>.toCollection() = this.map { it.toModel() }

fun Model.dayOfWeekCollection() {
  addAttribute("dayOfWeekCollection", DAY_OF_WEEK_MODELS)
}

fun Model.dayOfWeekCollection(daysOfWeek: List<DayOfWeek>) {
  addAttribute("dayOfWeekCollection", daysOfWeek.toCollection())
}

data class SkippedDateModel(
  val date: String,
  val links: Map<String, String>,
)

fun LocalDate.toSkippedDateModel(training: Training) = SkippedDateModel(
  this.ger(),
  mapOf(
    "delete" to "/trainings/${training.id.value}/remove-skipped-date?skippedDateToRemove=${iso()}",
  ),
)

data class TrainingModel(
  val id: String,
  val court: CourtModel,
  val dayOfWeek: DayOfWeekModel,
  val fromTime: String,
  val toTime: String,
  val description: String,
  val skippedDays: List<SkippedDateModel>,
  val links: Map<String, String>
)

fun Training.toModel(court: Court) = TrainingModel(
  this.id.value,
  court.toModel(),
  this.dayOfWeek.toModel(),
  this.fromSlot.formatFromTime(),
  this.toSlot.formatToTime(),
  this.description.value,
  this.skippedDates.map { it.toSkippedDateModel(this) },
  mapOf(
    "entity" to "/trainings/${id.value}/entity",
    "delete" to "/trainings/${id.value}/delete",
    "add-skipped-date" to "/trainings/${id.value}/add-skipped-date",
  )
)

fun Model.trainingEntry(training: Training, court: Court) {
  addAttribute("training", training.toModel(court))
}

data class TrainingCollection(
  val items: List<TrainingModel>,
  val links: Map<String, String>,
)

fun List<Training>.toCollection(courts: Map<TrainingId, Court>) = TrainingCollection(
  map {
    it.toModel(
      courts[it.id] ?: error("There is no court with id ${it.id}")
    )
  },
  mapOf(
    "create" to "/trainings/create"
  )
)

fun Model.trainingCollection(trainings: List<Training>, courts: Map<TrainingId, Court>) {
  addAttribute("trainingCollection", trainings.toCollection(courts))
}
