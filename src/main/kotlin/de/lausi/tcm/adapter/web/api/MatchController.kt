package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.PageAssembler
import de.lausi.tcm.adapter.web.userId
import de.lausi.tcm.application.NOTHING
import de.lausi.tcm.application.match.*
import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal
import java.time.LocalDate

data class CreateMatchRequest(
  val date: LocalDate,
  val courtIds: List<String>,
  val fromSlotId: Int,
  val teamId: String,
  val opponentTeamName: String,
)

@Controller
@RequestMapping("/matches")
class MatchController(
  private val pageAssembler: PageAssembler,
  private val getMatchesUseCase: GetMatchesUseCase,
  private val createMatchUseCase: CreateMatchUseCase,
  private val deleteMatchUseCase: DeleteMatchUseCase,
) {

  @GetMapping
  fun getView(principal: Principal, model: Model): String {
    return with(pageAssembler) {
      model.preparePage("Events", principal) {
        getMatchCollection(principal, model)
      }
    }
  }

  @GetMapping("/collection")
  fun getMatchCollection(principal: Principal, model: Model): String {
    return runContext(getMatchesUseCase.execute(principal.userId(), NOTHING), model) {
      model.matchCollection(it.matches, it.courts, it.teams, it.captains)
      "views/matches/collection"
    }
  }

  @GetMapping("/create")
  fun getCreateMatch(principal: Principal, model: Model): String {
    return runContext(createMatchUseCase.context(principal.userId(), NOTHING), model) {
      model.courtCollection(it.courts)
      model.teamCollection(it.teams, it.captains)
      model.slotCollection(SlotRepository.findAll(), LocalDate.now())
      "views/matches/create"
    }
  }


  @PostMapping("/create")
  fun createMatch(principal: Principal, model: Model, request: CreateMatchRequest): String {
    val command = CreateMatchCommand(
      TeamId(request.teamId),
      request.date,
      request.courtIds.map { CourtId(it) },
      Slot(request.fromSlotId),
      MatchOpponentName(request.opponentTeamName),
    )

    return runUseCase(
      createMatchUseCase.execute(principal.userId(), command),
      model,
      { getCreateMatch(principal, model) }) {
      getMatchCollection(principal, model)
    }
  }

  @GetMapping("/{matchId}/delete")
  fun getDeleteMatch(principal: Principal, model: Model, @PathVariable matchId: String): String {
    val params = DeleteMatchContextParams(
      MatchId(matchId),
    )

    return runContext(deleteMatchUseCase.context(principal.userId(), params), model) {
      model.matchEntity(it.match, it.courts, it.team, it.captain)
      "views/matches/delete"
    }
  }

  @PostMapping("/{matchId}/delete")
  fun deleteMatch(model: Model, principal: Principal, @PathVariable matchId: String): String {
    val command = DeleteMatchCommand(
      MatchId(matchId),
    )

    return runUseCase(
      deleteMatchUseCase.execute(principal.userId(), command),
      model,
      { getDeleteMatch(principal, model, matchId) }) {
      getMatchCollection(principal, model)
    }
  }
}
