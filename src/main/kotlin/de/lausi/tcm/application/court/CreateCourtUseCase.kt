package de.lausi.tcm.application.court

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Component

data class CreateCourtCommand(
  val courtName: CourtName,
)

data class CreateCourtResult(
  val court: Court,
)

enum class CreateCourtError {
  COURT_NAME_ALREADY_EXISTS,
}

@Component
class CreateCourtUseCase(
  private val permissions: Permissions,
  private val courtRepository: CourtRepository,
) : UseCase<CreateCourtCommand, CreateCourtResult, CreateCourtError> {

  override fun checkPermission(userId: MemberId) {
    permissions.assertGroup(userId, MemberGroup.ADMIN)
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

    return Either.Success(CreateCourtResult(
      court
    ))
  }
}
