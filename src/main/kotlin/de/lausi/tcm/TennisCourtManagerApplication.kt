package de.lausi.tcm

import de.lausi.tcm.domain.model.Court
import de.lausi.tcm.domain.model.CourtRepository
import de.lausi.tcm.domain.model.Member
import de.lausi.tcm.domain.model.MemberRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@SpringBootApplication
class TennisCourtManagerApplication

fun main(args: Array<String>) {
  runApplication<TennisCourtManagerApplication>(*args)
}

@Component
class SetUp(private val courtRepository: CourtRepository, private val memberRepository: MemberRepository) {

  @EventListener(ApplicationStartedEvent::class)
  fun setUp() {
    putCourt("Platz 1")
    putCourt("Platz 2")
    putCourt("Platz 3")

    putMember("tom.lausmann", "Tom", "Lausmann")
    putMember("tobias@pfeifer.de", "Tobias", "Pfeifer")
    putMember("add.katzinsky@gmail.com", "Ann", "Katzinsky")
    putMember("hong.ahnle@gmaia.com", "Hong", "Le")
  }

  fun putCourt(courtName: String) {
    if (!courtRepository.existsById(courtName)) {
      courtRepository.save(Court(courtName))
    }
  }

  fun putMember(email: String, firstname: String, lastname: String) {
    if (!memberRepository.existsByFirstnameAndLastname(firstname, lastname)) {
      memberRepository.save(Member(email, firstname, lastname))
    }
  }
}
