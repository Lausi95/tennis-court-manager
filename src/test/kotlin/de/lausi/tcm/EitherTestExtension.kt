package de.lausi.tcm

import org.junit.jupiter.api.fail

fun <A, B> Either<A, B>.assertSuccess(): A {
  if (this is Either.Success<A>) {
    return this.value
  }
  fail { "Expected Either.Success, but its Either.Error" }
}

fun <A, B> Either<A, B>.assertError(): List<B> {
  if (this is Either.Error<B>) {
    return this.value
  }
  fail { "Expected Either.Error, but its Either.Success" }
}

