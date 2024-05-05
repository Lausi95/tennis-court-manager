package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Group
import de.lausi.tcm.domain.model.Member
import de.lausi.tcm.domain.model.MemberRepository
import de.lausi.tcm.domain.model.MemberService
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
  val groups: List<String>,
  val links: Map<String, String> = mapOf())

data class MemberCollection(
  val items: List<MemberModel>,
  val links: Map<String, String> = mapOf())

data class UpdateGroupRequest(
  val group: Group)

@Controller
@RequestMapping("/api/members")
class MemberController(
  private val memberRepository: MemberRepository,
  private val memberService: MemberService,
) {

  @GetMapping
  fun getMembers(model: Model): String {
    val members = memberRepository.findAll().sortedBy { it.firstname }
    return model.memberCollection(members)
  }

  @GetMapping("/{memberId}")
  fun getMember(model: Model, @PathVariable memberId: String): String {
    val member = memberService.getMember(memberId)
    return model.member(member)
  }

  @PostMapping("/{memberId}")
  fun updateMemberGroup(model: Model, @PathVariable memberId: String, request: UpdateGroupRequest, principal: Principal): String {
    memberService.getMember(principal.name).assertRoles(Group.ADMIN)
    val member = memberService.toggleGroup(memberId, request.group)
    return model.member(member)
  }

  fun Member.toModel(): MemberModel {
    return MemberModel(id, firstname, lastname, groups.map { it.toString() }, mapOf(
      "self" to "/api/members/$id",
      "update" to "/api/members/$id",
    ))
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
