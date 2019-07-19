package org.echs.model;

import org.echs.exception.DataNotFoundException;

import java.util.stream.Stream;

public enum Department {
    MED_SPLST("MED_SPLST"),
    GENMED("GEN_MED"),
    DENTAL("DENTAL");

    private String departmentName;

    Department(String departmentName) {
        this.departmentName = departmentName;
    }

    public static Department fromDepartmentName(final String departmentName) {
        return Stream.of(Department.values())
                .filter(department -> departmentName.equals(department.departmentName))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Invalid department name"));
    }
}
