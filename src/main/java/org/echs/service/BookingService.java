package org.echs.service;

import org.echs.database.DatabaseClass;
import org.echs.exception.DataNotFoundException;
import org.echs.model.Booking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.isNull;

public class BookingService {

    private Map<Long, Booking> bookings = DatabaseClass.getBookings();

    public BookingService() {
        bookings.put(1L, new Booking(1, "Sujith", "Sreekumar",  "9:00 AM", "10:00 AM"));
        bookings.put(2L, new Booking(2, "Divya", "Sreekumar",  "11:00 AM", "11:00 AM"));
    }

    public List<Booking> getBookings() {
        return new ArrayList<Booking>(bookings.values());
    }

    public Booking getBooking(long id) {
        Booking booking = bookings.get(id);
        if (isNull(booking)) {
            throw new DataNotFoundException(format("Booking for id '%s' is not available", id));
        }
        return booking;
    }

    public Booking addBooking(Booking booking) {
        booking.setId(bookings.size() + 1);
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public Booking update(Booking booking) {
        if(booking.getId() <= 0) {
            return null;
        }
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public Booking remove(long id) {
        return bookings.remove(id);
    }

}
