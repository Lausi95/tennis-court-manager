package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ReservationModel(
  val id: String,
  val date: String,
  val court: CourtModel,
  val fromTime: String,
  val toTime: String,
  val players: List<MemberModel>,
  val links: Map<String, String> = mapOf(),
)

data class ReservationCollection(
  val items: List<ReservationModel>,
  val links: Map<String, String> = mapOf(),
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
  private val reservationRepository: ReservationRespository,
  private val reservationService: ReservationService,
  private val occupancyPlanService: OccupancyPlanService,
  private val occupancyPlanController: OccupancyPlanController,
  private val reservationRespository: ReservationRespository,
  private val courtRepository: CourtRepository,
  private val memberRepository: MemberRepository,
) {

  @GetMapping
  fun getReservations(model: Model, principal: Principal): String {
    model.addAttribute("userId", principal.name)

    val items = reservationRespository.findByCreatorIdAndDateGreaterThanEqual(principal.getName(), LocalDate.now()).map { reservation ->
      val court = courtRepository.findById(reservation.courtId).map { CourtModel(it.id, it.name) }.orElseGet { CourtModel("", "???") }
      val members = reservation.memberIds
        .map { memberId -> memberRepository.findById(memberId).map { MemberModel(it.id, it.firstname, it.lastname, it.groups.map { it.toString() }) }.orElseGet { MemberModel("", "", "???", emptyList()) } }

      ReservationModel(reservation.id,
        reservation.date.format(DateTimeFormatter.ISO_DATE),
        court,
        formatFromTime(reservation.fromSlot),
        formatToTime(reservation.toSlot),
        members,
      )
    }

    val reservationCollection = ReservationCollection(items)
    model.addAttribute("reservationCollection", reservationCollection)

    courtController.getCourts(model)
    slotController.getSlots(model)
    memberController.getMembers(model)

    return "views/reservations"
  }

  @PostMapping
  fun createReservation(model: Model, params: PostReservationParams, principal: Principal): String {
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

    if (reservation.hasCoreTimeSlot() && reservation.memberIds.size < 2) {
      errors.add("Du musst mit mindestens einer anderen Person in der Kernzeit spielen. (Kein Aufschlagtraining in der Kernzeit)")
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
}
