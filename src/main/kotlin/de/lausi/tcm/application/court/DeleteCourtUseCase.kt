package de.lausi.tcm.application.court

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.domain.model.*
import org.springframework.stereotype.Component

data class DeleteCourtContextParams(
  val courtId: CourtId,
)

data class DeleteCourtContext(
  val court: Court,
)

data class DeleteCourtCommand(
  val courtId: CourtId,
)

@Component
class DeleteCourtUseCase(
  val permissions: Permissions,
  val courtRepository: CourtRepository,
) : UseCase<DeleteCourtContextParams, DeleteCourtContext, DeleteCourtCommand, Nothing?, Nothing?> {

  override fun checkContextPermission(
    userId: MemberId,
    contextParams: DeleteCourtContextParams
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun getContext(params: DeleteCourtContextParams): Either<DeleteCourtContext, Nothing?> {
    val court = courtRepository.findById(params.courtId)
      ?: return Either.Error()

    return Either.Success(
      DeleteCourtContext(court)
    )
  }

  override fun checkCommandPermission(
    userId: MemberId,
    command: DeleteCourtCommand
  ): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: DeleteCourtCommand): Either<Nothing?, Nothing?> {
    val court = courtRepository.findById(command.courtId)
      ?: return Either.Error()

    courtRepository.delete(court.id)

    return Either.Success()
  }
}