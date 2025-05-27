package de.lausi.tcm.application.member

import de.lausi.tcm.Either
import de.lausi.tcm.application.ReadUseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class GetMembersResult(val members: List<Member>)

@UseCaseComponent
class GetMembersUseCase(
  private val permissions: Permissions,
  private val memberRepository: MemberRepository,
) : ReadUseCase<Nothing?, GetMembersResult, Nothing> {

  override fun checkPermission(userId: MemberId, command: Nothing?): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: Nothing?): Either<GetMembersResult, Nothing> {
    val members = memberRepository.findAll()
    return Either.Success(GetMembersResult(members))
  }
}