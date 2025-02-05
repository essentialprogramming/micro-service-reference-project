package com.monitoring.exception;

import com.util.web.JsonResponse;

import javax.annotation.Priority;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;

@Provider
@Priority(1)
public class BeanValidationExceptionHandler implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintValidations = e.getConstraintViolations();
        ConstraintViolation<?> constraintValidation = constraintValidations.iterator().next();

        final JsonResponse jsonResponse = new JsonResponse()
                .with("Message", constraintValidation.getMessage())
                .with("Status", Response.Status.BAD_REQUEST.getStatusCode() + " (" + Response.Status.BAD_REQUEST.getReasonPhrase() + ")")
                .done();
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(jsonResponse)
                .build();
    }
}
