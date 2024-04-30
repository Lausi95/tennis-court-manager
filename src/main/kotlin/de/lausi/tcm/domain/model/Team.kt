package de.lausi.tcm.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

@Document("team")
data class Team(
  @Id val id: String,
  val name: String,
  val captainMemberId: String,
)

interface TeamRepository : MongoRepository<Team, String> {

  fun existsByName(name: String): Boolean
}
