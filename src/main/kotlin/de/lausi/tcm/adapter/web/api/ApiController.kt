package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.domain.model.MemberService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal

@Controller
@RequestMapping("/api")
class ApiController(private val memberService: MemberService) {

}
