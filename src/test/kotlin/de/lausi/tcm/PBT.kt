package de.lausi.tcm

import de.lausi.tcm.domain.model.member.*
import io.github.serpro69.kfaker.Faker
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

fun <T> randomSubset(items: Array<T>): Set<T> {
  return randomSubset(items.toSet())
}
