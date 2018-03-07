package org.echs.database;

import org.echs.model.Holiday;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HolidayDaoImpl implements HolidayDao {

    private static final Logger logger = LoggerFactory.getLogger(BookingDaoImpl.class);

    @Override
    public List<Holiday> getHolidaysForTheYear(String year) throws SQLException {
        List<Holiday> holidays = new ArrayList<>();
        ResultSet rs = null;

        String sql = "SELECT * FROM holiday WHERE year = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, year);
            rs = statement.executeQuery();
            while (rs.next()) {
                Holiday holiday = new Holiday();
                holiday.setsNo(rs.getInt("s_no"));
                holiday.setOccasion(rs.getString("occasion"));
                holiday.setDate(rs.getString("date"));
                holiday.setDay(rs.getString("day"));
                holiday.setYear(rs.getString("year"));
                holidays.add(holiday);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rs.close();
        }
        return holidays;
    }

    @Override
    public void createHolidayEntry(Holiday holiday) {

        String sql = "INSERT INTO holiday VALUES(?,?,?,?,?)";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, holiday.getsNo());
            statement.setString(2, holiday.getOccasion());
            statement.setDate(3, Date.valueOf(holiday.getDate()));
            statement.setString(4, holiday.getDay());
            statement.setString(5, holiday.getYear());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating holiday entry failed, no rows affected.");
            } else {
                logger.info("Created {} row ", affectedRows);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
