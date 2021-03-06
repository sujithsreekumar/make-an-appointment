package org.echs.database;

import org.echs.model.BookingEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public interface BookingDao {

    BookingEntity getBooking(long id) throws Exception;

    List<BookingEntity> getAllBookingsByDate(LocalDate date) throws Exception;

    List<BookingEntity> getBookingsForDoctor(String doctorName) throws Exception;

    boolean hasBooking(String serviceNumber, String patient_name, String department, Date date);

    BookingEntity createBooking(BookingEntity bookingEntity) throws Exception;

    BookingEntity updateBooking(BookingEntity bookingEntity);

    BookingEntity deleteBooking(long id);

//    void generateReport() throws Exception;

    byte[] generateReportUsingiText() throws Exception;

}
