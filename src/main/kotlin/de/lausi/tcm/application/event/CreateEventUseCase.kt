package de.lausi.tcm.application.event

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*
import java.time.LocalDate

data class CreateEventContext(
  val courts: List<Court>,
)

data class CreateEventCommand(
  val date: LocalDate,
  val courtIds: List<CourtId>,
  val fromSlot: Slot,
  val toSlot: Slot,
  val description: EventDescription,
)

data class CreateEventResult(
  val eventId: EventId,
)

enum class CreateEventError {
}

@UseCaseComponent
class CreateEventUseCase(
  private val permissions: Permissions,
  private val eventRepository: EventRepository,
  private val courtRepository: CourtRepository,
) : UseCase<Nothing?, CreateEventContext, CreateEventCommand, CreateEventResult, CreateEventError> {

  override fun checkContextPermission(
    userId: MemberId,
    contextParams: Nothing?
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.EVENT_MANAGEMENT)
  }

  override fun getContext(params: Nothing?): Either<CreateEventContext, CreateEventError> {
    val courts = courtRepository.findAll()
    return Either.Success(CreateEventContext(courts))
  }

  override fun checkCommandPermission(
    userId: MemberId,
    command: CreateEventCommand
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.EVENT_MANAGEMENT)
  }

  override fun handle(command: CreateEventCommand): Either<CreateEventResult, CreateEventError> {
    // todo: validation

    val event = Event(
      command.date,
      command.courtIds,
      command.fromSlot,
      command.toSlot,
      command.description
    )

    eventRepository.save(event)

    return Either.Success(CreateEventResult(event.id))
  }
}