package de.lausi.tcm.application.team

import de.lausi.tcm.Either
import de.lausi.tcm.application.ReadUseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class GetTeamsResult(
  val teams: List<Team>,
  val captains: Map<MemberId, Member>,
)

enum class GetTeamsError

@UseCaseComponent
class GetTeamsUseCase(
  private val permissions: Permissions,
  private val teamRepository: TeamRepository,
  private val memberRepository: MemberRepository,
) : ReadUseCase<Nothing?, GetTeamsResult, GetTeamsError> {

  override fun checkPermission(userId: MemberId, command: Nothing?): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TEAM_CAPTAIN)
  }

  override fun handle(command: Nothing?): Either<GetTeamsResult, GetTeamsError> {
    val teams = teamRepository.findAll().sortedBy { it.name }

    val captains = mutableMapOf<MemberId, Member>()
    teams.forEach {
      memberRepository.findById(it.captainId)?.let { captain ->
        captains[it.captainId] = captain
      }
    }

    return Either.Success(GetTeamsResult(teams, captains))
  }
}
