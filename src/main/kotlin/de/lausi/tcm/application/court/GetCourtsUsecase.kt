package de.lausi.tcm.application.court

import de.lausi.tcm.Either
import de.lausi.tcm.application.ReadUseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class GetCourtsResult(
  val courts: List<Court>,
)

@UseCaseComponent
class GetCourtsUsecase(
  private val courtRepository: CourtRepository,
  private val permissions: Permissions,
) : ReadUseCase<Nothing?, GetCourtsResult, Nothing?> {

  override fun checkPermission(userId: MemberId, command: Nothing?): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: Nothing?): Either<GetCourtsResult, Nothing?> {
    val courts = courtRepository.findAll()
    return Either.Success(GetCourtsResult(courts))
  }
}
