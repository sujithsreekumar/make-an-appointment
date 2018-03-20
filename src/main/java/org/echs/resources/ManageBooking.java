package org.echs.resources;

import org.echs.model.Booking;
import org.echs.model.BookingEntity;
import org.echs.model.ErrorMessage;
import org.echs.model.Holiday;
import org.echs.service.BookingService;
import org.echs.service.HolidayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/bookings")
@Produces(value = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ManageBooking {

    private static final Logger logger = LoggerFactory.getLogger(ManageBooking.class);
    BookingService bookingService = new BookingService();
    HolidayService holidayService = new HolidayService();

    @GET
    public Response getBookings() throws Exception {
        List<BookingEntity> bookingEntities = bookingService.getBookings();
        List<Booking> bookings = mapToBooking(bookingEntities);
        return Response.status(Response.Status.OK).entity(bookings).tag("found").build();
    }


    @GET
    @Path("/{doctorName}")
    public Response getBooking(@PathParam("doctorName") String doctorName) throws Exception {
        List<BookingEntity> bookingEntities = bookingService.getBookings(doctorName);
        List<Booking> bookings = mapToBooking(bookingEntities);
        return Response.status(Response.Status.OK).entity(bookings).tag("found").build();
    }

    @POST
    @Path("/make")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response makeBooking(Booking booking, @Context UriInfo uriInfo) throws Exception {
        if (LocalDateTime.now(ZoneId.of("Asia/Kolkata")).getDayOfWeek().equals(DayOfWeek.SUNDAY) ||
                isPublicHoliday() || isLastDayOfTheMonth()) {
            return Response.status(Response.Status.OK)
                    .entity(new ErrorMessage("No OP on Sundays and public holidays",
                            204, "http://echs.gov.in/img/contact/kochi.html"))
                    .tag("No OP today")
                    .build();
        }
        if ((LocalTime.now(ZoneId.of("Asia/Kolkata")).isAfter(LocalTime.of(6, 0, 0)) &&
                LocalTime.now(ZoneId.of("Asia/Kolkata")).isBefore(LocalTime.of(8, 0,0)))
                || booking.getServiceNumber().equalsIgnoreCase("89102B")) {
            BookingEntity bookingEntity = new BookingEntity(booking);
            logger.info("Calling 'addBooking' service...");
            Booking newBooking = new Booking(bookingService.addBooking(bookingEntity));
            String newId = String.valueOf(newBooking.getId());
            URI uri = uriInfo.getAbsolutePathBuilder().path(newId).build();
            return Response.created(uri)
                    .entity(newBooking)
                    .tag("Confirmed")
                    .build();
        } else {
            return Response.status(Response.Status.OK)
                    .entity(new ErrorMessage("You can make your booking only between 0600 and 0800 Hrs",
                            204, "http://echs.gov.in/img/contact/kochi.html"))
                    .tag("Time window not open")
                    .build();
        }
    }


    @PUT
    @Path("/{bookingId}")
    @Consumes(MediaType.APPLICATION_JSON)
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
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteBooking(@PathParam("bookingId") long id) throws Exception {
        bookingService.remove(id);
        return Response.status(Response.Status.OK).tag("deleted").build();
    }

    private List<Booking> mapToBooking(List<BookingEntity> bookingEntities) {
        return bookingEntities.stream()
                .map(Booking::new)
                .collect(toList());
    }

    private boolean isPublicHoliday() throws Exception {
        return holidayService.getHolidaysList().stream()
                .map(Holiday::getDate)
                .anyMatch(date -> String.valueOf(LocalDate.now(ZoneId.of("Asia/Kolkata"))).equals(date));
    }

    private boolean isLastDayOfTheMonth() {
        return LocalDate.now(ZoneId.of("Asia/Kolkata")).with(TemporalAdjusters.lastDayOfMonth())
                .equals(LocalDate.now(ZoneId.of("Asia/Kolkata")));
    }

}
