package de.lausi.tcm.domain.model.member

import de.lausi.tcm.domain.model.MemberGroup
import de.lausi.tcm.randomMember
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class MemberTest {

  @ParameterizedTest
  @EnumSource(MemberGroup::class)
  fun shouldToggleGroup(group: MemberGroup) {
    val member = randomMember()
    val hasGroup = member.hasGroup(group)

    member.toggleGroup(group)
    val hasGroupAfterToggle = member.hasGroup(group)

    assertThat(hasGroupAfterToggle).isEqualTo(!hasGroup)
  }

  @ParameterizedTest
  @EnumSource(MemberGroup::class)
  fun shouldHaveGroupLikeBeforeAfterTwoToggles(group: MemberGroup) {
    val member = randomMember()
    val hasGroup = member.hasGroup(group)

    member.toggleGroup(group)
    member.toggleGroup(group)
    val hasGroupAfterToggle = member.hasGroup(group)

    assertThat(hasGroupAfterToggle).isEqualTo(hasGroup)
  }
}
