package de.lausi.tcm.application

import de.lausi.tcm.domain.model.Team
import de.lausi.tcm.domain.model.TeamRepository
import de.lausi.tcm.domain.model.member.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class TeamUseCase(
  private val teamRepository: TeamRepository,
  private val memberRepository: MemberRepository,
  private val memberService: MemberService,
) {

  fun createTeam(userMemberId: MemberId, teamName: String, teamCaptainMemberId: String): Team {
    memberService.assertGroup(userMemberId, MemberGroup.TEAM_CAPTAIN)
    if (teamRepository.existsByName(teamName)) {
      error("Team with name $teamName already exists.")
    }
    if (!memberRepository.exists(MemberId(teamCaptainMemberId))) {
      error("Member with name $teamCaptainMemberId does not exists.")
    }
    val team = Team(UUID.randomUUID().toString(), teamName, teamCaptainMemberId)
    return teamRepository.save(team)
  }

  fun deleteTeam(userMemberId: MemberId, teamId: String) {
    memberService.assertGroup(userMemberId, MemberGroup.TEAM_CAPTAIN)
    teamRepository.deleteById(teamId)
  }

  fun getAllTeams(): List<Team> {
    return teamRepository.findAll()
  }

  fun getMember(memberId: MemberId): Member? {
    return memberRepository.findById(memberId)
  }
}
