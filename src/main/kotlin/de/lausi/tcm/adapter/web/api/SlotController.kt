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
    return model.slotCollection(SlotRepository.findAll())
  }

  @GetMapping("/{slotId}")
  fun getSlot(model: Model, @PathVariable(name = "slotId") slotIndex: Int): String {
    val slot = SlotRepository.findByIndex(slotIndex) ?: error("Slot does not exist")
    return model.slot(slot)
  }

  fun Slot.toModel(): SlotModel {
    return SlotModel(
      this.index,
      this.formatFromTime(),
      this.formatToTime(),
      false,
      mapOf(
        "self" to "/api/slots/$this"
      ),
    )
  }

  fun Model.slot(slot: Slot): String {
    addAttribute("slot", slot.toModel())
    return "entity/slot"
  }

  fun Model.slotCollection(slots: List<Slot>): String {
    val items = slots.map { it.toModel() }
    addAttribute("slotCollection", SlotCollection(items, mapOf(
      "self" to "/api/slots"
    )))
    return "collection/slot"
  }
}
