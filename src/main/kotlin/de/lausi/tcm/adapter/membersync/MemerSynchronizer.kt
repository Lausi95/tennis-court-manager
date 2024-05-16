package de.lausi.tcm.adapter.membersync

import de.lausi.tcm.domain.model.Group
import de.lausi.tcm.domain.model.Member
import de.lausi.tcm.domain.model.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class MemerSynchronizer(
  private val memberRepository: MemberRepository,
  private val keycloakAdapter: KeycloakAdapter,
) {

  private val log = LoggerFactory.getLogger(javaClass)

  @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
  fun synchronzeMembers() {
    val importedMembers = keycloakAdapter.getKeycloakUsers()
      .filter { it.emailVerified }
      .map { Member(it.username, it.firstName, it.lastName) }

    val currentMembers = memberRepository.findAll()

    val memberMap = mutableMapOf<String, Member>()
    importedMembers.forEach { memberMap[it.id] = it }
    currentMembers.forEach {
      if (memberMap[it.id] != null) {
        memberMap[it.id] = it
      }
    }

    // Tom is always admin. Always >:D
    memberMap["tom.lausmann"]?.let {
      if (!it.groups.contains(Group.ADMIN)) {
        memberMap["tom.lausmann"] = it.copy(groups = it.groups.plus(Group.ADMIN))
      }
    }

    memberRepository.deleteAll()
    memberRepository.saveAll(memberMap.values)

    log.info("Synchronized members: {}", memberMap.size)
  }
}
