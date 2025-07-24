package com.lksnext.ParkingELadron.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {
    public static String toUtcIsoString(Date date, String hourStr) {
        // 1. Convierte Date a LocalDate en la zona local
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // 2. Convierte "HH:mm" a LocalTime
        LocalTime localTime = LocalTime.parse(hourStr);
        // 3. Crea ZonedDateTime local y conviértelo a UTC
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, localTime, ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
        // 4. Formatea como ISO 8601
        return zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static String isoToLocalHour(String isoString) {
        ZonedDateTime zdt;
        try {
            zdt = ZonedDateTime.parse(isoString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            zdt = ZonedDateTime.parse(isoString, DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")));
        }
        ZonedDateTime localZdt = zdt.withZoneSameInstant(ZoneId.systemDefault());
        return localZdt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static boolean timeOverlapsIso(String start1, String end1, String start2, String end2) {
        ZonedDateTime s1 = ZonedDateTime.parse(start1, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        ZonedDateTime e1 = ZonedDateTime.parse(end1, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        ZonedDateTime s2 = ZonedDateTime.parse(start2, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        ZonedDateTime e2 = ZonedDateTime.parse(end2, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return !(e1.compareTo(s2) <= 0 || e2.compareTo(s1) <= 0);
    }
}
