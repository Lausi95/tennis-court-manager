package de.lausi.tcm.application.training

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*
import java.time.DayOfWeek

data class CreateTrainingContext(
  val courts: List<Court>,
)

data class CreateTrainingCommand(
  val courtId: CourtId,
  val description: TrainingDescription,
  val dayOfWeek: DayOfWeek,
  val fromSlot: Slot,
  val toSlot: Slot,
)

data class CreateTrainingResponse(
  val trainingId: TrainingId,
)

@UseCaseComponent
class CreateTraingUseCase(
  private val permissions: Permissions,
  private val trainingRepository: TrainingRepository,
  private val courtRepository: CourtRepository,
) : UseCase<Nothing?, CreateTrainingContext, CreateTrainingCommand, CreateTrainingResponse, Nothing?> {
  override fun checkContextPermission(
    userId: MemberId,
    contextParams: Nothing?
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun getContext(params: Nothing?): Either<CreateTrainingContext, Nothing?> {
    val courts = courtRepository.findAll()
    return Either.Success(CreateTrainingContext(courts))
  }

  override fun checkCommandPermission(
    userId: MemberId,
    command: CreateTrainingCommand
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: CreateTrainingCommand): Either<CreateTrainingResponse, Nothing?> {
    val training = Training(
      command.dayOfWeek,
      command.courtId,
      command.fromSlot,
      command.toSlot,
      command.description,
      mutableSetOf(),
    )

    trainingRepository.save(training)

    return Either.Success(CreateTrainingResponse(training.id))
  }
}
