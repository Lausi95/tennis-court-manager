package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Court
import de.lausi.tcm.domain.model.Member
import de.lausi.tcm.domain.model.Reservation
import de.lausi.tcm.domain.model.ReservationId
import de.lausi.tcm.ger
import org.springframework.ui.Model

data class ReservationModel(
  val id: String,
  val date: String,
  val court: CourtModel,
  val fromTime: String,
  val toTime: String,
  val creator: MemberModel,
  val players: MemberCollection,
  val links: Map<String, String>,
)

fun Reservation.toModel(court: Court, creator: Member, players: List<Member>) = ReservationModel(
  this.id.value,
  this.date.ger(),
  court.toModel(),
  this.fromSlot.formatFromTime(),
  this.toSlot.formatToTime(),
  creator.toModel(),
  players.toCollection(),
  mapOf(
    "self" to "/reservations/${this.id.value}",
    "delete" to "/reservations/${this.id.value}/delete",
  )
)

fun Model.reservationEntity(reservation: Reservation, court: Court, creator: Member, players: List<Member>) {
  addAttribute("reservation", reservation.toModel(court, creator, players))
}

data class ReservationCollection(
  val items: List<ReservationModel>,
  val links: Map<String, String>,
)

fun List<Reservation>.toCollection(
  courts: Map<ReservationId, Court>,
  creators: Map<ReservationId, Member>,
  players: Map<ReservationId, List<Member>>
) =
  ReservationCollection(
    this.map {
      it.toModel(
        courts[it.id] ?: error("Cannot map Court"),
        creators[it.id] ?: error("Cannot map Creator"),
        players[it.id] ?: error("Cannot map Players"),
      )
    },
    mapOf()
  )

fun Model.reservationCollection(
  reservations: List<Reservation>,
  courts: Map<ReservationId, Court>,
  creators: Map<ReservationId, Member>,
  players: Map<ReservationId, List<Member>>
) {
  addAttribute("reservationCollection", reservations.toCollection(courts, creators, players))
}