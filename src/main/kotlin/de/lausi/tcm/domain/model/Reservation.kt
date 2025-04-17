package de.lausi.tcm.domain.model

import de.lausi.tcm.domain.model.member.MemberId
import de.lausi.tcm.domain.model.member.MemberRepository
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Document("reservation")
data class Reservation(
  val courtId: CourtId,
  val date: LocalDate,
  val fromSlot: Slot,
  val toSlot: Slot,
  val creatorId: MemberId,
  val playerIds: List<MemberId>,
  val id: String = UUID.randomUUID().toString(),
) {

  fun slotAmount(): Int = Slot.distance(fromSlot, toSlot)

  fun isToday(): Boolean = date.isBefore(LocalDate.now().plusDays(2))
}

/**
 * Interface for a Repository that stores [Reservation] entities.
 */
interface ReservationRespository : MongoRepository<Reservation, String> {

  /**
   * Finds all reservations at the given date on the given court.
   *
   * @param date Date to find the reservations of
   * @param courtId ID of the court to find the reservations of
   */
  fun findByDateAndCourtId(date: LocalDate, courtId: CourtId): List<Reservation>

  /**
   * Finds all Reservation where the member with the given member id is the
   * creator after the given min date.
   *
   * @param memberId ID of the meber to load the reservations from
   * @param minDate Min Date for the reservations to load.
   * All reservations should be that date or later.
   */
  fun findByCreatorIdAndDateGreaterThanEqual(memberId: String, minDate: LocalDate): List<Reservation>
}

@Component
class ReservationService(
  private val memberRepository: MemberRepository,
  private val reservationRepository: ReservationRespository
) : OccupancyPlanResolver {

  override fun OccupancyPlan.addBlock(date: LocalDate, courtIds: List<CourtId>) {
    courtIds.forEach { courtId ->
      reservationRepository.findByDateAndCourtId(date, courtId).forEach {
        addBlock(courtId, it.toBlock())
      }
    }
  }

  fun Reservation.toBlock(): Block {
    val members = memberRepository.findById(playerIds)
    val description = members.joinToString(" & ") { it.formatName() }
    return Block(BlockType.FREE_PLAY, fromSlot, toSlot, description)
  }
}
