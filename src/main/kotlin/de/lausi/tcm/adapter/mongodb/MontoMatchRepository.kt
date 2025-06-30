package de.lausi.tcm.adapter.mongodb

import de.lausi.tcm.domain.model.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Document("match")
private data class MongoMatch(
  @Id val id: String,
  val date: LocalDate,
  val courtIds: List<String>,
  val fromSlot: Int,
  val teamId: String,
  val opponentTeamName: String,
) {

  fun toMatch() = Match(
    date,
    courtIds.map { CourtId(it) },
    Slot(fromSlot),
    TeamId(teamId),
    MatchOpponentName(opponentTeamName),
    MatchId(id),
  )
}

private interface MongoMatchRepository : MongoRepository<MongoMatch, String> {

  fun findByDateGreaterThanEqual(date: LocalDate): List<MongoMatch>

  fun findByCourtIdsContainsAndDate(courtId: String, date: LocalDate): List<MongoMatch>
}

@Component
private class MatchRepositoryImpl(private val mongoRepository: MongoMatchRepository) : MatchRepository {

  override fun findByDateGreaterThanEqual(date: LocalDate): List<Match> {
    return mongoRepository.findByDateGreaterThanEqual(date).map { it.toMatch() }
  }

  override fun findByCourtIdsContainsAndDate(courtId: CourtId, date: LocalDate): List<Match> {
    return mongoRepository.findByCourtIdsContainsAndDate(courtId.value, date).map { it.toMatch() }
  }

  override fun save(match: Match): Match {
    return mongoRepository.save(
      MongoMatch(
        match.id.value,
        match.date,
        match.courtIds.map { it.value },
        match.fromSlot.index,
        match.teamId.value,
        match.opponentTeamName.value,
      )
    ).toMatch()
  }

  override fun delete(matchId: MatchId) {
    mongoRepository.deleteById(matchId.value)
  }

  override fun findAll(): List<Match> {
    return mongoRepository.findAll().map { it.toMatch() }
  }

  override fun findById(matchId: MatchId): Match? {
    return mongoRepository.findById(matchId.value).orElse(null)?.toMatch()
  }
}
