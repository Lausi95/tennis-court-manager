package de.lausi.tcm.domain.model

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

data class MatchId(val value: String = UUID.randomUUID().toString())
data class MatchOpponentName(val value: String)

data class Match(
  val date: LocalDate,
  val courtIds: List<CourtId>,
  val fromSlot: Slot,
  val teamId: TeamId,
  val opponentTeamName: MatchOpponentName,
  val id: MatchId = MatchId(),
) {

  fun toSlot() = Slot(fromSlot.index + 9)
}

interface MatchRepository {

  fun findByDateGreaterThanEqual(date: LocalDate): List<Match>

  fun findByCourtIdsContainsAndDate(courtId: CourtId, date: LocalDate): List<Match>

  fun save(match: Match): Match

  fun delete(matchId: MatchId)

  fun findAll(): List<Match>

  fun findById(matchId: MatchId): Match?
}

@Component
class MatchOccupancyPlanResolver(
  private val teamRepository: TeamRepository,
  private val matchRepository: MatchRepository,
) : OccupancyPlanResolver {

  override fun forBlockType() = BlockType.MATCH

  override fun OccupancyPlan.addBlock(date: LocalDate, courtIds: List<CourtId>) {
    courtIds.map { courtId ->
      matchRepository.findByCourtIdsContainsAndDate(courtId, date).forEach {
        addBlock(courtId, it.toBlock())
      }
    }
  }

  fun Match.toBlock(): Block {
    val teamName = teamRepository.findById(teamId)?.name?.value ?: "???"
    return Block(BlockType.MATCH, fromSlot, toSlot(), "$teamName vs. ${opponentTeamName.value}")
  }
}
