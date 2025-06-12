package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.PageAssembler
import de.lausi.tcm.adapter.web.userId
import de.lausi.tcm.application.ballmachine.*
import de.lausi.tcm.domain.model.BallmachineBookingId
import de.lausi.tcm.domain.model.CourtId
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal
import java.time.LocalDate

data class CreateBallmachineBookingRequest(
  val courtId: String,
  val date: LocalDate,
  val slotId: Int,
)

@Controller
@RequestMapping("/ballmachine-bookings")
private class BallmachineBookingController(
  private val pageAssembler: PageAssembler,
  private val getBallmachineBookingsUseCase: GetBallmachineBookingsUseCase,
  private val createBallmachineBookingUseCase: CreateBallmachineBookingUseCase,
  private val cancelBallmachineBookingUseCase: CancelBallmachineBookingUseCase,
) {

  @GetMapping
  fun getView(principal: Principal, model: Model): String {
    return with(pageAssembler) {
      model.preparePage("BallmachineBookings", principal) {
        getBallmachineBookingCollection(principal, model)
      }
    }
  }

  @GetMapping("/collection")
  fun getBallmachineBookingCollection(principal: Principal, model: Model): String {
    return runContext(
      getBallmachineBookingsUseCase.execute(
        principal.userId(),
        GetBallmachinBookingsCommand(principal.userId(), LocalDate.now())
      ), model
    ) {
      model.ballmachineBookingCollection(it.ballmachineBookings, it.courts, it.members)
      "views/ballmachineBookings/collection"
    }
  }

  @GetMapping("/create")
  fun getCreateBallmachineBooking(principal: Principal, model: Model): String {
    return runContext(createBallmachineBookingUseCase.context(principal.userId(), null), model) {
      model.courtCollection(it.courts)
      model.slotCollection(it.slots, LocalDate.now())
      "views/ballmachineBookings/create"
    }
  }

  @PostMapping("/create")
  fun createBallmachineBooking(principal: Principal, model: Model, request: CreateBallmachineBookingRequest): String {
    val command = CreateBallmachineBookingCommand(
      principal.userId(),
      CourtId(request.courtId),
      request.date,
      request.slotId,
    )

    return runUseCase(
      createBallmachineBookingUseCase.execute(principal.userId(), command),
      model,
      { getCreateBallmachineBooking(principal, model) }) {
      getBallmachineBookingCollection(principal, model)
    }
  }

  @GetMapping("/{ballmachineBookingId}/delete")
  fun getDeleteBallmachineBooking(
    principal: Principal,
    model: Model,
    @PathVariable ballmachineBookingId: String
  ): String {
    val params = CancelBallmachineBookingContextParams(
      BallmachineBookingId(ballmachineBookingId),
    )
    return runContext(cancelBallmachineBookingUseCase.context(principal.userId(), params), model) {
      model.ballmachineBookingEntity(it.ballmachineBooking, it.court, it.member)
      "views/ballmachineBookings/delete"
    }
  }

  @PostMapping("/{ballmachineBookingId}/delete")
  fun deleteBallmachineBooking(
    principal: Principal,
    model: Model,
    @PathVariable ballmachineBookingId: String
  ): String {
    val command = CancelBallmachineBookingCommand(
      BallmachineBookingId(ballmachineBookingId),
    )

    return runUseCase(
      cancelBallmachineBookingUseCase.execute(principal.userId(), command),
      model,
      { getDeleteBallmachineBooking(principal, model, ballmachineBookingId) }) {
      getBallmachineBookingCollection(principal, model)
    }
  }
}