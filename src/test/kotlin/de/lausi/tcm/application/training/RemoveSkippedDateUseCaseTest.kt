package de.lausi.tcm.application.training

import de.lausi.tcm.assertError
import de.lausi.tcm.assertSuccess
import de.lausi.tcm.domain.model.CourtRepository
import de.lausi.tcm.domain.model.Permissions
import de.lausi.tcm.domain.model.TrainingRepository
import de.lausi.tcm.randomTraining
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class RemoveSkippedDateUseCaseTest {

  @Mock
  private lateinit var permissions: Permissions

  @Mock
  private lateinit var trainingRepository: TrainingRepository

  @Mock
  private lateinit var courtRepository: CourtRepository

  private lateinit var usecase: RemoveSkippedDateUseCase

  @BeforeEach
  fun setUp() {
    usecase = RemoveSkippedDateUseCase(permissions, trainingRepository, courtRepository)
  }

  @Test
  fun `should remove skipped date`() {
    val someDate = LocalDate.now()
    val training = randomTraining { skippedDates = mutableSetOf(someDate) }
    trainingRepository.stub { on { findById(training.id) } doReturn training }

    val command = RemoveSkippedDateCommand(training.id, someDate)
    usecase.handle(command).assertSuccess()

    assertThat(training.skippedDates).doesNotContain(someDate)
  }

  @Test
  fun `should fail, when given date is not a skipped date`() {
    val someDate = LocalDate.now()
    val training = randomTraining {}
    trainingRepository.stub { on { findById(training.id) } doReturn training }

    val command = RemoveSkippedDateCommand(training.id, someDate)
    val errors = usecase.handle(command).assertError()

    assertThat(errors).contains("Das Datum kann nicht entfernt werden, da es nicht in den Ausnahmen steht.")
  }
}
