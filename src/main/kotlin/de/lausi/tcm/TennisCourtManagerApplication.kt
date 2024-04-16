package de.lausi.tcm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TennisCourtManagerApplication

fun main(args: Array<String>) {
  runApplication<TennisCourtManagerApplication>(*args)
}
