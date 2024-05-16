package de.lausi.tcm.domain.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

enum class Group {
  ADMIN,
  EVENT_MANAGEMENT,
  TEAM_CAPTAIN,
  TRAINER,
}

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
  val lastname: String,
  val groups: Set<Group> = setOf(),
) {

  fun formatName(): String {
    return "$firstname $lastname"
  }

  fun formatShortName(): String {
    return "$firstname ${lastname.substring(0..1)}."
  }

  fun assertRoles(vararg roles: Group) {
    roles.forEach {
      if (!groups.contains(it)) {
        throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }
    }
  }
}

interface MemberRepository : MongoRepository<Member, String> {

  fun existsByFirstnameAndLastname(firstname: String, lastname: String): Boolean
}

@Component
class MemberService(private val memberRepository: MemberRepository) {

  val log: Logger = LoggerFactory.getLogger(javaClass)

  fun getMember(id: String): Member {
    return memberRepository.findById(id).orElseThrow {
      log.warn("Could not find Member with id $id in database")
      ResponseStatusException(HttpStatus.NOT_FOUND, "Member with id $id not found")
    }
  }

  fun toggleGroup(memberId: String, group: Group): Member {
    val member = getMember(memberId)

    val updatedMember = if (member.groups.contains(group)) {
      log.info("Adding group $group to member $member")
      member.copy(groups = member.groups.minus(group))
    } else {
      log.info("Removing group $group to member $member")
      member.copy(groups = member.groups.plus(group))
    }

    memberRepository.save(updatedMember)

    return updatedMember
  }

  fun exists(name: String): Boolean {
    return memberRepository.existsById(name)
  }
}
