package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*
import de.lausi.tcm.ger
import org.springframework.ui.Model

data class MatchModel(
  val id: String,
  val date: String,
  val courts: CourtCollection,
  val fromTime: String,
  val toTime: String,
  val team: TeamModel,
  val oppenentTeamName: String,
  val links: Map<String, String> = mapOf()
)

fun Match.toModel(courts: List<Court>, team: Team, teamCaptain: Member) = MatchModel(
  this.id.value,
  this.date.ger(),
  courts.toCollection(),
  this.fromSlot.formatFromTime(),
  this.fromSlot.match().formatToTime(),
  team.toModel(teamCaptain),
  this.opponentTeamName.value,
  mapOf(
    "delete" to "/matches/${this.id.value}/delete"
  )
)

fun Model.matchEntity(match: Match, courts: List<Court>, team: Team, captain: Member) {
  addAttribute("match", match.toModel(courts, team, captain))
}

data class MatchCollection(
  val items: List<MatchModel>,
  val links: Map<String, String> = mapOf(),
)

fun List<Match>.toCollection(
  courts: Map<MatchId, List<Court>>,
  teams: Map<MatchId, Team>,
  captains: Map<MatchId, Member>
) = MatchCollection(
  this.map {
    it.toModel(
      courts[it.id] ?: error("courts does not exist"),
      teams[it.id] ?: error("teams does not exist"),
      captains[it.id] ?: error("captain does not exist"),
    )
  },
  mapOf(
    // TODO fill links
  )
)

fun Model.matchCollection(
  matches: List<Match>,
  courts: Map<MatchId, List<Court>>,
  teams: Map<MatchId, Team>,
  captains: Map<MatchId, Member>
) {
  addAttribute("matchCollection", matches.toCollection(courts, teams, captains))
}
