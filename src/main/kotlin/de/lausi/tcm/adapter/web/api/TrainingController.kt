package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.adapter.web.PageAssembler
import de.lausi.tcm.adapter.web.userId
import de.lausi.tcm.application.NOTHING
import de.lausi.tcm.application.training.CreateTraingUseCase
import de.lausi.tcm.application.training.CreateTrainingCommand
import de.lausi.tcm.application.training.GetTrainingsUseCase
import de.lausi.tcm.domain.model.CourtId
import de.lausi.tcm.domain.model.Slot
import de.lausi.tcm.domain.model.SlotRepository
import de.lausi.tcm.domain.model.TrainingDescription
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal
import java.time.DayOfWeek
import java.time.LocalDate

data class CreateTrainingRequest(
  val dayOfWeek: DayOfWeek,
  val courtId: String,
  val fromSlot: Int,
  val toSlot: Int,
  val description: String,
)

@Controller
@RequestMapping("/trainings")
class TrainingController(
  private val pageAssembler: PageAssembler,
  private val getTrainingsUseCase: GetTrainingsUseCase,
  private val createTraingUseCase: CreateTraingUseCase,
) {

  @GetMapping
  fun getView(principal: Principal, model: Model): String {
    return with(pageAssembler) {
      model.preparePage("Trainings", principal) {
        getTrainingCollection(principal, model)
      }
    }
  }

  @GetMapping("/collection")
  fun getTrainingCollection(principal: Principal, model: Model): String {
    return runContext(getTrainingsUseCase.execute(principal.userId(), NOTHING), model) {
      model.trainingCollection(it.trainings, it.courtsByTraining)
      model.courtCollection(it.courts)
      model.dayOfWeekCollection()
      "views/trainings/collection"
    }
  }

  @GetMapping("/create")
  fun getCreateTraining(principal: Principal, model: Model): String {
    return runContext(createTraingUseCase.context(principal.userId(), NOTHING), model) {
      model.courtCollection(it.courts)
      model.dayOfWeekCollection()
      model.slotCollection(SlotRepository.findAll(), LocalDate.now())
      "views/trainings/create"
    }
  }

  @PostMapping("/create")
  fun createTraining(principal: Principal, model: Model, request: CreateTrainingRequest): String {
    val command = CreateTrainingCommand(
      CourtId(request.courtId),
      TrainingDescription(request.description),
      request.dayOfWeek,
      Slot(request.fromSlot),
      Slot(request.toSlot),
    )

    return runUseCase(
      createTraingUseCase.execute(principal.userId(), command),
      model,
      { getCreateTraining(principal, model) }) {
      getTrainingCollection(principal, model)
    }
  }

  // TODO: Delte Use Case

  // TODO: Edit Use Case

  // TODO: Exception Dates Use Case
}
