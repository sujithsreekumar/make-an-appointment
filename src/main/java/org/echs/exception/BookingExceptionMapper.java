package org.echs.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.echs.model.ErrorMessage;

@Provider
public class BookingExceptionMapper implements ExceptionMapper<BookingException> {
    @Override
    public Response toResponse(BookingException e) {
        ErrorMessage errorMessage = new ErrorMessage(e.getMessage(), 304,
                "http://echs.gov.in/img/contact/kochi.html");
        return Response.status(Response.Status.OK).entity(errorMessage).build();
    }
}
