package de.lausi.tcm.application.team

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.domain.model.*
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

    val team = Team(command.teamName, command.captainId)

    teamRepository.save(team)

    return Either.Success(CreateTeamResult(team))
  }
}
