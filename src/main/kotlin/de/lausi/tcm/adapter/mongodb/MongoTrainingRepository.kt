package de.lausi.tcm.adapter.mongodb

import de.lausi.tcm.domain.model.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate

@Document("training")
private data class MongoTraining(
  @Id val id: String,
  val dayOfWeek: DayOfWeek,
  val courtId: String,
  val fromSlot: Int,
  val toSlot: Int,
  val description: String,
  val skippedDates: MutableSet<LocalDate>,
) {

  fun toTraining() = Training(
    dayOfWeek,
    CourtId(courtId),
    Slot(fromSlot),
    Slot(toSlot),
    TrainingDescription(description),
    skippedDates,
    TrainingId(id),
  )
}

private interface MongoTrainingRepository : MongoRepository<MongoTraining, String> {

  fun findByDayOfWeekAndCourtId(dayOfWeek: DayOfWeek, courtId: String): List<MongoTraining>
}

@Component
private class TrainingRepositoryImpl(private val mongoRepository: MongoTrainingRepository) : TrainingRepository {

  override fun findAll(): List<Training> {
    return mongoRepository.findAll().map { it.toTraining() }
  }

  override fun findByDayOfWeekAndCourtId(dayOfWeek: DayOfWeek, courtId: CourtId): List<Training> {
    return mongoRepository.findByDayOfWeekAndCourtId(dayOfWeek, courtId.value).map { it.toTraining() }
  }

  override fun findById(trainingId: TrainingId): Training? {
    return mongoRepository.findById(trainingId.value).orElse(null)?.toTraining()
  }

  override fun save(training: Training): Training {
    return mongoRepository.save(
      MongoTraining(
        training.id.value,
        training.dayOfWeek,
        training.courtId.value,
        training.fromSlot.index,
        training.toSlot.index,
        training.description.value,
        training.skippedDates,
      )
    ).toTraining()
  }

  override fun delete(trainingId: TrainingId) {
    return mongoRepository.deleteById(trainingId.value)
  }
}
