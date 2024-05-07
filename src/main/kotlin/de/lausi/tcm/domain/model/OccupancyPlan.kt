package de.lausi.tcm.domain.model

import org.springframework.stereotype.Component
import java.time.LocalDate

enum class BlockType(val priority: Int) {
  FREE(0),
  FREE_PLAY(1),
  TRAINING(2),
  EVENT(3),
  MATCH(100),
}

data class Block(
  val type: BlockType,
  val fromSlot: Int,
  val toSlot: Int,
  val description: String,
) {

  fun contains(slot: Int): Boolean {
    return slot in fromSlot..toSlot
  }

  fun collidesWith(other: Block): Boolean {
    val otherSlots = (other.fromSlot..other.toSlot)
    return (fromSlot..toSlot).any { it in otherSlots }
  }
}

interface OccupancyPlanResolver {

  fun OccupancyPlan.addBlock(date: LocalDate, courtIds: List<String>)
}

class OccupancyPlan(courtIds: List<String>, private val minSlot: Int, private val maxSlot: Int) {

  private val blocksByCourt: MutableMap<String, MutableSet<Block>> = mutableMapOf()

  init {
    courtIds.forEach { blocksByCourt[it] = mutableSetOf() }
  }

  fun addBlock(courtId: String, block: Block){
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

  fun canPlace(courtId: String, block: Block): Boolean {
    blocksByCourt[courtId]?.let { blocks ->
      val collidingBlocks = blocks.filter { it.collidesWith(block) }
      if (collidingBlocks.isEmpty() || collidingBlocks.all { it.type.priority < block.type.priority }) {
        return true
      }
    }
    return false
  }

  fun render(courtId: String): List<Block> {
    val result = mutableListOf<Block>()
    val blocks = blocksByCourt[courtId] ?: error("$courtId not found")

    for (slot in minSlot..maxSlot) {
      val block = blocks.find { it.contains(slot) }
      if (block == null) {
        result.add(Block(BlockType.FREE, slot, slot, "frei"))
        continue
      }
      if (block.fromSlot == slot) {
        result.add(block)
      }
    }

    return result
  }

  fun addBlock(date: LocalDate, courtIds: List<String>, occupancyPlanResolver: OccupancyPlanResolver) {
    with (occupancyPlanResolver) {
      addBlock(date, courtIds)
    }
  }
}

@Component
class OccupancyPlanService(private val occupancyPlanResolvers: List<OccupancyPlanResolver>) {

  fun getOccupancyPlan(date: LocalDate, courtIds: List<String>): OccupancyPlan {
    val occupancyPlan = OccupancyPlan(courtIds, MIN_SLOT, MAX_SLOT)
    occupancyPlanResolvers.forEach { occupancyPlan.addBlock(date, courtIds, it) }

    return occupancyPlan
  }
}
