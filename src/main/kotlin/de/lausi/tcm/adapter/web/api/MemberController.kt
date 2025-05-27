package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.PageAssembler
import de.lausi.tcm.adapter.web.userId
import de.lausi.tcm.application.NOTHING
import de.lausi.tcm.application.member.GetMembersUseCase
import de.lausi.tcm.application.member.UpdateMemberCommand
import de.lausi.tcm.application.member.UpdateMemberContextParams
import de.lausi.tcm.application.member.UpdateMemberUseCase
import de.lausi.tcm.domain.model.MemberGroup
import de.lausi.tcm.domain.model.MemberId
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal

data class UpdateMemberRequest(
  val admin: String?,
  val eventManager: String?,
  val teamCaptain: String?,
  val trainer: String?,
)

@Controller
@RequestMapping("/members")
class MemberController(
  private val pageAssembler: PageAssembler,
  private val getMembersUseCase: GetMembersUseCase,
  private val updateMemberUseCase: UpdateMemberUseCase,
) {

  @GetMapping
  fun getView(principal: Principal, model: Model): String {
    return with(pageAssembler) {
      model.preparePage("Mitglieder", principal) {
        getMemberCollection(principal, model)
      }
    }
  }

  @GetMapping("/collection")
  fun getMemberCollection(principal: Principal, model: Model): String {
    return runContext(getMembersUseCase.execute(principal.userId(), NOTHING), model) {
      model.memberCollection(it.members)
      "views/members/collection"
    }
  }

  @GetMapping("/{memberId}/edit")
  fun getEditMember(principal: Principal, model: Model, @PathVariable memberId: String): String {
    val params = UpdateMemberContextParams(
      MemberId(memberId),
    )

    return runContext(updateMemberUseCase.context(principal.userId(), params), model) {
      model.memberEntity(it.memeber)
      "views/members/edit"
    }
  }

  @PostMapping("/{memberId}/edit")
  fun editMember(
    model: Model,
    @PathVariable memberId: String,
    request: UpdateMemberRequest,
    principal: Principal
  ): String {
    val command = UpdateMemberCommand(
      MemberId(memberId),
      mapOf(
        MemberGroup.ADMIN to (request.admin == "on"),
        MemberGroup.TEAM_CAPTAIN to (request.teamCaptain == "on"),
        MemberGroup.TRAINER to (request.trainer == "on"),
        MemberGroup.EVENT_MANAGEMENT to (request.eventManager == "on"),
      ),
    )

    return runUseCase(
      updateMemberUseCase.execute(principal.userId(), command),
      model,
      { getEditMember(principal, model, memberId) }) {
      getMemberCollection(principal, model)
    }
  }
}