package de.lausi.tcm.domain.model

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

/**
 * Represents a person that is a member within the club.
 *
 * @param id ID of the member
 * @param firstname Firstname of the member
 * @param lastname Lastname of the memeber
 */
@Document("member")
data class Member(
  val id: String,
  val firstname: String,
  val lastname: String
)

interface MemberRepository : MongoRepository<Member, String> {

  fun existsByFirstnameAndLastname(firstname: String, lastname: String): Boolean
}
