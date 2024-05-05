package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

data class SlotModel(
  val id: Int,
  val time: String,
  val endTime: String,
  val core: Boolean,
  val links: Map<String, String> = mapOf(),
)

data class SlotCollection(
  val items: List<SlotModel>,
  val links: Map<String, String> = mapOf(),
)

@Controller
@RequestMapping("/api/slots")
class SlotController {

  @GetMapping
  fun getSlots(model: Model): String {
    val slots = (MIN_SLOT..MAX_SLOT)
    return model.slotCollection(slots.toList())
  }

  @GetMapping("/{slotId}")
  fun getSlot(model: Model, @PathVariable slotId: Int): String {
    return model.slot(slotId)
  }

  fun Int.toModel(): SlotModel {
    return SlotModel(
      this,
      formatFromTime(this),
      formatToTime(this),
      isCoreTimeSlot(this),
      mapOf(
        "self" to "/api/slots/$this"
      )
    )
  }

  fun Model.slot(slot: Int): String {
    addAttribute("slot", slot.toModel())
    return "entity/slot"
  }

  fun Model.slotCollection(slots: List<Int>): String {
    val items = slots.map { it.toModel() }
    addAttribute("slotCollection", SlotCollection(items, mapOf(
      "self" to "/api/slots"
    )))
    return "collection/slot"
  }
}
