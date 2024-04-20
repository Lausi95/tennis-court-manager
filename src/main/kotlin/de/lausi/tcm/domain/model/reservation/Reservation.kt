package de.lausi.tcm.domain.model.reservation

import java.time.LocalDate

data class Reservation(
  val court: String,
  val date: LocalDate,
  val fromSlot: Int,
  val toSlot: Int,
  val players: List<String>,
)
