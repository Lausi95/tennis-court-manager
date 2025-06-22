package de.lausi.tcm.domain.model

import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

data class TrainingId(val value: String = UUID.randomUUID().toString())
data class TrainingDescription(val value: String)

data class Training(
  val dayOfWeek: DayOfWeek,
  val courtId: CourtId,
  val fromSlot: Slot,
  val toSlot: Slot,
  val description: TrainingDescription,
  val skippedDates: MutableSet<LocalDate>,
  val id: TrainingId = TrainingId(),
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

    return fromSlot.isInBoundariesOfSlots(
      other.fromSlot,
      other.fromSlot
    ) || toSlot.isInBoundariesOfSlots(other.fromSlot, other.toSlot)
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
class TrainingOccupancyPlanResolver(private val trainingRepository: TrainingRepository) : OccupancyPlanResolver {

  override fun forBlockType() = BlockType.TRAINING

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
