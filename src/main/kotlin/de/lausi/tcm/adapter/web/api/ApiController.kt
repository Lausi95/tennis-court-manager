package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.MemberService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal

data class NavLinkModel(
  val name: String,
  val target: String,
)

data class ApiModel(val navLinks: List<NavLinkModel>)

@Controller
@RequestMapping("/api")
class ApiController(private val memberService: MemberService) {

  fun getApi(model: Model, principal: Principal) {
    val user = memberService.getMember(principal.name)

    val links = mutableListOf(
      NavLinkModel("Home", "/"),
      NavLinkModel("Buchen", "/reservations")
    )

    if (user.groups.isNotEmpty()) {
      links.add(NavLinkModel("Admin", "/admin"))
    }

    model.addAttribute("rootCollection", ApiModel(links))
  }
}
