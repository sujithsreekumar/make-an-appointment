package org.echs.service;

import org.echs.database.LeaveDao;
import org.echs.database.LeaveDaoImpl;
import org.echs.model.Leave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class LeaveService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    LeaveDao leaveDao = new LeaveDaoImpl();

    public List<String> getTodaysLeaves() throws SQLException {
        return leaveDao.getDoctorsOnLeave(LocalDate.now(ZoneId.of("Asia/Kolkata")));
    }

    public boolean isOnLeave(String doctorName, String department) throws Exception {
        return leaveDao.isOnLeave(doctorName, department);
    }

    public void updateLeave(Leave leave) {
        leaveDao.updateLeave(leave);
    }
}
