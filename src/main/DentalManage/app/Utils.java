package app;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Calendar;

public class Utils {

    public static int calculateWorkdays(LocalDate startDate, LocalDate endDate){

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        startCal.setTime(Timestamp.valueOf(startDate.atStartOfDay()));
        endCal.setTime(Timestamp.valueOf(endDate.atStartOfDay()));
        int workDays = 0;
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            endCal.add(Calendar.DATE,1);
        }
        if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            startCal.setTime(Timestamp.valueOf(endDate.atStartOfDay()));
            endCal.setTime(Timestamp.valueOf(startDate.atStartOfDay()));
        }
        do {
            startCal.add(Calendar.DAY_OF_MONTH, 1);
            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                workDays++;
            }
        } while (startCal.getTimeInMillis() < endCal.getTimeInMillis());
        return workDays;
    }
}
