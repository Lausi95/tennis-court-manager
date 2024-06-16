package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*
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
import java.util.UUID

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
  private val matchRepository: MatchRepository,
  private val courtRepository: CourtRepository,
  private val teamRepository: TeamRepository,
  private val memberRepository: MemberRepository,
  private val courtController: CourtController,
  private val teamController: TeamController,
  private val slotController: SlotController,
  private val memberService: MemberService,
  private val matchService: MatchService,
  private val occupancyPlanService: OccupancyPlanService,
  private val courtService: CourtService,
) {

  @GetMapping
  fun getMatches(model: Model): String {
    val items = matchRepository.findByDateGreaterThanEqual(LocalDate.now()).map { match ->
      val courts = with (courtController) { courtService.getCourts(match.courtIds).map { it.toModel() } }

      val team = teamRepository.findById(match.teamId).map { team ->
        val captainName = memberRepository.findById(team.captainMemberId).map { it.formatName() }.orElseGet { "???" }
        TeamModel(team.id, team.name, captainName)
      }.orElseGet {
        TeamModel("", "???", "???")
      }
      MatchModel(
        match.id,
        match.date.format(DateTimeFormatter.ISO_DATE),
        courts,
        formatFromTime(match.fromSlot),
        formatToTime(match.toSlot()),
        team,
        match.opponentTeamName,
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
    memberService.getMember(principal.name).assertRoles(Group.TEAM_CAPTAIN)

    val errors = mutableListOf<String>()

    if (request.courtIds.isEmpty()) {
      errors.add("Es muss mindestens ein Platz ausgewaehlt sein")
    }

    if (!request.courtIds.all { courtRepository.existsById(it) }) {
      errors.add("Einer der Plaetze existiert nicht")
    }

    if (!teamRepository.existsById(request.teamId)) {
      errors.add("Das ausgewaehlte team existiert nicht")
    }

    val match = Match(
      UUID.randomUUID().toString(),
      request.date,
      request.courtIds,
      request.fromSlotId,
      request.teamId,
      request.opponentTeamName,
    )

    with(matchService) {
      val reservationBlock = match.toBlock()
      val occupancyPlan = occupancyPlanService.getOccupancyPlan(request.date, request.courtIds)
      request.courtIds.forEach { courtId ->
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
  fun deleteMatch(model: Model, principal: Principal, @PathVariable matchId: String): String {
    memberService.getMember(principal.name).assertRoles(Group.TEAM_CAPTAIN)

    matchRepository.deleteById(matchId)
    return getMatches(model)
  }
}
