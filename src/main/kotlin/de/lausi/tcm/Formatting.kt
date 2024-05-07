package de.lausi.tcm

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun LocalDate.iso() = format(DateTimeFormatter.ISO_DATE)
fun LocalDate.ger() = format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
