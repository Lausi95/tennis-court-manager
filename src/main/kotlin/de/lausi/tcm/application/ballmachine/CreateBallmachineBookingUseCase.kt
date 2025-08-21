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
  private val ballmachineBookingOccupancyPlanResolver: BallmachineBookingOccupancyPlanResolver,
  private val courtRepository: CourtRepository,
  private val ballmachinePasscodeResolver: BallmachinePasscodeResolver,
  private val occupancyPlanService: OccupancyPlanService,
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

    // Cannot book into the past
    if (command.date.isBefore(LocalDate.now())) {
      return Either.Error("Du Kannst nicht in die Vergangenheit buchen.")
    }

    // Cannot use ballmachine, when it is preoccupied
    val courtIds = courtRepository.findAll().map { it.id }
    val ballmachineOccupancyPlanService = OccupancyPlanService(listOf(ballmachineBookingOccupancyPlanResolver))
    val ballmachineOccupancyPlan = ballmachineOccupancyPlanService.getOccupancyPlan(command.date, courtIds)
    val block = with(ballmachineBookingOccupancyPlanResolver) {
      ballmachineBooking.toBlock()
    }
    if (!courtIds.all { ballmachineOccupancyPlan.canPlace(it, block) }) {
      return Either.Error("Die Ballmaschine ist zur dieser Zeit schon in Benutzung.")
    }

    // Cannot boock when something else is on the court
    val occupancyPlan = occupancyPlanService.getOccupancyPlan(command.date, listOf(command.courtId))
    if (!occupancyPlan.canPlace(command.courtId, block)) {
      return Either.Error("Der Platz ist um diese Zeit schon belegt.")
    }

    // Cannot book 14 Days into the future
    if (command.date.isAfter(LocalDate.now().plusDays(14L))) {
      return Either.Error("Du kannst maximal 14 Tage im vorraus Buchen.")
    }

    // BUT: Can always book on the same day
    if (command.date != LocalDate.now()) {
      // If you already have a booking
      if (ballmachineBookingRepository.findByMemberIdAndDateGreaterThanEqual(command.memberId, LocalDate.now())
          .isNotEmpty()
      ) {
        return Either.Error("Du kannst maximal 1 Buchung im vorraus taetigen.")
      }

      // Cannot use ballmachine in the coretime
      // if (slot.isCore(command.date) || slot.plus(2).isCore(command.date)) {
      //  return Either.Error("Die Ballmaschine kann nicht in der Kernzeit benutzt werden.")
      //}
    }

    ballmachineBookingRepository.save(ballmachineBooking)

    return Either.Success(CreateBallmachineBookingResult(ballmachineBooking.id))
  }
}
