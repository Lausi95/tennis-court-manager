package de.lausi.tcm.adapter.mongodb

import de.lausi.tcm.domain.model.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component

@Document("team")
private data class MongoTeam(
  @Id val id: String,
  val name: String,
  val captainId: String,
) {

  fun toModel(): Team = Team(
    TeamName(name),
    MemberId("tom.lausmann"),
    TeamId(id),
  )
}

private interface MongoTeamRepository : MongoRepository<MongoTeam, String> {

  fun existsByName(name: String): Boolean
}

@Component
private class TeamRepositoryImpl(private val mongoRepository: MongoTeamRepository) : TeamRepository {

  override fun existsById(teamId: TeamId): Boolean {
    return mongoRepository.existsById(teamId.value)
  }

  override fun existsByName(teamName: TeamName): Boolean {
    return mongoRepository.existsByName(teamName.value)
  }

  override fun findById(teamId: TeamId): Team? {
    return mongoRepository.findById(teamId.value).orElse(null)?.toModel()
  }

  override fun findAll(): List<Team> {
    return mongoRepository.findAll().map { it.toModel() }
  }

  override fun save(team: Team): Team {
    return mongoRepository.save(
      MongoTeam(
        team.id.value,
        team.name.value,
        team.captainId.value,
      )
    ).toModel()
  }

  override fun delete(teamId: TeamId) {
    mongoRepository.deleteById(teamId.value)
  }
}