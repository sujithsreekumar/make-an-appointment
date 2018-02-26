package org.echs.service;

import org.apache.commons.lang3.StringUtils;
import org.echs.database.BookingDao;
import org.echs.database.BookingDaoImpl;
import org.echs.exception.InvalidInputException;
import org.echs.model.BookingEntity;
import org.echs.model.Doctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final LocalDateTime DAY_START = LocalDate.now().atTime(9, 30, 0);
    private final LocalDateTime DAY_END = LocalDate.now().atTime(13, 30, 0);
    BookingDao bookingDao = new BookingDaoImpl();

    public List<BookingEntity> getBookings() throws Exception {
        return bookingDao.getAllBookingsByDate(LocalDateTime.now().toLocalDate());
    }

    public BookingEntity getBooking(long id) throws Exception {
        logger.info("Retrieving all bookings for today...");
        return bookingDao.getBooking(id);
    }

    public List<BookingEntity> getBookings(String doctorName) throws Exception {
        logger.info("Retrieving all bookings for Doctor : {}", doctorName);
        return bookingDao.getBookingsForDoctor(doctorName);
    }

    public BookingEntity addBooking(BookingEntity booking) throws Exception {
        String patientName = booking.getPatientName();
        String doctorName = booking.getDoctorName();
        String department = booking.getDepartment();

        if (patientName.isEmpty()) {
            throw new InvalidInputException("You must specify Patient name");
        }

        if (StringUtils.isEmpty(doctorName) && StringUtils.isEmpty(department)) {
            throw new InvalidInputException("Please specify the doctor name or at least the department " +
                    "you need consultation for");
        }
        LocalDateTime preferredTime = booking.getPreferredTime();

        if (StringUtils.isNotEmpty(doctorName)) {
            Doctor doc = Doctor.fromDoctorName(doctorName);
            List<LocalDateTime> fullSlots = getFullSlots(doc.getConsultationDuration());

            List<String> doctorNames = doc.getDoctorNames();

            List<LocalDateTime> freeSlots = getFreeSlots(doctorName, fullSlots, doctorNames);

            allotTime(booking, preferredTime, freeSlots);
            booking.setDepartment(doc.getDepartment());

        } else {
            Doctor doc = Doctor.fromDepartment(department);
            List<LocalDateTime> fullSlots = getFullSlots(doc.getConsultationDuration());

            List<String> doctorNames = doc.getDoctorNames();
            doctorName = doctorNames.get(new Random().nextInt(doctorNames.size()));

            List<LocalDateTime> freeSlots = getFreeSlots(doctorName, fullSlots, doctorNames);

            allotTime(booking, preferredTime, freeSlots);
        }
        booking.setDoctorName(doctorName);
        return bookingDao.createBooking(booking);
    }

    private List<LocalDateTime> getFreeSlots(String doctorName, List<LocalDateTime> fullSlots, List<String> doctorNames) {
        List<LocalDateTime> allottedSlots = getAllottedSlotsForDoctor(doctorName);

        while (String.valueOf(allottedSlots.size()).equals(String.valueOf(fullSlots.size()))) {
            doctorName = doctorNames.get(new Random().nextInt(doctorNames.size()));
            allottedSlots = getAllottedSlotsForDoctor(doctorName);
        }

        List<LocalDateTime> allottedSlotsForRandomDoctor = allottedSlots;

        return fullSlots.stream()
                .filter(slot -> !allottedSlotsForRandomDoctor.contains(slot))
                .collect(toList());
    }

    private void allotTime(BookingEntity booking, LocalDateTime preferredTime, List<LocalDateTime> freeSlots) {
        if (null != preferredTime) {
            if (freeSlots.contains(preferredTime)) {
                logger.info("Making booking with preferred time...");
                booking.setAllottedTime(booking.getPreferredTime());
            } else {
                logger.info("Making booking with system allotted time...");
                booking.setAllottedTime(freeSlots.get(0));
            }
        } else {
            logger.info("Making booking with system allotted time...");
            booking.setAllottedTime(freeSlots.get(0));
        }
    }

    private List<LocalDateTime> getFullSlots(long duration) {
        List<LocalDateTime> slots = newArrayList();
        LocalDateTime slot = DAY_START;
        while (slot.isBefore(DAY_END)) {
            slots.add(slot);
            slot = slot.plusMinutes(duration);
        }
        return slots;
    }

    public BookingEntity update(BookingEntity booking) throws Exception {
        return bookingDao.updateBooking(booking);
    }

    public BookingEntity remove(long id) throws Exception {
        return bookingDao.deleteBooking(id);
    }

    private List<LocalDateTime> getAllottedSlotsForDoctor(String doctorName) {
        List<LocalDateTime> slotsForDoctor = newArrayList();
        try {
            slotsForDoctor = this.getBookings(doctorName).stream()
                    .map(booking -> booking.getAllottedTime())
                    .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slotsForDoctor;
    }
}
