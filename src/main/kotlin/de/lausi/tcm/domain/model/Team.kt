package de.lausi.tcm.domain.model

import java.util.*

data class TeamId(val value: String = UUID.randomUUID().toString())
data class TeamName(val value: String) : Comparable<TeamName> {
  override fun compareTo(other: TeamName): Int {
    return this.value.compareTo(other.value)
  }
}

data class Team(
  val name: TeamName,
  val captainId: MemberId,
  val id: TeamId = TeamId(),
)

interface TeamRepository {

  fun existsById(teamId: TeamId): Boolean

  fun existsByName(teamName: TeamName): Boolean

  fun findById(teamId: TeamId): Team?

  fun findAll(): List<Team>

  fun save(team: Team): Team

  fun delete(teamId: TeamId)
}