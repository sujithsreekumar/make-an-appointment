package org.echs.service;

import org.echs.database.HolidayDao;
import org.echs.database.HolidayDaoImpl;
import org.echs.model.Holiday;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class HolidayService {

    HolidayDao holidayDao = new HolidayDaoImpl();

    public List<Holiday> getHolidaysList() throws Exception {
        return holidayDao.getHolidaysForTheYear(String.valueOf(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).getYear()));
    }

    public  void createHolidayEntry(List<Holiday> holidays){
        holidays.forEach(holiday -> holidayDao.createHolidayEntry(holiday));
    }
}
