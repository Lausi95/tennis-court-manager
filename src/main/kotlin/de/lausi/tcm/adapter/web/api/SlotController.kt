package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.MAX_SLOT
import de.lausi.tcm.domain.model.MIN_SLOT
import de.lausi.tcm.domain.model.formatFromTime
import de.lausi.tcm.domain.model.isCoreTimeSlot
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

data class SlotModel(
  val id: Int,
  val time: String,
  val core: Boolean,
  val links: Map<String, String>,
)

data class SlotCollection(
  val items: List<SlotModel>,
  val count: Int,
  val links: Map<String, String>,
)

@Controller
@RequestMapping("/api/slots")
class SlotController {

  @GetMapping(headers = ["accept=application/json"])
  fun getSlotCollection(): SlotCollection {
    val items = (MIN_SLOT..MAX_SLOT).map { SlotModel(it, formatFromTime(it), isCoreTimeSlot(it), mapOf()) }
    return SlotCollection(items, items.size, mapOf())
  }

  @GetMapping
  fun getSlots(model: Model): String {
    model.addAttribute("slotCollection", getSlotCollection())
    return "views/slots"
  }
}
