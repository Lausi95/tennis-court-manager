package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.PageAssembler
import de.lausi.tcm.adapter.web.userId
import de.lausi.tcm.application.NOTHING
import de.lausi.tcm.application.court.*
import de.lausi.tcm.domain.model.CourtId
import de.lausi.tcm.domain.model.CourtName
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal

data class CreateCourtRequest(
  val name: String,
)

data class UpdateCourtRequest(
  val name: String,
)

@Controller
@RequestMapping("/courts")
class CourtController(
  private val pageAssembler: PageAssembler,
  private val getCourtsUsecase: GetCourtsUsecase,
  private val createCourtUseCase: CreateCourtUseCase,
  private val updateCourtUseCase: UpdateCourtUseCase,
  private val deleteCourtUseCase: DeleteCourtUseCase,
) {

  @GetMapping
  fun getView(principal: Principal, model: Model): String {
    return with(pageAssembler) {
      model.preparePage("Courts", principal) {
        getCourtCollection(principal, model)
      }
    }
  }

  @GetMapping("/collection")
  fun getCourtCollection(principal: Principal, model: Model): String {
    return runContext(getCourtsUsecase.execute(principal.userId(), NOTHING), model) {
      model.courtCollection(it.courts)
      "views/courts/collection"
    }
  }

  @GetMapping("/create")
  fun getCreateCourtForm(principal: Principal, model: Model): String {
    return runContext(createCourtUseCase.context(principal.userId(), NOTHING), model) {
      "views/courts/create"
    }
  }

  @PostMapping("/create")
  fun createCourt(
    model: Model,
    principal: Principal,
    request: CreateCourtRequest
  ): String {
    val command = CreateCourtCommand(
      CourtName(request.name),
    )

    return runUseCase(
      createCourtUseCase.execute(principal.userId(), command),
      model,
      { getCreateCourtForm(principal, model) }) {
      getCourtCollection(principal, model)
    }
  }

  @GetMapping("/{courtId}/edit")
  fun getUpdateCourt(
    principal: Principal,
    model: Model,
    @PathVariable courtId: String
  ): String {
    val params = UpdateCourtContextParams(
      CourtId(courtId),
    )

    return runContext(updateCourtUseCase.context(principal.userId(), params), model) {
      model.courtEntity(it.court)
      "views/courts/edit"
    }
  }

  @PostMapping("/{courtId}/edit")
  fun updateCourt(
    principal: Principal,
    model: Model,
    @PathVariable courtId: String,
    request: UpdateCourtRequest,
  ): String {
    val command = UpdateCourtCommand(
      CourtId(courtId),
      CourtName(request.name.trim()),
    )

    return runUseCase(
      updateCourtUseCase.execute(principal.userId(), command),
      model,
      { getUpdateCourt(principal, model, courtId) }) {
      getCourtCollection(principal, model)
    }
  }

  @GetMapping("/{courtId}/delete")
  fun getDeleteCourt(principal: Principal, model: Model, @PathVariable courtId: String): String {
    val params = DeleteCourtContextParams(
      CourtId(courtId),
    )

    return runContext(deleteCourtUseCase.context(principal.userId(), params), model) {
      model.courtEntity(it.court)
      "views/courts/delete"
    }
  }

  @PostMapping("/{courtId}/delete")
  fun deleteCourt(princpal: Principal, model: Model, @PathVariable courtId: String): String {
    val command = DeleteCourtCommand(
      CourtId(courtId),
    )

    return runUseCase(
      deleteCourtUseCase.execute(princpal.userId(), command),
      model,
      { getDeleteCourt(princpal, model, courtId) }) {
      getCourtCollection(princpal, model)
    }
  }
}
