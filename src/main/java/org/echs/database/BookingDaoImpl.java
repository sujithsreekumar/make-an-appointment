package org.echs.database;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
import org.echs.exception.DataNotFoundException;
import org.echs.exception.InvalidInputException;
import org.echs.model.BookingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookingDaoImpl implements BookingDao {
    private static final Logger logger = LoggerFactory.getLogger(BookingDaoImpl.class);

    @Override
    public BookingEntity getBooking(long id) {
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
            statement.setDate(2, Date.valueOf(LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1)));
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
    public boolean hasBooking(String serviceNumber, String patient_name, String department, Date date) {
        logger.info("Checking if user has already made a booking for the day");
        String sql = "SELECT COUNT(1) FROM booking WHERE " +
                "service_number = ? AND patient_name = ? AND department = ? AND date = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, serviceNumber);
            statement.setString(2, patient_name);
            statement.setString(3, department);
            statement.setDate(4, date);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            logger.error("Exception : ", e.getMessage());
            throw new InvalidInputException(e.getMessage());
        } catch (Exception e) {
            logger.error("Exception : ", e.getMessage());
            throw new InvalidInputException(e.getMessage());
        }
    }

    @Override
    public BookingEntity createBooking(BookingEntity booking) {
        logger.info("Going to insert into booking table...");
        String sql = "INSERT INTO booking VALUES (DEFAULT,?,?,?,?,?,?,?) ";
//                "ON CONFLICT ON CONSTRAINT BOOKING_PKEY DO NOTHING ";
//                "ON CONFLICT (service_number, patient_name, department, date) DO NOTHING ";
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
                throw new SQLException("A booking is already confirmed for the patient at the specified department.");
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

//    @Override
//    public void generateReport() throws Exception {
//        Connection connection = Database.getConnection();
//
//        String sql = "SELECT doctor_name, department, service_number, patient_name, date, allotted_time  " +
//                "FROM booking ORDER BY doctor_name";
//
//        JasperReportBuilder report = DynamicReports.report();
//        report
//                .columns(
//                        Columns.column("Doctor Name", "doctor_name", DataTypes.stringType()),
//                        Columns.column("Department", "department", DataTypes.stringType()),
//                        Columns.column("Service Number", "service_number", DataTypes.stringType()),
//                        Columns.column("Patient Name", "patient_name", DataTypes.stringType()),
//                        Columns.column("Date", "date", DataTypes.dateType()),
//                        Columns.column("Allotted Time", "allotted_time", DataTypes.stringType()))
//                .title(
//                        Components.text(" TODAY'S SMS APPOINTMENTS ")
//                                .setHorizontalAlignment(HorizontalAlignment.CENTER))
//                .pageFooter(Components.pageXofY())
//                .setDataSource(sql, connection);
//
//        try {
//            logger.info("Creating PDF file now...");
//            report.toPdf(new FileOutputStream("sms_bookings.pdf"));
//            logger.info("Done creating PDF file");
//        } catch (DRException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    public byte[] generateReportUsingiText() throws Exception {
        String sql = "SELECT doctor_name, department, service_number, patient_name, date, allotted_time  " +
                "FROM booking WHERE date = ? ORDER BY doctor_name, allotted_time";

        ResultSet rs = null;
        Document document = new Document();
        InputStream inputStream = null;
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.BOLD);
        Font bolditalicFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLDITALIC);

        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            LocalDate localDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));
            statement.setDate(1, Date.valueOf(localDate));
            rs = statement.executeQuery();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, byteOutputStream);
            document.open();
            Phrase header = new Phrase("Today's Appointments (" + localDate.toString() + ")", bolditalicFont);
            ColumnText.showTextAligned(pdfWriter.getDirectContent(), Element.ALIGN_CENTER,
                    header,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.top() + 10, 0);
            document.add(new DottedLineSeparator());
            document.add(new Phrase(" "));
            document.add(new Phrase(" "));

            PdfPTable pdfPTable = new PdfPTable(6);
            pdfPTable.setWidths(new float[] {5f, 20f, 20f, 18f, 24f, 13f});

            pdfPTable.addCell(new Phrase("S.No", boldFont));
            pdfPTable.addCell(new Phrase("Doctor Name", boldFont));
            pdfPTable.addCell(new Phrase("Department", boldFont));
            pdfPTable.addCell(new Phrase("Service Number", boldFont));
            pdfPTable.addCell(new Phrase("Patient Name", boldFont));
            pdfPTable.addCell(new Phrase("Allotted Time", boldFont));
            PdfPCell table_cell;
            int i = 0;

            while (rs.next()) {
                i = ++i;
                table_cell = new PdfPCell(new Phrase(String.valueOf(i)));
                pdfPTable.addCell(table_cell);
                String doctor_name = rs.getString("doctor_name");
                table_cell = new PdfPCell(new Phrase(doctor_name));
                pdfPTable.addCell(table_cell);
                String department = rs.getString("department");
                table_cell = new PdfPCell(new Phrase(department));
                pdfPTable.addCell(table_cell);
                String service_number = rs.getString("service_number");
                table_cell = new PdfPCell(new Phrase(service_number));
                pdfPTable.addCell(table_cell);
                String patient_name = rs.getString("patient_name");
                table_cell = new PdfPCell(new Phrase(patient_name));
                pdfPTable.addCell(table_cell);
                Timestamp allotted_time = rs.getTimestamp("allotted_time");
                table_cell = new PdfPCell(new Phrase(allotted_time.toLocalDateTime().toLocalTime().toString()));
                pdfPTable.addCell(table_cell);
            }
            document.add(pdfPTable);
            document.close();
            pdfWriter.close();
            return byteOutputStream.toByteArray();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataNotFoundException(e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
