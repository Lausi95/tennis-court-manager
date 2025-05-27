package de.lausi.tcm.adapter.web

import de.lausi.tcm.domain.model.MemberId
import java.security.Principal

fun Principal.userId(): MemberId {
  return MemberId(this.name)
}
