package service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ETACalculator {

    private static final double DEFAULT_SPEED = 30.0; // Standard fallback speed in km/h
    private static final DateTimeFormatter AM_PM_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    /**
     * Rounds distance to 1 decimal place.
     */
    public static double roundDistance(double distance) {
        return Math.round(distance * 10.0) / 10.0;
    }

    public static double calculateCurrentBusDistance(double journeyProgress, double totalDistance) {
        if (journeyProgress <= 0 || totalDistance <= 0) {
            return 0.0;
        }
        return (journeyProgress / 100.0) * totalDistance;
    }

    public static double calculateRemainingDistance(double stopDistance, double currentBusDistance) {
        return Math.max(0.0, stopDistance - currentBusDistance);
    }

    public static long calculateETAMinutes(double distanceKm, double speedKmPerHour) {
        if (distanceKm <= 0) {
            return 0; // At target stop
        }
        
        double effectiveSpeed = (speedKmPerHour <= 0) ? DEFAULT_SPEED : speedKmPerHour;
        long minutes = Math.round((distanceKm / effectiveSpeed) * 60.0);
        
        // Return at least 1 minute if distance is greater than 0
        return Math.max(1, minutes);
    }

    public static String formatETAString(long minutes) {
        if (minutes <= 0) {
            return "Arrived";
        }
        return minutes + " mins";
    }

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
            e.printStackTrace();
            return timeStr;
        }
    }

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
            e.printStackTrace();
            return "12:00 PM";
        }
    }
}