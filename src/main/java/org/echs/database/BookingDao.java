package org.echs.database;

import org.echs.model.BookingEntity;

import java.time.LocalDate;
import java.util.List;

public interface BookingDao {
    BookingEntity getBooking(long id) throws Exception;

    BookingEntity createBooking(BookingEntity customer) throws Exception;

    BookingEntity updateBooking(BookingEntity customer);

    BookingEntity deleteBooking(long id);

    List<BookingEntity> getAllBookings(LocalDate date) throws Exception;
}
