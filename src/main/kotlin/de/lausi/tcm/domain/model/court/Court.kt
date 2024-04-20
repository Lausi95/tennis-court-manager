package de.lausi.tcm.domain.model.court

import de.lausi.tcm.domain.model.reservation.Reservation

const val MIN_SLOT = 12
const val MAX_SLOT = 44

fun formatSlot(slot: Int): String {
  val minutes = if (slot % 2 == 0) "00" else "30"
  val hours = (slot / 2).toString()
  val filledHours = if (hours.length == 1) "0$hours" else hours

  return "$filledHours:$minutes"
}

enum class BlockType {
  FREE,
  FREE_PLAY,
  KIDS_TRAINING,
  ADULT_TRAINING,
  EVENT,
  ENCOUNTER,
}

data class Block(
  val type: BlockType,
  val fromSlot: Int,
  val toSlot: Int,
  val description: String,
) {
  val span = toSlot - fromSlot
  val fromTime = formatSlot(fromSlot)
  val toTime = formatSlot(toSlot + 1)
}

data class Court(
  val name: String,
  val blocks: List<Block>
) {

  companion object {
    fun build(name: String, reservations: List<Reservation>): Court {
      val blocks = mutableListOf<Block>()

      var currentSlot = MIN_SLOT
      while (currentSlot < MAX_SLOT) {
        val reservation = reservations.find { it.fromSlot == currentSlot }
        if (reservation != null) {
          blocks.add(Block(BlockType.FREE_PLAY, reservation.fromSlot, reservation.toSlot, reservation.players.joinToString(" & ")))
          currentSlot = reservation.toSlot + 1
          continue
        }

        blocks.add(Block(BlockType.FREE, currentSlot, currentSlot, "frei"))
        currentSlot += 1
      }

      return Court(name, blocks)
    }
  }
}
