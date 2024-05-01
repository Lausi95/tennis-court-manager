package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.MemberRepository
import de.lausi.tcm.domain.model.Team
import de.lausi.tcm.domain.model.TeamRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class TeamModel(
  val id: String,
  val name: String,
  val captainName: String,
  val links: Map<String, String>
)

data class TeamCollection(
  val items: List<TeamModel>,
  val links: Map<String, String>
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
) {

  @GetMapping
  fun getTeams(model: Model): String {
    val items = teamRepository.findAll().map { team ->
      val captainName = memberRepository.findById(team.captainMemberId)
        .map { member -> member.firstname + " " + member.lastname }
        .orElse("???")

      TeamModel(team.id, team.name, captainName, mapOf(
        "create" to "/api/teams",
        "delete" to "/api/teams/${team.id}"
      ))
    }

    val teamCollection = TeamCollection(items, mapOf())
    model.addAttribute("teamCollection", teamCollection)

    memberController.getMembers(model)
    return "views/teams"
  }

  @PostMapping
  fun createTeam(model: Model, request: CreateTeamRequest): String {
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
  fun deleteTeam(model: Model, @PathVariable teamId: String): String {
    teamRepository.deleteById(teamId)
    return getTeams(model)
  }
}
