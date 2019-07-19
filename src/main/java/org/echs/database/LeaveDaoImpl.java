package org.echs.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.echs.exception.DataNotFoundException;
import org.echs.exception.InvalidInputException;
import org.echs.model.Department;
import org.echs.model.DoctorNames;
import org.echs.model.Doctors;
import org.echs.model.Leave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;

public class LeaveDaoImpl implements LeaveDao {

    private static final Logger logger = LoggerFactory.getLogger(LeaveDaoImpl.class);

    @Override
    public Leave updateLeave(Leave leave) {
        String sql = "INSERT INTO leave VALUES(?,?,?) ON CONFLICT (doctor_name, department, date) DO NOTHING";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, leave.getDoctorName());
            statement.setString(2, leave.getDepartment());
            statement.setDate(3, Date.valueOf(leave.getDate()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Leave for this doctor has already been marked.");
            } else {
                logger.info("Created {} row ", affectedRows);
            }
        } catch (SQLException e) {
            logger.error("Error : ", e.getMessage());
            throw new InvalidInputException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error : ", e.getMessage());
            throw new InvalidInputException(e.getMessage());
        }
        return leave;
    }

    @Override
    public boolean isOnLeave(String doctorName, String department) throws Exception {
        boolean isOnleave = false;
        String sql = "SELECT * FROM leave WHERE doctor_name = ? AND department = ? AND date = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, doctorName);
            statement.setString(2, department);
            statement.setDate(3, Date.valueOf(LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1)));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    isOnleave = true;
                }
            }
        }
        return isOnleave;
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
        final HashMap<String, Set<String>> doctorsMap = new HashMap<>();
        String sql = "SELECT * FROM doctors GROUP BY doctor_name, department";
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                doctorsMap.put(rs.getString("department"), rs.getString("doctor_name"));
            }
        }
    }
}
class DoctorsRowMapper implements RowMapper<Doctors> {

    @Override
    public Doctors mapRow(ResultSet rs, int rowNum) throws SQLException {

        final Doctors doctors = new Doctors();
        final DoctorNames doctorNames = new DoctorNames();

        doctors.setDepartment(Department.fromDepartmentName(rs.getString("department")));
        doctorNames.setName(rs.getString("doctor_name"));
        doctors.setDoctorNames(doctorNames);

        return state;
    }

}