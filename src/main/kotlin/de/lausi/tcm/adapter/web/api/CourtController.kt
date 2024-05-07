package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Court
import de.lausi.tcm.domain.model.CourtRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

data class CourtModel(
  val id: String,
  val name: String,
  val links: Map<String, String> = mapOf(),
)

data class CourtCollection(
  val items: List<CourtModel>,
  val links: Map<String, String> = mapOf(),
)

@Controller
@RequestMapping("/api/courts")
class CourtController(private val courtRepository: CourtRepository) {

  @GetMapping
  fun getCourts(model: Model): String {
    val items = courtRepository.findAll().map { it.toModel() }
    val courtCollection = CourtCollection(items)

    model.addAttribute("courtCollection", courtCollection)

    return "views/courts"
  }

  fun Court.toModel(): CourtModel {
    return CourtModel(
      id,
      name,
      mapOf()
    )
  }
}
