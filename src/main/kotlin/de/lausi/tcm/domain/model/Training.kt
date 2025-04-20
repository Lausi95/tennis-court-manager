package de.lausi.tcm.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

data class TrainingId(val value: String = UUID.randomUUID().toString())
data class TrainingDescription(val value: String)

@Document("training")
data class Training(
  val dayOfWeek: DayOfWeek,
  val courtId: CourtId,
  val fromSlot: Slot,
  val toSlot: Slot,
  val description: TrainingDescription,
  val skippedDates: MutableSet<LocalDate>,
  @Id val id: TrainingId = TrainingId(),
) {

  fun collidesWith(other: Training): Boolean {
    val sameCourt = courtId == other.courtId
    if (!sameCourt) {
      return false
    }

    val sameDayOfWeek = dayOfWeek == other.dayOfWeek
    if (!sameDayOfWeek) {
      return false
    }

    return fromSlot.isInBoundariesOfSlots(other.fromSlot, other.fromSlot) || toSlot.isInBoundariesOfSlots(other.fromSlot, other.toSlot)
  }

  fun addSkippedDate(date: LocalDate) {
    if (skippedDates.contains(date)) {
      error("Date already in skipped dates.")
    }
    skippedDates.add(date)
  }

  fun removeSkippedDate(date: LocalDate) {
    if (!skippedDates.contains(date)) {
      error("Date is not in skipped dates.")
    }
    skippedDates.remove(date)
  }
}

interface TrainingRepository {

  fun findAll(): List<Training>

  fun findByDayOfWeekAndCourtId(dayOfWeek: DayOfWeek, courtId: CourtId): List<Training>

  fun findById(trainingId: TrainingId): Training?

  fun save(training: Training): Training

  fun delete(trainingId: TrainingId)
}

@Component
class TrainingService(private val trainingRepository: TrainingRepository): OccupancyPlanResolver {

  override fun OccupancyPlan.addBlock(date: LocalDate, courtIds: List<CourtId>) {
    courtIds.forEach { courtId ->
      trainingRepository.findByDayOfWeekAndCourtId(date.dayOfWeek, courtId).forEach {
        if (!it.skippedDates.contains(date)) {
          addBlock(courtId, it.toBlock())
        }
      }
    }
  }

  fun Training.toBlock(): Block {
    return Block(
      BlockType.TRAINING,
      fromSlot,
      toSlot,
      description.value,
    )
  }
}
