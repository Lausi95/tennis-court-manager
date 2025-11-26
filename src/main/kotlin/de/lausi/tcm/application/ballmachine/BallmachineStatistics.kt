package de.lausi.tcm.application.ballmachine

import de.lausi.tcm.domain.model.BallmachineBookingRepository
import de.lausi.tcm.domain.model.Member
import de.lausi.tcm.domain.model.MemberRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime

data class MemberBookingReportEntry(
  val date: LocalDate,
  val time: LocalTime,
)

data class MemberBookingReport(
  val member: Member,
  val entries: List<MemberBookingReportEntry>,
) {
  val hours = entries.size
}

@Service
class BallmachineStatistics(
  private val ballmachineBookingRepository: BallmachineBookingRepository,
  private val memberRepository: MemberRepository
) {

  fun getBallmachineBookingStatistic(fromDate: LocalDate, toDate: LocalDate): List<MemberBookingReport> {
    val bookings = ballmachineBookingRepository.findByDateBetween(fromDate, toDate)
    return bookings.groupBy { it.memberId }.map { entry ->
      val member = memberRepository.findById(entry.key) ?: error("Member not found")
      val entries = entry.value.map {
        MemberBookingReportEntry(it.date, LocalTime.parse(it.slot.formatToTimeIso()))
      }
      MemberBookingReport(member, entries)
    }
  }
}
