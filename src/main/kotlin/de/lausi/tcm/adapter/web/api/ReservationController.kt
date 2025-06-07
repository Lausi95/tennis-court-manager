package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.PageAssembler
import de.lausi.tcm.adapter.web.userId
import de.lausi.tcm.adapter.web.userInfo
import de.lausi.tcm.application.reservation.*
import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDate

data class CreateReservationRequest(
  val date: LocalDate,
  val courtId: String,
  val slotId: Int,
  val duration: Int,
  val memberId1: String,
  val memberId2: String,
  val memberId3: String,
  val memberId4: String,
)

@Controller
@RequestMapping("/reservations")
class ReservationController(
  private val pageAssembler: PageAssembler,
  private val getReservationsUseCase: GetReservationsUseCase,
  private val createReservationUseCase: CreateReservationUseCase,
  private val cancelReservationUseCase: CancelReservationUseCase,
  private val courtRepository: CourtRepository
) {

  @GetMapping
  fun getView(principal: Principal, model: Model, @RequestParam(required = false) use: String?): String {
    return with(pageAssembler) {
      model.preparePage("Buchen", principal) {
        return@preparePage if (use == "create") {
          getCreateReservation(principal, model)
        } else {
          getReservationCollection(principal, model)
        }
      }
    }
  }

  @GetMapping("/collection")
  fun getReservationCollection(principal: Principal, model: Model): String {
    val command = GetReservationsCommand(
      principal.userId(),
      LocalDate.now()
    )

    return runContext(getReservationsUseCase.execute(principal.userId(), command), model) {
      model.reservationCollection(
        it.reservations,
        it.courts,
        it.creators,
        it.players,
      )
      "views/reservations/collection"
    }
  }

  @GetMapping("/create")
  fun getCreateReservation(principal: Principal, model: Model): String {
    val params = CreateReservationContextParams(
      principal.userId(),
    )

    return runContext(createReservationUseCase.context(principal.userId(), params), model) {
      model.memberEntity(it.self)
      model.memberCollection(it.members)
      model.courtCollection(courtRepository.findAll())
      model.slotCollection(SlotRepository.findAll())
      model.userInfo(principal)
      "views/reservations/create"
    }
  }

  @PostMapping("/create")
  fun createReservation(principal: Principal, model: Model, request: CreateReservationRequest): String {
    val memberIds = listOf(request.memberId1, request.memberId2, request.memberId3, request.memberId4)
      .filter { it.isNotEmpty() }
      .map { MemberId(it) }

    val command = CreateReservationCommand(
      request.date,
      request.slotId,
      request.slotId + request.duration - 1,
      CourtId(request.courtId),
      principal.userId(),
      memberIds,
    )

    return runUseCase(
      createReservationUseCase.execute(principal.userId(), command),
      model,
      { getCreateReservation(principal, model) }) {
      getReservationCollection(principal, model)
    }
  }

  @GetMapping("/{reservationId}/delete")
  fun getDeleteReservation(principal: Principal, model: Model, @PathVariable reservationId: String): String {
    val params = CancelReservationContextParams(
      ReservationId(reservationId),
    )

    return runContext(cancelReservationUseCase.context(principal.userId(), params), model) {
      model.reservationEntity(it.reservation, it.court, it.creator, it.players)
      "views/reservations/delete"
    }
  }

  @PostMapping("/{reservationId}/delete")
  fun deleteReservation(principal: Principal, model: Model, @PathVariable reservationId: String): String {
    val command = CancelReservationCommand(
      ReservationId(reservationId),
    )

    return runUseCase(
      cancelReservationUseCase.execute(principal.userId(), command),
      model,
      { getDeleteReservation(principal, model, reservationId) }) {
      getReservationCollection(principal, model)
    }
  }
}
