package de.lausi.tcm.adapter.web

import de.lausi.tcm.adapter.web.api.toModel
import de.lausi.tcm.domain.model.MemberGroup
import de.lausi.tcm.domain.model.MemberRepository
import org.springframework.stereotype.Component
import org.springframework.ui.Model
import java.security.Principal

fun Model.errors(errors: List<String>) {
  addAttribute("errors", errors)
}

fun Model.userInfo(principal: Principal) {
  addAttribute("userId", principal.userId().value)
}

@Component
class PageAssembler(private val memberRepository: MemberRepository) {
  fun Model.preparePage(
    currentPage: String,
    principal: Principal,
    func: (model: Model) -> String
  ): String {
    val member = memberRepository.findById(principal.userId()) ?: return "unverified"

    val links = mutableListOf("Home", "Buchen")
    if (member.groups.any { it.adminArea }) {
      links.add("Admin")
    }
    if (member.groups.contains(MemberGroup.ADMIN) || member.groups.contains(MemberGroup.BALLMACHINE)) {
      links.add("Ballmachine")
    }

    addAttribute("links", links)
    addAttribute("currentPage", currentPage)
    addAttribute("view", func(this))
    addAttribute("member", member.toModel())

    return "page"
  }
}
