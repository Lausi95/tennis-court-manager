package de.lausi.tcm.domain.model

import org.springframework.stereotype.Component

const val MIN_SLOT = 14
const val MAX_SLOT = 43

data class SlotId(val value: Int)

data class Slot(
        val id: SlotId,
)

@Component
class SlotRepository() {

  private val slots: Map<SlotId, Slot>

  init {
    val slots = mutableMapOf<SlotId, Slot>()
    (MIN_SLOT..MAX_SLOT).map { SlotId(it) }.forEach { slots[it] = Slot(it) }

    this.slots = slots
  }

  fun exists(slotId: SlotId): Boolean = slots.containsKey(slotId)

  fun findAll(): List<Slot> = slots.values.sortedBy { it.id.value }.toList()
}

fun formatFromTimeIso(slot: Int): String {
  val hours = (slot / 2).toString().padStart(2, '0')
  val minutes = if (slot % 2 == 0) "00" else "30"
  return "$hours:$minutes:00"
}

fun formatToTimeIso(slot: Int): String {
  val hours = ((slot + 1) / 2).toString().padStart(2, '0')
  val minutes = if ((slot + 1) % 2 == 0) "00" else "30"
  return "$hours:$minutes:00"
}

fun formatFromTime(slot: Int): String {
  val hours = (slot / 2).toString()
  val minutes = if (slot % 2 == 0) "00" else "30"
  return "${hours.padStart(2, '0')}:$minutes"
}

fun formatToTime(slot: Int): String {
  val hours = ((slot + 1) / 2).toString()
  val minutes = if ((slot + 1) % 2 == 0) "00" else "30"
  return "${hours.padStart(2, '0')}:$minutes"
}

fun formatDuration(fromSlot: Int, toSlot: Int): String {
  val hours = ((toSlot - fromSlot) / 2).toString()
  val minutes = if ((toSlot - fromSlot) % 2 == 0) "00" else "30"
  return "${hours.padStart(2, '0')}:$minutes"
}

fun slotAmount(fromSlot: Int, toSlot: Int) = toSlot - fromSlot + 1

fun isCoreTimeSlot(slot: Int): Boolean {
  return slot >= 17 * 2 && slot < 21 * 2
}
