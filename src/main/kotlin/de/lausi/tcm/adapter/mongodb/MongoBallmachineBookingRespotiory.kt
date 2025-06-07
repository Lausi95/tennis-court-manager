package de.lausi.tcm.adapter.mongodb

import de.lausi.tcm.domain.model.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Document("ballmachineBooking")
private data class MongoBallmachineBooking(
  @Id val id: String,
  val courtId: String,
  val date: LocalDate,
  val slot: Int,
  val memberId: String,
  val passCode: String,
) {

  fun toBallmachineBooking() = BallmachineBooking(
    CourtId(courtId),
    date,
    Slot(slot),
    MemberId(memberId),
    BallmachineBookingPassCode(passCode),
    BallmachineBookingId(id),
  )
}

private interface MongoBallmachineBookingRepository : MongoRepository<MongoBallmachineBooking, String> {

  fun findByMemberId(memberId: String): List<MongoBallmachineBooking>

  fun findByDateAndCourtId(date: LocalDate, courtId: String): List<MongoBallmachineBooking>

  fun findByMemberIdAndDateGreaterThanEqual(memberId: String, minDate: LocalDate): List<MongoBallmachineBooking>
}

@Component
private class BallmachineBookingRepositoryImpl(
  private val mongoRepository: MongoBallmachineBookingRepository
) : BallmachineBookingRepository {

  override fun findAll(): List<BallmachineBooking> {
    return mongoRepository.findAll().map { it.toBallmachineBooking() }
  }

  override fun findByMemberId(memberId: MemberId): List<BallmachineBooking> {
    return mongoRepository.findByMemberId(memberId.value).map { it.toBallmachineBooking() }
  }

  override fun findByDateAndCourtId(
    date: LocalDate,
    courtId: CourtId
  ): List<BallmachineBooking> {
    return mongoRepository.findByDateAndCourtId(date, courtId.value).map { it.toBallmachineBooking() }
  }

  override fun findByMemberIdAndDateGreaterThanEqual(
    memberId: MemberId,
    minDate: LocalDate
  ): List<BallmachineBooking> {
    return mongoRepository.findByMemberIdAndDateGreaterThanEqual(memberId.value, minDate)
      .map { it.toBallmachineBooking() }
  }

  override fun save(ballmachineBooking: BallmachineBooking): BallmachineBooking {
    return mongoRepository.save(
      MongoBallmachineBooking(
        ballmachineBooking.id.value,
        ballmachineBooking.courtId.value,
        ballmachineBooking.date,
        ballmachineBooking.slot.index,
        ballmachineBooking.memberId.value,
        ballmachineBooking.passCode.value,
      )
    ).toBallmachineBooking()
  }

  override fun delete(ballmachineBookingId: BallmachineBookingId) {
    mongoRepository.deleteById(ballmachineBookingId.value)
  }

  override fun findById(ballmachineBookingId: BallmachineBookingId): BallmachineBooking? {
    return mongoRepository.findById(ballmachineBookingId.value).orElse(null)?.toBallmachineBooking()
  }
}
