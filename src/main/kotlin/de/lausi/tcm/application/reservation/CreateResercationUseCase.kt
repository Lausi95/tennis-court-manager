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

    val reservation = Reservation(
      command.courtId,
      command.date,
      fromSlot,
      toSlot,
      command.creatorId,
      command.playerIds,
    )

    // 1. No Reservation, when something is on the same time
    val occupancyPlan = occupancyPlanService.getOccupancyPlan(command.date, listOf(command.courtId))
    val block = with(reservationOccupancyPlanResolver) {
      reservation.toBlock()
    }
    if (!occupancyPlan.canPlace(command.courtId, block)) {
      return Either.Error("Zur dieser zeit ist der Platz bereits belegt.")
    }

    // 2. Max 2 Hours
    if (Slot.distance(fromSlot, toSlot) > 4) {
      return Either.Error("Du kannst ausserhalb der kernzeit maximal 2 Stunden am Stueck buchen.")
    }


    // 4. Cannot book 14 Days into the future
    if (command.date.isAfter(LocalDate.now().plusDays(14L))) {
      return Either.Error("Du kannst maximal 14 Tage im vorraus Buchen.")
    }

    // BUT: Can always book on the same day
    if (command.date != LocalDate.now()
    ) {
      // 3. Max 1 Hour in core time
      if ((fromSlot.isCore() || toSlot.isCore()) && Slot.distance(fromSlot, toSlot) > 2) {
        return Either.Error("Du kannst innerhalb der Kernzeit maximal 1 Stunde am Stueck buchen.")
      }

      // 5. If you already have a booking
      if (reservationRepository.findByCreatorIdAndDateGreaterThanEqual(command.creatorId, LocalDate.now()).isNotEmpty()
      ) {
        return Either.Error("Du kannst maximal 1 Buchung im vorraus taetigen.")
      }
    }

    reservationRepository.save(reservation)

    return Either.Success(CreateReservationResult(reservation.id))
  }
}