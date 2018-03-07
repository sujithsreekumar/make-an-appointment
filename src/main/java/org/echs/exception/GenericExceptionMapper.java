package org.echs.exception;

import org.echs.model.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable throwable) {
        ErrorMessage errorMessage = new ErrorMessage(throwable.getMessage(), 500,
                "http://echs.gov.in/img/contact/kochi.html");
        return Response.status(Response.Status.OK)
                .entity(errorMessage)
                .build();
    }
}
