package de.lausi.tcm.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component

@Document("court")
data class Court(
  @Id val id: String,
  val name: String) {

  constructor(name: String) : this(name, name)
}

interface CourtRepository : MongoRepository<Court, String> {

  fun existsByName(name: String): Boolean

  fun existsByNameAndIdNot(name: String, id: String): Boolean
}

@Component
class CourtService(private val courtRepository: CourtRepository) {

  fun getCourt(courtId: String): Court? = courtRepository.findById(courtId).orElse(null)

  fun getCourts(): Iterable<Court> = courtRepository.findAll()

  fun getCourts(courtIds: Iterable<String>): Iterable<Court> = courtRepository.findAllById(courtIds)
}
