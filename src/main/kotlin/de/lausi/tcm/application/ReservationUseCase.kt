package de.lausi.tcm.application

import de.lausi.tcm.domain.model.member.Member
import de.lausi.tcm.domain.model.member.MemberId
import de.lausi.tcm.domain.model.member.MemberRepository
import org.springframework.stereotype.Component

@Component
class ReservationUseCase(
  private val memberRepository: MemberRepository
) {

  fun getMember(memberId: MemberId): Member? {
    return memberRepository.findById(memberId)
  }
}
