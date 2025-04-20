package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*
import de.lausi.tcm.domain.model.member.MemberId
import de.lausi.tcm.domain.model.member.MemberRepository
import de.lausi.tcm.ger
import de.lausi.tcm.iso
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.DayOfWeek
import java.time.LocalDate

data class ReservationModel(
  val id: String,
  val date: String,
  val court: CourtModel,
  val fromTime: String,
  val toTime: String,
  val players: List<MemberModel>,
  val links: Map<String, String>,
)

data class ReservationCollection(
  val items: List<ReservationModel>,
  val links: Map<String, String>,
)

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

data class PostTrainingParams(
  val dayOfWeek: DayOfWeek,
  val courtId: String,
  val fromSlot: Int,
  val toSlot: Int,
  val description: String,
)

@Controller
@RequestMapping("/api/reservations")
class ReservationController(
  private val courtController: CourtController,
  private val slotController: SlotController,
  private val memberController: MemberController,
  private val reservationRepository: ReservationRepository,
  private val reservationService: ReservationService,
  private val occupancyPlanService: OccupancyPlanService,
  private val occupancyPlanController: OccupancyPlanController,
  private val courtRepository: CourtRepository,
  private val memberRepository: MemberRepository,
) {

  @GetMapping
  fun getReservations(model: Model, principal: Principal): String {
    model.addAttribute("userId", principal.name)

    val items = reservationRepository.findByCreatorIdAndDateGreaterThanEqual(principal.name, LocalDate.now()).sortedWith(compareBy(Reservation::date, Reservation::fromSlot)).map { reservation ->
      val court = with(courtController) {
        courtRepository.findById(reservation.courtId)?.toModel() ?: CourtModel.NOT_FOUND
      }

      val members = with(memberController) {
        reservation.playerIds.map { memberRepository.findById(MemberId(it.value))?.toModel() ?: MemberModel.NOT_FOUND }
      }

      ReservationModel(
        reservation.id.value,
        reservation.date.ger(),
        court,
        reservation.fromSlot.formatFromTime(),
        reservation.toSlot.formatToTime(),
        members,
        mapOf(
          "delete" to "/api/reservations/${reservation.id}"
        )
      )
    }

    val reservationCollection = ReservationCollection(items, mapOf())
    model.addAttribute("reservationCollection", reservationCollection)

    courtController.getCourts(model)
    slotController.getSlots(model)
    memberController.getMembers(model)

    model.addAttribute("minDate", LocalDate.now().iso())
    model.addAttribute("maxDate", LocalDate.now().plusDays(14).iso())

    return "views/reservations"
  }

  @PostMapping
  fun createReservation(model: Model, params: PostReservationParams, principal: Principal): String {
    val errors = mutableListOf<String>()

    val courtId = CourtId(params.courtId.split(",")[0])
    val creatorId = MemberId(params.memberId1)
    val playerIds = listOf(params.memberId1, params.memberId2, params.memberId3, params.memberId4).filter { it.isNotBlank() }.map { MemberId(it) }

    val reservation = Reservation(courtId, params.date, Slot(params.slotId), Slot(params.slotId + params.duration), creatorId, playerIds)

    // TODO: Move rules to a domain service and make them customizable
    val futureReservations = reservationRepository.findByCreatorIdAndDateGreaterThanEqual(params.memberId1, LocalDate.now())
    if (!reservation.isToday()) {
      errors.add(
        """Du hast bereits eine Buchung in der Kernzeit.
        Du kannst maximal 1 Buchung in der Kernzeit haben.
        Die Kernzeit ist das Wochenende und unter der Woche 17:00 - 20:00 Uhr.""".lineSequence().map { it.trim() }.joinToString(" "))
    }

    if (LocalDate.now().plusDays(14) <= params.date) {
      errors.add("Du kannst maximal 14 Tage in die Zukunft buchen")
    }

    with(reservationService) {
      val reservationBlock = reservation.toBlock()
      val occupancyPlan = occupancyPlanService.getOccupancyPlan(params.date, listOf(courtId))
      if (!occupancyPlan.canPlace(courtId, reservationBlock)) {
        errors.add("Der Platz ist zu dem Zeitraum schon belegt.")
      }
    }

    if (errors.isEmpty()) {
      reservationRepository.save(reservation)
      return occupancyPlanController.getOccupancyPlan(model, params.date)
    } else {
      model.addAttribute("errors", errors)
      return getReservations(model, principal)
    }
  }

  @ResponseBody
  @DeleteMapping("/{reservationId}")
  fun deleteReservation(@PathVariable(name = "reservationId") reservationIdValue: String) {
    val reservationId = ReservationId(reservationIdValue)
    reservationRepository.delete(reservationId)
  }
}
