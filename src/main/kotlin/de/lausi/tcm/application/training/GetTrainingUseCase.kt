package de.lausi.tcm.application.training

import de.lausi.tcm.Either
import de.lausi.tcm.application.ReadUseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class GetTrainingCommand(
  val trainingId: TrainingId,
)

data class GetTrainingResponse(
  val training: Training,
  val court: Court,
)

@UseCaseComponent
class GetTrainingUseCase(
  private val permissions: Permissions,
  private val trainingRepository: TrainingRepository,
  private val courtRepository: CourtRepository
) :
  ReadUseCase<GetTrainingCommand, GetTrainingResponse, String> {

  override fun checkPermission(
    userId: MemberId,
    command: GetTrainingCommand
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TRAINER)
  }

  override fun handle(command: GetTrainingCommand): Either<GetTrainingResponse, String> {
    val training = trainingRepository.findById(command.trainingId)
      ?: return Either.Error("Das angefragte Training konnte nicht gefunden werden.")

    val court = courtRepository.findById(training.courtId)
      ?: return Either.Error("Der Plats des Trainings konnte nicht gefunden werden.")

    return Either.Success(GetTrainingResponse(training, court))
  }
}
