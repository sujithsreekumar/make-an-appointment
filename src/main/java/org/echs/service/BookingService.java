package org.echs.service;

import org.echs.database.BookingDao;
import org.echs.database.BookingDaoImpl;
import org.echs.model.BookingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class BookingService {
    private final LocalDateTime LUNCH_START = LocalDateTime.now().withHour(12).withMinute(59);
    private final LocalDateTime LUNCH_END = LocalDateTime.now().withHour(13).withMinute(59);
    private final LocalDateTime DAY_START = LocalDateTime.now().withHour(8).withMinute(59);
    private final LocalDateTime DAY_END = LocalDateTime.now().withHour(15).withMinute(39);
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    BookingDao bookingDao = new BookingDaoImpl();

    public List<BookingEntity> getBookings() throws Exception {
        return bookingDao.getAllBookings(LocalDateTime.now().toLocalDate());
    }

    public BookingEntity getBooking(long id) throws Exception {
        logger.info("Retrieving all bookings for today...");
        return bookingDao.getBooking(id);
    }

    public BookingEntity addBooking(BookingEntity booking) throws Exception {
        List<LocalDateTime> allotedSlots = getAllotedSlots();

        if (!allotedSlots.isEmpty()) {
            if (!allotedSlots.contains(booking.getPreferredTime()) && isValidWorkingHour(booking.getPreferredTime())) {
                booking.setAllotedTime(booking.getPreferredTime());
                logger.info("Making booking with preferred time...");
                return bookingDao.createBooking(booking);
            }
            else {
                LocalDateTime preferredTime = booking.getPreferredTime();
                while (allotedSlots.contains(preferredTime)) {
                    preferredTime = tryNextSlot(preferredTime);
                }
                booking.setAllotedTime(preferredTime);
                logger.info("Making booking with next available time...");
                return bookingDao.createBooking(booking);
            }
        }
        else {
          booking.setAllotedTime(booking.getPreferredTime());
            logger.info("Making booking with next available time...");
          return bookingDao.createBooking(booking);
        }
    }

    public BookingEntity update(BookingEntity booking) throws Exception {
        return bookingDao.updateBooking(booking);
    }

    public BookingEntity remove(long id) throws Exception {
        return bookingDao.deleteBooking(id);
    }

    private List<LocalDateTime> getAllotedSlots() throws Exception {
        return this.getBookings().stream()
                .map(booking -> booking.getAllotedTime())
                .collect(toList());
    }

    private boolean isValidWorkingHour(LocalDateTime localDateTime) {
        return localDateTime.isAfter(DAY_START) && localDateTime.isBefore(DAY_END) &&
                !(localDateTime.isAfter(LUNCH_START) && localDateTime.isBefore(LUNCH_END));
    }

    private LocalDateTime tryNextSlot(LocalDateTime localDateTime) {
        LocalDateTime dateTime = localDateTime.plusMinutes(20L);
        while (!isValidWorkingHour(dateTime)) {
            dateTime = tryNextSlot(dateTime);
        }
        return dateTime;
    }
}
