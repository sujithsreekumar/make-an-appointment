package org.echs.model;

import org.echs.exception.DataNotFoundException;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public enum Doctor {
    MED_SPLST("MED_SPLST", asList("Dr. Raichu", "Dr. Joseph"), 8),
    GEN_MED_SURG("GEN_MED_SURG", asList("Dr. Koshy"), 5),
    GEN_MED("GEN_MED", asList("Dr. Koshy", "Dr. Beena", "Dr. Umadevi", "Dr. Anu"), 5),
    DENTAL("DENTAL", asList("Dr. Manavi", "Dr. Krishan"), 15);

    private String department;
    private List<String> doctorNames;
    private long consultationDuration;

    Doctor(String department, List<String> doctorNames, int consultationDuration) {
        this.department = department;
        this.doctorNames = doctorNames;
        this.consultationDuration = consultationDuration;
    }

    public String getDepartment() {
        return department;
    }

    public List<String> getDoctorNames() {
        return doctorNames;
    }

    public long getConsultationDuration() {
        return consultationDuration;
    }

    public static List<String> doctorsFromDepartment(String department) {
        return fromDepartment(department).getDoctorNames();
    }

    public static long getConsulationDuration(String department) {
        return fromDepartment(department).getConsultationDuration();
    }

    public static Doctor fromDepartment(String department) {
        return Stream.of(Doctor.values())
                .filter(doctor -> doctor.getDepartment().equals(department))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Invalid Department"));
    }

    public static Doctor fromDoctorName(String doctorName) {
        return Stream.of(Doctor.values())
                .filter(doctor -> doctor.getDoctorNames().contains(doctorName))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Doctor name is not valid"));
    }


}