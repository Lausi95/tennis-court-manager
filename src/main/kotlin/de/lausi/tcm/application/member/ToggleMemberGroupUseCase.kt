package de.lausi.tcm.application.member

import de.lausi.tcm.Either
import de.lausi.tcm.application.UseCase
import de.lausi.tcm.domain.model.member.*
import org.springframework.stereotype.Component

data class ToggleMemberGroupCommand(
  val memberId: MemberId,
  val group: MemberGroup,
)

data class ToggleMemberGroupResult(
  val member: Member,
)

enum class ToggleMemberGroupError {
  MEMBER_DOES_NOT_EXIST,
}

@Component
class ToggleMemberGroupUseCase(
  val permissions: Permissions,
  val memberRepository: MemberRepository,
): UseCase<ToggleMemberGroupCommand, ToggleMemberGroupResult, ToggleMemberGroupError> {

  override fun checkPermission(userId: MemberId) {
    permissions.assertGroup(userId, MemberGroup.ADMIN)
  }

  override fun handle(command: ToggleMemberGroupCommand): Either<ToggleMemberGroupResult, ToggleMemberGroupError> {
    val member = memberRepository.findById(command.memberId)
      ?: return Either.Error(ToggleMemberGroupError.MEMBER_DOES_NOT_EXIST)

    member.toggleGroup(command.group)
    memberRepository.save(member)

    return Either.Success(ToggleMemberGroupResult(member))
  }
}