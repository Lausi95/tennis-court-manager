package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Court
import de.lausi.tcm.domain.model.Event
import de.lausi.tcm.domain.model.EventId
import de.lausi.tcm.ger
import org.springframework.ui.Model

data class EventModel(
  val id: String,
  val date: String,
  val courts: CourtCollection,
  val fromTime: String,
  val toTime: String,
  val description: String,
  val links: Map<String, String> = mapOf(),
)

fun Event.toModel(courts: List<Court>) = EventModel(
  this.id.value,
  this.date.ger(),
  courts.toCollection(),
  this.fromSlot.formatFromTime(),
  this.toSlot.formatToTime(),
  this.description.value,
  mapOf(
    "delete" to "/events/${this.id.value}/delete",
  ),
)

fun Model.eventEntity(event: Event, courts: List<Court>) {
  addAttribute("event", event.toModel(courts))
}

data class EventCollection(
  val items: List<EventModel>,
  val links: Map<String, String> = mapOf(),
)

fun List<Event>.toCollection(courts: Map<EventId, List<Court>>) = EventCollection(
  this.map {
    it.toModel(
      courts[it.id] ?: error("Courts not found."),
    )
  },
  mapOf(
    // TODO
  ),
)

fun Model.eventCollection(events: List<Event>, courts: Map<EventId, List<Court>>) {
  addAttribute("eventCollection", events.toCollection(courts))
}
