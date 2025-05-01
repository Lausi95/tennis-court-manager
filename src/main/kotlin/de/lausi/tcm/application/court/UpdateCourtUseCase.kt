package de.lausi.tcm.application.court

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Component

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

@Component
class UpdateCourtUseCase(
  private val permissions: Permissions,
  private val courtRepository: CourtRepository,
) : UseCase<UpdateCourtCommand, UpdateCourtResponse, UpdateCourtError> {

  override fun checkPermission(userId: MemberId) {
    permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: UpdateCourtCommand): Either<UpdateCourtResponse, UpdateCourtError> {
    val errors = mutableListOf<UpdateCourtError>()

    val courtOrNull = courtRepository.findById(command.courtId)
    if (courtOrNull == null) {
      errors.add(UpdateCourtError.COURT_DOES_NOT_EXIST)
    }
    val court = courtOrNull!!

    if (courtRepository.existsByNameAndIdNot(command.name, command.courtId)) {
      errors.add(UpdateCourtError.COURT_NAME_ALREADY_EXISTS)
    }

    if (errors.isNotEmpty()) {
      return Either.Error(errors)
    }

    court.name = command.name

    courtRepository.save(court)

    return Either.Success(UpdateCourtResponse(
      court,
    ))
  }
}