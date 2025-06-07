package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.Member
import de.lausi.tcm.domain.model.MemberGroup
import org.springframework.ui.Model

data class MemberModel(
  val id: String,
  val firstname: String,
  val lastname: String,
  val fullname: String,
  val groups: List<String>,
  val isAdmin: Boolean,
  val isEventManager: Boolean,
  val isTrainer: Boolean,
  val isTeamCaptain: Boolean,
  val isBallmachine: Boolean,
  val links: Map<String, String>,
)

fun Member.toModel() = MemberModel(
  id.value,
  firstname.value,
  lastname.value,
  formatName(),
  groups.map { it.name },
  groups.contains(MemberGroup.ADMIN),
  groups.contains(MemberGroup.EVENT_MANAGEMENT),
  groups.contains(MemberGroup.TRAINER),
  groups.contains(MemberGroup.TEAM_CAPTAIN),
  groups.contains(MemberGroup.BALLMACHINE),
  mapOf(
    "self" to "/members/${id.value}",
    "edit" to "/members/${id.value}/edit",
  ),
)

fun Model.memberEntity(member: Member) {
  addAttribute("member", member.toModel())
}

data class MemberCollection(
  val items: List<MemberModel>,
  val links: Map<String, String>,
)

fun List<Member>.toCollection() = MemberCollection(
  map { it.toModel() },
  mapOf(
    "self" to "/members",
    "synchronize" to "/members/synchronize",
  )
)

fun Model.memberCollection(members: List<Member>) {
  addAttribute("memberCollection", members.toCollection())
}
