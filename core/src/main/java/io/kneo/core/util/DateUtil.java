package io.kneo.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateUtil {

    public static ZonedDateTime getStartOfDayOrNow(LocalDate date) {
        if (date != null) {
            return date.atStartOfDay(ZoneId.systemDefault());
        } else {
            return LocalDateTime.now().atZone(ZoneId.systemDefault());
        }
    }
}
