package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class EventModel(
  val id: String,
  val date: String,
  val courts: List<CourtModel>,
  val fromTime: String,
  val toTime: String,
  val description: String,
  val links: Map<String, String> = mapOf(),
)

data class EventCollection(
  val items: List<EventModel>,
  val links: Map<String, String> = mapOf(),
)

data class CreateEventRequest(
  val date: LocalDate,
  val courtIds: List<String>,
  val fromSlotId: Int,
  val toSlotId: Int,
  val description: String,
)

@Controller
@RequestMapping("/api/events")
class EventController(
  private val eventRepository: EventRepository,
  private val courtRepository: CourtRepository,
  private val courtController: CourtController,
  private val slotController: SlotController,
  private val memberService: MemberService,
  private val eventService: EventService,
  private val occupancyPlanService: OccupancyPlanService,
) {

  @GetMapping
  fun getEvents(model: Model): String {
    val items = eventRepository.findByDateGreaterThanEqual(LocalDate.now()).map { event ->
      val courts = event.courtIds.map { courtId -> courtRepository.findById(courtId).map { CourtModel(it.id, it.name) }.orElseThrow() }
      EventModel(
        event.id,
        event.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
        courts,
        formatFromTime(event.fromSlot),
        formatToTime(event.toSlot),
        event.description,
        mapOf(
          "self" to "/api/events/${event.id}",
          "delete" to "/api/events/${event.id}"
        )
      )
    }

    model.addAttribute("eventCollection", EventCollection(items, mapOf(
      "self" to "/api/events",
      "create" to "/api/events",
    )))

    courtController.getCourts(model)
    slotController.getSlots(model)

    return "views/events"
  }

  @PostMapping
  fun createEvent(model: Model, principal: Principal, request: CreateEventRequest): String {
    memberService.getMember(principal.name).assertRoles(Group.EVENT_MANAGEMENT)

    val errors = mutableListOf<String>()

    if (!request.courtIds.all { courtRepository.existsById(it) }) {
      errors.add("Ein oder mehrere Plaetze existieren nicht")
    }

    if (request.courtIds.isEmpty()) {
      errors.add("Es muss mindestens ein Platz ausgewaehlt werden")
    }

    val event = Event(
      UUID.randomUUID().toString(),
      request.date,
      request.courtIds,
      request.fromSlotId,
      request.toSlotId,
      request.description
    )

    with(eventService) {
      val reservationBlock = event.toBlock()
      val occupancyPlan = occupancyPlanService.getOccupancyPlan(request.date, request.courtIds)
      request.courtIds.forEach { courtId ->
        if (!occupancyPlan.canPlace(courtId, reservationBlock)) {
          errors.add("Der Platz ist zu dem Zeitraum schon belegt.")
        }
      }
    }

    if (errors.isNotEmpty()) {
      model.addAttribute("errors", errors)
      return getEvents(model)
    }

    eventRepository.save(event)

    return getEvents(model)
  }

  @DeleteMapping("/{eventId}")
  fun deleteEvent(model: Model, principal: Principal, @PathVariable eventId: String): String {
    memberService.getMember(principal.name).assertRoles(Group.EVENT_MANAGEMENT)
    eventRepository.deleteById(eventId)
    return getEvents(model)
  }
}
