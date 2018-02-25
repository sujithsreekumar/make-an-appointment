package org.echs.resources;

import org.echs.model.Booking;
import org.echs.model.BookingEntity;
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
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static java.time.LocalTime.now;
import static java.util.stream.Collectors.toList;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(value = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ManageBooking {

    BookingService bookingService = new BookingService();

    @GET
    public Response getBookings() throws Exception {
        List<BookingEntity> bookingEntities = bookingService.getBookings();
        List<Booking> bookings = bookingEntities.stream()
                .map(Booking::new)
                .collect(toList());
        return Response.status(Response.Status.OK).entity(bookings).tag("found").build();
    }

    @GET
    @Path("/{bookingId}")
    public Response getBooking(@PathParam("bookingId") long bookingId) throws Exception {
        BookingEntity booking = bookingService.getBooking(bookingId);
        return Response.status(Response.Status.OK).entity(booking).tag("found").build();
    }

    @POST
    @Path("/make")
    public Response makeBooking(Booking booking, @Context UriInfo uriInfo) throws Exception {
        if (LocalDateTime.now().atZone(ZoneId.systemDefault()).getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity("Sorry.. No OPs on Sundays and public holidays")
                    .tag("No OP today")
                    .build();
        }
        if (now().isAfter(LocalTime.of(9, 0)) &&
                now().isBefore(LocalTime.of(23, 59))) {
            BookingEntity bookingEntity = new BookingEntity(booking);
            Booking newBooking = new Booking(bookingService.addBooking(bookingEntity));
            String newId = String.valueOf(newBooking.getId());
            URI uri = uriInfo.getAbsolutePathBuilder().path(newId).build();
            return Response.created(uri)
                    .entity(newBooking)
                    .tag("Confirmed")
                    .build();

        } else {
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity("You can make your bookings only between 6 AM and 9AM")
                    .tag("Time window not open")
                    .build();
        }
    }

    @PUT
    @Path("/{bookingId}")
    public Response updateBooking(@PathParam("bookingId") long id, Booking booking) throws Exception {
        booking.setId(id);
        bookingService.update(new BookingEntity(booking));
        return Response.status(Response.Status.OK)
                .entity(booking)
                .tag("Updated")
                .build();
    }

    @DELETE
    @Path("/{bookingId}")
    public Response deleteBooking(@PathParam("bookingId") long id) throws Exception {
        bookingService.remove(id);
        return Response.status(Response.Status.OK).tag("deleted").build();
    }

}
