package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*
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
  private val teamRepository: TeamRepository,
  private val memberRepository: MemberRepository,
  private val memberController: MemberController,
  private val memberService: MemberService,
) {

  @GetMapping
  fun getTeams(model: Model): String {
    val items = teamRepository.findAll().map { team ->
      val captainName = memberRepository.findById(team.captainMemberId)
        .map { member -> member.firstname + " " + member.lastname }
        .orElse("???")

      TeamModel(team.id, team.name, captainName, mapOf(
        "delete" to "/api/teams/${team.id}"
      ))
    }

    val teamCollection = TeamCollection(items, mapOf(
      "create" to "/api/teams",
    ))
    model.addAttribute("teamCollection", teamCollection)

    memberController.getMembers(model)
    return "views/teams"
  }

  @PostMapping
  fun createTeam(model: Model, principal: Principal, request: CreateTeamRequest): String {
    memberService.getMember(principal.name).assertRoles(Group.TEAM_CAPTAIN)

    val errors = mutableListOf<String>()

    if (teamRepository.existsByName(request.name)) {
      errors.add("Team mit diesen namen existiert bereits.")
    }

    if (!memberRepository.existsById(request.captainMemberId)) {
      errors.add("Das mitglied existiert nicht.")
    }

    if (errors.isNotEmpty()) {
      model.addAttribute("errors", errors)
      return getTeams(model)
    }

    val team = Team(UUID.randomUUID().toString(), request.name, request.captainMemberId)
    teamRepository.save(team)

    return getTeams(model)
  }

  @DeleteMapping("/{teamId}")
  fun deleteTeam(model: Model, principal: Principal, @PathVariable teamId: String): String {
    memberService.getMember(principal.name).assertRoles(Group.TEAM_CAPTAIN)
    teamRepository.deleteById(teamId)
    return getTeams(model)
  }
}
