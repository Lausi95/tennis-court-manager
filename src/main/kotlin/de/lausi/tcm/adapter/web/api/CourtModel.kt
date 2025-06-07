package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Court
import org.springframework.ui.Model

data class CourtModel(
  val id: String,
  val name: String,
  val links: Map<String, String>,
)

fun Court.toModel() = CourtModel(
  this.id.value,
  this.name.value,
  mapOf(
    "edit" to "/courts/${this.id.value}/edit",
    "delete" to "/courts/${this.id.value}/delete",
  ),
)

fun Model.courtEntity(court: Court) {
  addAttribute("court", court.toModel())
}

data class CourtCollection(
  val items: List<CourtModel>,
  val links: Map<String, String>,
)

fun List<Court>.toCollection() = CourtCollection(
  this.map { it.toModel() },
  mapOf(
    "create" to "/create"
  ),
)

fun Model.courtCollection(courts: List<Court>) {
  addAttribute("courtCollection", courts.toCollection())
}
