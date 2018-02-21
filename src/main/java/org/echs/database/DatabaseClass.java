package org.echs.database;

import org.echs.model.Booking;

import java.util.HashMap;
import java.util.Map;

public class DatabaseClass {

    private static Map<Long, Booking> bookings= new HashMap<>();

    public static Map<Long, Booking> getBookings() {
        return bookings;
    }
}
