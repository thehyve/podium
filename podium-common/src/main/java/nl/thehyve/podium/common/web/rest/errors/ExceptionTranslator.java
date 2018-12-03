/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.web.rest.errors;

import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.InvalidRequest;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.exceptions.ServiceNotAvailable;
import nl.thehyve.podium.common.security.annotations.Public;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

import javax.validation.ConstraintViolation;
import java.util.List;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 */
@ControllerAdvice
@Public
public class ExceptionTranslator {

    private final Logger log = LoggerFactory.getLogger(ExceptionTranslator.class);

    @ExceptionHandler(ConcurrencyFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorRepresentation processConcurencyError(ConcurrencyFailureException ex) {
        return new ErrorRepresentation(ErrorConstants.ERR_CONCURRENCY_FAILURE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorRepresentation processValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        return processFieldErrors(fieldErrors);
    }

    @ExceptionHandler(CustomParameterizedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ParameterizedErrorRepresentation processParameterizedValidationError(CustomParameterizedException ex) {
        return ex.getErrorVM();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorRepresentation processAccessDeniedException(AccessDeniedException e) {
        return new ErrorRepresentation(ErrorConstants.ERR_ACCESS_DENIED, e.getMessage());
    }

    @ExceptionHandler(AccessDenied.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorRepresentation processAccessDenied(AccessDenied e) {
        return new ErrorRepresentation(ErrorConstants.ERR_ACCESS_DENIED, e.getMessage());
    }

    @ExceptionHandler(ResourceNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorRepresentation processResourceNotFound(ResourceNotFound e) {
        return new ErrorRepresentation(ErrorConstants.ERR_RESOURCE_NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ServiceNotAvailable.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ResponseBody
    public ErrorRepresentation processServiceNotAvailable(ServiceNotAvailable e) {
        return new ErrorRepresentation(ErrorConstants.ERR_SERVICE_NOT_AVAILABLE, e.getMessage());
    }

    private ErrorRepresentation processFieldErrors(List<FieldError> fieldErrors) {
        ErrorRepresentation dto = new ErrorRepresentation(ErrorConstants.ERR_VALIDATION);

        for (FieldError fieldError : fieldErrors) {
            dto.add(fieldError.getObjectName(), fieldError.getField(), fieldError.getCode());
        }

        return dto;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorRepresentation processMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return new ErrorRepresentation(ErrorConstants.ERR_METHOD_NOT_SUPPORTED, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRepresentation> processRuntimeException(Exception ex) {
        BodyBuilder builder;
        ErrorRepresentation errorVM;
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        log.error("[Internal server error] {} - {}", ex.getMessage(), ex.getClass().toString());
        log.error("[Internal server error] Stack trace", ex);
        if (responseStatus != null) {
            builder = ResponseEntity.status(responseStatus.value());
            errorVM = new ErrorRepresentation("error." + responseStatus.value().value(), responseStatus.reason());
        } else {
            builder = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
            errorVM = new ErrorRepresentation(ErrorConstants.ERR_INTERNAL_SERVER_ERROR, "Internal server error");
        }
        return builder.body(errorVM);
    }

    @ExceptionHandler(InvalidRequest.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorRepresentation handleInvalidRequest(InvalidRequest e) {
        log.error("Invalid request: " + e.getMessage());
        ErrorRepresentation dto = new ErrorRepresentation(ErrorConstants.ERR_VALIDATION);
        for (ConstraintViolation violation : e.getConstraintViolations()) {
            dto.add("request", violation.getPropertyPath().toString(), violation.getMessage());
        }
        return dto;
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorRepresentation> processMultipartException(Exception e){
        log.error("Error with a file upload: " + e.getMessage(), e);
        BodyBuilder builder = ResponseEntity.status(HttpStatus.BAD_REQUEST);
        ErrorRepresentation errorVM = new ErrorRepresentation(ErrorConstants.ERR_INTERNAL_SERVER_ERROR, e.getMessage());
        return builder.body(errorVM);
    }

}
