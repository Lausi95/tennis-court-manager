package de.lausi.tcm.application.ballmachine

import de.lausi.tcm.Either
import de.lausi.tcm.application.ReadUseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*
import java.time.LocalDate

data class GetBallmachinBookingsCommand(
  val memberId: MemberId?,
  val minDate: LocalDate?,
)

data class GetBallmachinBookingsResult(
  val ballmachineBookings: List<BallmachineBooking>,
  val courts: Map<BallmachineBookingId, Court>,
  val members: Map<BallmachineBookingId, Member>,
)

@UseCaseComponent
class GetBallmachineBookingsUseCase(
  private val permissions: Permissions,
  private val ballmachineBookingRepository: BallmachineBookingRepository,
  private val courtRepository: CourtRepository,
  private val memberRepository: MemberRepository
) :
  ReadUseCase<GetBallmachinBookingsCommand, GetBallmachinBookingsResult, String> {

  override fun checkPermission(
    userId: MemberId,
    command: GetBallmachinBookingsCommand
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.BALLMACHINE)
  }

  override fun handle(command: GetBallmachinBookingsCommand): Either<GetBallmachinBookingsResult, String> {
    val ballmachineBookings = when {
      (command.memberId != null && command.minDate != null) -> ballmachineBookingRepository.findByMemberIdAndDateGreaterThanEqual(
        command.memberId,
        command.minDate
      )

      (command.memberId != null) -> ballmachineBookingRepository.findByMemberId(command.memberId)
      (command.minDate != null) -> error("not implemented yet")
      else -> ballmachineBookingRepository.findAll()
    }

    val courts = mutableMapOf<BallmachineBookingId, Court>()
    val members = mutableMapOf<BallmachineBookingId, Member>()
    ballmachineBookings.forEach {
      courts[it.id] = courtRepository.findById(it.courtId) ?: return Either.Error("Konnte Platz nicht aufloesen")
      members[it.id] = memberRepository.findById(it.memberId) ?: return Either.Error("Konnte Mitglied nicht aufloesen")
    }

    return Either.Success(
      GetBallmachinBookingsResult(
        ballmachineBookings,
        courts,
        members
      )
    )
  }
}