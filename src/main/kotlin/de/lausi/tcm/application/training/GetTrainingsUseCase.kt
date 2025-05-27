package de.lausi.tcm.application.training

import de.lausi.tcm.Either
import de.lausi.tcm.application.ReadUseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class GetTrainingsResponse(
  val trainings: List<Training>,
  val courtsByTraining: Map<TrainingId, Court>,
  val courts: List<Court>,
)

@UseCaseComponent
class GetTrainingsUseCase(
  private val permissions: Permissions,
  private val trainingsRepository: TrainingRepository,
  private val courtRepository: CourtRepository,
) : ReadUseCase<Nothing?, GetTrainingsResponse, Nothing?> {

  override fun checkPermission(userId: MemberId, command: Nothing?): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: Nothing?): Either<GetTrainingsResponse, Nothing?> {
    val trainings = trainingsRepository.findAll()
    val courtsByTraining = mutableMapOf<TrainingId, Court>()
    trainings.forEach {
      val court = courtRepository.findById(it.courtId) ?: return Either.Error()
      courtsByTraining[it.id] = court
    }
    val courts = courtRepository.findAll();

    return Either.Success(GetTrainingsResponse(trainings, courtsByTraining, courts))
  }
}