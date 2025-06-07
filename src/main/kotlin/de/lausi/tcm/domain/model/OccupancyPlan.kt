package de.lausi.tcm.domain.model

import org.springframework.stereotype.Component
import java.time.LocalDate

enum class BlockType(val priority: Int) {
  FREE(0),
  FREE_PLAY(1),
  BALLMACHINE(1),
  TRAINING(2),
  EVENT(3),
  MATCH(100),
}

data class Block(
  val type: BlockType,
  val fromSlot: Slot,
  val toSlot: Slot,
  val description: String,
) {

  fun contains(slot: Slot): Boolean {
    return slot.isInBoundariesOfSlots(fromSlot, toSlot)
  }

  fun collidesWith(other: Block): Boolean {
    val otherRange = other.indices()
    return indices().any { it in otherRange }
  }

  private fun indices(): IntRange = (this.fromSlot.index..this.toSlot.index)
}

fun interface OccupancyPlanResolver {

  fun OccupancyPlan.addBlock(date: LocalDate, courtIds: List<CourtId>)
}

class OccupancyPlan(courtIds: List<CourtId>) {

  val blocksByCourt: MutableMap<CourtId, MutableSet<Block>> = mutableMapOf()

  init {
    courtIds.forEach { blocksByCourt[it] = mutableSetOf() }
  }

  fun addBlock(courtId: CourtId, block: Block) {
    blocksByCourt[courtId]?.let { blocks ->
      val collidingBlocks = blocks.filter { it.collidesWith(block) }
      if (collidingBlocks.all { it.type.priority < block.type.priority }) {
        collidingBlocks.forEach { blocks.remove(it) }
        blocks.add(block)
      }
      if (collidingBlocks.isEmpty()) {
        blocks.add(block)
      }
    }
  }

  fun canPlace(courtId: CourtId, block: Block): Boolean {
    blocksByCourt[courtId]?.let { blocks ->
      val collidingBlocks = blocks.filter { it.collidesWith(block) }
      if (collidingBlocks.isEmpty() || collidingBlocks.all { it.type.priority < block.type.priority }) {
        return true
      }
    }
    return false
  }

  fun render(courtId: CourtId): List<Block> {
    val result = mutableListOf<Block>()
    val blocks = (blocksByCourt[courtId] ?: error("$courtId not found")).toList()

    SlotRepository.findAll().forEach { slot ->
      val block = blocks.find { it.contains(slot) }
      if (block == null) {
        result.add(Block(BlockType.FREE, slot, slot, "frei"))
        return@forEach
      }
      if (block.fromSlot == slot) {
        result.add(block)
      }
    }

    return result
  }

  fun addBlock(date: LocalDate, courtIds: List<CourtId>, occupancyPlanResolver: OccupancyPlanResolver) {
    with(occupancyPlanResolver) {
      addBlock(date, courtIds)
    }
  }
}

@Component
class OccupancyPlanService(private val occupancyPlanResolvers: List<OccupancyPlanResolver>) {

  fun getOccupancyPlan(date: LocalDate, courtIds: List<CourtId>): OccupancyPlan {
    val occupancyPlan = OccupancyPlan(courtIds)
    occupancyPlanResolvers.forEach { occupancyPlan.addBlock(date, courtIds, it) }
    return occupancyPlan
  }
}
