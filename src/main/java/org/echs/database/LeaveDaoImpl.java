package org.echs.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Strings;
import org.echs.exception.DataNotFoundException;
import org.echs.exception.InvalidInputException;
import org.echs.model.Department;
import org.echs.model.DoctorNames;
import org.echs.model.Doctors;
import org.echs.model.Leave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaveDaoImpl implements LeaveDao {

    private static final Logger logger = LoggerFactory.getLogger(LeaveDaoImpl.class);

    @Override
    public Leave updateLeave(Leave leave) throws Exception {
        String sql = "INSERT INTO leave VALUES(?,?,?,?) ON CONFLICT (doctor_name, department, from_date, to_date) DO NOTHING";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setLeaveAttributes(statement, leave);
            final int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Leave for this doctor has already been marked.");
            } else {
                logger.info("Created {} row ", affectedRows);
            }
        } catch (SQLException e) {
            logger.error("Error : ", e.getMessage());
            throw new InvalidInputException(e.getMessage());
        }
        return leave;
    }

    @Override
    public void updateLeave(List<Leave> leaves) throws Exception {
        String sql = "INSERT INTO leave VALUES(?,?,?,?) ON CONFLICT (doctor_name, department, from_date) " +
                "DO UPDATE set to_date = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            leaves.forEach(leave -> {
                try {
                    setLeaveAttributes(statement, leave);
                    statement.setDate(5, Date.valueOf(Strings.isNullOrEmpty(leave.getToDate()) ?
                            leave.getFromDate() : leave.getToDate()));
                    statement.executeUpdate();
                } catch (SQLException e) {
                    logger.error("SQL Exception occurred ", e.getMessage());
                    throw new InvalidInputException(e.getMessage());
                }
            });
        }
    }

    @Override
    public boolean isOnLeave(String doctorName, String department) throws Exception {
        String sql = "SELECT * FROM leave WHERE doctor_name = ? AND department = ? ";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, doctorName);
            statement.setString(2, department);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    final LocalDate from_date = resultSet.getDate("from_date").toLocalDate();
                    final LocalDate to_date = resultSet.getDate("to_date").toLocalDate();
                    final LocalDate dateToCheck = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1);
                    if (doesDateFallOnStartOrEndOfLeavePeriod(from_date, to_date, dateToCheck) ||
                            isDateInMidOfLeavePeriod(from_date, to_date, dateToCheck))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> getDoctorsOnLeave(LocalDate date) throws SQLException {
        List<String> doctorNames = new ArrayList<>();
        ResultSet rs = null;
        String sql = "SELECT doctor_name FROM leave WHERE date = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            rs = statement.executeQuery();
            while (rs.next()) {
                doctorNames.add(rs.getString("doctor_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataNotFoundException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return doctorNames;
    }

    @Override
    public List<Doctors> getDepartmentsAndDoctors() throws Exception {
        final HashMap<Department, List<DoctorNames>> docMap = new HashMap<>();
        String sql = "SELECT * FROM doctors GROUP BY department, doctor_name";
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                DoctorNames doctorNames = new DoctorNames();
                if (docMap.isEmpty() || !docMap.containsKey(Department.fromDepartmentName(rs.getString("department")))) {
                    List<DoctorNames> docsInDep = new ArrayList<>();
                    final String doctor_name = rs.getString("doctor_name");
                    doctorNames.setName(doctor_name);
                    docsInDep.add(doctorNames);
                    docMap.put(Department.fromDepartmentName(rs.getString("department")), docsInDep);
                } else {
                    final String doctor_name = rs.getString("doctor_name");
                    final Department department = Department.fromDepartmentName(rs.getString("department"));
                    final List<DoctorNames> avlDocs = docMap.get(department);
                    doctorNames.setName(doctor_name);
                    avlDocs.add(doctorNames);
                }
            }
        }
        List<Doctors> doctorsList = new ArrayList<>();
        docMap.entrySet().iterator().forEachRemaining(departmentListEntry -> doctorsList.add(new Doctors(departmentListEntry.getKey(), departmentListEntry.getValue())));
        return doctorsList;
    }

    private void setLeaveAttributes(PreparedStatement statement, Leave leave) throws SQLException {
        statement.setString(1, leave.getDoctorName());
        statement.setString(2, leave.getDepartment());
        statement.setDate(3, Date.valueOf(leave.getFromDate()));
        if (!Strings.isNullOrEmpty(leave.getToDate())) {
            statement.setDate(4, Date.valueOf(leave.getToDate()));
        } else {
            statement.setDate(4, Date.valueOf(leave.getFromDate()));
        }
    }

    private boolean doesDateFallOnStartOrEndOfLeavePeriod(LocalDate from_date, LocalDate to_date, LocalDate dateToCheck) {
        return dateToCheck.isEqual(from_date) || dateToCheck.isEqual(to_date);
    }

    private boolean isDateInMidOfLeavePeriod(LocalDate from_date, LocalDate to_date, LocalDate dateToCheck) {
        return dateToCheck.isAfter(from_date) && dateToCheck.isBefore(to_date);
    }
}
