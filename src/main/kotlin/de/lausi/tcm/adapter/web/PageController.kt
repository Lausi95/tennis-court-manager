package de.lausi.tcm.adapter.web

import de.lausi.tcm.IsoDate
import de.lausi.tcm.adapter.web.api.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.security.Principal
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Controller
private class HomeController(
  private val occupancyPlanController: OccupancyPlanController,
  private val trainingController: TrainingController,
  private val reservationController: ReservationController,
  private val teamController: TeamController,
  private val uniqueTrainingController: UniqueTrainingController,
) {

  @GetMapping("/")
  fun getOccupancyPlan(model: Model, @RequestParam(name = "date") @IsoDate date: LocalDate?): String {
    if (date == null) {
      return "redirect:/?date=${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}"
    }

    model.addAttribute("currentPage", "home")
    occupancyPlanController.getOccupancyPlan(model, date)
    return "pages/occupancy-plan"
  }

  @GetMapping("/trainings")
  fun getTrainig(model: Model): String {
    model.addAttribute("currentPage", "training")
    trainingController.getTrainings(model)
    return "pages/trainings"
  }

  @GetMapping("/unique-trainings")
  fun getUniqueTrainig(model: Model): String {
    model.addAttribute("currentPage", "unique-training")
    uniqueTrainingController.getUniqueTrainings(model)
    return "pages/unique-trainings"
  }

  @GetMapping("/reservations")
  fun getBook(model: Model, principal: Principal): String {
    model.addAttribute("currentPage", "book")
    reservationController.getReservations(model, principal)
    return "pages/reservations"
  }

  @GetMapping("/teams")
  fun getTeams(model: Model): String {
    model.addAttribute("currentPage", "teams")
    teamController.getTeams(model)
    return "pages/teams"
  }
}
