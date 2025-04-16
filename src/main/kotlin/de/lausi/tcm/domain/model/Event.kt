package de.lausi.tcm.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

data class Event(
  @Id val id: String,
  val date: LocalDate,
  val courtIds: List<CourtId>,
  val fromSlot: Int,
  val toSlot: Int,
  val description: String,
)

interface EventRepository: MongoRepository<Event, String> {

  fun findByCourtIdsContainsAndDate(courtId: CourtId, date: LocalDate): List<Event>

  fun findByDateGreaterThanEqual(date: LocalDate): List<Event>
}

@Component
class EventService(private val eventRepository: EventRepository) : OccupancyPlanResolver {

  override fun OccupancyPlan.addBlock(date: LocalDate, courtIds: List<CourtId>) {
    courtIds.map { courtId ->
      eventRepository.findByCourtIdsContainsAndDate(courtId, date).forEach {
        addBlock(courtId, it.toBlock())
      }
    }
  }

  fun Event.toBlock(): Block {
    return Block(BlockType.EVENT, fromSlot, toSlot, description)
  }
}
