package de.lausi.tcm.adapter.web

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
    if (member.groups.isNotEmpty()) {
      links.add("Admin")
    }

    addAttribute("links", links)
    addAttribute("currentPage", currentPage)
    addAttribute("view", func(this))

    return "page"
  }
}
