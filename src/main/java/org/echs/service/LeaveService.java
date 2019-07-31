package org.echs.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.google.common.base.Strings;
import org.echs.database.LeaveDao;
import org.echs.database.LeaveDaoImpl;
import org.echs.exception.InvalidInputException;
import org.echs.model.Doctor;
import org.echs.model.Doctors;
import org.echs.model.Leave;

public class LeaveService {

    LeaveDao leaveDao = new LeaveDaoImpl();

    public List<String> getTodaysLeaves() throws SQLException {
        return leaveDao.getDoctorsOnLeave(LocalDate.now(ZoneId.of("Asia/Kolkata")));
    }

    public void updateLeave(Leave leave) throws Exception {
        validateLeaveData(leave);
        leaveDao.updateLeave(leave);
    }

    public void updateLeave(List<Leave> leaves) throws Exception {
        leaves.forEach(this::validateLeaveData);
        leaveDao.updateLeave(leaves);
    }

    public List<Doctors> getDepartmentsAndDoctors() throws Exception {
        return leaveDao.getDepartmentsAndDoctors();
    }

    private void validateLeaveData(Leave leave) {
        Doctor.fromDepartment(leave.getDepartment());
        Doctor.fromDoctorName(leave.getDoctorName());
        try {
            DateTimeFormatter.ISO_LOCAL_DATE.parse(leave.getFromDate());
            if (!Strings.isNullOrEmpty(leave.getToDate())) {
                DateTimeFormatter.ISO_LOCAL_DATE.parse(leave.getToDate());
            }
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Date is not a valid format. Here is a sample format : 2018-01-01");
        }
    }
}
