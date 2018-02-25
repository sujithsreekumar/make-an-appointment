package org.echs.database;

import org.echs.model.BookingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDaoImpl implements BookingDao {
    private static final Logger logger = LoggerFactory.getLogger(BookingDaoImpl.class);

    @Override
    public BookingEntity getBooking(long id) throws Exception {
        BookingEntity booking = new BookingEntity();

        String sql = "SELECT * FROM booking where id = " + id;
        try (Connection con = Database.getConnection();
             Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                booking.setId(rs.getInt("id"));
                booking.setPatientName(rs.getString("patient_name"));
                booking.setDoctorName(rs.getString("doctor_name"));
                booking.setPreferredTime(rs.getTimestamp("preferred_time").toLocalDateTime());
                booking.setAllotedTime(rs.getTimestamp("alloted_time").toLocalDateTime());
            }
        } catch (Exception e) {
            //TODO handle exception
        }
        return booking;
    }

    @Override
    public BookingEntity createBooking(BookingEntity booking) throws Exception {
        String sql = "INSERT INTO booking VALUES(DEFAULT,?,?,?,?,?)";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, booking.getPatientName());
            statement.setString(2, booking.getDoctorName());
            statement.setDate(3, Date.valueOf(booking.getDate()));
            statement.setTimestamp(4, Timestamp.valueOf(booking.getPreferredTime()));
            statement.setTimestamp(5, Timestamp.valueOf(booking.getAllotedTime()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            else {
                logger.info("Created {} row ", affectedRows);
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    booking.setId(generatedKeys.getLong(1));
                }
                else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            //TODO handle exception
        }
        return booking;
    }

    @Override
    public BookingEntity updateBooking(BookingEntity booking) {
        String sql = "UPDATE booking SET" +
                "patient_name = ?" +
                "doctor_name = ?" +
                "date = ?" +
                "preferred_time = ?" +
                "alloted_time = ?" +
                " where id = " + booking.getId();
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, booking.getPatientName());
            statement.setString(2, booking.getDoctorName());
            statement.setDate(3, Date.valueOf(booking.getDate()));
            statement.setTimestamp(3, Timestamp.valueOf(booking.getPreferredTime()));
            statement.setTimestamp(4, Timestamp.valueOf(booking.getAllotedTime()));
            statement.executeUpdate();
        } catch (Exception e) {
            //TODO handle exception
        }
        return booking;
    }

    @Override
    public BookingEntity deleteBooking(long id) {
        return null;
    }

    @Override
    public List<BookingEntity> getAllBookings(LocalDate today) throws Exception {
        List<BookingEntity> bookings = new ArrayList<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM booking WHERE date = ?";
        try {
            con = Database.getConnection();
            statement = con.prepareStatement(sql);
            statement.setDate(1, Date.valueOf(today));
            rs = statement.executeQuery();
            while (rs.next()) {
                BookingEntity booking = new BookingEntity();
                booking.setId(rs.getInt("id"));
                booking.setPatientName(rs.getString("patient_name"));
                booking.setDoctorName(rs.getString("doctor_name"));
                booking.setDate(rs.getDate("date").toLocalDate());
                booking.setPreferredTime(rs.getTimestamp("preferred_time").toLocalDateTime());
                booking.setAllotedTime(rs.getTimestamp("alloted_time").toLocalDateTime());
                bookings.add(booking);
            }
        } catch (SQLException e) {
            //TODO handle exception
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
        return bookings;
    }
}
