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
  val links: Map<String, String>)

data class MemberCollection(
  val items: List<MemberModel>,
  val count: Int,
  val links: Map<String, String>)

@Controller
@RequestMapping("/api/members")
class MemberController(private val memberRepository: MemberRepository) {

  @ResponseBody
  @GetMapping(headers = ["accept=application/json"])
  fun getMemberCollection(): MemberCollection {
    val memberModels = memberRepository.findAll()
      .map { MemberModel(it.id, it.firstname, it.lastname, mapOf("self" to "/api/members/${it.id}")) }
      .sortedBy { it.firstname }

    return MemberCollection(
      memberModels,
      memberModels.size,
      mapOf("self" to "/api/members"))
  }

  @GetMapping
  fun getMembers(model: Model): String {
    model.addAttribute("memberCollection", getMemberCollection())
    return "views/members"
  }
}
