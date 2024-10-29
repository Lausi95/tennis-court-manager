package de.lausi.tcm

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun LocalDate.iso(): String = format(DateTimeFormatter.ISO_DATE)
fun LocalDate.ger(): String = format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
