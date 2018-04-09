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
            List<LocalDateTime> freeSlots = getFreeSlotsForDoctor(doctorName, fullSlots);

            if (!freeSlots.isEmpty()) {
                allotTime(booking, preferredTime, freeSlots);
                booking.setDepartment(doc.getDepartment());
            } else {
                makeBookingWithAnAvailableDoctor(booking, department, preferredTime, doc.getDoctorNames(), fullSlots);
            }

        } else {
            Doctor doc = Doctor.fromDepartment(department);

            List<String> doctorNames = doc.getDoctorNames();
            List<LocalDateTime> fullSlots = getFullSlots(doc.getConsultationDuration());

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

            makeBookingWithAnAvailableDoctor(booking, department, preferredTime, doctorNames, fullSlots);

        }
        booking.setDepartment(department);
        booking.setDate(LocalDate.now(ZoneId.of("Asia/Kolkata")));
        return bookingDao.createBooking(booking);
    }

    private void makeBookingWithAnAvailableDoctor(BookingEntity booking, String department, LocalDateTime preferredTime, List<String> doctorNames, List<LocalDateTime> fullSlots) {
        String doctorName;
        List<String> doctorsAvailable = doctorNames.stream()
                .filter((String doctor) -> {
                    try {
                        return !leaveDao.isOnLeave(doctor, department);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .collect(toList());

        while (!doctorsAvailable.isEmpty()) {
            doctorName = doctorsAvailable.get(new Random().nextInt(doctorsAvailable.size()));
            List<LocalDateTime> freeSlots = getFreeSlotsForDoctor(doctorName, fullSlots);
            if (freeSlots.isEmpty()) {
                doctorsAvailable.remove(doctorName);
            } else {
                allotTime(booking, preferredTime, freeSlots);
                booking.setDoctorName(doctorName); //set the new doctor name in booking entity
                break;
            }
        }
        if (doctorsAvailable.isEmpty()) {
            throw new BookingException("No appointments available for this department.");
        }
    }

    private List<LocalDateTime> getFreeSlotsForDoctor(String doctorName, List<LocalDateTime> fullSlots) {
        List<LocalDateTime> allottedSlots = getAllottedSlotsForDoctor(doctorName);

        return fullSlots.stream()
                .filter(slot -> !allottedSlots.contains(slot))
                .collect(toList());
    }

    private void allotTime(BookingEntity booking, LocalDateTime preferredTime, List<LocalDateTime> freeSlots) {
        if (null != preferredTime) {
            if (freeSlots.contains(preferredTime)) {
                logger.info("Making booking with preferred time...");
                booking.setAllottedTime(booking.getPreferredTime());
            } else {
                logger.info("Making booking with system allotted time...");
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
                        if (tempPreferredTimeObj.isBefore(DAY_START)) {
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
