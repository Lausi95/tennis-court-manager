package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.application.ballmachine.BallmachineStatistics
import de.lausi.tcm.application.ballmachine.MemberBookingReport
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController("/reports")
class ReportController(private val ballmachineStatistics: BallmachineStatistics) {

  @GetMapping(
    path = ["/ballmachine-report"],
    params = ["from", "to"]
  )
  fun getBallmachineReport(
    @RequestParam from: LocalDate,
    @RequestParam to: LocalDate
  ): ResponseEntity<List<MemberBookingReport>?> {
    val statistic = ballmachineStatistics.getBallmachineBookingStatistic(from, to)
    return ResponseEntity.ok().body(statistic)
  }

  @GetMapping(
    path = ["/ballmachine-report.csv"],
    params = ["from", "to"]
  )
  fun getBallmachineReportCsv(
    @RequestParam from: LocalDate,
    @RequestParam to: LocalDate
  ): ResponseEntity<String?> {
    val statistic = ballmachineStatistics.getBallmachineBookingStatistic(from, to)

    var csv = "name,hours\n"
    statistic.forEach {
      csv += "${it.member.formatName()},${it.hours}"
    }

    return ResponseEntity.ok()
      .header("Content-Type", "text/csv")
      .header("Content-Disposition", "attachment; filename=\"ballmachine-report-$from-$to.csv\"\n")
      .body(csv)
  }
}