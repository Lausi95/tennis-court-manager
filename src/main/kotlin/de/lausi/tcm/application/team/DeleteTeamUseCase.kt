package de.lausi.tcm.application.team

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Component

data class DeleteTeamContextParams(
  val teamId: TeamId,
)

data class DeleteTeamContext(
  val team: Team,
  val captain: Member?,
)

data class DeleteTeamCommand(
  val teamId: TeamId,
)

enum class DeleteTeamError {
  TEAM_DOES_NOT_EXIST,
}

@Component
class DeleteTeamUseCase(
  private val permissions: Permissions,
  private val teamRepository: TeamRepository,
  private val memberRepository: MemberRepository,
) : UseCase<
        DeleteTeamContextParams,
        DeleteTeamContext,
        DeleteTeamCommand,
        Nothing?,
        DeleteTeamError> {

  override fun checkContextPermission(userId: MemberId, contextParams: DeleteTeamContextParams): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TEAM_CAPTAIN)
  }

  override fun getContext(params: DeleteTeamContextParams): Either<DeleteTeamContext, DeleteTeamError> {
    val team = teamRepository.findById(params.teamId)
      ?: return Either.Error(listOf(DeleteTeamError.TEAM_DOES_NOT_EXIST))

    val captain = memberRepository.findById(team.captainId)

    return Either.Success(DeleteTeamContext(team, captain))
  }

  override fun checkCommandPermission(userId: MemberId, command: DeleteTeamCommand): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TEAM_CAPTAIN)
  }

  override fun handle(command: DeleteTeamCommand): Either<Nothing?, DeleteTeamError> {
    if (!teamRepository.existsById(command.teamId)) {
      return Either.Error(listOf(DeleteTeamError.TEAM_DOES_NOT_EXIST))
    }

    teamRepository.delete(command.teamId)

    return Either.Success(null)
  }
}
