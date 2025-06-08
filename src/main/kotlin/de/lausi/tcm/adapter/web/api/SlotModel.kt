package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Slot
import org.springframework.ui.Model
import java.time.LocalDate

data class SlotModel(
  val id: Int,
  val time: String,
  val endTime: String,
  val core: Boolean,
  val links: Map<String, String> = mapOf(),
)

fun Slot.toModel(date: LocalDate): SlotModel = SlotModel(
  this.index,
  this.formatFromTime(),
  this.formatToTime(),
  this.isCore(date),
  mapOf(
    "self" to "/api/slots/$this"
  ),
)

fun Model.slotEntity(slot: Slot, date: LocalDate) {
  addAttribute("slot", slot.toModel(date))
}

data class SlotCollection(
  val items: List<SlotModel>,
  val links: Map<String, String> = mapOf(),
)

fun List<Slot>.toCollection(date: LocalDate): SlotCollection {
  return SlotCollection(
    map { it.toModel(date) },
    mapOf(
      "self" to "/api/slots"
    ),
  )
}

fun Model.slotCollection(slots: List<Slot>, date: LocalDate) {
  addAttribute("slotCollection", slots.toCollection(date))
}

