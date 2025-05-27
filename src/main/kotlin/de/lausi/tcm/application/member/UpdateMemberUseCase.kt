package de.lausi.tcm.application.member

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.application.UseCaseComponent
import de.lausi.tcm.domain.model.*

data class UpdateMemberContextParams(
  val memberId: MemberId,
)

data class UpdateMemberContext(
  val memeber: Member,
)

data class UpdateMemberCommand(
  val memberId: MemberId,
  val groups: Map<MemberGroup, Boolean>,
)

data class UpdateMemberResult(
  val member: Member,
)

enum class UpdateMemberError {
  MEMBER_DOES_NOT_EXIST,
}

@UseCaseComponent
class UpdateMemberUseCase(
  val permissions: Permissions,
  val memberRepository: MemberRepository,
) : UseCase<
        UpdateMemberContextParams,
        UpdateMemberContext,
        UpdateMemberCommand,
        UpdateMemberResult,
        UpdateMemberError> {

  override fun checkContextPermission(userId: MemberId, contextParams: UpdateMemberContextParams): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun getContext(params: UpdateMemberContextParams): Either<UpdateMemberContext, UpdateMemberError> {
    val member = memberRepository.findById(params.memberId)
      ?: return Either.Error(listOf(UpdateMemberError.MEMBER_DOES_NOT_EXIST))

    return Either.Success(UpdateMemberContext(member))
  }

  override fun checkCommandPermission(userId: MemberId, command: UpdateMemberCommand): Boolean {
    return permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: UpdateMemberCommand): Either<UpdateMemberResult, UpdateMemberError> {
    val member = memberRepository.findById(command.memberId)
      ?: return Either.Error(listOf(UpdateMemberError.MEMBER_DOES_NOT_EXIST))

    command.groups.forEach {
      val group = it.key
      val groupIsActive = it.value

      if (!member.groups.contains(group) && groupIsActive) {
        member.groups.add(group)
      }

      if (member.groups.contains(group) && !groupIsActive) {
        member.groups.remove(group)
      }
    }

    memberRepository.save(member)

    return Either.Success(UpdateMemberResult(member))
  }
}