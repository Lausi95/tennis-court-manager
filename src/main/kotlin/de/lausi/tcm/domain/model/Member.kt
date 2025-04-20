package de.lausi.tcm.domain.model

import org.springframework.stereotype.Component

data class MemberId(val value: String)
data class MemberFirstname(val value: String) : Comparable<MemberFirstname> {
  override fun compareTo(other: MemberFirstname): Int {
    return this.value.compareTo(other.value)
  }
}

data class MemberLastname(val value: String)
enum class MemberGroup {
  ADMIN,
  EVENT_MANAGEMENT,
  TEAM_CAPTAIN,
  TRAINER,
}

data class Member(
  val id: MemberId,
  val firstname: MemberFirstname,
  val lastname: MemberLastname,
  val groups: MutableSet<MemberGroup>,
) {

  fun hasGroup(group: MemberGroup): Boolean = this.groups.contains(group)

  fun toggleGroup(group: MemberGroup) {
    if (hasGroup(group)) {
      groups.remove(group)
    } else {
      groups.add(group)
    }
  }

  fun formatName() = "${firstname.value} ${lastname.value}"
}

interface MemberRepository {

  fun exists(memberId: MemberId): Boolean

  fun findById(memberId: MemberId): Member?

  fun findById(memberIds: List<MemberId>): List<Member>

  fun findAll(): List<Member>

  fun save(member: Member)

  fun delete(member: Member)
}

@Component
class Permissions(private val memberRepository: MemberRepository) {

  fun assertGroup(memberId: MemberId, memberGroup: MemberGroup) {
    val member = memberRepository.findById(memberId) ?: error("Member with id $memberId not found")
    if (!member.hasGroup(memberGroup)) {
      error("Member with id $memberId has not the group $memberGroup")
    }
  }
}
