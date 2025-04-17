package de.lausi.tcm.adapter.mongodb

import de.lausi.tcm.domain.model.Court
import de.lausi.tcm.domain.model.CourtId
import de.lausi.tcm.domain.model.CourtName
import de.lausi.tcm.domain.model.CourtRepository
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component

@Document("court")
private data class MongoCourt(
  @Id val id: String,
  val name: String,
) {
  fun toCourt(): Court = Court(
    CourtName(name),
    CourtId(id),
  )
}

private interface MongoCourtRepository : MongoRepository<MongoCourt, String> {

  fun existsByName(name: String): Boolean

  fun existsByNameAndIdNot(name: String, id: String): Boolean
}

@Component
private class CourtRepositoryImpl(val mongoRepository: MongoCourtRepository) : CourtRepository {

  override fun existsById(courtId: CourtId): Boolean {
    return mongoRepository.existsById(courtId.value)
  }

  override fun existsByName(courtName: CourtName): Boolean {
    return mongoRepository.existsByName(courtName.value)
  }

  override fun existsByNameAndIdNot(courtName: CourtName, courtId: CourtId): Boolean {
    return mongoRepository.existsByNameAndIdNot(courtName.value, courtId.value)
  }

  override fun allExistById(courtIds: List<CourtId>): Boolean {
    return courtIds.all { this.existsById(it) }
  }

  override fun findById(courtId: CourtId): Court? {
    return mongoRepository.findById(courtId.value).orElse(null)?.toCourt()
  }

  override fun findAll(): List<Court> {
    return mongoRepository.findAll().map { it.toCourt() }
  }

  override fun findAllById(courtIds: List<CourtId>): List<Court> {
    val courtIdValues = courtIds.map { it.value }.toMutableList()
    return mongoRepository.findAllById(courtIdValues).map { it.toCourt() }
  }

  override fun save(court: Court) {
    mongoRepository.save(MongoCourt(
      court.id.value,
      court.name.value,
    ))
  }
}
