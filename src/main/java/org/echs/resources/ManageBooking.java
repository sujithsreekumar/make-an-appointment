package org.echs.resources;

import org.echs.model.Booking;
import org.echs.service.BookingService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.LocalTime;
import java.util.List;

import static java.time.LocalTime.now;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(value = { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class ManageBooking {

    BookingService bookingService = new BookingService();

    @GET
    public List<Booking> getBookings() {
        return bookingService.getBookings();
    }

    @GET
    @Path("/{bookingId}")
    public Booking getBooking(@PathParam("bookingId") long bookingId) {
        return bookingService.getBooking(bookingId);
    }

    @POST
    @Path("/make")
    public Response makeBooking(Booking booking, @Context UriInfo uriInfo) {
        if (now().isAfter(LocalTime.of(9,0)) ||
                now().isBefore(LocalTime.of(6,0))) {
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity("You can make your bookings only between 6 AM and 9AM")
                    .tag("Time window not open")
                    .build();
        }
        else {
            Booking newBooking = bookingService.addBooking(booking);
            String newId = String.valueOf(newBooking.getId());
            URI uri = uriInfo.getAbsolutePathBuilder().path(newId).build();
            return Response.created(uri)
                    .entity(newBooking)
                    .tag("Confirmed")
                    .build();
        }
    }

    @PUT
    @Path("/{bookingId}")
    public Booking updateBooking(@PathParam("bookingId") long id,  Booking booking) {
        booking.setId(id);
        return bookingService.update(booking);
    }

    @DELETE
    @Path("/{bookingId}")
    public void deleteBooking(@PathParam("bookingId") long id) {
        bookingService.remove(id);
    }

}
