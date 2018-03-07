package org.echs.exception;

import org.echs.model.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidInputExceptionMapper implements ExceptionMapper<InvalidInputException> {
    @Override
    public Response toResponse(InvalidInputException e) {
        ErrorMessage errorMessage = new ErrorMessage(e.getMessage(), 304,
                "http://echs.gov.in/img/contact/kochi.html");
        return Response.status(Response.Status.OK)
                .entity(errorMessage)
                .build();
    }
}
