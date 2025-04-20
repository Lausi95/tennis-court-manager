package de.lausi.tcm.application

import de.lausi.tcm.Either
import de.lausi.tcm.domain.model.MemberId

interface UseCase<C, R, E> {

  fun checkPermission(userId: MemberId)

  fun handle(command: C): Either<R, E>

  fun execute(userId: MemberId, commandProvider: () -> C): Either<R, E> {
    checkPermission(userId)
    val command = commandProvider()
    return handle(command)
  }
}
