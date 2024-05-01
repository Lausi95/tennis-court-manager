package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
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

  @GetMapping(headers = ["accept=application/json"])
  fun getSlots(model: Model): String {
    val items = (MIN_SLOT..MAX_SLOT).map { SlotModel(it, formatFromTime(it), formatToTime(it), isCoreTimeSlot(it), mapOf()) }
    val slotCollection = SlotCollection(items)

    model.addAttribute("slotCollection", slotCollection)

    return "views/slots"
  }
}
