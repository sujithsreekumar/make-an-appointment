package org.echs.database;

import org.echs.model.Doctors;
import org.echs.model.Leave;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface LeaveDao {
    Leave updateLeave(Leave leave);
    boolean isOnLeave(String doctorName, String department) throws Exception;
    List<String> getDoctorsOnLeave(LocalDate localDate) throws SQLException;
    List<Doctors> getDepartmentsAndDoctors() throws Exception;
}
