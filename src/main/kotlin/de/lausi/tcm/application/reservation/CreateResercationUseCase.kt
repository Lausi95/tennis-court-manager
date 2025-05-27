package de.lausi.tcm.application.reservation

import de.lausi.tcm.Either
import de.lausi.tcm.application.NOT_RESTRICTED
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Component
import java.time.LocalDate

data class CreateReservationContextParams(
  val selfId: MemberId,
)

data class CreateReservationContext(
  val self: Member,
  val members: List<Member>,
)

data class CreateReservationCommand(
  val date: LocalDate,
  val fromSlotIndex: Int,
  val toSlotIndex: Int,
  val courtId: CourtId,
  val creatorId: MemberId,
  val playerIds: List<MemberId>,
)

data class CreateReservationResult(
  val reservationId: ReservationId,
)

enum class CreateReservationError {
  CURRENT_USER_DOES_NOT_EXIST,
  FROM_SLOT_DOES_NOT_EXIST,
  TO_SLOT_DOES_NOT_EXIST,
}

@Component
class CreateReservationUseCase(
  private val permissions: Permissions,
  private val reservationRepository: ReservationRepository,
  private val memberRepository: MemberRepository,
) : UseCase<
        CreateReservationContextParams,
        CreateReservationContext,
        CreateReservationCommand,
        CreateReservationResult,
        CreateReservationError> {

  override fun checkContextPermission(userId: MemberId, contextParams: CreateReservationContextParams): Boolean =
    NOT_RESTRICTED

  override fun getContext(params: CreateReservationContextParams): Either<CreateReservationContext, CreateReservationError> {
    val self = memberRepository.findById(params.selfId)
      ?: return Either.Error(listOf(CreateReservationError.CURRENT_USER_DOES_NOT_EXIST))

    val members = memberRepository.findAll().filter { it.id != params.selfId }

    return Either.Success(
      CreateReservationContext(
        self,
        members
      )
    )
  }

  override fun checkCommandPermission(userId: MemberId, command: CreateReservationCommand): Boolean {
    if (command.creatorId != userId) {
      return permissions.assertGroup(userId, MemberGroup.ADMIN)
    }

    if (!command.playerIds.contains(command.creatorId)) {
      return permissions.assertGroup(userId, MemberGroup.ADMIN)
    }

    return true
  }

  override fun handle(command: CreateReservationCommand): Either<CreateReservationResult, CreateReservationError> {
    val fromSlot = SlotRepository.findByIndex(command.fromSlotIndex)
      ?: return Either.Error(listOf(CreateReservationError.FROM_SLOT_DOES_NOT_EXIST))

    val toSlot = SlotRepository.findByIndex(command.fromSlotIndex)
      ?: return Either.Error(listOf(CreateReservationError.TO_SLOT_DOES_NOT_EXIST))

    // TODO Validation!

    val reservation = Reservation(
      command.courtId,
      command.date,
      fromSlot,
      toSlot,
      command.creatorId,
      command.playerIds,
    )

    reservationRepository.save(reservation)

    return Either.Success(CreateReservationResult(reservation.id))
  }
}