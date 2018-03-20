package org.echs.service;

import org.apache.commons.lang3.StringUtils;
import org.echs.database.BookingDao;
import org.echs.database.BookingDaoImpl;
import org.echs.database.LeaveDao;
import org.echs.database.LeaveDaoImpl;
import org.echs.exception.BookingException;
import org.echs.exception.InvalidInputException;
import org.echs.model.BookingEntity;
import org.echs.model.Doctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final LocalDateTime DAY_START = LocalDate.now(ZoneId.of("Asia/Kolkata")).atTime(10, 00, 0);
    private final LocalDateTime DAY_END = LocalDate.now(ZoneId.of("Asia/Kolkata")).atTime(12, 00, 0);
    BookingDao bookingDao = new BookingDaoImpl();
    LeaveDao leaveDao = new LeaveDaoImpl();

    public List<BookingEntity> getBookings() throws Exception {
        return bookingDao.getAllBookingsByDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDate());
    }

    public void generateReport() throws Exception {
        bookingDao.generateReport();
    }

    public BookingEntity getBooking(long id) throws Exception {
        logger.info("Retrieving all bookings for today...");
        return bookingDao.getBooking(id);
    }

    public List<BookingEntity> getBookings(String doctorName) throws Exception {
        logger.info("Retrieving all bookings for Doctor : {}", doctorName);
        return bookingDao.getBookingsForDoctor(doctorName);
    }

    public synchronized BookingEntity addBooking(BookingEntity booking) throws Exception {
        String patientName = booking.getPatientName();
        String doctorName = booking.getDoctorName();
        String department = booking.getDepartment().toUpperCase();

        if (StringUtils.isEmpty(patientName)) {
            throw new InvalidInputException("You must specify Patient name");
        }

        if (StringUtils.isEmpty(doctorName) && StringUtils.isEmpty(department)) {
            throw new InvalidInputException("Please specify the doctor name or at least the department " +
                    "you need consultation for");
        }
        LocalDateTime preferredTime = booking.getPreferredTime();

        if (StringUtils.isNotEmpty(doctorName) && !leaveDao.isOnLeave(doctorName, department)) {
            Doctor doc = Doctor.fromDoctorName(doctorName.toUpperCase());
            List<LocalDateTime> fullSlots = getFullSlots(doc.getConsultationDuration());

            List<String> doctorNames = doc.getDoctorNames();

            List<LocalDateTime> freeSlots = getFreeSlots(doctorName, fullSlots, doctorNames);

            allotTime(booking, preferredTime, freeSlots);
            booking.setDepartment(doc.getDepartment());

        } else {
            Doctor doc = Doctor.fromDepartment(department);
            List<LocalDateTime> fullSlots = getFullSlots(doc.getConsultationDuration());

            List<String> doctorNames = doc.getDoctorNames();

            if (doctorNames.stream()
                    .allMatch(docName -> {
                        try {
                            return leaveDao.isOnLeave(docName, department);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return true;
                        }
                    })) {
                throw new BookingException("No doctor available today from this department");
            }

            doctorName = doctorNames.get(new Random().nextInt(doctorNames.size()));

            while (leaveDao.isOnLeave(doctorName, department)) {
                doctorName = doctorNames.get(new Random().nextInt(doctorNames.size()));
            }

            List<LocalDateTime> freeSlots = getFreeSlots(doctorName, fullSlots, doctorNames);
            if (freeSlots.isEmpty()) {
                throw new BookingException("Bookings are full for the day for this department.");
            }

            allotTime(booking, preferredTime, freeSlots);
        }
        booking.setDepartment(department);
        booking.setDoctorName(doctorName);
        booking.setDate(LocalDate.now(ZoneId.of("Asia/Kolkata")));
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
            if ( freeSlots.contains(preferredTime)) {
                logger.info("Making booking with preferred time...");
                booking.setAllottedTime(booking.getPreferredTime());
            } else {
                logger.info("Making booking with system allotted time...");
//                booking.setAllottedTime(freeSlots.get(0));
                boolean reachedEOD = false;
                boolean reachedSOD = false;
                boolean slotFound = false;
                LocalDateTime tempPreferredTimeObj = preferredTime;
                while (!freeSlots.contains(tempPreferredTimeObj) && !reachedEOD) {
                    tempPreferredTimeObj = tempPreferredTimeObj.plusMinutes(1);
                    if (tempPreferredTimeObj.isEqual(DAY_END) || tempPreferredTimeObj.isAfter(DAY_END)) {
                        reachedEOD = true;
                        break;
                    }
                    if (freeSlots.contains(tempPreferredTimeObj)) {
                        slotFound = true;
                        break;
                    }
                }
                if (reachedEOD && !slotFound) {
                    tempPreferredTimeObj = preferredTime;
                    while (!freeSlots.contains(tempPreferredTimeObj) && !reachedSOD) {
                        tempPreferredTimeObj = tempPreferredTimeObj.minusMinutes(1);
                        if (tempPreferredTimeObj.isEqual(DAY_START) || tempPreferredTimeObj.isBefore(DAY_START)) {
                            reachedSOD = true;
                            break;
                        }
                    }
                }
                if (reachedSOD && reachedEOD) {
                    String message = "Was supposed to find a slot, but couldn't. This is supposed to be a synchronized operation.";
                    logger.error(message);
                    throw new BookingException(message);
                }
                booking.setAllottedTime(tempPreferredTimeObj);
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
