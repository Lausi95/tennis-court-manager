package de.lausi.tcm.application.team

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.domain.model.TeamId
import de.lausi.tcm.domain.model.TeamRepository
import de.lausi.tcm.domain.model.member.MemberGroup
import de.lausi.tcm.domain.model.member.MemberId
import de.lausi.tcm.domain.model.member.Permissions
import org.springframework.stereotype.Component

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
): UseCase<DeleteTeamCommand, Nothing?, DeleteTeamError> {

  override fun checkPermission(userId: MemberId) {
    permissions.assertGroup(userId, MemberGroup.TEAM_CAPTAIN)
  }

  override fun handle(command: DeleteTeamCommand): Either<Nothing?, DeleteTeamError> {
    if (!teamRepository.existsById(command.teamId)) {
      return Either.Error(DeleteTeamError.TEAM_DOES_NOT_EXIST)
    }

    teamRepository.delete(command.teamId)

    return Either.Success(null)
  }
}
