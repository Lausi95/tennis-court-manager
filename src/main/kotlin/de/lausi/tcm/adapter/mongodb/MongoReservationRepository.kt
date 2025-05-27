package de.lausi.tcm.adapter.mongodb

import de.lausi.tcm.domain.model.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Document("reservation")
data class MongoReservation(
  @Id val id: String,
  val courtId: String,
  val date: LocalDate,
  val fromSlot: Int,
  val toSlot: Int,
  val creatorId: String,
  val playerIds: List<String>,
) {

  fun toReservation(): Reservation = Reservation(
    CourtId(courtId),
    date,
    Slot(fromSlot),
    Slot(toSlot),
    MemberId(creatorId),
    playerIds.map { MemberId(it) },
    ReservationId(id),
  )
}

private interface MongoReservationRespository : MongoRepository<MongoReservation, String> {

  fun findByCreatorId(creatorId: String): List<MongoReservation>

  fun findByDateAndCourtId(date: LocalDate, courtId: String): List<MongoReservation>

  fun findByCreatorIdAndDateGreaterThanEqual(memberId: String, minDate: LocalDate): List<MongoReservation>
}

@Component
private class ReservationRepositoryImpl(private val mongoRepository: MongoReservationRespository) :
  ReservationRepository {

  override fun findAll(): List<Reservation> {
    return mongoRepository.findAll().map { it.toReservation() }
  }

  override fun findByCretorId(memberId: MemberId): List<Reservation> {
    return mongoRepository.findByCreatorId(memberId.value).map { it.toReservation() }
  }

  override fun findByDateAndCourtId(date: LocalDate, courtId: CourtId): List<Reservation> {
    return mongoRepository.findByDateAndCourtId(date, courtId.value).map { it.toReservation() }
  }

  override fun findByCreatorIdAndDateGreaterThanEqual(memberId: String, minDate: LocalDate): List<Reservation> {
    return mongoRepository.findByCreatorIdAndDateGreaterThanEqual(memberId, minDate).map { it.toReservation() }
  }

  override fun save(reservation: Reservation): Reservation {
    return mongoRepository.save(MongoReservation(
      reservation.id.value,
      reservation.courtId.value,
      reservation.date,
      reservation.fromSlot.index,
      reservation.toSlot.index,
      reservation.creatorId.value,
      reservation.playerIds.map { it.value }
    )).toReservation()
  }

  override fun delete(reservationId: ReservationId) {
    mongoRepository.deleteById(reservationId.value)
  }

  override fun findById(reservationId: ReservationId): Reservation? {
    return mongoRepository.findById(reservationId.value).orElse(null)?.toReservation()
  }
}
