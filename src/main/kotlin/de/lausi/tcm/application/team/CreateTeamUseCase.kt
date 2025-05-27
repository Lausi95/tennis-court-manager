package de.lausi.tcm.application.team

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class CreateTeamContext(
  val members: List<Member>,
)

data class CreateTeamCommand(
  val teamName: TeamName,
  val captainId: MemberId,
)

data class CreateTeamResult(
  val team: Team,
)

enum class CreateTeamError {
  TEAM_NAME_ALREADY_EXISTS,
  CAPTAIN_DOES_NOT_EXIST,
}

@UseCaseComponent
class CreateTeamUseCase(
  private val teamRepository: TeamRepository,
  private val memberRepository: MemberRepository,
  private val permissions: Permissions,
) : UseCase<
        Nothing?,
        CreateTeamContext,
        CreateTeamCommand,
        CreateTeamResult,
        CreateTeamError> {

  override fun checkContextPermission(userId: MemberId, contextParams: Nothing?): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun getContext(params: Nothing?): Either<CreateTeamContext, CreateTeamError> {
    val members = memberRepository.findAll()
    return Either.Success(CreateTeamContext(members))
  }

  override fun checkCommandPermission(userId: MemberId, command: CreateTeamCommand): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: CreateTeamCommand): Either<CreateTeamResult, CreateTeamError> {
    val errors = mutableListOf<CreateTeamError>()

    if (teamRepository.existsByName(command.teamName)) {
      errors.add(CreateTeamError.TEAM_NAME_ALREADY_EXISTS)
    }

    if (!memberRepository.exists(command.captainId)) {
      errors.add(CreateTeamError.CAPTAIN_DOES_NOT_EXIST)
    }

    if (errors.isNotEmpty()) {
      return Either.Error(errors)
    }

    val team = Team(command.teamName, command.captainId)

    teamRepository.save(team)

    return Either.Success(CreateTeamResult(team))
  }
}
