package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.MemberRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

data class MemberModel(
  val id: String,
  val firstname: String,
  val lastname: String,
  val links: Map<String, String> = mapOf())

data class MemberCollection(
  val items: List<MemberModel>,
  val count: Int,
  val links: Map<String, String> = mapOf())

@Controller
@RequestMapping("/api/members")
class MemberController(private val memberRepository: MemberRepository) {

  @GetMapping
  fun getMembers(model: Model): String {
    val memberModels = memberRepository.findAll()
      .map { MemberModel(it.id, it.firstname, it.lastname, mapOf("self" to "/api/members/${it.id}")) }
      .sortedBy { it.firstname }

    val memberCollection = MemberCollection(
      memberModels,
      memberModels.size,
      mapOf("self" to "/api/members"))

    model.addAttribute("memberCollection", memberCollection)

    return "views/members"
  }
}
