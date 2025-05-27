package de.lausi.tcm.application.match

import de.lausi.tcm.Either
import de.lausi.tcm.application.ReadUseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class GetMatchesResponse(
  val matches: List<Match>,
  val courts: Map<MatchId, List<Court>>,
  val teams: Map<MatchId, Team>,
  val captains: Map<MatchId, Member>,
)

@UseCaseComponent
class GetMatchesUseCase(
  private val matchRepository: MatchRepository,
  private val courtRepository: CourtRepository,
  private val teamRepository: TeamRepository,
  private val memberRepository: MemberRepository,
) : ReadUseCase<Nothing?, GetMatchesResponse, Nothing> {

  override fun checkPermission(userId: MemberId, command: Nothing?): Boolean {
    return true
  }

  override fun handle(command: Nothing?): Either<GetMatchesResponse, Nothing> {
    val matches = matchRepository.findAll()

    val courts = mutableMapOf<MatchId, List<Court>>()
    val teams = mutableMapOf<MatchId, Team>()
    val captains = mutableMapOf<MatchId, Member>()
    matches.forEach {
      courts[it.id] = courtRepository.findAllById(it.courtIds)
      val team = teamRepository.findById(it.teamId) ?: return Either.Error()
      teams[it.id] = team
      captains[it.id] = memberRepository.findById(team.captainId) ?: return Either.Error()
    }

    return Either.Success(GetMatchesResponse(matches, courts, teams, captains))
  }
}