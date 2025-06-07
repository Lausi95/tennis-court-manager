package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Slot
import org.springframework.ui.Model

data class SlotModel(
  val id: Int,
  val time: String,
  val endTime: String,
  val core: Boolean,
  val links: Map<String, String> = mapOf(),
)

fun Slot.toModel(): SlotModel = SlotModel(
  this.index,
  this.formatFromTime(),
  this.formatToTime(),
  this.isCore(),
  mapOf(
    "self" to "/api/slots/$this"
  ),
)

fun Model.slotEntity(slot: Slot) {
  addAttribute("slot", slot.toModel())
}

data class SlotCollection(
  val items: List<SlotModel>,
  val links: Map<String, String> = mapOf(),
)

fun List<Slot>.toCollection(): SlotCollection {
  return SlotCollection(
    map { it.toModel() },
    mapOf(
      "self" to "/api/slots"
    ),
  )
}

fun Model.slotCollection(slots: List<Slot>) {
  addAttribute("slotCollection", slots.toCollection())
}

