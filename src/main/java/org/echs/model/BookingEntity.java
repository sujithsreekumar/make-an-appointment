package org.echs.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class BookingEntity {
    private long id;
    private String patientName;
    private String doctorName;
    private String department;
    private LocalDate date;
    private LocalDateTime preferredTime;
    private LocalDateTime allottedTime;

    public BookingEntity() {
    }

    public BookingEntity(Booking booking) {
        this.id = booking.getId();
        this.patientName = booking.getPatientName();
        this.doctorName = booking.getDoctorName();
        this.department = booking.getDepartment();
        this.date = LocalDate.now();
        if (isNotEmpty(booking.getPreferredTime())) {
            this.preferredTime = LocalDateTime.of(LocalDate.now(),
                    LocalTime.parse(booking.getPreferredTime(), DateTimeFormatter.ISO_LOCAL_TIME));
        } else {
            this.preferredTime = LocalDate.now().atTime(9,30,0);
        }
        this.allottedTime = isEmpty(booking.getAllottedTime()) ? null : LocalDateTime.of(LocalDate.now(),
                LocalTime.parse(booking.getAllottedTime(), DateTimeFormatter.ISO_LOCAL_TIME));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDepartment() { return department; }

    public void setDepartment(String department) { this.department = department; }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public LocalDateTime getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(LocalDateTime preferredTime) {
        this.preferredTime = preferredTime;
    }

    public LocalDateTime getAllottedTime() {
        return allottedTime;
    }

    public void setAllottedTime(LocalDateTime allottedTime) {
        this.allottedTime = allottedTime;
    }
}
