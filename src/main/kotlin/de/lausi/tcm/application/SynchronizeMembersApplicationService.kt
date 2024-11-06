package de.lausi.tcm.application

import de.lausi.tcm.domain.model.iam.IamUser
import de.lausi.tcm.domain.model.iam.IamUserRepository
import de.lausi.tcm.domain.model.member.*
import org.springframework.stereotype.Component

@Component
class SynchronizeMembersApplicationService(
  private val iamUserRepository: IamUserRepository, private val memberRepository: MemberRepository
) {

  fun synchronizeMembers() {
    val iamUsers = iamUserRepository.findAll()
    val members = memberRepository.findAll()

    createNotExistingMembers(iamUsers, members)
    deleteNotExistingIamUsers(iamUsers, members)
  }

  private fun createNotExistingMembers(iamUsers: List<IamUser>, members: List<Member>) {
    for (iamUser in iamUsers) {
      if (members.any { it.id.value == iamUser.id.value }) {
        continue
      }

      val member = Member(
        MemberId(iamUser.id.value),
        MemberFirstname(iamUser.firstname.value),
        MemberLastname(iamUser.lastname.value),
        mutableSetOf()
      )

      memberRepository.save(member)
    }
  }

  private fun deleteNotExistingIamUsers(iamUsers: List<IamUser>, members: List<Member>) {
    for (member in members) {
      if (iamUsers.any { it.id.value == member.id.value }) {
        continue
      }

      memberRepository.delete(member)
    }
  }
}
