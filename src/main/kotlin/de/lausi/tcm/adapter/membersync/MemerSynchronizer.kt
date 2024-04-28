package de.lausi.tcm.adapter.membersync

import de.lausi.tcm.domain.model.Member
import de.lausi.tcm.domain.model.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MemerSynchronizer(
  private val memberRepository: MemberRepository,
  private val keycloakAdapter: KeycloakAdapter,
) {

  private val log = LoggerFactory.getLogger(javaClass)

  @EventListener(ApplicationStartedEvent::class)
  @Scheduled(fixedDelay = 1000 * 60 * 10) // every 10 min
  fun synchronzeMembers() {
    val members = keycloakAdapter.getKeycloakUsers()
      .filter { it.emailVerified }
      .map { Member(it.username, it.firstName, it.lastName) }

    memberRepository.deleteAll()
    memberRepository.saveAll(members)

    log.info("Synchronized members: {}", members.size)
  }
}
