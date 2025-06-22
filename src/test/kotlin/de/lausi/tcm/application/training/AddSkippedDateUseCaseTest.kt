package de.lausi.tcm.application.training

import de.lausi.tcm.assertError
import de.lausi.tcm.assertSuccess
import de.lausi.tcm.domain.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import java.time.DayOfWeek
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AddSkippedDateUseCaseTest {

  @Mock
  private lateinit var permissions: Permissions

  @Mock
  private lateinit var trainingRepository: TrainingRepository

  private lateinit var addSkippedDateUseCase: AddSkippedDateUseCase

  @BeforeEach
  fun setUp() {
    addSkippedDateUseCase = AddSkippedDateUseCase(permissions, trainingRepository)
  }

  @Nested
  @DisplayName("getContext()")
  inner class CheckContext {

    @Test
    fun `should return error, when training does not exist`() {
      val params = AddSkippedDateContextParams(TrainingId())
      val errors = addSkippedDateUseCase.getContext(params).assertError()
      assertThat(errors).contains("Das Training konnte nicht gefunden werden.")
    }

    @Test
    fun `should return success, when training exists`() {
      val training =
        Training(DayOfWeek.SATURDAY, CourtId(), Slot(10), Slot(12), TrainingDescription("Foo"), mutableSetOf())

      trainingRepository.stub {
        on { findById(training.id) } doReturn training
      }

      val params = AddSkippedDateContextParams(training.id)
      addSkippedDateUseCase.getContext(params).assertSuccess()
    }
  }

  @Nested
  @DisplayName("handle()")
  inner class Handle {
    @Test
    fun `should add skipped date to training`() {
      val training =
        Training(DayOfWeek.SATURDAY, CourtId(), Slot(10), Slot(12), TrainingDescription("Foo"), mutableSetOf())

      trainingRepository.stub {
        on { findById(training.id) } doReturn training
      }

      val command = AddSkippedDateCommand(training.id, LocalDate.of(2025, 6, 21))
      addSkippedDateUseCase.handle(command).assertSuccess()

      assertThat(training.skippedDates).contains(LocalDate.of(2025, 6, 21))
    }

    @Test
    fun `should fail, when given training does not exsit`() {
      val command = AddSkippedDateCommand(TrainingId(), LocalDate.of(2025, 6, 21))
      val errors = addSkippedDateUseCase.handle(command).assertError()
      assertThat(errors).contains("Das Training konnte nicht gefunden werden.")
    }

    @Test
    fun `should fail, when day of week of skipped date does not match day of week of training`() {
      val training =
        Training(DayOfWeek.MONDAY, CourtId(), Slot(10), Slot(12), TrainingDescription("Foo"), mutableSetOf())

      trainingRepository.stub {
        on { findById(training.id) } doReturn training
      }

      val command = AddSkippedDateCommand(training.id, LocalDate.of(2025, 6, 21))
      val errors = addSkippedDateUseCase.handle(command).assertError()
      assertThat(errors).contains("Der angegebene Tag ist nicht am selben Wochentag, wie das Training. Bist du sicher, dass du diesen Tag meinst?")
    }

    @Test
    fun `should fail, when skipped day is already contained in training`() {
      val training =
        Training(
          DayOfWeek.SATURDAY, CourtId(), Slot(10), Slot(12), TrainingDescription("Foo"), mutableSetOf(
            LocalDate.of(2025, 6, 21)
          )
        )

      trainingRepository.stub {
        on { findById(training.id) } doReturn training
      }

      val command = AddSkippedDateCommand(training.id, LocalDate.of(2025, 6, 21))
      val errors = addSkippedDateUseCase.handle(command).assertError()

      assertThat(errors).contains("Der angegebene Tag ist bereits in den Ausnahmen enthalten.")
    }
  }
}