package de.lausi.tcm.adapter.web

import io.sentry.Sentry
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView

@ControllerAdvice
class TCMExceptionHandler {

  @ExceptionHandler(Exception::class)
  fun handleGenericException(exception: Exception): ModelAndView {
    Sentry.captureException(exception)
    return ModelAndView("error")
  }
}
