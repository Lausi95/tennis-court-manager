package de.lausi.tcm.application.ballmachine

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*
import java.time.LocalDateTime

data class CancelBallmachineBookingContextParams(
  val ballmachineBookingId: BallmachineBookingId
)

data class CancelBallmachineBookingContext(
  val ballmachineBooking: BallmachineBooking,
  val member: Member,
  val court: Court,
)

data class CancelBallmachineBookingCommand(
  val ballmachineBookingId: BallmachineBookingId
)

@UseCaseComponent
class CancelBallmachineBookingUseCase(
  private val permissions: Permissions,
  private val ballmachineBookingRepository: BallmachineBookingRepository,
  private val memberRepository: MemberRepository,
  private val courtRepository: CourtRepository
) :
  UseCase<CancelBallmachineBookingContextParams, CancelBallmachineBookingContext, CancelBallmachineBookingCommand, Nothing?, String> {
  override fun checkContextPermission(
    userId: MemberId,
    contextParams: CancelBallmachineBookingContextParams
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.BALLMACHINE)
  }

  override fun getContext(params: CancelBallmachineBookingContextParams): Either<CancelBallmachineBookingContext, String> {
    val ballmachineBooking = ballmachineBookingRepository.findById(params.ballmachineBookingId)
      ?: return Either.Error("Ballmaschinenbuchung konnte nicht gefunden werden.")

    val member = memberRepository.findById(ballmachineBooking.memberId)
      ?: return Either.Error("Mitglied der Buchung konnte nicht gefunden werden.")

    val court = courtRepository.findById(ballmachineBooking.courtId)
      ?: return Either.Error("Plats der Buchung konnte nicht gefunden werden.")

    return Either.Success(
      CancelBallmachineBookingContext(
        ballmachineBooking,
        member,
        court,
      )
    )
  }

  override fun checkCommandPermission(
    userId: MemberId,
    command: CancelBallmachineBookingCommand
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.BALLMACHINE)
  }

  override fun handle(command: CancelBallmachineBookingCommand): Either<Nothing?, String> {
    val ballmachineBooking = ballmachineBookingRepository.findById(command.ballmachineBookingId)
      ?: return Either.Error("Ballmachine booker gefunden werden.")


    val maxCancelTime = ballmachineBooking.date.atTime(ballmachineBooking.slot.toTime()).minusHours(2L)
    if (LocalDateTime.now().isAfter(maxCancelTime)) {
      return Either.Error("Du kannst die Ballmaschine maximal bis 2h im Vorraus stonieren.")
    }

    ballmachineBookingRepository.delete(ballmachineBooking.id)

    return Either.Success()
  }
}