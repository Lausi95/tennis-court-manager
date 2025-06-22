package de.lausi.tcm.application.training

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*
import java.time.LocalDate

data class AddSkippedDateContextParams(
  val trainingId: TrainingId,
)

data class AddSkippedDateContext(
  val training: Training,
)

data class AddSkippedDateCommand(
  val trainingId: TrainingId,
  val skippedDate: LocalDate,
)

@UseCaseComponent
class AddSkippedDateUseCase(private val permissions: Permissions, private val trainingRepository: TrainingRepository) :
  UseCase<
          AddSkippedDateContextParams,
          AddSkippedDateContext,
          AddSkippedDateCommand,
          Nothing?,
          String> {

  override fun checkContextPermission(
    userId: MemberId,
    contextParams: AddSkippedDateContextParams
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TRAINER)
  }

  override fun getContext(params: AddSkippedDateContextParams): Either<AddSkippedDateContext, String> {
    val training = trainingRepository.findById(params.trainingId)
      ?: return Either.Error("Das Training konnte nicht gefunden werden.")

    return Either.Success(AddSkippedDateContext(training))
  }

  override fun checkCommandPermission(
    userId: MemberId,
    command: AddSkippedDateCommand
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.TRAINER)
  }

  override fun handle(command: AddSkippedDateCommand): Either<Nothing?, String> {
    val training = trainingRepository.findById(command.trainingId)
      ?: return Either.Error("Das Training konnte nicht gefunden werden.")

    if (training.dayOfWeek != command.skippedDate.dayOfWeek) {
      return Either.Error("Der angegebene Tag ist nicht am selben Wochentag, wie das Training. Bist du sicher, dass du diesen Tag meinst?")
    }

    if (training.skippedDates.contains(command.skippedDate)) {
      return Either.Error("Der angegebene Tag ist bereits in den Ausnahmen enthalten.")
    }

    training.skippedDates.add(command.skippedDate)

    trainingRepository.save(training)

    return Either.Success()
  }
}
