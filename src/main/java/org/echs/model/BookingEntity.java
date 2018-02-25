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
    private LocalDate date;
    private LocalDateTime preferredTime;
    private LocalDateTime allotedTime;

    public BookingEntity() {
    }

    public BookingEntity(Booking booking) {
        this.id = booking.getId();
        this.patientName = booking.getPatientName();
        this.doctorName = booking.getDoctorName();
        this.date = LocalDate.now();
        if (isNotEmpty(booking.getPreferredTime())) {
            this.preferredTime = LocalDateTime.of(LocalDate.now(),
                    LocalTime.parse(booking.getPreferredTime(), DateTimeFormatter.ISO_LOCAL_TIME));
        } else {
            this.preferredTime = LocalDate.now().atTime(9,0,0);
        }
        this.allotedTime = isEmpty(booking.getAllotedTime()) ? null : LocalDateTime.of(LocalDate.now(),
                LocalTime.parse(booking.getAllotedTime(), DateTimeFormatter.ISO_LOCAL_TIME));
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

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public LocalDateTime getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(LocalDateTime preferredTime) {
        this.preferredTime = preferredTime;
    }

    public LocalDateTime getAllotedTime() {
        return allotedTime;
    }

    public void setAllotedTime(LocalDateTime allotedTime) {
        this.allotedTime = allotedTime;
    }
}
