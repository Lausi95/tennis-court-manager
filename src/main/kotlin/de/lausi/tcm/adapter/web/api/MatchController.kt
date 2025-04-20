package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.memberId
import de.lausi.tcm.domain.model.*
import de.lausi.tcm.domain.model.MemberGroup
import de.lausi.tcm.domain.model.MemberRepository
import de.lausi.tcm.domain.model.Permissions
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class MatchModel(
  val id: String,
  val date: String,
  val courts: List<CourtModel>,
  val fromTime: String,
  val toTime: String,
  val team: TeamModel,
  val oppenentTeamName: String,
  val links: Map<String, String> = mapOf()
)

data class MatchCollection(
  val items: List<MatchModel>,
  val links: Map<String, String> = mapOf(),
)

data class CreateMatchRequest(
  val date: LocalDate,
  val courtIds: List<String>,
  val fromSlotId: Int,
  val teamId: String,
  val opponentTeamName: String,
)

@Controller
@RequestMapping("/api/matches")
class MatchController(
  private val permissions: Permissions,
  private val memberRepository: MemberRepository,
  private val matchRepository: MatchRepository,
  private val courtRepository: CourtRepository,
  private val teamRepository: TeamRepository,
  private val courtController: CourtController,
  private val teamController: TeamController,
  private val slotController: SlotController,
  private val matchService: MatchService,
  private val occupancyPlanService: OccupancyPlanService,
) {

  @GetMapping
  fun getMatches(model: Model): String {
    val items = matchRepository.findByDateGreaterThanEqual(LocalDate.now()).map { match ->
      val courts = with (courtController) { courtRepository.findAllById(match.courtIds).map { it.toModel() } }

      val team = teamRepository.findById(match.teamId)?.let { team ->
        val captainName = memberRepository.findById(team.captainId)?.formatName() ?: "???"
        TeamModel(team.id.value, team.name.value, captainName)
      } ?: TeamModel("???", "???", "???")

      MatchModel(
        match.id.value,
        match.date.format(DateTimeFormatter.ISO_DATE),
        courts,
        match.fromSlot.formatFromTime(),
        match.toSlot().formatToTime(),
        team,
        match.opponentTeamName.value,
        mapOf(
          "self" to "/api/matches/${match.id}",
          "delete" to "/api/matches/${match.id}",
        )
      )
    }

    model.addAttribute("matchCollection", MatchCollection(items, mapOf(
      "self" to "/api/matches",
      "create" to "/api/matches"
    )))

    courtController.getCourts(model)
    teamController.getTeams(model)
    slotController.getSlots(model)

    return "views/matches"
  }

  @PostMapping
  fun createMatch(model: Model, principal: Principal, request: CreateMatchRequest): String {
    permissions.assertGroup(principal.memberId(), MemberGroup.TEAM_CAPTAIN)

    val errors = mutableListOf<String>()
    val courtIds = request.courtIds.map { CourtId(it) }
    val teamId = TeamId(request.teamId)

    if (request.courtIds.isEmpty()) {
      errors.add("Es muss mindestens ein Platz ausgewaehlt sein")
    }

    if (!courtRepository.allExistById(courtIds)) {
      errors.add("Einer der Plaetze existiert nicht")
    }

    if (!teamRepository.existsById(teamId)) {
      errors.add("Das ausgewaehlte team existiert nicht")
    }

    val match = Match(
      request.date,
      courtIds,
      Slot(request.fromSlotId),
      teamId,
      MatchOpponentName(request.opponentTeamName),
    )

    with(matchService) {
      val reservationBlock = match.toBlock()
      val occupancyPlan = occupancyPlanService.getOccupancyPlan(request.date, courtIds)
      courtIds.forEach { courtId ->
        if (!occupancyPlan.canPlace(courtId, reservationBlock)) {
          errors.add("Der Platz ist zu dem Zeitraum schon belegt.")
        }
      }
    }

    if (errors.isNotEmpty()) {
      model.addAttribute("errors", errors)
      return getMatches(model)
    }

    matchRepository.save(match)

    return getMatches(model)
  }

  @DeleteMapping("/{matchId}")
  fun deleteMatch(model: Model, principal: Principal, @PathVariable(name = "matchId") matchIdValue: String): String {
    permissions.assertGroup(principal.memberId(), MemberGroup.TEAM_CAPTAIN)

    val matchId = MatchId(matchIdValue)
    matchRepository.delete(matchId)

    return getMatches(model)
  }
}
