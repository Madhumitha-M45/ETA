package service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ETACalculator {

    private static final double DEFAULT_SPEED = 25.0; // Fallback speed in km/h
    private static final DateTimeFormatter AM_PM_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    public static double calculateCurrentBusDistance(double journeyProgress, double totalDistance) {
        return (journeyProgress / 100.0) * totalDistance;
    }

    public static double calculateRemainingDistance(double stopDistance, double currentBusDistance) {
        return Math.max(0.0, stopDistance - currentBusDistance);
    }

    public static long calculateETAMinutes(double distanceKm, double speedKmPerHour) {
        if (distanceKm <= 0) return 1; // Minimum 1 min for non-zero remaining distance
        double effectiveSpeed = (speedKmPerHour <= 0) ? DEFAULT_SPEED : speedKmPerHour;
        return Math.max(1, Math.round((distanceKm / effectiveSpeed) * 60.0));
    }

    public static String formatETAString(long minutes) {
        return Math.max(1, minutes) + " mins";
    }

    // Converts "15:00" or "15:00:00" to "03:00 PM"
    public static String format12HourTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return "12:00 PM";
        }
        try {
            String cleanTime = timeStr.trim();
            if (cleanTime.contains(" ")) {
                cleanTime = cleanTime.split(" ")[1];
            }
            if (cleanTime.contains(".")) {
                cleanTime = cleanTime.substring(0, cleanTime.indexOf("."));
            }
            LocalTime time = LocalTime.parse(cleanTime);
            return time.format(AM_PM_FORMATTER);
        } catch (Exception e) {
            return timeStr; // Fallback to raw string if parsing fails
        }
    }

    // Calculates AM/PM clock time from timestamp + ETA minutes
    public static String calculateArrivalTime(String lastUpdatedTimestamp, long etaMinutes) {
        if (lastUpdatedTimestamp == null || lastUpdatedTimestamp.trim().isEmpty()) {
            return "12:00 PM";
        }
        try {
            String cleanTimestamp = lastUpdatedTimestamp.trim();
            LocalTime baseTime;

            if (cleanTimestamp.contains(" ")) {
                DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(cleanTimestamp, fullFormatter);
                baseTime = dateTime.toLocalTime();
            } else {
                if (cleanTimestamp.contains(".")) {
                    cleanTimestamp = cleanTimestamp.substring(0, cleanTimestamp.indexOf("."));
                }
                baseTime = LocalTime.parse(cleanTimestamp);
            }

            LocalTime arrivalTime = baseTime.plusMinutes(Math.max(0, etaMinutes));
            return arrivalTime.format(AM_PM_FORMATTER);
        } catch (Exception e) {
            return "12:00 PM";
        }
    }
}