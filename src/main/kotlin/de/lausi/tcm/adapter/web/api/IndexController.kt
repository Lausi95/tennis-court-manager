package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.IsoDate
import de.lausi.tcm.adapter.web.PageAssembler
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.security.Principal
import java.time.LocalDate

@Controller
@RequestMapping("/")
class IndexController(
  private val pageAssembler: PageAssembler,
  private val occupancyPlanController: OccupancyPlanController,
) {

  @GetMapping
  fun getIndex(principal: Principal, model: Model, @RequestParam(required = false) @IsoDate date: LocalDate?): String {
    return with(pageAssembler) {
      model.preparePage("Home", principal) {
        occupancyPlanController.getOccupancyPlan(principal, model, date ?: LocalDate.now())
      }
    }
  }

  @GetMapping("/admin")
  fun getAdmin(principal: Principal, model: Model): String {
    return with(pageAssembler) {
      model.preparePage("Admin", principal) {
        "views/admin"
      }
    }
  }
}