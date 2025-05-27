package de.lausi.tcm.application.event

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class DeleteEventContextParams(
  val eventId: EventId,
)

data class DeleteEventContext(
  val event: Event,
  val courts: List<Court>,
)

data class DeleteEventCommand(
  val eventId: EventId,
)

enum class DeleteEventError {
  EVENT_NOT_FOUND,
}

@UseCaseComponent
class DeleteEventUseCase(
  private val permissions: Permissions,
  private val eventRepository: EventRepository,
  private val courtRepository: CourtRepository,
) : UseCase<DeleteEventContextParams, DeleteEventContext, DeleteEventCommand, Nothing?, DeleteEventError> {

  override fun checkContextPermission(
    userId: MemberId,
    contextParams: DeleteEventContextParams
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.EVENT_MANAGEMENT)
  }

  override fun getContext(params: DeleteEventContextParams): Either<DeleteEventContext, DeleteEventError> {
    val event = eventRepository.findById(params.eventId)
      ?: return Either.Error(DeleteEventError.EVENT_NOT_FOUND)

    val courts = courtRepository.findAllById(event.courtIds)

    return Either.Success(DeleteEventContext(event, courts))
  }

  override fun checkCommandPermission(
    userId: MemberId,
    command: DeleteEventCommand
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.EVENT_MANAGEMENT)
  }

  override fun handle(command: DeleteEventCommand): Either<Nothing?, DeleteEventError> {
    if (!eventRepository.existsById(command.eventId)) {
      return Either.Error(DeleteEventError.EVENT_NOT_FOUND)
    }

    eventRepository.delete(command.eventId)

    return Either.Success()
  }
}