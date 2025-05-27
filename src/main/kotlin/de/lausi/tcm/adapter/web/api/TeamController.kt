package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.PageAssembler
import de.lausi.tcm.adapter.web.userId
import de.lausi.tcm.application.NOTHING
import de.lausi.tcm.application.team.*
import de.lausi.tcm.domain.model.MemberId
import de.lausi.tcm.domain.model.TeamId
import de.lausi.tcm.domain.model.TeamName
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal

data class CreateTeamRequest(
  val name: String,
  val captainMemberId: String,
)

@Controller
@RequestMapping("/teams")
class TeamController(
  private val pageAssembler: PageAssembler,
  private val getTeamUseCase: GetTeamsUseCase,
  private val createTeamUseCase: CreateTeamUseCase,
  private val deleteTeamUseCase: DeleteTeamUseCase,
) {

  @GetMapping
  fun getView(principal: Principal, model: Model): String {
    return with(pageAssembler) {
      model.preparePage("Events", principal) {
        getTeamCollection(principal, model)
      }
    }
  }

  @GetMapping("/collection")
  fun getTeamCollection(
    principal: Principal,
    model: Model,
  ): String {
    return runContext(getTeamUseCase.execute(principal.userId(), NOTHING), model) {
      model.teamCollection(it.teams, it.captains)
      "views/teams/collection"
    }
  }

  @GetMapping("/create")
  fun getCreateTeam(principal: Principal, model: Model): String {
    return runContext(createTeamUseCase.context(principal.userId(), NOTHING), model) {
      model.memberCollection(it.members)
      "views/teams/create"
    }
  }

  @PostMapping("/create")
  fun createTeam(principal: Principal, model: Model, request: CreateTeamRequest): String {
    val command = CreateTeamCommand(
      TeamName(request.name),
      MemberId(request.captainMemberId),
    )

    return runUseCase(
      createTeamUseCase.execute(principal.userId(), command),
      model,
      { getCreateTeam(principal, model) }) {
      getTeamCollection(principal, model)
    }
  }

  @GetMapping("/{teamId}/delete")
  fun getDeleteTeam(principal: Principal, model: Model, @PathVariable("teamId") teamId: String): String {
    val params = DeleteTeamContextParams(
      TeamId(teamId),
    )

    return runContext(deleteTeamUseCase.context(principal.userId(), params), model) {
      model.teamEntity(it.team, it.captain)
      "views/teams/delete"
    }
  }

  @PostMapping("/{teamId}/delete")
  fun deleteTeam(model: Model, principal: Principal, @PathVariable(name = "teamId") teamIdValue: String): String {
    val command = DeleteTeamCommand(
      TeamId(teamIdValue),
    )

    return runUseCase(
      deleteTeamUseCase.execute(principal.userId(), command),
      model,
      { getDeleteTeam(principal, model, teamIdValue) }) {
      getTeamCollection(principal, model)
    }
  }
}
