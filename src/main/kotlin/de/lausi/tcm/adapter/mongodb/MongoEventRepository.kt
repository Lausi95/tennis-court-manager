package de.lausi.tcm.adapter.mongodb

import de.lausi.tcm.domain.model.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Document("event")
private data class MongoEvent(
  @Id val id: String,
  val date: LocalDate,
  val courtIds: List<String>,
  val fromSlot: Int,
  val toSlot: Int,
  val description: String,
) {

  fun toEvent(): Event {
    return Event(
      date,
      courtIds.map { CourtId(it) },
      Slot(fromSlot),
      Slot(toSlot),
      EventDescription(description),
      EventId(id),
    )
  }
}

@Repository
private interface MongoEventRepository : MongoRepository<MongoEvent, String> {

  fun findByCourtIdsContainsAndDate(courtId: String, date: LocalDate): List<MongoEvent>

  fun findByDateGreaterThanEqual(date: LocalDate): List<MongoEvent>
}

@Component
private class EventRepositoryImpl(
  private val mongoRepository: MongoEventRepository,
) : EventRepository {

  override fun findByCourtIdsContainsAndDate(courtId: CourtId, date: LocalDate): List<Event> {
    return mongoRepository.findByCourtIdsContainsAndDate(courtId.value, date).map { it.toEvent() }
  }

  override fun findByDateGreaterThanEqual(date: LocalDate): List<Event> {
    return mongoRepository.findByDateGreaterThanEqual(date).map { it.toEvent() }
  }

  override fun save(event: Event): Event {
    return mongoRepository.save(MongoEvent(
      event.id.value,
      event.date,
      event.courtIds.map { it.value },
      event.fromSlot.index,
      event.toSlot.index,
      event.description.value,
    )).toEvent()
  }

  override fun delete(id: EventId) {
    mongoRepository.deleteById(id.value)
  }
}
