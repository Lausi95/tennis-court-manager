package de.lausi.tcm.application

import de.lausi.tcm.domain.model.*
import de.lausi.tcm.domain.model.member.*
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate

data class CreateTrainingCommand(
  val dayOfWeek: DayOfWeek,
  val courtId: CourtId,
  val fromSlot: Int,
  val toSlot: Int,
  val description: String,
)

@Component
class TrainingUseCase(
  private val trainingRepository: TrainingRepository,
  private val permissions: Permissions,
) {

  fun getAllTrainings(): List<Training> {
    return trainingRepository.findAll()
      .sortedBy { it.fromSlot }
      .sortedBy { it.dayOfWeek }
  }

  fun getTraining(trainingId: String): Training {
    return trainingRepository.findById(trainingId).orElse(null) ?: error("Training $trainingId not found")
  }

  fun createTraining(userMemberId: MemberId, command: CreateTrainingCommand): Training {
    permissions.assertGroup(userMemberId, MemberGroup.TRAINER)

    val fromSlot = SlotRepository.findByIndex(command.fromSlot) ?: error("From Slot Does not exist")
    val toSlot = SlotRepository.findByIndex(command.toSlot) ?: error("To Slot Does not exist")

    val newTraining = Training(
      command.dayOfWeek,
      command.courtId,
      fromSlot,
      toSlot,
      command.description,
      mutableSetOf()
    )

    val alreadyExistingTrainings = trainingRepository.findByDayOfWeekAndCourtId(command.dayOfWeek, command.courtId)
    if (alreadyExistingTrainings.any { it.collidesWith(newTraining) }) {
      error("New Training collides with already existing trainings.")
    }

    trainingRepository.save(newTraining)
    return newTraining
  }

  fun deleteTraining(userMemberId: MemberId, trainingId: String) {
    permissions.assertGroup(userMemberId, MemberGroup.TRAINER)

    trainingRepository.deleteById(trainingId)
  }

  fun addSkippedDateToTraining(userMemberId: MemberId, trainingId: String, date: LocalDate): Training {
    permissions.assertGroup(userMemberId, MemberGroup.TRAINER)

    val training = trainingRepository.findById(trainingId).orElse(null) ?: error("Training $trainingId not found")
    training.addSkippedDate(date)
    trainingRepository.save(training)
    return training
  }

  fun removeSkippedDateFromTraining(userMemberId: MemberId, trainingId: String, date: LocalDate): Training {
    permissions.assertGroup(userMemberId, MemberGroup.TRAINER)

    val training = trainingRepository.findById(trainingId).orElse(null) ?: error("Training $trainingId not found")
    training.removeSkippedDate(date)
    trainingRepository.save(training)
    return training
  }
}
