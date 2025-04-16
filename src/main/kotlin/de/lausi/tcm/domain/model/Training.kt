package de.lausi.tcm.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

@Document("training")
data class Training(
  @Id val id: String,
  val dayOfWeek: DayOfWeek,
  val courtId: CourtId,
  val fromSlot: Int,
  val toSlot: Int,
  val description: String,
  val skippedDates: MutableSet<LocalDate>,
) {

  constructor(dayOfWeek: DayOfWeek, courtId: CourtId, fromSlot: Int, toSlot: Int, description: String) : this(
    UUID.randomUUID().toString(),
    dayOfWeek,
    courtId,
    fromSlot,
    toSlot,
    description,
    mutableSetOf(),
  )

  fun collidesWith(other: Training): Boolean {
    val sameCourt = courtId == other.courtId
    if (!sameCourt) {
      return false
    }

    val sameDayOfWeek = dayOfWeek == other.dayOfWeek
    if (!sameDayOfWeek) {
      return false
    }

    val oneOrMoreSameSlots = (fromSlot..toSlot).any { (other.fromSlot..other.toSlot).contains(it) }
    return oneOrMoreSameSlots
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

interface TrainingRepository : MongoRepository<Training, String> {

  fun findByDayOfWeekAndCourtId(dayOfWeek: DayOfWeek, courtId: CourtId): List<Training>
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
    return Block(BlockType.TRAINING, fromSlot, toSlot, description)
  }
}
