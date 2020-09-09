package com.example.arduinoandroidapplication;

import android.os.Build;
import androidx.annotation.RequiresApi;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String fixDate(String date){
        LocalDateTime formattedDate = convertStringToDate(date);
        LocalDateTime givenDate = makeLocalTime(formattedDate);
        return convertDateToDString(givenDate);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static LocalDateTime makeLocalTime(LocalDateTime formattedDate) {
        ZonedDateTime z = ZonedDateTime.of(formattedDate, ZoneOffset.UTC);
        ZoneId s = ZoneId.of("Israel");
        return LocalDateTime.ofInstant(z.toInstant(),s);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static LocalDateTime convertStringToDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(date, formatter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String convertDateToDString(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String convertDateToDString(ZonedDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
