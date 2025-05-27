package de.lausi.tcm.application.reservation

import de.lausi.tcm.Either
import de.lausi.tcm.application.ReadUseCase
import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Component
import java.time.LocalDate

data class GetReservationsCommand(
  val creatorId: MemberId?,
  val minDate: LocalDate?,
)

data class GetReservationsResult(
  val reservations: List<Reservation>,
  val courts: Map<ReservationId, Court>,
  val creators: Map<ReservationId, Member>,
  val players: Map<ReservationId, List<Member>>,
)

@Component
class GetReservationsUseCase(
  private val reservationRepository: ReservationRepository,
  private val memberRepository: MemberRepository,
  private val courtRepository: CourtRepository,
  private val permissions: Permissions,
) :
  ReadUseCase<GetReservationsCommand, GetReservationsResult, Nothing> {

  override fun checkPermission(userId: MemberId, command: GetReservationsCommand): Boolean {
    if (command.creatorId == null
      || command.creatorId != userId
      || command.minDate == null
      || command.minDate.isBefore(LocalDate.now())
    ) {
      return permissions.assertGroup(userId, MemberGroup.ADMIN)
    }

    return true
  }

  override fun handle(command: GetReservationsCommand): Either<GetReservationsResult, Nothing> {
    var reservations = if (command.creatorId == null) {
      reservationRepository.findAll()
    } else {
      reservationRepository.findByCretorId(command.creatorId)
    }

    if (command.minDate != null) {
      reservations = reservations.filter { it.date.isEqual(command.minDate) || it.date.isAfter(command.minDate) }
    }

    val courts = mutableMapOf<ReservationId, Court>()
    val creators = mutableMapOf<ReservationId, Member>()
    val players = mutableMapOf<ReservationId, List<Member>>()
    reservations.forEach {
      courts[it.id] = courtRepository.findById(it.courtId) ?: error("TODO")
      creators[it.id] = memberRepository.findById(it.creatorId) ?: error("TODO")
      players[it.id] = memberRepository.findById(it.playerIds)
    }

    return Either.Success(
      GetReservationsResult(
        reservations,
        courts,
        creators,
        players,
      )
    )
  }
}