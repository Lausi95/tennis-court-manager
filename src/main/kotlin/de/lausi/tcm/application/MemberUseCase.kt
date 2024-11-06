package de.lausi.tcm.application

import de.lausi.tcm.domain.model.member.*
import org.springframework.stereotype.Component

@Component
class MemberUseCase(
  private val memberRepository: MemberRepository,
  private val memberService: MemberService
) {

  fun getAllMembers(): List<Member> {
    return memberRepository
      .findAll()
      .sortedBy { it.firstname }
  }

  fun getOneMember(memberId: MemberId): Member {
    return memberService.getMember(memberId)
  }

  fun toggleGroup(userMemberId: MemberId, memberId: MemberId, group: MemberGroup): Member {
    memberService.assertGroup(userMemberId, MemberGroup.ADMIN)
    val member = memberService.getMember(memberId)
    member.toggleGroup(group)
    memberRepository.save(member)
    return member
  }
}
