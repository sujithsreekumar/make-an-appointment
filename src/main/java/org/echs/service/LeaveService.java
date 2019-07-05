package org.echs.service;

import org.echs.database.LeaveDao;
import org.echs.database.LeaveDaoImpl;
import org.echs.exception.InvalidInputException;
import org.echs.model.Doctor;
import org.echs.model.Doctors;
import org.echs.model.Leave;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class LeaveService {

    LeaveDao leaveDao = new LeaveDaoImpl();

    public List<String> getTodaysLeaves() throws SQLException {
        return leaveDao.getDoctorsOnLeave(LocalDate.now(ZoneId.of("Asia/Kolkata")));
    }

    public void updateLeave(Leave leave) {
        Doctor.fromDepartment(leave.getDepartment());
        Doctor.fromDoctorName(leave.getDoctorName());

        try {
            DateTimeFormatter.ISO_LOCAL_DATE.parse(leave.getDate());
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Date is not a valid format. Here is a sample format : 2018-01-01");
        }
        leaveDao.updateLeave(leave);
    }

    public Doctors getDepartmentsAndDoctors() {
        return leaveDao.getDepartmentsAndDoctors();
    }
}
