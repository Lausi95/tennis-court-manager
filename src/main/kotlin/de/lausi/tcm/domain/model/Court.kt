package de.lausi.tcm.domain.model

import java.util.*

data class CourtId(val value: String = UUID.randomUUID().toString())
data class CourtName(val value: String)

data class Court(
  var name: CourtName,
  val id: CourtId = CourtId(),
)

interface CourtRepository {

  fun existsById(courtId: CourtId): Boolean

  fun existsByName(courtName: CourtName): Boolean

  fun existsByNameAndIdNot(courtName: CourtName, courtId: CourtId): Boolean

  fun allExistById(courtIds: List<CourtId>): Boolean

  fun findAll(): List<Court>

  fun findAllById(courtIds: List<CourtId>): List<Court>

  fun findById(courtId: CourtId): Court?

  fun save(court: Court): Court

  fun delete(id: CourtId)
}
