package org.echs.database;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.exception.DRException;
import org.echs.exception.DataNotFoundException;
import org.echs.exception.InvalidInputException;
import org.echs.model.BookingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
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
                booking.setAllottedTime(rs.getTimestamp("allotted_time").toLocalDateTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataNotFoundException(e.getMessage());
        }
        return booking;
    }

    @Override
    public List<BookingEntity> getBookingsForDoctor(String doctorName) throws Exception {
        List<BookingEntity> bookings = new ArrayList<>();
        ResultSet rs = null;

        String sql = "SELECT * FROM booking WHERE doctor_name = ? AND date = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, doctorName);
            statement.setDate(2, Date.valueOf(LocalDate.now(ZoneId.of("Asia/Kolkata"))));
            rs = statement.executeQuery();
            while (rs.next()) {
                BookingEntity booking = new BookingEntity();
                booking.setId(rs.getInt("id"));
                booking.setServiceNumber(rs.getString("service_number"));
                booking.setPatientName(rs.getString("patient_name"));
                booking.setDoctorName(rs.getString("doctor_name"));
                booking.setDepartment(rs.getString("department"));
                booking.setDate(rs.getDate("date").toLocalDate());
                booking.setPreferredTime(rs.getTimestamp("preferred_time").toLocalDateTime());
                booking.setAllottedTime(rs.getTimestamp("allotted_time").toLocalDateTime());
                bookings.add(booking);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataNotFoundException(e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return bookings;
    }

    @Override
    public List<BookingEntity> getAllBookingsByDate(LocalDate today) throws Exception {
        List<BookingEntity> bookings = new ArrayList<>();
        ResultSet rs = null;

        String sql = "SELECT * FROM booking WHERE date = ? ORDER BY department, doctor_name, allotted_time";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(today));
            rs = statement.executeQuery();
            while (rs.next()) {
                BookingEntity booking = new BookingEntity();
                booking.setId(rs.getInt("id"));
                booking.setServiceNumber(rs.getString("service_number"));
                booking.setPatientName(rs.getString("patient_name"));
                booking.setDoctorName(rs.getString("doctor_name"));
                booking.setDepartment(rs.getString("department"));
                booking.setDate(rs.getDate("date").toLocalDate());
                booking.setPreferredTime(rs.getTimestamp("preferred_time").toLocalDateTime());
                booking.setAllottedTime(rs.getTimestamp("allotted_time").toLocalDateTime());
                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataNotFoundException(e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return bookings;
    }

    @Override
    public BookingEntity createBooking(BookingEntity booking) {
        logger.info("Going to insert into booking table...");
        String sql = "INSERT INTO booking VALUES(DEFAULT,?,?,?,?,?,?,?)";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, booking.getServiceNumber());
            statement.setString(2, booking.getPatientName());
            statement.setString(3, booking.getDoctorName());
            statement.setString(4, booking.getDepartment().toUpperCase());
            statement.setDate(5, Date.valueOf(booking.getDate()));
            statement.setTimestamp(6, Timestamp.valueOf(booking.getPreferredTime()));
            statement.setTimestamp(7, Timestamp.valueOf(booking.getAllottedTime()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating booking failed, no rows affected.");
            } else {
                logger.info("Created {} row ", affectedRows);
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    booking.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating booking failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Error : ", e.getMessage());
            throw new InvalidInputException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error : ", e.getMessage());
            throw new InvalidInputException(e.getMessage());
        }
        return booking;
    }

    @Override
    public BookingEntity updateBooking(BookingEntity booking) {
        String sql = "UPDATE booking SET" +
                "service_number = ?" +
                "patient_name = ?" +
                "doctor_name = ?" +
                "department = ?" +
                "date = ?" +
                "preferred_time = ?" +
                "allotted_time = ?" +
                " where id = " + booking.getId();
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, booking.getServiceNumber());
            statement.setString(2, booking.getPatientName());
            statement.setString(3, booking.getDoctorName());
            statement.setString(4, booking.getDepartment());
            statement.setDate(5, Date.valueOf(booking.getDate()));
            statement.setTimestamp(6, Timestamp.valueOf(booking.getPreferredTime()));
            statement.setTimestamp(7, Timestamp.valueOf(booking.getAllottedTime()));
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidInputException(e.getMessage());
        }
        return booking;
    }

    @Override
    public BookingEntity deleteBooking(long id) {
        return null;
    }

    @Override
    public void generateReport() throws Exception {
        Connection connection = Database.getConnection();

        String sql = "SELECT doctor_name, department, service_number, patient_name, date, allotted_time  " +
                "FROM booking ORDER BY doctor_name";

        JasperReportBuilder report = DynamicReports.report();
        report
                .columns(
                        Columns.column("Doctor Name", "doctor_name", DataTypes.stringType()),
                        Columns.column("Department", "department", DataTypes.stringType()),
                        Columns.column("Service Number", "service_number", DataTypes.stringType()),
                        Columns.column("Patient Name", "patient_name", DataTypes.stringType()),
                        Columns.column("Date", "date", DataTypes.dateType()),
                        Columns.column("Allotted Time", "allotted_time", DataTypes.stringType()))
                .title(
                        Components.text(" TODAY'S SMS APPOINTMENTS ")
                                .setHorizontalAlignment(HorizontalAlignment.CENTER))
                .pageFooter(Components.pageXofY())
                .setDataSource(sql, connection);

        try {
            logger.info("Creating PDF file now...");
            report.toPdf(new FileOutputStream("sms_bookings.pdf"));
            logger.info("Done creating PDF file");
        } catch (DRException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
