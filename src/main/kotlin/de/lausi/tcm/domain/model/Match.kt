package de.lausi.tcm.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Document("match")
data class Match(
  @Id val id: String,
  val date: LocalDate,
  val courtIds: List<CourtId>,
  val fromSlot: Int,
  val teamId: TeamId,
  val opponentTeamName: String,
) {

  fun toSlot() = fromSlot + 9
}

interface MatchRepository: MongoRepository<Match, String> {

  fun findByDateGreaterThanEqual(date: LocalDate): List<Match>

  fun findByCourtIdsContainsAndDate(courtId: CourtId, date: LocalDate): List<Match>
}

@Component
class MatchService(
  private val matchRepository: MatchRepository,
  private val teamRepository: TeamRepository,
): OccupancyPlanResolver {

  override fun OccupancyPlan.addBlock(date: LocalDate, courtIds: List<CourtId>) {
    courtIds.map { courtId ->
      matchRepository.findByCourtIdsContainsAndDate(courtId, date).forEach {
        addBlock(courtId, it.toBlock())
      }
    }
  }

  fun Match.toBlock(): Block {
    val teamName = teamRepository.findById(teamId)?.name ?: "???"
    return Block(BlockType.MATCH, fromSlot, toSlot(), "$teamName vs. $opponentTeamName")
  }
}
