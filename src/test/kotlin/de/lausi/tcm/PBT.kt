package de.lausi.tcm

import de.lausi.tcm.domain.model.*
import io.github.serpro69.kfaker.Faker
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random

val faker = Faker()

fun randomMember() = Member(
  MemberId(faker.eSport.players()),
  MemberFirstname(faker.name.firstName()),
  MemberLastname(faker.name.lastName()),
  randomSubset(MemberGroup.entries.toSet()).toMutableSet()
)

fun <T> randomSubset(items: Set<T>): Set<T> {
  val result = mutableSetOf<T>()
  for (item in items) {
    if (Random.nextBoolean()) {
      result.add(item)
    }
  }
  return result
}

fun <T> randomSubset(items: Collection<T>): Set<T> {
  return randomSubset(items.toSet())
}

fun <T> randomItem(items: List<T>): T {
  val index = Random.nextInt(0, items.size)
  return items[index]
}

class CourtTestData {
  var name: CourtName = CourtName(faker.name.name())
  var id: CourtId = CourtId()
}

fun randomCourt(block: CourtTestData.() -> Unit): Court {
  val data = CourtTestData()
  data.block()
  return Court(
    data.name,
    data.id,
  )
}

class TrainingTestData {
  var dayOfWeek: DayOfWeek = randomItem(DayOfWeek.entries)
  var courtId: CourtId = CourtId()
  var fromSlot: Slot = Slot(Random.nextInt(10, 20))
  var toSlot: Slot = Slot(Random.nextInt(20, 30))
  var description: TrainingDescription = TrainingDescription(faker.movie.quote())
  var skippedDates: MutableSet<LocalDate> = mutableSetOf()
  var id: TrainingId = TrainingId()
}

fun randomTraining(block: TrainingTestData.() -> Unit): Training {
  val data = TrainingTestData()
  data.block()
  return Training(
    data.dayOfWeek,
    data.courtId,
    data.fromSlot,
    data.toSlot,
    data.description,
    data.skippedDates,
    data.id
  )
}
