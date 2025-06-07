package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.BallmachineBooking
import de.lausi.tcm.domain.model.BallmachineBookingId
import de.lausi.tcm.domain.model.Court
import de.lausi.tcm.domain.model.Member
import de.lausi.tcm.ger
import org.springframework.ui.Model

data class BallmachineBookingModel(
  val id: String,
  val court: CourtModel,
  val date: String,
  val time: String,
  val member: MemberModel,
  val passCode: String,
  val links: Map<String, String>,
)

fun BallmachineBooking.toModel(court: Court, member: Member): BallmachineBookingModel {
  return BallmachineBookingModel(
    this.id.value,
    court.toModel(),
    this.date.ger(),
    this.slot.formatFromTime(),
    member.toModel(),
    this.passCode.value,
    mapOf(
      "delete" to "/ballmachine-bookings/${this.id.value}/delete"
    )
  )
}

fun Model.ballmachineBookingEntity(ballmachineBooking: BallmachineBooking, court: Court, member: Member) {
  addAttribute("ballmachineBooking", ballmachineBooking.toModel(court, member))
}

data class BallmachineBookingCollection(
  val items: List<BallmachineBookingModel>,
  val links: Map<String, String>,
)

fun List<BallmachineBooking>.toCollection(
  courts: Map<BallmachineBookingId, Court>,
  members: Map<BallmachineBookingId, Member>,
): BallmachineBookingCollection {
  return BallmachineBookingCollection(
    map {
      it.toModel(
        courts[it.id] ?: error("Court by booking id ${it.id} not found"),
        members[it.id] ?: error("Member by booking id ${it.id} not found"),
      )
    },
    mapOf(
      "create" to "/ballmachine-bookings/create"
    )
  )
}

fun Model.ballmachineBookingCollection(
  ballmachineBookings: List<BallmachineBooking>,
  courts: Map<BallmachineBookingId, Court>,
  members: Map<BallmachineBookingId, Member>,
) {
  addAttribute("ballmachineBookingCollection", ballmachineBookings.toCollection(courts, members))
}
