package de.lausi.tcm.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Document("unique-training")
data class UniqueTraining(
  @Id val id: String,
  val date: LocalDate,
  val courtId: String,
  val fromSlot: Int,
  val toSlot: Int,
  val description: String,
)

interface UniqueTrainingRepository: MongoRepository<UniqueTraining, String> {

  fun findByDateGreaterThanEqual(date: LocalDate): List<UniqueTraining>

  fun findByCourtIdAndDate(courtId: String, date: LocalDate): List<UniqueTraining>
}

@Component class UniqueTrainingService(private val uniqueTrainingRepository: UniqueTrainingRepository): OccupancyPlanResolver {
  override fun OccupancyPlan.addBlock(date: LocalDate, courtIds: List<String>) {
    courtIds.forEach { courtId ->
      uniqueTrainingRepository.findByCourtIdAndDate(courtId, date).forEach {
        addBlock(courtId, it.toBlock())
      }
    }
  }

  fun UniqueTraining.toBlock() : Block {
    return Block(BlockType.TRAINING, fromSlot, toSlot, description)
  }
}
