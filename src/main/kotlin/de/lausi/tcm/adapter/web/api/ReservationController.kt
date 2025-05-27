package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.userId
import de.lausi.tcm.application.reservation.*
import de.lausi.tcm.domain.model.CourtId
import de.lausi.tcm.domain.model.MemberId
import de.lausi.tcm.domain.model.ReservationId
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal
import java.time.LocalDate

data class PostReservationParams(
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
@RequestMapping("/api/reservations")
class ReservationController(
  private val getReservationsUseCase: GetReservationsUseCase,
  private val createReservationUseCase: CreateReservationUseCase,
  private val cancelReservationUseCase: CancelReservationUseCase
) {

  @GetMapping
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
      "views/reservation/collection"
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
      "views/reservation/create"
    }
  }

  @PostMapping("/create")
  fun createReservation(principal: Principal, model: Model, params: PostReservationParams): String {
    val memberIds = listOf(params.memberId1, params.memberId2, params.memberId3, params.memberId4)
      .filter { it.isNotEmpty() }
      .map { MemberId(it) }

    val command = CreateReservationCommand(
      params.date,
      params.slotId,
      params.slotId + params.duration,
      CourtId(params.courtId),
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

  @GetMapping("/{reservationId}/cancel")
  fun getCancelReservation(principal: Principal, model: Model, @PathVariable reservationId: String): String {
    val params = CancelReservationContextParams(
      ReservationId(reservationId),
    )

    return runContext(cancelReservationUseCase.context(principal.userId(), params), model) {
      model.reservationEntity(it.reservation, it.court, it.creator, it.players)
      "views/reservation/cancel"
    }
  }

  @PostMapping("/{reservationId}/cancel")
  fun cancelReservation(principal: Principal, model: Model, @PathVariable reservationId: String): String {
    val command = CancelReservationCommand(
      ReservationId(reservationId),
    )

    return runUseCase(
      cancelReservationUseCase.execute(principal.userId(), command),
      model,
      { getCancelReservation(principal, model, reservationId) }) {
      getReservationCollection(principal, model)
    }
  }
}
