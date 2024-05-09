package de.lausi.tcm.adapter.web

import de.lausi.tcm.IsoDate
import de.lausi.tcm.adapter.web.api.*
import de.lausi.tcm.domain.model.MemberService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
  private val matchController: MatchController,
  private val eventController: EventController,
  private val memberController: MemberController,
  private val apiController: ApiController,
  private val memberService: MemberService,
) {

  fun Model.preparePage(currentPage: String, view: String, principal: Principal, func: (model: Model) -> Any): String {
    if (!memberService.exists(principal.name)) {
      return "unverified"
    }

    apiController.getApi(this, principal)
    addAttribute("currentPage", currentPage)
    addAttribute("view", view)

    func(this)

    return "page"
  }

  @GetMapping("/")
  fun getOccupancyPlan(model: Model, principal: Principal, @RequestParam(name = "date") @IsoDate date: LocalDate?): String {
    if (!memberService.exists(principal.name)) {
      return "unverified"
    }

    if (date == null) {
      return "redirect:/?date=${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}"
    }

    return model.preparePage("Home", "views/occupancy-plan", principal) {
      occupancyPlanController.getOccupancyPlan(model, date)
    }
  }

  @GetMapping("/trainings")
  fun getTrainigs(model: Model, principal: Principal): String {
    return model.preparePage("Trainings", "views/trainings", principal) {
      trainingController.getTrainings(model)
    }
  }

  @GetMapping("/trainings/{trainingId}")
  fun getTraining(model: Model, principal: Principal, @PathVariable trainingId: String): String {
    return model.preparePage("Training", "views/training", principal) {
      trainingController.getTraining(model, principal, trainingId)
    }
  }

  @GetMapping("/unique-trainings")
  fun getUniqueTrainig(model: Model, principal: Principal): String {
    return model.preparePage("Einzeltrainings", "views/unique-trainings", principal) {
      uniqueTrainingController.getUniqueTrainings(model)
    }
  }

  @GetMapping("/reservations")
  fun getBook(model: Model, principal: Principal): String {
    return model.preparePage("Buchen", "views/reservations", principal) {
      reservationController.getReservations(model, principal)
    }
  }

  @GetMapping("/teams")
  fun getTeams(model: Model, principal: Principal): String {
    return model.preparePage("Teams", "views/teams", principal) {
      teamController.getTeams(model)
    }
  }

  @GetMapping("/matches")
  fun getMatches(model: Model, principal: Principal): String {
    return model.preparePage("Punktspiele", "views/matches", principal) {
      matchController.getMatches(model)
    }
  }

  @GetMapping("/events")
  fun getEvents(model: Model, principal: Principal): String {
    return model.preparePage("Events", "views/events", principal) {
      eventController.getEvents(model)
    }
  }

  @GetMapping("/members")
  fun getMembers(model: Model, principal: Principal): String {
    return model.preparePage("Mitglieder", "views/members", principal) {
      memberController.getMembers(model)
    }
  }

  @GetMapping("/admin")
  fun getAdmin(model: Model, principal: Principal): String {
    return model.preparePage("Admin", "views/admin", principal) {
      memberController.getMember(model, principal.name)
    }
  }
}
