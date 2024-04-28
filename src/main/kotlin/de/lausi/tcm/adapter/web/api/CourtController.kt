package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.CourtRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

data class CourtModel(
  val id: String,
  val name: String,
  val links: Map<String, String>
)

data class CourtCollection(
  val items: List<CourtModel>,
  val count: Int,
  val links: Map<String, String>
)

@Controller
@RequestMapping("/api/courts")
class CourtController(private val courtRepository: CourtRepository) {

  @GetMapping(headers = ["accept=application/json"])
  @ResponseBody
  fun getCourtCollection(): CourtCollection {
    val items = courtRepository.findAll().map { CourtModel(it.id, it.name, mapOf()) }
    return CourtCollection(
      items,
      items.size,
      mapOf()
    )
  }

  @GetMapping
  fun getCourts(model: Model): String {
    model.addAttribute("courtCollection", getCourtCollection())
    return "views/courts"
  }
}
