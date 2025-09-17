package de.lausi.tcm.adapter.startup

import de.lausi.tcm.domain.model.Court
import de.lausi.tcm.domain.model.CourtName
import de.lausi.tcm.domain.model.CourtRepository
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
private class CourtInitialization(private val courtRepository: CourtRepository) {

  @EventListener(ApplicationStartedEvent::class)
  fun onApplicationStartedEvent() {
    if (courtRepository.findAll().isNotEmpty()) {
      return
    }

    listOf(
      Court(CourtName("Platz 1")),
      Court(CourtName("Platz 2")),
      Court(CourtName("Platz 3"))
    ).forEach { courtRepository.save(it) }
  }
}
