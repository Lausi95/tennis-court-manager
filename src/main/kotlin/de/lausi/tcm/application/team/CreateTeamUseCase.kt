package de.lausi.tcm.application.team

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.domain.model.Team
import de.lausi.tcm.domain.model.TeamId
import de.lausi.tcm.domain.model.TeamName
import de.lausi.tcm.domain.model.TeamRepository
import de.lausi.tcm.domain.model.member.*
import org.springframework.stereotype.Component

data class CreateTeamCommand(
  val teamName: TeamName,
  val captainId: MemberId,
)

data class CreateTeamResult(val team: Team)

enum class CreateTeamError {
  TEAM_NAME_ALREADY_EXISTS,
  CAPTAIN_DOES_NOT_EXIST,
}

@Component
class CreateTeamUseCase(
  private val teamRepository: TeamRepository,
  private val memberRepository: MemberRepository,
  private val permissions: Permissions,
): UseCase<CreateTeamCommand, CreateTeamResult, CreateTeamError> {

  override fun checkPermission(userId: MemberId) {
    permissions.assertGroup(userId, MemberGroup.TEAM_CAPTAIN)
  }

  override fun handle(command: CreateTeamCommand): Either<CreateTeamResult, CreateTeamError> {
    if (teamRepository.existsByName(command.teamName)) {
      return Either.Error(CreateTeamError.TEAM_NAME_ALREADY_EXISTS)
    }

    if (!memberRepository.exists(command.captainId)) {
      return Either.Error(CreateTeamError.CAPTAIN_DOES_NOT_EXIST)
    }

    val team = Team(TeamId.generate(), command.teamName, command.captainId)

    teamRepository.save(team)

    return Either.Success(CreateTeamResult(team))
  }
}
