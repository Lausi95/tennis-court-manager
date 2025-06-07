package de.lausi.tcm.domain.model

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

data class BallmachineBookingId(val value: String = UUID.randomUUID().toString())

data class BallmachineBookingPassCode(val value: String)

data class BallmachineBooking(
  val courtId: CourtId,
  val date: LocalDate,
  val slot: Slot,
  val memberId: MemberId,
  val passCode: BallmachineBookingPassCode,
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
    return Block(BlockType.BALLMACHINE, slot, slot.plus(1), description)
  }
}
