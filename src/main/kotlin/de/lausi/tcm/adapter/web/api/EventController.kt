package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.PageAssembler
import de.lausi.tcm.adapter.web.userId
import de.lausi.tcm.application.NOTHING
import de.lausi.tcm.application.event.*
import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal
import java.time.LocalDate

data class CreateEventRequest(
  val date: LocalDate,
  val courtIds: List<String>,
  val fromSlotId: Int,
  val toSlotId: Int,
  val description: String,
)

@Controller
@RequestMapping("/events")
class EventController(
  private val pageAssembler: PageAssembler,
  private val getEventsUseCase: GetEventsUseCase,
  private val createEventUseCase: CreateEventUseCase,
  private val deleteEventUseCase: DeleteEventUseCase,
) {

  @GetMapping
  fun getView(principal: Principal, model: Model): String {
    return with(pageAssembler) {
      model.preparePage("Events", principal) {
        getEventCollection(principal, model)
      }
    }
  }

  @GetMapping("/collection")
  fun getEventCollection(principal: Principal, model: Model): String {
    val command = GetEventsCommand(null)

    return runContext(getEventsUseCase.execute(principal.userId(), command), model) {
      model.eventCollection(it.events, it.courts)
      "views/events/collection"
    }
  }

  @GetMapping("/create")
  fun getCreateEvent(principal: Principal, model: Model): String {
    return runContext(createEventUseCase.context(principal.userId(), NOTHING), model) {
      model.slotCollection(SlotRepository.findAll(), LocalDate.now())
      model.courtCollection(it.courts)
      "views/events/create"
    }
  }

  @PostMapping("/create")
  fun createEvent(principal: Principal, model: Model, request: CreateEventRequest): String {
    val command = CreateEventCommand(
      request.date,
      request.courtIds.map { CourtId(it) },
      Slot(request.fromSlotId),
      Slot(request.toSlotId),
      EventDescription(request.description)
    )

    return runUseCase(
      createEventUseCase.execute(principal.userId(), command),
      model,
      { getCreateEvent(principal, model) }) {
      getEventCollection(principal, model)
    }
  }

  @GetMapping("/{eventId}/delete")
  fun getDeleteEvent(principal: Principal, model: Model, @PathVariable eventId: String): String {
    val params = DeleteEventContextParams(
      EventId(eventId),
    )

    return runContext(deleteEventUseCase.context(principal.userId(), params), model) {
      model.eventEntity(it.event, it.courts)
      "views/events/delete"
    }
  }

  @PostMapping("/{eventId}/delete")
  fun deleteEvent(principal: Principal, model: Model, @PathVariable eventId: String): String {
    val command = DeleteEventCommand(
      EventId(eventId),
    )

    return runUseCase(
      deleteEventUseCase.execute(principal.userId(), command),
      model,
      { getDeleteEvent(principal, model, eventId) }) {
      getEventCollection(principal, model)
    }
  }
}
