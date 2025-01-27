package com.yoku.guildmaster.exceptions.handler

import com.yoku.guildmaster.entity.response.ErrorResponse
import com.yoku.guildmaster.exceptions.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.PrintWriter
import java.io.StringWriter

@ControllerAdvice
class GlobalExceptionHandler {


    fun appendStackTraceToCustomError(ex: Throwable, err: ErrorResponse): Unit{
        val stringWriter = StringWriter()
        ex.printStackTrace(PrintWriter(stringWriter))
        err.stackTrace = stringWriter.toString()
    }

    fun handleException(ex: Throwable, status: HttpStatus, includeStackTrace: Boolean = false): ResponseEntity<ErrorResponse>{
        val errorMessage = ex.message ?: "Unknown error occurred"
        val errorResponse = ErrorResponse(status, errorMessage)

        if(includeStackTrace){
            appendStackTraceToCustomError(ex, errorResponse)
        }
        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(OrganisationNotFoundException::class)
    fun handleOrganisationNotFoundException(ex: OrganisationNotFoundException): ResponseEntity<ErrorResponse> {
        return handleException(ex, HttpStatus.NOT_FOUND, true)
    }

    @ExceptionHandler(InvalidArgumentException::class)
    fun handleInvalidArgumentException(ex: InvalidArgumentException): ResponseEntity<ErrorResponse> {
        return handleException(ex, HttpStatus.BAD_REQUEST, true)
    }

    @ExceptionHandler(InvalidOrganisationPermissionException::class)
    fun handleInvalidOrganisationPermissionException(ex: InvalidOrganisationPermissionException): ResponseEntity<ErrorResponse> {
        return handleException(ex, HttpStatus.FORBIDDEN, true)
    }

    @ExceptionHandler(MemberNotFoundException::class)
    fun handleMemberNotFoundException(ex: MemberNotFoundException): ResponseEntity<ErrorResponse> {
        return handleException(ex, HttpStatus.NOT_FOUND, true)
    }

    @ExceptionHandler(InvitationNotFoundException::class)
    fun handleInvitationNotFoundException(ex: InvitationNotFoundException): ResponseEntity<ErrorResponse> {
        return handleException(ex, HttpStatus.NOT_FOUND, true)
    }
}