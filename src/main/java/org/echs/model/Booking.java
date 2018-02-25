package org.echs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Booking {
    private long id;
    private String patientName;
    private String doctorName;
    private String date;
    private String preferredTime;
    private String allotedTime;

    public Booking() {
    }

    public Booking(BookingEntity bookingEntity) {
        this.id = bookingEntity.getId();
        this.patientName = bookingEntity.getPatientName();
        this.doctorName = bookingEntity.getDoctorName();
        this.date = bookingEntity.getDate().toString();
        this.preferredTime = bookingEntity.getPreferredTime().toLocalTime().toString();
        this.allotedTime = bookingEntity.getAllotedTime().toLocalTime().toString();
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }

    public String getAllotedTime() {
        return allotedTime;
    }

    public void setAllotedTime(String allotedTime) {
        this.allotedTime = allotedTime;
    }
}
