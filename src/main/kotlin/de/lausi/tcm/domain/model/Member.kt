package de.lausi.tcm.domain.model

import org.springframework.stereotype.Component

data class MemberId(val value: String)

data class MemberFirstname(val value: String) : Comparable<MemberFirstname> {
  override fun compareTo(other: MemberFirstname): Int {
    return this.value.compareTo(other.value)
  }
}

data class MemberLastname(val value: String)

enum class MemberGroup(val adminArea: Boolean) {
  ADMIN(true),
  EVENT_MANAGEMENT(true),
  TEAM_CAPTAIN(true),
  TRAINER(true),
  BALLMACHINE(false),
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

  fun assertGroup(userId: MemberId, memberGroup: MemberGroup): Boolean {
    return memberRepository.findById(userId)?.let {
      it.hasGroup(MemberGroup.ADMIN) || it.hasGroup(memberGroup)
    } ?: false
  }
}
