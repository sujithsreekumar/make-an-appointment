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

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final LocalDateTime DAY_START = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1).atTime(8, 15, 0);
    private final LocalDateTime DAY_END = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1).atTime(10, 15, 0);
    BookingDao bookingDao = new BookingDaoImpl();
    LeaveDao leaveDao = new LeaveDaoImpl();

    public List<BookingEntity> getBookings() throws Exception {
        return bookingDao.getAllBookingsByDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDate());
    }

    public byte[] generateReport() throws Exception {
        return bookingDao.generateReportUsingiText();
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

        if (StringUtils.isNotEmpty(doctorName)) {
            Doctor doc = Doctor.fromDoctorName(doctorName.toUpperCase());
            if (! doc.getDepartment().equals(department)) {
                department = doc.getDepartment();
            }
            List<LocalDateTime> fullSlots = getFullSlots(doc.getConsultationDuration(), doc.getDepartment());
            List<String> doctorNames = doc.getDoctorNames();

            if (!leaveDao.isOnLeave(doctorName, department)) {
                List<LocalDateTime> freeSlots = getFreeSlotsForDoctor(doctorName, fullSlots);

                if (!freeSlots.isEmpty()) {
                    allotTime(booking, preferredTime, freeSlots);
                    booking.setDepartment(department);
                } else {
                    makeBookingWithAnAvailableDoctor(booking, department, preferredTime, doctorNames, fullSlots);
                }
            }
            else {
                makeBookingWithAnAvailableDoctor(booking, department, preferredTime, doctorNames, fullSlots);
            }
        } else {
            Doctor doc = Doctor.fromDepartment(department);
            String deptmnt = department;

            List<String> doctorNames = doc.getDoctorNames();
            List<LocalDateTime> fullSlots = getFullSlots(doc.getConsultationDuration(), department);

            if (doctorNames.stream()
                    .allMatch(docName -> {
                        try {
                            return leaveDao.isOnLeave(docName, deptmnt);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return true;
                        }
                    })) {
                throw new BookingException("No doctor available today for this department");
            }

            makeBookingWithAnAvailableDoctor(booking, department, preferredTime, doctorNames, fullSlots);

        }
        booking.setDate(LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1));
        return bookingDao.createBooking(booking);
    }

    private void makeBookingWithAnAvailableDoctor(BookingEntity booking, String department, LocalDateTime preferredTime, List<String> doctorNames, List<LocalDateTime> fullSlots) throws Exception {
        String doctorName;
        List<LocalDateTime> freeSlots;
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
            freeSlots = getFreeSlotsForDoctor(doctorName, fullSlots);
            if (freeSlots.isEmpty()) {
                doctorsAvailable.remove(doctorName);
            } else {
                allotTime(booking, preferredTime, freeSlots);
                booking.setDoctorName(doctorName); //set the new doctor name in booking entity
                booking.setDepartment(department);
                break;
            }
        }
        if (doctorsAvailable.isEmpty()) {
            /**
             * This is a special case handling wherein a patient who couldn't get a MED_SPLST appointment is tried to be given an appointment with GEN_MED
             */
            if (Doctor.fromDepartment(department).equals(Doctor.MED_SPLST)) {
                //Check if this patient already has a MED_SPLST booking for the dame date
                boolean hasBooking = bookingDao.hasBooking(booking.getServiceNumber(), booking.getPatientName(), booking.getDepartment(), Date.valueOf(LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1)));
                if (!hasBooking) {
                    tryToGetAGenMedBooking(booking, preferredTime);
                } else {
                    throw new BookingException("Patient already has an existing booking with MED_SPLST for the selected date.");
                }
            }
        }
    }

    private void tryToGetAGenMedBooking(BookingEntity booking, LocalDateTime preferredTime) {
        String doctorName;
        List<LocalDateTime> freeSlots;
        Doctor genMedDoctor = Doctor.fromDepartment("GEN_MED");
        List<String> genMedDoctorNames = genMedDoctor.getDoctorNames();
        List<String> availableGenMedDoctors = genMedDoctorNames.stream()
                .filter((String doctor) -> {
                    try {
                        return !leaveDao.isOnLeave(doctor, genMedDoctor.getDepartment());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .collect(toList());
        while (!availableGenMedDoctors.isEmpty()) {
            doctorName = availableGenMedDoctors.get(new Random().nextInt(availableGenMedDoctors.size()));
            freeSlots = getFreeSlotsForDoctor(doctorName, getFullSlots(genMedDoctor.getConsultationDuration(), genMedDoctor.getDepartment()));
            if (freeSlots.isEmpty()) {
                availableGenMedDoctors.remove(doctorName);
            } else {
                allotTime(booking, preferredTime, freeSlots);
                booking.setDoctorName(doctorName); //set the new doctor name in booking entity
                booking.setDepartment(genMedDoctor.getDepartment()); //set GEN_MED as department
                break;
            }
        }

        if (availableGenMedDoctors.isEmpty()) {
            throw new BookingException("No appointments available for this department. We tried GEN_MED as well for you, but sorry, that too didn't have any vacancies today.");
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
                LocalDateTime day_start;
                LocalDateTime day_end;
                if (booking.getDepartment().equals("MED_SPLST")) {
                    day_start = DAY_START.plusMinutes(15);  //this is because MED_SPLST appointments start from 8:30 AM
                    day_end = DAY_END.plusMinutes(15);
                } else {
                    day_start = DAY_START;
                    day_end = DAY_END;
                }
                LocalDateTime tempPreferredTimeObj = preferredTime;
                while (!freeSlots.contains(tempPreferredTimeObj) && !reachedEOD) {
                    tempPreferredTimeObj = tempPreferredTimeObj.plusMinutes(1);
                    if (tempPreferredTimeObj.isEqual(day_end) || tempPreferredTimeObj.isAfter(day_end)) {
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
                        if (tempPreferredTimeObj.isBefore(day_start)) {
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

    private List<LocalDateTime> getFullSlots(long duration, String department) {
        List<LocalDateTime> slots = newArrayList();
        LocalDateTime day_start;
        LocalDateTime day_end;
        if (department.equals("MED_SPLST")) {
            day_start = DAY_START.plusMinutes(15);
            day_end = DAY_END.plusMinutes(15);
        } else {
            day_start = DAY_START;
            day_end = DAY_END;
        }

        LocalDateTime slot = day_start;
        while (slot.isBefore(day_end)) {
            slots.add(slot);
            slot = slot.plusMinutes(duration);
        }
        return slots;
    }

    public BookingEntity update(BookingEntity booking) {
        return bookingDao.updateBooking(booking);
    }

    public BookingEntity remove(long id) {
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
