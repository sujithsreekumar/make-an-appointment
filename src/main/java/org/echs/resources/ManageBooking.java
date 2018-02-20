package org.echs.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalTime;

import static java.time.LocalTime.now;

@Path("/bookings")
public class ManageBooking {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getBookings() {
        return "Today's Bookings";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/make")
    public String makeBooking() {
        if (now().isAfter(LocalTime.of(9,0)) ||
                now().isBefore(LocalTime.of(6,0))) {
            return "You can make your bookings only between 6 AM and 9AM";
        }
        else {
            return "Your booking is confirmed";
        }
    }
}
