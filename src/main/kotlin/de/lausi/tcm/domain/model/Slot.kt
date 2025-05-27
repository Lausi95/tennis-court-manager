package de.lausi.tcm.domain.model

const val MIN_SLOT = 14
const val MAX_SLOT = 43

data class Slot(
  val index: Int
) : Comparable<Slot> {

  override fun compareTo(other: Slot): Int {
    return index.compareTo(other.index)
  }

  fun isInBoundariesOfSlots(fromSlot: Slot, toSlot: Slot): Boolean {
    return index in fromSlot.index..toSlot.index
  }

  fun formatFromTimeIso(): String {
    val hours = (index / 2).toString().padStart(2, '0')
    val minutes = if (index % 2 == 0) "00" else "30"
    return "$hours:$minutes:00"
  }

  fun formatToTimeIso(): String {
    val hours = ((index + 1) / 2).toString().padStart(2, '0')
    val minutes = if ((index + 1) % 2 == 0) "00" else "30"
    return "$hours:$minutes:00"
  }

  fun formatFromTime(): String {
    val hours = (index / 2).toString()
    val minutes = if (index % 2 == 0) "00" else "30"
    return "${hours.padStart(2, '0')}:$minutes"
  }

  fun formatToTime(): String {
    val hours = ((index + 1) / 2).toString()
    val minutes = if ((index + 1) % 2 == 0) "00" else "30"
    return "${hours.padStart(2, '0')}:$minutes"
  }

  companion object {
    fun formatDuration(fromSlot: Slot, toSlot: Slot): String {
      val hours = ((toSlot.index - fromSlot.index) / 2).toString()
      val minutes = if ((toSlot.index - fromSlot.index) % 2 == 0) "00" else "30"
      return "${hours.padStart(2, '0')}:$minutes"
    }

    fun distance(fromSlot: Slot, toSlot: Slot): Int {
      return toSlot.index - fromSlot.index + 1
    }
  }

  fun match(): Slot = Slot(index + 10)

  fun plus(duration: Int): Slot {
    return Slot(index + duration - 1)
  }
}

class SlotRepository {
  companion object {
    private val slots: Map<Int, Slot>

    init {
      val slots = mutableMapOf<Int, Slot>()

      (MIN_SLOT..MAX_SLOT).forEach { slots[it] = Slot(it) }

      this.slots = slots
    }

    fun findAll(): List<Slot> = slots.values.sortedBy { it.index }.toList()

    fun findByIndex(index: Int): Slot? = slots[index]
  }
}
