package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.Either
import de.lausi.tcm.adapter.web.memberId
import de.lausi.tcm.application.member.ToggleMemberGroupCommand
import de.lausi.tcm.application.member.ToggleMemberGroupUseCase
import de.lausi.tcm.domain.model.member.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal

data class MemberModel(
  val id: String,
  val firstname: String,
  val lastname: String,
  val fullname: String,
  val groups: List<String>,
  val links: Map<String, String> = mapOf()) {

  companion object {
    val NOT_FOUND = MemberModel("???", "???", "???", "??? ???", emptyList(), emptyMap())
  }
}

data class MemberCollection(
  val items: List<MemberModel>,
  val links: Map<String, String> = mapOf())

data class UpdateGroupRequest(
  val group: MemberGroup)

@Controller
@RequestMapping("/api/members")
class MemberController(
  private val toggleMemberGroupUseCase: ToggleMemberGroupUseCase,
  private val memberRepository: MemberRepository,
) {

  @GetMapping
  fun getMembers(model: Model): String {
    val members = memberRepository.findAll()
    return model.memberCollection(members)
  }

  @GetMapping("/{memberId}")
  fun getMember(model: Model, @PathVariable("memberId") memberIdValue: String): String {
    val memberId = MemberId(memberIdValue)
    val member = memberRepository.findById(memberId) ?: error("???")
    return model.member(member)
  }

  @PostMapping("/{memberId}")
  fun toggleMemberGroup(model: Model, @PathVariable("memberId") memberIdValue: String, request: UpdateGroupRequest, principal: Principal): String {
    val result = toggleMemberGroupUseCase.execute(principal.memberId()) {
      ToggleMemberGroupCommand(
        MemberId(memberIdValue),
        request.group
      )
    }

    if (result is Either.Success) {
      return model.member(result.value.member)
    }

    if (result is Either.Error) {
      error("TODO")
    }

    error("unreachable")
  }

  fun Member.toModel(): MemberModel {
    return MemberModel(
      id.value,
      firstname.value,
      lastname.value,
      formatName(),
      groups.map { it.toString() },
      mapOf(
        "self" to "/api/members/$id",
        "update" to "/api/members/$id",
      ),
    )
  }

  fun Model.member(member: Member): String {
    addAttribute("member", member.toModel())
    return "entity/member"
  }

  fun Model.memberCollection(members: List<Member>): String {
    val items = members.map { it.toModel() }
    addAttribute("memberCollection", MemberCollection(items, mapOf(
      "self" to "/api/members"
    )))
    return "collection/member"
  }
}
