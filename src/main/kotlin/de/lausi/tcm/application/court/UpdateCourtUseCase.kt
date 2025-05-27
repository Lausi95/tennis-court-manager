package de.lausi.tcm.application.court

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class UpdateCourtContextParams(
  val courtId: CourtId,
)

data class UpdateCourtContext(
  val court: Court,
)

data class UpdateCourtCommand(
  val courtId: CourtId,
  val name: CourtName,
)

data class UpdateCourtResponse(
  val court: Court,
)

enum class UpdateCourtError {
  COURT_DOES_NOT_EXIST,
  COURT_NAME_ALREADY_EXISTS,
}

@UseCaseComponent
class UpdateCourtUseCase(
  private val permissions: Permissions,
  private val courtRepository: CourtRepository,
) : UseCase<UpdateCourtContextParams, UpdateCourtContext, UpdateCourtCommand, UpdateCourtResponse, UpdateCourtError> {

  override fun checkContextPermission(userId: MemberId, contextParams: UpdateCourtContextParams): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun getContext(params: UpdateCourtContextParams): Either<UpdateCourtContext, UpdateCourtError> {
    val court = courtRepository.findById(params.courtId)
      ?: return Either.Error(UpdateCourtError.COURT_DOES_NOT_EXIST)

    return Either.Success(UpdateCourtContext(court))
  }

  override fun checkCommandPermission(userId: MemberId, command: UpdateCourtCommand): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: UpdateCourtCommand): Either<UpdateCourtResponse, UpdateCourtError> {
    val court = courtRepository.findById(command.courtId)
      ?: return Either.Error(UpdateCourtError.COURT_DOES_NOT_EXIST)

    if (courtRepository.existsByNameAndIdNot(command.name, command.courtId)) {
      return Either.Error(UpdateCourtError.COURT_NAME_ALREADY_EXISTS)
    }

    court.name = command.name

    courtRepository.save(court)

    return Either.Success(
      UpdateCourtResponse(
        court,
      )
    )
  }
}