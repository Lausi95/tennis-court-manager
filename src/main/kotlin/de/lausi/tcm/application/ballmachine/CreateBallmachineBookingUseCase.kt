package de.lausi.tcm.application.ballmachine

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*
import java.time.LocalDate

data class CreateBallmachineBookingContext(
  val courts: List<Court>,
  val slots: List<Slot>,
)

data class CreateBallmachineBookingCommand(
  val memberId: MemberId,
  val courtId: CourtId,
  val date: LocalDate,
  val slotIndex: Int,
)

data class CreateBallmachineBookingResult(
  val ballmachineBookingId: BallmachineBookingId,
)

@UseCaseComponent
class CreateBallmachineBookingUseCase(
  private val permissions: Permissions,
  private val ballmachineBookingRepository: BallmachineBookingRepository,
  private val occupancyPlanService: OccupancyPlanService,
  private val ballmachineBookingOccupancyPlanResolver: BallmachineBookingOccupancyPlanResolver,
  private val courtRepository: CourtRepository,
  private val ballmachinePasscodeResolver: BallmachinePasscodeResolver,
) :
  UseCase<Nothing?, CreateBallmachineBookingContext, CreateBallmachineBookingCommand, CreateBallmachineBookingResult, String> {

  override fun checkContextPermission(
    userId: MemberId,
    contextParams: Nothing?
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.BALLMACHINE)
  }

  override fun getContext(params: Nothing?): Either<CreateBallmachineBookingContext, String> {
    val courts = courtRepository.findAll()
    val slots = SlotRepository.findAll()
    return Either.Success(CreateBallmachineBookingContext(courts, slots))
  }

  override fun checkCommandPermission(
    userId: MemberId,
    command: CreateBallmachineBookingCommand,
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.BALLMACHINE)
  }

  override fun handle(command: CreateBallmachineBookingCommand): Either<CreateBallmachineBookingResult, String> {
    val slot = SlotRepository.findByIndex(command.slotIndex) ?: return Either.Error("Slot not found")

    val ballmachineBooking = BallmachineBooking(
      command.courtId,
      command.date,
      slot,
      command.memberId,
      ballmachinePasscodeResolver.getPasscode(command.date.dayOfWeek, slot)
    )

    // 1. Cannot use ballmachine, when it is preoccupied
    val courtIds = courtRepository.findAll().map { it.id }
    val occupancyPlan = occupancyPlanService.getOccupancyPlan(command.date, courtIds)
    val block = with(ballmachineBookingOccupancyPlanResolver) {
      ballmachineBooking.toBlock()
    }
    if (!courtIds.all { occupancyPlan.canPlace(it, block) }) {
      return Either.Error("Die Ballmaschine ist zur dieser Zeit schon in Benutzung.")
    }


    // 3. Cannot book 14 Days into the future
    if (command.date.isAfter(LocalDate.now().plusDays(14L))) {
      return Either.Error("Du kannst maximal 14 Tage im vorraus Buchen.")
    }

    // BUT: Can always book on the same day
    if (command.date != LocalDate.now()) {
      // 4. If you already have a booking
      if (ballmachineBookingRepository.findByMemberIdAndDateGreaterThanEqual(command.memberId, LocalDate.now())
          .isNotEmpty()
      ) {
        return Either.Error("Du kannst maximal 1 Buchung im vorraus taetigen.")
      }

      // 2. Cannot use ballmachine in the coretime
      if (slot.isCore() || slot.plus(2).isCore()) {
        return Either.Error("Die Ballmaschine kann nicht in der Kernzeit benutzt werden.")
      }
    }

    ballmachineBookingRepository.save(ballmachineBooking)

    return Either.Success(CreateBallmachineBookingResult(ballmachineBooking.id))
  }
}
