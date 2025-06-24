package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.IsoDate
import de.lausi.tcm.adapter.web.PageAssembler
import de.lausi.tcm.adapter.web.userId
import de.lausi.tcm.application.NOTHING
import de.lausi.tcm.application.training.*
import de.lausi.tcm.domain.model.*
import de.lausi.tcm.iso
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
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

data class AddSkippedDateRequest(
  val skippedDate: LocalDate,
)

data class RemoveSkippedDateRequest(
  @IsoDate val skippedDate: LocalDate,
)

@Controller
@RequestMapping("/trainings")
class TrainingController(
  private val pageAssembler: PageAssembler,
  private val getTrainingsUseCase: GetTrainingsUseCase,
  private val createTraingUseCase: CreateTraingUseCase,
  private val getTrainingUseCase: GetTrainingUseCase,
  private val addSkippedDateUseCase: AddSkippedDateUseCase,
  private val removeSkippedDateUseCase: RemoveSkippedDateUseCase,
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

  @GetMapping("/{trainingId}/entity")
  fun getTrainingEntity(principal: Principal, model: Model, @PathVariable trainingId: String): String {
    val command = GetTrainingCommand(
      TrainingId(trainingId)
    )

    return runContext(getTrainingUseCase.execute(principal.userId(), command), model) {
      model.trainingEntry(it.training, it.court)
      "views/trainings/entity"
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

  @GetMapping("/{trainingId}/add-skipped-date")
  fun getAddSkippedDate(principal: Principal, model: Model, @PathVariable trainingId: String): String {
    val params = AddSkippedDateContextParams(
      TrainingId(trainingId)
    )

    return runContext(addSkippedDateUseCase.context(principal.userId(), params), model) {
      model.trainingEntry(it.training, it.court)
      "views/trainings/add-skipped-date"
    }
  }

  @PostMapping("/{trainingId}/add-skipped-date")
  fun addSkippedDate(principal: Principal, model: Model, @PathVariable trainingId: String, request: AddSkippedDateRequest): String {
    val command = AddSkippedDateCommand(
      TrainingId(trainingId),
      request.skippedDate,
    )

    return runUseCase(addSkippedDateUseCase.execute(principal.userId(), command), model, { getAddSkippedDate(principal, model, trainingId) }) {
      getTrainingEntity(principal, model, trainingId)
    }
  }

  @GetMapping("/{trainingId}/remove-skipped-date")
  fun getRemoveSkippedDate(principal: Principal, model: Model, @PathVariable trainingId: String, @RequestParam @IsoDate skippedDateToRemove: LocalDate): String {
    val params = RemoveSkippedDateContextParams(
      TrainingId(trainingId),
      skippedDateToRemove,
    )

    return runContext(removeSkippedDateUseCase.context(principal.userId(), params), model) {
      model.trainingEntry(it.training, it.court)
      model.addAttribute("dateToRemove", it.skippedDateToRemove.iso())
      "view/trainings/remove-skipped-date"
    }
  }

  @PostMapping("/{trainingId}/remove-skipped-date")
  fun removeSkippedDate(principal: Principal, model: Model, @PathVariable trainingId: String, request: RemoveSkippedDateRequest): String {
    val command = RemoveSkippedDateCommand(
      TrainingId(trainingId),
      request.skippedDate,
    )

    return runUseCase(removeSkippedDateUseCase.execute(principal.userId(), command), model, { getRemoveSkippedDate(principal, model, trainingId, request.skippedDate) }) {
      getTrainingEntity(principal, model, trainingId)
    }
  }
}
