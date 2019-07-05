package org.echs.model;

import java.util.List;
import java.util.Objects;

public class Doctors {
    private Department department;
    private List<DoctorNames> doctorNames;

    public Doctors(Department department, List<DoctorNames> doctorNames) {
        this.department = department;
        this.doctorNames = doctorNames;
    }

    public Doctors() {
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<DoctorNames> getDoctorNames() {
        return doctorNames;
    }

    public void setDoctorNames(List<DoctorNames> doctorNames) {
        this.doctorNames = doctorNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doctors)) return false;
        Doctors doctors = (Doctors) o;
        return getDepartment() == doctors.getDepartment() &&
                getDoctorNames().equals(doctors.getDoctorNames());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDepartment(), getDoctorNames());
    }
}
