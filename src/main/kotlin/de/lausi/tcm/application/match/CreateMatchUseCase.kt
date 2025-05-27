package de.lausi.tcm.application.match

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*
import java.time.LocalDate

data class CreateMatchContext(
  val courts: List<Court>,
  val teams: List<Team>,
  val captains: Map<MemberId, Member>,
)

data class CreateMatchCommand(
  val teamId: TeamId,
  val date: LocalDate,
  val courts: List<CourtId>,
  val startSlot: Slot,
  val opponentName: MatchOpponentName,
)

data class CreateMatchResult(
  val matchId: MatchId
)

enum class CreateMatchError {
}

@UseCaseComponent
class CreateMatchUseCase(
  private val permissions: Permissions,
  private val matchRepository: MatchRepository,
  private val courtRepository: CourtRepository,
  private val teamRepository: TeamRepository,
  private val memberRepository: MemberRepository,
) :
  UseCase<Nothing?, CreateMatchContext, CreateMatchCommand, CreateMatchResult, CreateMatchError> {

  override fun checkContextPermission(userId: MemberId, contextParams: Nothing?): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TEAM_CAPTAIN)
  }

  override fun getContext(params: Nothing?): Either<CreateMatchContext, CreateMatchError> {
    val courts = courtRepository.findAll()
    val teams = teamRepository.findAll()
    val captains = mutableMapOf<MemberId, Member>()
    teams.forEach {
      captains[it.captainId] = memberRepository.findById(it.captainId) ?: return Either.Error()
    }

    return Either.Success(CreateMatchContext(courts, teams, captains))
  }

  override fun checkCommandPermission(userId: MemberId, command: CreateMatchCommand): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TEAM_CAPTAIN)
  }

  override fun handle(command: CreateMatchCommand): Either<CreateMatchResult, CreateMatchError> {
    // TODO Validation

    val match = Match(
      command.date,
      command.courts,
      command.startSlot,
      command.teamId,
      command.opponentName
    )

    matchRepository.save(match)

    return Either.Success(CreateMatchResult(match.id))
  }
}