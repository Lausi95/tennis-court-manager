package de.lausi.tcm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TennisCourtManagerApplication

fun main(args: Array<String>) {
  runApplication<TennisCourtManagerApplication>(*args)
}
