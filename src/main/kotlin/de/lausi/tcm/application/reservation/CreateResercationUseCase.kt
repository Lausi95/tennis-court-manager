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
  val courts: List<Court>,
  val slots: List<Slot>,
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

@Component
class CreateReservationUseCase(
  private val permissions: Permissions,
  private val reservationRepository: ReservationRepository,
  private val memberRepository: MemberRepository,
  private val occupancyPlanService: OccupancyPlanService,
  private val reservationOccupancyPlanResolver: ReservationOccupancyPlanResolver,
  private val courtRepository: CourtRepository,
) : UseCase<
        CreateReservationContextParams,
        CreateReservationContext,
        CreateReservationCommand,
        CreateReservationResult,
        String> {

  override fun checkContextPermission(userId: MemberId, contextParams: CreateReservationContextParams): Boolean =
    NOT_RESTRICTED

  override fun getContext(params: CreateReservationContextParams): Either<CreateReservationContext, String> {
    val self = memberRepository.findById(params.selfId)
      ?: return Either.Error(listOf("Nutzer existiert nicht."))

    val members = memberRepository.findAll()
    val courts = courtRepository.findAll()
    val slots = SlotRepository.findAll()

    return Either.Success(
      CreateReservationContext(
        self,
        members,
        courts,
        slots,
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

  override fun handle(command: CreateReservationCommand): Either<CreateReservationResult, String> {
    val fromSlot = SlotRepository.findByIndex(command.fromSlotIndex)
      ?: return Either.Error(listOf("Startzeit existiert nicht"))

    val toSlot = SlotRepository.findByIndex(command.toSlotIndex)
      ?: return Either.Error(listOf("Endzeit existiert nicht"))

    val slots = listOf(fromSlot, toSlot)

    val reservation = Reservation(
      command.courtId,
      command.date,
      fromSlot,
      toSlot,
      command.creatorId,
      command.playerIds,
    )

    // No Reservation, when something is on the same time
    val occupancyPlan = occupancyPlanService.getOccupancyPlan(command.date, listOf(command.courtId))
    val block = with(reservationOccupancyPlanResolver) {
      reservation.toBlock()
    }
    if (!occupancyPlan.canPlace(command.courtId, block)) {
      return Either.Error("Zur dieser zeit ist der Platz bereits belegt.")
    }

    // Max 2 Hours
    if (Slot.distance(fromSlot, toSlot) > 4) {
      return Either.Error("Du kannst ausserhalb der kernzeit maximal 2 Stunden am Stueck buchen.")
    }


    // Cannot book 14 Days into the future
    if (command.date.isAfter(LocalDate.now().plusDays(14L))) {
      return Either.Error("Du kannst maximal 14 Tage im vorraus Buchen.")
    }

    // BUT: Can always book on the same day
    if (command.date != LocalDate.now()
    ) {
      // Max 1 Hour in core time
      if ((fromSlot.isCore(command.date) || toSlot.isCore(command.date)) && Slot.distance(fromSlot, toSlot) > 2) {
        return Either.Error("Du kannst innerhalb der Kernzeit maximal 1 Stunde am Stueck buchen.")
      }

      // Max 1 Coretime booking
      val futureReservations =
        reservationRepository.findByCreatorIdAndDateGreaterThanEqual(command.creatorId, LocalDate.now().plusDays(1L))
      if (slots.any { it.isCore(command.date) } && futureReservations.any {
          listOf(
            it.fromSlot,
            it.toSlot
          ).any { slot -> slot.isCore(it.date) }
        }) {
        return Either.Error("Du kannst maximal 1 Kernzeitbuchung in der Zukunft haben.")
      }

      // Max one booking on the same day
      if (futureReservations.any { it.date == command.date }) {
        return Either.Error("Du kannst maximal 1 Reservierung am selben tag haben.")
      }
    }

    reservationRepository.save(reservation)

    return Either.Success(CreateReservationResult(reservation.id))
  }
}