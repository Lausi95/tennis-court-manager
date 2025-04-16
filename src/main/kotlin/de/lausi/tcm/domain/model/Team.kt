package de.lausi.tcm.domain.model

import de.lausi.tcm.domain.model.member.MemberId
import java.util.UUID

data class TeamId(val value: String) {
  companion object {
    fun generate() = TeamId(UUID.randomUUID().toString())
  }
}
data class TeamName(val value: String)

data class Team(
  val id: TeamId,
  val name: TeamName,
  val captainId: MemberId,
)

interface TeamRepository {

  fun existsById(teamId: TeamId): Boolean

  fun existsByName(teamName: TeamName): Boolean

  fun findById(teamId: TeamId): Team?

  fun findAll(): List<Team>

  fun save(team: Team)

  fun delete(teamId: TeamId)
}