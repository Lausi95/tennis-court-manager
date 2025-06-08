package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.SlotRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDate

@Controller
@RequestMapping("/api/slots")
class SlotController {

  @GetMapping
  fun getSlots(model: Model): String {
    val slots = SlotRepository.findAll()
    model.slotCollection(slots, LocalDate.now())
    return "/views/slot/collection"
  }

  @GetMapping("/{slotId}")
  fun getSlot(model: Model, @PathVariable(name = "slotId") slotIndex: Int): String {
    val slot = SlotRepository.findByIndex(slotIndex) ?: error("Slot does not exist")
    model.slotEntity(slot, LocalDate.now())
    return "/views/slot/entity"
  }
}
