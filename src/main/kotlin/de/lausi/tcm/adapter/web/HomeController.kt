package de.lausi.tcm.adapter.web

import de.lausi.tcm.IsoDate
import de.lausi.tcm.adapter.web.api.CourtController
import de.lausi.tcm.adapter.web.api.MemberController
import de.lausi.tcm.adapter.web.api.OccupancyPlanController
import de.lausi.tcm.adapter.web.api.SlotController
import de.lausi.tcm.domain.model.OccupancyPlanService
import de.lausi.tcm.domain.model.Reservation
import de.lausi.tcm.domain.model.ReservationRespository
import de.lausi.tcm.domain.model.ReservationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
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
private class HomeController(
  private val occupancyPlanController: OccupancyPlanController,
  private val courtController: CourtController,
  private val slotController: SlotController,
  private val memberController: MemberController,
  private val reservationRepository: ReservationRespository,
  private val occupancyPlanService: OccupancyPlanService,
  private val reservationService: ReservationService,
) {

  @GetMapping("/")
  fun getHome(model: Model, @RequestParam(name = "date") @IsoDate date: LocalDate?): String {
    model.addAttribute("currentPage", "home")

    occupancyPlanController.getOccupancyPlan(date ?: LocalDate.now(), model)

    return "home"
  }

  @GetMapping("/book")
  fun getBook(model: Model, principal: Principal): String {
    model.addAttribute("currentPage", "book")
    model.addAttribute("userId", principal.name)

    courtController.getCourts(model)
    slotController.getSlots(model)
    memberController.getMembers(model)

    return "book"
  }

  @PostMapping("/book")
  fun postBook(model: Model, params: PostReservationParams, principal: Principal): String {
    val errors = mutableListOf<String>()

    val courtId = params.courtId.split(",")[0]
    val reservation = Reservation(
      courtId,
      params.date,
      params.slotId,
      params.slotId + params.duration,
      params.memberId1,
      listOf(params.memberId1, params.memberId2, params.memberId3, params.memberId4).filter { it.isNotBlank() })

    // TODO: Move rules to a domain service and make them customizable
    val futureReservations = reservationRepository.findByCreatorIdAndDateGreaterThanEqual(params.memberId1, LocalDate.now())
    if (reservation.hasCoreTimeSlot() && futureReservations.any { it.hasCoreTimeSlot() }) {
      errors.add("Du hast bereits eine Buchung inder Kernzeit. Du kannst maximal 1 Buchung in der Kernzeit haben. Die Kernzeit ist das Wochenende und unter der Woche 17:00 - 20:00 Uhr")
    }

    if (LocalDate.now().plusDays(14) <= params.date) {
      errors.add("Du kannst maximal 14 Tage in die Zukunft buchen")
    }

    with (reservationService) {
      val reservationBlock = reservation.toBlock()
      val occupancyPlan = occupancyPlanService.getOccupancyPlan(params.date, listOf(courtId))
      if (!occupancyPlan.canPlace(courtId, reservationBlock)) {
        errors.add("Der Platz ist zu dem Zeitraum schon belegt.")
      }
    }

    if (errors.isEmpty()) {
      reservationRepository.save(reservation)
      return "redirect:/?date=${params.date}"
    } else {
      model.addAttribute("errors", errors)
      return getBook(model, principal)
    }
  }
}
