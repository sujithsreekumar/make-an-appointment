package org.echs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Leave {
    private String doctorName;
    private String department;
    private String date;

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
}
