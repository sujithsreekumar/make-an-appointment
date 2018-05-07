package org.echs.model;

import org.echs.exception.DataNotFoundException;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public enum Doctor {
    MED_SPLST("MED_SPLST", asList("DR.RAICHU", "DR.JOSEPH"), 10),
    GEN_MED("GEN_MED", asList("DR.UMADEVI", "DR.VIGY", "DR.SUDHA", "DR.KOSHI"), 6),
    DENTAL("DENTAL", asList("DR.MANAVI", "DR.KRISHAN"), 20);

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

    public static Doctor fromDepartment(String department) {
        return Stream.of(Doctor.values())
                .filter(doctor -> doctor.getDepartment().equals(department))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Invalid Department"));
    }

    public static Doctor fromDoctorName(String doctorName) {
        return Stream.of(Doctor.values())
                .filter(doctor -> checkIfPresent(doctorName, doctor))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Doctor name is not valid"));
    }

    private static boolean checkIfPresent(String doctorName, Doctor doctor) {
        return doctor.getDoctorNames().stream()
                .anyMatch(doctorName::equalsIgnoreCase);
    }


}
