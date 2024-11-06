package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.memberId
import de.lausi.tcm.application.TeamUseCase
import de.lausi.tcm.domain.model.*
import de.lausi.tcm.domain.model.member.MemberGroup
import de.lausi.tcm.domain.model.member.MemberId
import de.lausi.tcm.domain.model.member.MemberRepository
import de.lausi.tcm.domain.model.member.MemberService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

data class TeamModel(
  val id: String,
  val name: String,
  val captainName: String,
  val links: Map<String, String> = mapOf(),
)

data class TeamCollection(
  val items: List<TeamModel>,
  val links: Map<String, String> = mapOf(),
)

data class CreateTeamRequest(
  val name: String,
  val captainMemberId: String,
)

@Controller
@RequestMapping("/api/teams")
class TeamController(
  private val memberController: MemberController,
  private val teamUseCase: TeamUseCase,
) {

  @GetMapping
  fun getTeams(model: Model): String {
    val items = teamUseCase.getAllTeams().map { team ->
      val captainName = teamUseCase.getMember(MemberId(team.captainMemberId))?.formatName() ?: "???"

      TeamModel(
        team.id, team.name, captainName, mapOf(
          "delete" to "/api/teams/${team.id}"
        )
      )
    }

    val teamCollection = TeamCollection(
      items, mapOf(
        "create" to "/api/teams",
      )
    )
    model.addAttribute("teamCollection", teamCollection)

    memberController.getMembers(model)
    return "views/teams"
  }

  @PostMapping
  fun createTeam(model: Model, principal: Principal, request: CreateTeamRequest): String {
    val team = teamUseCase.createTeam(principal.memberId(), request.name, request.captainMemberId)
    return getTeams(model)
  }

  @DeleteMapping("/{teamId}")
  fun deleteTeam(model: Model, principal: Principal, @PathVariable teamId: String): String {
    teamUseCase.deleteTeam(principal.memberId(), teamId)
    return getTeams(model)
  }
}
