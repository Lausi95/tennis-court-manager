package de.lausi.tcm.application.training

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*
import java.time.LocalDate

data class RemoveSkippedDateContextParams(
  val trainingId: TrainingId,
  val skippedDateToRemove: LocalDate,
)

data class RemoveSkippedDateContext(
  val training: Training,
  val court: Court,
  val skippedDateToRemove: LocalDate,
)

data class RemoveSkippedDateCommand(
  val trainingId: TrainingId,
  val skippedDateToRemove: LocalDate,
)

@UseCaseComponent
class RemoveSkippedDateUseCase(private val permissions: Permissions, private val trainingRepository: TrainingRepository, private val courtRepository: CourtRepository) : UseCase<RemoveSkippedDateContextParams, RemoveSkippedDateContext, RemoveSkippedDateCommand, Nothing?, String> {

  override fun checkContextPermission(userId: MemberId, contextParams: RemoveSkippedDateContextParams): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TRAINER)
  }

  override fun getContext(params: RemoveSkippedDateContextParams): Either<RemoveSkippedDateContext, String> {
    val training = trainingRepository.findById(params.trainingId)
      ?: return Either.Error("Das Training konnte nicht ermittelt werden.")

    val court = courtRepository.findById(training.courtId)
      ?: return Either.Error("Der Platz des Trainings konnte nicht ermittelt werden.")

    return Either.Success(
      RemoveSkippedDateContext(
        training,
        court,
        params.skippedDateToRemove,
      )
    )
  }

  override fun checkCommandPermission(userId: MemberId, command: RemoveSkippedDateCommand): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TRAINER)
  }

  override fun handle(command: RemoveSkippedDateCommand): Either<Nothing?, String> {
    val training = trainingRepository.findById(command.trainingId)
      ?: return Either.Error("Das Training konnte nicht ermittelt werden.")

    if (!training.skippedDates.contains(command.skippedDateToRemove)) {
      return Either.Error("Das Datum kann nicht entfernt werden, da es nicht in den Ausnahmen steht.")
    }

    training.skippedDates.remove(command.skippedDateToRemove)
    trainingRepository.save(training)

    return Either.Success()
  }
}
