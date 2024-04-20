package de.lausi.tcm

import org.springframework.format.annotation.DateTimeFormat

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
annotation class IsoDate
