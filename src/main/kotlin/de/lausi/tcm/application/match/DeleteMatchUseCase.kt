package de.lausi.tcm.application.match

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class DeleteMatchContextParams(
  val matchId: MatchId,
)

data class DeletematchContext(
  val match: Match,
  val courts: List<Court>,
  val team: Team,
  val captain: Member,
)

data class DeleteMatchCommand(
  val matchId: MatchId,
)

enum class DeleteMatchError {
}

@UseCaseComponent
class DeleteMatchUseCase(
  val permissions: Permissions,
  val matchRepository: MatchRepository,
  val courtRepository: CourtRepository,
  val teamRepository: TeamRepository,
  val memberRepository: MemberRepository,
) :
  UseCase<
          DeleteMatchContextParams,
          DeletematchContext,
          DeleteMatchCommand,
          Nothing?,
          DeleteMatchError> {

  override fun checkContextPermission(userId: MemberId, contextParams: DeleteMatchContextParams): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun getContext(params: DeleteMatchContextParams): Either<DeletematchContext, DeleteMatchError> {
    val match = matchRepository.findById(params.matchId)
      ?: return Either.Error()

    val team = teamRepository.findById(match.teamId)
      ?: return Either.Error()

    val captain = memberRepository.findById(team.captainId)
      ?: return Either.Error()

    val courts = courtRepository.findAllById(match.courtIds)

    return Either.Success(DeletematchContext(match, courts, team, captain))
  }

  override fun checkCommandPermission(userId: MemberId, command: DeleteMatchCommand): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: DeleteMatchCommand): Either<Nothing?, DeleteMatchError> {
    matchRepository.delete(command.matchId)
    return Either.Success()
  }
}