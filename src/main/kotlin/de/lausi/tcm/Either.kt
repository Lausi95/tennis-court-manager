package de.lausi.tcm

sealed class Either<out A, out B> {
  class Success<A>(val value: A): Either<A, Nothing>()
  class Error<B>(val value: List<B>): Either<Nothing, B>()
}
