package de.lausi.tcm.application

import de.lausi.tcm.Either
import de.lausi.tcm.domain.model.MemberId
import org.springframework.stereotype.Component

val NOTHING: Nothing? = null
const val NOT_RESTRICTED = true

interface UseCase<CP, CO, C, R, E> {

  fun checkContextPermission(userId: MemberId, contextParams: CP): Boolean

  fun getContext(params: CP): Either<CO, E>

  fun context(userId: MemberId, contextParams: CP): Either<CO, E> {
    if (!checkContextPermission(userId, contextParams)) {
      return Either.Unauthorized
    }

    return getContext(contextParams)
  }

  fun checkCommandPermission(userId: MemberId, command: C): Boolean

  fun handle(command: C): Either<R, E>

  fun execute(userId: MemberId, command: C): Either<R, E> {
    if (!checkCommandPermission(userId, command)) {
      return Either.Unauthorized
    }

    return handle(command)
  }
}

interface ReadUseCase<C, R, E> {

  fun checkPermission(userId: MemberId, command: C): Boolean

  fun handle(command: C): Either<R, E>

  fun execute(userId: MemberId, command: C): Either<R, E> {
    if (!checkPermission(userId, command)) {
      return Either.Unauthorized
    }

    return handle(command)
  }
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Component
annotation class UseCaseComponent
