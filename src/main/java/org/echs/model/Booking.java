package org.echs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Booking {
    private long id;
    private String patientName;
    private String doctorName;
    private String preferredTime;
    private String allotedTime;

    public Booking() {
    }

    public Booking(long id, String patientName, String doctorName, String preferredTime, String allotedTime) {
        this.id = id;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.preferredTime = preferredTime;
        this.allotedTime = allotedTime;
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
