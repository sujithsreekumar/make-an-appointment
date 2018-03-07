package org.echs.database;

import org.echs.model.Holiday;

import java.sql.SQLException;
import java.util.List;

public interface HolidayDao {

    List<Holiday> getHolidaysForTheYear(String year) throws SQLException;
    void createHolidayEntry(Holiday holiday);

}
