package de.lausi.tcm.adapter.web

import de.lausi.tcm.adapter.web.api.CourtController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDate

@Controller
private class HomeController(val courtController: CourtController) {

  @GetMapping("/")
  fun getHome(model: Model): String {
    courtController.getCourts(LocalDate.now(), model)

    model.addAttribute("currentPage", "home")

    return "home"
  }

  @GetMapping("/book")
  fun getBook(model: Model): String {
    model.addAttribute("currentPage", "book")

    return "book"
  }
}
