package org.echs.database;

import org.echs.exception.DataNotFoundException;
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
import java.util.List;

public class LeaveDaoImpl implements LeaveDao {

    private static final Logger logger = LoggerFactory.getLogger(LeaveDaoImpl.class);

    @Override
    public Leave updateLeave(Leave leave) {
        String sql = "INSERT INTO leave VALUES(?,?,?)";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, leave.getDoctorName());
            statement.setString(2, leave.getDepartment());
            statement.setDate(3, Date.valueOf(leave.getDate()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating Leave entry failed, no rows affected.");
            } else {
                logger.info("Created {} row ", affectedRows);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
            statement.setDate(3, Date.valueOf(LocalDate.now(ZoneId.of("Asia/Kolkata"))));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    isOnleave = true;
                }
            }
        }
        return  isOnleave;
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
}