package de.lausi.tcm.adapter.web

import de.lausi.tcm.domain.model.member.MemberId
import java.security.Principal

fun Principal.memberId(): MemberId {
  return MemberId(this.name)
}
