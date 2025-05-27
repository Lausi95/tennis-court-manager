package de.lausi.tcm.application.reservation

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class CancelReservationContextParams(
  val reservationId: ReservationId,
)

data class CancelReservationContext(
  val reservation: Reservation,
  val court: Court,
  val creator: Member,
  val players: List<Member>,
)

data class CancelReservationCommand(
  val reservationId: ReservationId,
)

enum class CancelReservationError {
  RESERVATION_NOT_FOUND,
  COURT_NOT_FOUND,
  CREATOR_NOT_FOUND,
}

@UseCaseComponent
class CancelReservationUseCase(
  private val permissions: Permissions,
  private val reservationRepository: ReservationRepository,
  private val courtRepository: CourtRepository,
  private val memberRepository: MemberRepository,
) :
  UseCase<
          CancelReservationContextParams,
          CancelReservationContext,
          CancelReservationCommand,
          Nothing?,
          CancelReservationError> {

  override fun checkContextPermission(userId: MemberId, contextParams: CancelReservationContextParams): Boolean {
    val reservation = reservationRepository.findById(contextParams.reservationId) ?: return false
    if (reservation.creatorId != userId) {
      return permissions.assertGroup(userId, MemberGroup.ADMIN)
    }

    return true
  }

  override fun getContext(params: CancelReservationContextParams): Either<CancelReservationContext, CancelReservationError> {
    val reservation = reservationRepository.findById(params.reservationId)
      ?: return Either.Error(CancelReservationError.RESERVATION_NOT_FOUND)

    val court = courtRepository.findById(reservation.courtId)
      ?: return Either.Error(CancelReservationError.COURT_NOT_FOUND)

    val creator = memberRepository.findById(reservation.creatorId)
      ?: return Either.Error(CancelReservationError.CREATOR_NOT_FOUND)

    val players = memberRepository.findById(reservation.playerIds)

    return Either.Success(
      CancelReservationContext(
        reservation,
        court,
        creator,
        players,
      )
    )
  }

  override fun checkCommandPermission(userId: MemberId, command: CancelReservationCommand): Boolean {
    val reservation = reservationRepository.findById(command.reservationId) ?: return false
    if (reservation.creatorId != userId) {
      return permissions.assertGroup(userId, MemberGroup.ADMIN)
    }

    return true
  }

  override fun handle(command: CancelReservationCommand): Either<Nothing?, CancelReservationError> {
    val reservation = reservationRepository.findById(command.reservationId)
      ?: return Either.Error(CancelReservationError.RESERVATION_NOT_FOUND)

    // TODO: don't actually delete it. Put a 'cancelled' flag, to see, who and when it got cancelled
    reservationRepository.delete(reservation.id)

    return Either.Success(null)
  }
}