package org.echs.database;

import org.echs.model.Booking;

import javax.ws.rs.core.Response;

public interface BookingDao {
    public Response getBooking(long id);
    public Response createBooking(Booking customer);
    public Response updateBooking(Booking customer);
    public Response deleteBooking(int id);
    public Response getAllBookings();
}
