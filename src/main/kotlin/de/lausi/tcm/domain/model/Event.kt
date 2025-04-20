package de.lausi.tcm.domain.model

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

data class EventId(val value: String = UUID.randomUUID().toString())
data class EventDescription(val value: String)

data class Event(
  val date: LocalDate,
  val courtIds: List<CourtId>,
  val fromSlot: Slot,
  val toSlot: Slot,
  val description: EventDescription,
  val id: EventId = EventId(),
)

interface EventRepository {

  fun findByCourtIdsContainsAndDate(courtId: CourtId, date: LocalDate): List<Event>

  fun findByDateGreaterThanEqual(date: LocalDate): List<Event>

  fun save(event: Event): Event

  fun delete(id: EventId)
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
    return Block(BlockType.EVENT, fromSlot, toSlot, description.value)
  }
}
