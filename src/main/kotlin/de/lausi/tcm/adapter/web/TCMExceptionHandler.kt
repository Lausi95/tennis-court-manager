package de.lausi.tcm.adapter.web

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView

@ControllerAdvice
class TCMExceptionHandler {

  @ExceptionHandler(Exception::class)
  fun handleGenericException(exception: Exception): ModelAndView {
    // TODO: alternate exception reporting
    return ModelAndView("error")
  }
}
