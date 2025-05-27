package de.lausi.tcm.application.event

import de.lausi.tcm.Either
import de.lausi.tcm.application.ReadUseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*
import java.time.LocalDate

data class GetEventsCommand(
  val minDate: LocalDate?,
)

data class GetEventsResponse(
  val events: List<Event>,
  val courts: Map<EventId, List<Court>>,
)

@UseCaseComponent
class GetEventsUseCase(
  private val eventRepository: EventRepository,
  private val courtRepository: CourtRepository,
) : ReadUseCase<GetEventsCommand, GetEventsResponse, Nothing> {

  override fun checkPermission(userId: MemberId, command: GetEventsCommand): Boolean {
    return true // everyone can see it
  }

  override fun handle(command: GetEventsCommand): Either<GetEventsResponse, Nothing> {
    val events = if (command.minDate != null) {
      eventRepository.findByDateGreaterThanEqual(command.minDate)
    } else {
      eventRepository.findAll()
    }

    val courts = mutableMapOf<EventId, List<Court>>()
    events.forEach {
      courts[it.id] = courtRepository.findAllById(it.courtIds)
    }

    return Either.Success(
      GetEventsResponse(
        events,
        courts,
      )
    )
  }
}