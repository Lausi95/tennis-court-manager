package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.Either
import de.lausi.tcm.adapter.web.memberId
import de.lausi.tcm.application.team.CreateTeamCommand
import de.lausi.tcm.application.team.CreateTeamUseCase
import de.lausi.tcm.application.team.DeleteTeamCommand
import de.lausi.tcm.application.team.DeleteTeamUseCase
import de.lausi.tcm.domain.model.*
import de.lausi.tcm.domain.model.member.MemberId
import de.lausi.tcm.domain.model.member.MemberRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.security.Principal

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
  private val teamRepository: TeamRepository,
  private val memberRepository: MemberRepository,
  private val createTeamUseCase: CreateTeamUseCase,
  private val deleteTeamUseCase: DeleteTeamUseCase,
) {

  @GetMapping
  fun getTeams(model: Model): String {
    val items = teamRepository.findAll().map { team ->
      val member = memberRepository.findById(team.captainId)
      val captainName = member?.formatName() ?: "???"
      TeamModel(
        team.id.value,
        team.name.value,
        captainName,
        mapOf(
          "delete" to "/api/teams/${team.id}"
        ),
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
    val createTeamResult = createTeamUseCase.execute(principal.memberId()) {
      CreateTeamCommand(
        TeamName(request.name),
        MemberId(request.captainMemberId),
      )
    }

    if (createTeamResult is Either.Success) {
      return getTeams(model)
    }

    if (createTeamResult is Either.Error) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, createTeamResult.value.name)
    }

    error("unreachable")
  }

  @DeleteMapping("/{teamId}")
  fun deleteTeam(model: Model, principal: Principal, @PathVariable(name = "teamId") teamIdValue: String): String {
    val deleteTeamResult = deleteTeamUseCase.execute(principal.memberId()) {
      DeleteTeamCommand(
        TeamId(teamIdValue),
      )
    }

    if (deleteTeamResult is Either.Success) {
      return getTeams(model)
    }

    if (deleteTeamResult is Either.Error) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, deleteTeamResult.value.name)
    }

    error("unreachable")
  }
}
