package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Court
import de.lausi.tcm.domain.model.CourtId
import de.lausi.tcm.domain.model.CourtName
import de.lausi.tcm.domain.model.CourtRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import java.security.Principal

data class CourtModel(
  val id: String,
  val name: String,
  val links: Map<String, String>,
) {

  companion object {
    val NOT_FOUND = CourtModel("???", "???", emptyMap())
  }
}

data class CourtCollection(
  val items: List<CourtModel>,
  val links: Map<String, String>,
)

data class CreateCourtRequest(
  val id: String,
  val name: String,
)

data class UpdateCourtRequest(
  val name: String,
)

@Controller
@RequestMapping("/api/courts")
class CourtController(private val courtRepository: CourtRepository) {

  @GetMapping
  fun getCourts(model: Model): String {
    val courts = courtRepository.findAll()
    return model.courtCollection(courts)
  }

  @GetMapping("/{courtId}")
  fun getCourt(model: Model, @PathVariable(name = "courtId") courtIdValue: String): String {
    val courtId = CourtId(courtIdValue)

    val court = courtRepository.findById(courtId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Court with id $courtId not found.")

    return model.court(court)
  }

  @GetMapping("/create-form")
  fun getCreateCourtForm(model: Model): String {
    return model.createCourtForm()
  }

  @PostMapping
  fun createCourt(model: Model, principal: Principal, request: CreateCourtRequest): String {
    val errors = mutableListOf<String>()

    val courtId = CourtId(request.id.trim())
    if (courtRepository.existsById(courtId)) {
      errors.add("Ein Platz mit der ID '${request.id}' existiert bereits.")
    }

    val courtName = CourtName(request.name.trim())
    if (courtRepository.existsByName(courtName)) {
      errors.add("Ein Platz mit dem Namen '${request.name}' existiert bereits.")
    }

    if (errors.isNotEmpty()) {
      return model.createCourtForm(errors)
    }

    val court = Court(courtName)
    courtRepository.save(court)

    return model.courtItem(court)
  }

  @GetMapping("/{courtId}/update-form")
  fun updateCourtForm(model: Model, @PathVariable(name = "courtIdValue") courtIdValue: String): String {
    val courtId = CourtId(courtIdValue)
    return model.updateCourtForm(courtId)
  }

  @PutMapping("/{courtId}")
  fun updateCourt(model: Model, @PathVariable(name = "courtId") courtIdValue: String, request: UpdateCourtRequest): String {
    val errors = mutableListOf<String>()

    val courtId = CourtId(courtIdValue)
    val courtName = CourtName(request.name.trim())

    if (courtRepository.existsByNameAndIdNot(courtName, courtId)) {
      errors.add("Der name kann nicht auf $courtName geaendert werden. Ein anderer Platz mit diesen Namen existiert bereits.")
    }

    if (errors.isNotEmpty()) {
      return model.updateCourtForm(courtId, errors)
    }

    val court = courtRepository.findById(courtId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    val updatedCourt = court.copy(name = courtName)
    courtRepository.save(updatedCourt)
    return model.courtItem(updatedCourt)
  }

  fun Court.toModel(): CourtModel {
    return CourtModel(id.value, name.value, mapOf(
      "self" to "/api/courts/${id.value}"
    ))
  }

  fun Iterable<Court>.toCollection(): CourtCollection {
    return CourtCollection(map { it.toModel() }, mapOf(
      "self" to "/api/courts",
      "create" to "/api/courts",
      "create-form" to "/api/courts/create-form"
    ))
  }

  fun Model.court(court: Court): String {
    val entity = court.toModel()
    addAttribute("court", entity)
    return "courts/entity"
  }

  fun Model.courtItem(court: Court): String {
    val item = court.toModel()
    addAttribute("item", item)
    return "courts/item"
  }

  fun Model.courtCollection(courts: Iterable<Court>): String {
    val courtCollection = courts.toCollection()
    addAttribute(courtCollection)
    return "courts/collection"
  }

  fun Model.createCourtForm(errors: List<String>? = null): String {
    errors?.let { addAttribute("errors", errors) }
    addAttribute("submit", "/api/courts")
    return "courts/forms/create"
  }

  fun Model.updateCourtForm(courtId: CourtId, errors: List<String>? = null): String {
    errors?.let { addAttribute("errors", errors) }
    addAttribute("submit", "/api/courts/${courtId.value}")
    return "courts/forms/update"
  }
}
