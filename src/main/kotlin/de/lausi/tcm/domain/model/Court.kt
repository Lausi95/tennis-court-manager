package de.lausi.tcm.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

@Document("court")
data class Court(
  @Id val id: String,
  val name: String) {

  constructor(name: String) : this(name, name)
}

interface CourtRepository : MongoRepository<Court, String>
