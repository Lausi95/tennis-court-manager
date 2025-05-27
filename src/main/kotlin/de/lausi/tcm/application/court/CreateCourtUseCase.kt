package de.lausi.tcm.application.court

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class CreateCourtCommand(
  val courtName: CourtName,
)

data class CreateCourtResult(
  val court: Court,
)

enum class CreateCourtError {
  COURT_NAME_ALREADY_EXISTS,
}

@UseCaseComponent
class CreateCourtUseCase(
  private val permissions: Permissions,
  private val courtRepository: CourtRepository,
) : UseCase<Nothing?, Nothing?, CreateCourtCommand, CreateCourtResult, CreateCourtError> {

  override fun checkContextPermission(userId: MemberId, contextParams: Nothing?): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun getContext(params: Nothing?): Either<Nothing?, CreateCourtError> {
    return Either.Success(null)
  }

  override fun checkCommandPermission(userId: MemberId, command: CreateCourtCommand): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: CreateCourtCommand): Either<CreateCourtResult, CreateCourtError> {
    val errors = mutableListOf<CreateCourtError>()

    if (courtRepository.existsByName(command.courtName)) {
      errors.add(CreateCourtError.COURT_NAME_ALREADY_EXISTS)
    }

    if (errors.isNotEmpty()) {
      return Either.Error(errors)
    }

    val court = courtRepository.save(Court(command.courtName))

    return Either.Success(
      CreateCourtResult(
        court
      )
    )
  }
}
