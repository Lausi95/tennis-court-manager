package de.lausi.tcm.application

import de.lausi.tcm.domain.model.*
import de.lausi.tcm.domain.model.member.MemberGroup
import de.lausi.tcm.domain.model.member.MemberId
import de.lausi.tcm.domain.model.member.MemberService
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

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
  private val courtRepository: CourtRepository,
  private val memberService: MemberService,
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
    memberService.assertGroup(userMemberId, MemberGroup.TRAINER)
    val newTraining = Training(
      UUID.randomUUID().toString(),
      command.dayOfWeek,
      command.courtId,
      command.fromSlot,
      command.toSlot,
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
    memberService.assertGroup(userMemberId, MemberGroup.TRAINER)
    trainingRepository.deleteById(trainingId)
  }

  fun addSkippedDateToTraining(userMemberId: MemberId, trainingId: String, date: LocalDate): Training {
    memberService.assertGroup(userMemberId, MemberGroup.TRAINER)
    val training = trainingRepository.findById(trainingId).orElse(null) ?: error("Training $trainingId not found")
    training.addSkippedDate(date)
    trainingRepository.save(training)
    return training
  }

  fun removeSkippedDateFromTraining(userMemberId: MemberId, trainingId: String, date: LocalDate): Training {
    memberService.assertGroup(userMemberId, MemberGroup.TRAINER)
    val training = trainingRepository.findById(trainingId).orElse(null) ?: error("Training $trainingId not found")
    training.removeSkippedDate(date)
    trainingRepository.save(training)
    return training
  }
}
