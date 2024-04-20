package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.IsoDate
import de.lausi.tcm.domain.model.court.Court
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CourtsCollection(
  val date: String,
  val nextDateUrl: String,
  val prevDateUrl: String,
  val courts: List<Court>
)

@Controller
@RequestMapping("/api/courts")
class CourtController {

  @GetMapping
  fun getCourts(@RequestParam @IsoDate date: LocalDate, model: Model): String {
    val nextDateUrl = "/api/courts?date=${date.plusDays(1).format(DateTimeFormatter.ISO_DATE)}"
    val prevDateUrl = "/api/courts?date=${date.minusDays(1).format(DateTimeFormatter.ISO_DATE)}"

    val courts = CourtsCollection(
      date.format(DateTimeFormatter.ISO_DATE),
      nextDateUrl,
      prevDateUrl,
      listOf(
        Court.build("Platz 1", listOf(
        )),
        Court.build("Platz 2", listOf(
        )),
        Court.build("Platz 3", listOf(
        )),
      )
    )

    model.addAttribute("courtsCollection", courts)

    return "views/courts"
  }
}
