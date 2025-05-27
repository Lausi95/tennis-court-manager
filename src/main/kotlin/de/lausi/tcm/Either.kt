package de.lausi.tcm

sealed class Either<out A, out B> {

  companion object {

    fun <E> Success(): Either<Nothing?, E> {
      return Success(null)
    }
  }

  class Success<A>(val value: A) : Either<A, Nothing>() {
    override fun unpack(): A? {
      return value
    }
  }

  class Error<B>(val value: List<B>) : Either<Nothing, B>() {
    constructor(vararg values: B) : this(values.toList())

    override fun unpack(): Nothing? {
      return null
    }
  }

  data object Unauthorized : Either<Nothing, Nothing>() {
    override fun unpack(): Nothing? {
      return null
    }
  }

  abstract fun unpack(): A?
}
