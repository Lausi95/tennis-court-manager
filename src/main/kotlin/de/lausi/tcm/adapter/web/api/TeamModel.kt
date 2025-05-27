package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Member
import de.lausi.tcm.domain.model.MemberId
import de.lausi.tcm.domain.model.Team
import org.springframework.ui.Model

data class TeamModel(
  val id: String,
  val name: String,
  val captainName: String,
  val links: Map<String, String> = mapOf(),
)

fun Team.toModel(captain: Member?): TeamModel = TeamModel(
  this.id.value,
  this.name.value,
  captain?.formatName() ?: "???",
  mapOf(
    "delete" to "/teams/${this.id.value}/delete",
  ),
)

fun Model.teamEntity(team: Team, captain: Member?) {
  addAttribute("team", team.toModel(captain))
}

data class TeamCollection(
  val items: List<TeamModel>,
  val links: Map<String, String> = mapOf(),
)

fun List<Team>.toCollection(captains: Map<MemberId, Member>) = TeamCollection(
  this.map { it.toModel(captains[it.captainId]) },
  mapOf(
    "self" to "/teams",
    "create" to "/teams/create",
  )
)

fun Model.teamCollection(teams: List<Team>, captains: Map<MemberId, Member>) {
  addAttribute("teamCollection", teams.toCollection(captains))
}
