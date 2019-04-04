package org.echs.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.format.DateTimeFormatter;

@XmlRootElement
public class Booking {
    private long id;
    private String serviceNumber;
    private String patientName;
    private String doctorName;
    private String department;
    private String date;
    private String preferredTime;
    private String allottedTime;

    public Booking() {
    }

    public Booking(BookingEntity bookingEntity) {
        this.id = bookingEntity.getId();
        this.serviceNumber = bookingEntity.getServiceNumber();
        this.patientName = bookingEntity.getPatientName();
        this.doctorName = bookingEntity.getDoctorName();
        this.department = bookingEntity.getDepartment();
        this.date = bookingEntity.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        this.preferredTime = bookingEntity.getPreferredTime().toLocalTime().toString();
        this.allottedTime = bookingEntity.getAllottedTime().toLocalTime().toString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServiceNumber() {
        return serviceNumber;
    }

    public void setServiceNumber(String serviceNumber) {
        this.serviceNumber = serviceNumber;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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

    public String getAllottedTime() {
        return allottedTime;
    }

    public void setAllottedTime(String allottedTime) {
        this.allottedTime = allottedTime;
    }

    @Override
    public String toString() {
        return  "id=" + id +
                ", serviceNumber='" + serviceNumber + '\'' +
                ", patientName='" + patientName + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", department='" + department + '\'' +
                ", date='" + date + '\'' +
                ", preferredTime='" + preferredTime + '\'' +
                ", allottedTime='" + allottedTime + '\'' ;
    }
}
