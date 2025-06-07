package de.lausi.tcm.domain.model

import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

data class BallmachineBookingId(val value: String = UUID.randomUUID().toString())

data class BallmachinePassCode(val value: String)

data class BallmachineBooking(
  val courtId: CourtId,
  val date: LocalDate,
  val slot: Slot,
  val memberId: MemberId,
  val passCode: BallmachinePassCode,
  val id: BallmachineBookingId = BallmachineBookingId(),
)

interface BallmachineBookingRepository {

  fun findAll(): List<BallmachineBooking>

  fun findByMemberId(memberId: MemberId): List<BallmachineBooking>

  fun findByDateAndCourtId(date: LocalDate, courtId: CourtId): List<BallmachineBooking>

  fun findByMemberIdAndDateGreaterThanEqual(memberId: MemberId, minDate: LocalDate): List<BallmachineBooking>

  fun save(ballmachineBooking: BallmachineBooking): BallmachineBooking

  fun delete(ballmachineBookingId: BallmachineBookingId)

  fun findById(ballmachineBookingId: BallmachineBookingId): BallmachineBooking?
}

@Component
class BallmachineBookingOccupancyPlanResolver(
  private val memberRepository: MemberRepository,
  private val ballmachineBookingRepository: BallmachineBookingRepository,
) : OccupancyPlanResolver {

  override fun OccupancyPlan.addBlock(date: LocalDate, courtIds: List<CourtId>) {
    courtIds.forEach { courtId ->
      ballmachineBookingRepository.findByDateAndCourtId(date, courtId).forEach {
        addBlock(courtId, it.toBlock())
      }
    }
  }

  fun BallmachineBooking.toBlock(): Block {
    val member = memberRepository.findById(this.memberId) ?: error("member not found")
    val description = "Ballmaschine ${member.formatName()}"
    return Block(BlockType.BALLMACHINE, slot, slot.plus(2), description)
  }
}

interface BallmachinePasscodeResolver {

  fun getPasscode(dayOfWeek: DayOfWeek, slot: Slot): BallmachinePassCode
}

@Component
class StaticBallmachinePasscodeResolver : BallmachinePasscodeResolver {

  override fun getPasscode(dayOfWeek: DayOfWeek, slot: Slot): BallmachinePassCode {
    return when {
      (dayOfWeek == DayOfWeek.MONDAY && slot.index >= 36) -> BallmachinePassCode("30607")
      (dayOfWeek == DayOfWeek.MONDAY && slot.index >= 24) -> BallmachinePassCode("22510")
      (dayOfWeek == DayOfWeek.MONDAY && slot.index >= 12) -> BallmachinePassCode("72380")
      (dayOfWeek == DayOfWeek.TUESDAY && slot.index >= 36) -> BallmachinePassCode("77306")
      (dayOfWeek == DayOfWeek.TUESDAY && slot.index >= 24) -> BallmachinePassCode("98077")
      (dayOfWeek == DayOfWeek.TUESDAY && slot.index >= 12) -> BallmachinePassCode("11771")
      (dayOfWeek == DayOfWeek.WEDNESDAY && slot.index >= 36) -> BallmachinePassCode("86508")
      (dayOfWeek == DayOfWeek.WEDNESDAY && slot.index >= 24) -> BallmachinePassCode("44213")
      (dayOfWeek == DayOfWeek.WEDNESDAY && slot.index >= 12) -> BallmachinePassCode("82414")
      (dayOfWeek == DayOfWeek.THURSDAY && slot.index >= 36) -> BallmachinePassCode("34581")
      (dayOfWeek == DayOfWeek.THURSDAY && slot.index >= 24) -> BallmachinePassCode("31857")
      (dayOfWeek == DayOfWeek.THURSDAY && slot.index >= 12) -> BallmachinePassCode("19162")
      (dayOfWeek == DayOfWeek.FRIDAY && slot.index >= 36) -> BallmachinePassCode("42986")
      (dayOfWeek == DayOfWeek.FRIDAY && slot.index >= 24) -> BallmachinePassCode("84536")
      (dayOfWeek == DayOfWeek.FRIDAY && slot.index >= 12) -> BallmachinePassCode("42615")
      (dayOfWeek == DayOfWeek.SATURDAY && slot.index >= 36) -> BallmachinePassCode("92896")
      (dayOfWeek == DayOfWeek.SATURDAY && slot.index >= 24) -> BallmachinePassCode("26283")
      (dayOfWeek == DayOfWeek.SATURDAY && slot.index >= 12) -> BallmachinePassCode("37569")
      (dayOfWeek == DayOfWeek.SUNDAY && slot.index >= 36) -> BallmachinePassCode("37587")
      (dayOfWeek == DayOfWeek.SUNDAY && slot.index >= 24) -> BallmachinePassCode("96146")
      (dayOfWeek == DayOfWeek.SUNDAY && slot.index >= 12) -> BallmachinePassCode("77825")
      else -> error("Cannot create Passcode for $slot")
    }
  }
}
