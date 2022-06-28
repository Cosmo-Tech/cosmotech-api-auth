// Copyright (c) Cosmo Tech.
// Licensed under the MIT license.
package com.cosmotech.api.auth.exceptions

import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.Problem
import org.zalando.problem.Status
import org.zalando.problem.spring.web.advice.ProblemHandling

@ControllerAdvice
open class CsmAuthExceptionHandling : ProblemHandling {

  override fun isCausalChainsEnabled() = true

  @ExceptionHandler
  fun handleInsufficientAuthenticationException(
      exception: InsufficientAuthenticationException,
      request: NativeWebRequest
  ): ResponseEntity<Problem> = create(Status.UNAUTHORIZED, exception, request)
}
