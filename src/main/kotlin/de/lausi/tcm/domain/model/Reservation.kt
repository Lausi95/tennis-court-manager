package de.lausi.tcm.domain.model

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

fun DayOfWeek.isWeekend() = this == DayOfWeek.SATURDAY || this == DayOfWeek.SUNDAY

@Document("reservation")
data class Reservation(
  val id: String,
  val courtId: String,
  val date: LocalDate,
  val fromSlot: Int,
  val toSlot: Int,
  val creatorId: String,
  val memberIds: List<String>,
) {

  constructor(courtId: String, date: LocalDate, fromSlot: Int, toSlot: Int, creatorId: String, memberIds: List<String>) : this(
    UUID.randomUUID().toString(),
    courtId,
    date,
    fromSlot,
    toSlot,
    creatorId,
    memberIds,
  )

  fun hasCoreTimeSlot(): Boolean {
    return date.dayOfWeek.isWeekend() || (fromSlot..toSlot).any { isCoreTimeSlot(it) }
  }
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
  fun findByDateAndCourtId(date: LocalDate, courtId: String): List<Reservation>

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

  override fun OccupancyPlan.addBlock(date: LocalDate, courtIds: List<String>) {
    courtIds.forEach { courtId ->
      reservationRepository.findByDateAndCourtId(date, courtId).forEach {
        addBlock(courtId, it.toBlock())
      }
    }
  }

  fun Reservation.toBlock(): Block {
    val members = memberRepository.findAllById(memberIds)
    val description = members.joinToString(" & ") { it.firstname + " " + it.lastname.substring(0..1) + "." }
    return Block(BlockType.FREE_PLAY, fromSlot, toSlot, description)
  }
}
