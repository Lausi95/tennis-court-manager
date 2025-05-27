package de.lausi.tcm.adapter.web.api

import de.lausi.tcm.Either
import de.lausi.tcm.adapter.web.errors
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ui.Model

private val log: Logger = LoggerFactory.getLogger("ErrorHandling")

fun <T1, T2> runContext(
  either: Either<T1, T2>,
  model: Model,
  onSuccess: (T1) -> String
): String {
  if (either is Either.Unauthorized) {
    return "views/unauthorized"
  }

  if (either is Either.Error) {
    model.errors(either.value.map { it.toString() })
    log.info("{}", either.value)
    return "views/error"
  }

  either as Either.Success

  return onSuccess(either.value)
}

fun <T1, T2> runUseCase(
  either: Either<T1, T2>,
  model: Model,
  onError: () -> String,
  onSuccess: (T1) -> String
): String {
  if (either is Either.Unauthorized) {
    return "views/unauthorized"
  }

  if (either is Either.Error) {
    model.errors(either.value.map { it.toString() })
    return onError()
  }

  either as Either.Success

  return onSuccess(either.value)
}
