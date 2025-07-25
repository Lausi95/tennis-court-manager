package de.lausi.tcm.application.training

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class DeleteTrainingContextParams(
  val trainingId: TrainingId,
)

data class DeleteTrainingContext(
  val training: Training,
  val court: Court,
)

data class DeleteTrainingCommand(
  val trainingId: TrainingId,
)

@UseCaseComponent
class DeleteTrainingUseCase(private val permissions: Permissions, private val trainingRepository: TrainingRepository, private val courtRepository: CourtRepository) : UseCase<DeleteTrainingContextParams, DeleteTrainingContext, DeleteTrainingCommand, Nothing?, String> {

  override fun checkContextPermission(userId: MemberId, contextParams: DeleteTrainingContextParams): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TRAINER)
  }

  override fun getContext(params: DeleteTrainingContextParams): Either<DeleteTrainingContext, String> {
    val training = trainingRepository.findById(params.trainingId) ?: return Either.Error("No Training with ID ${params.trainingId}")
    val court = courtRepository.findById(training.courtId) ?: return Either.Error("No Court with ID ${training.courtId}")
    return Either.Success(DeleteTrainingContext(training, court))
  }

  override fun checkCommandPermission(userId: MemberId, command: DeleteTrainingCommand): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TRAINER)
  }

  override fun handle(command: DeleteTrainingCommand): Either<Nothing?, String> {
    val training = trainingRepository.findById(command.trainingId) ?: return Either.Error("No Training with ID ${command.trainingId}")
    trainingRepository.delete(training.id)
    return Either.Success()
  }
}