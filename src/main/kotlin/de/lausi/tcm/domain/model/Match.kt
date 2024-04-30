package de.lausi.tcm.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document("match")
data class Match(
  @Id val id: String,
  val date: LocalDate,
  val courtIds: List<String>,
  val fromSlot: Int,
  val teamName: String,
  val opponentTeamName: String,
)
