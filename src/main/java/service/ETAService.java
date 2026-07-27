package service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import dto.ETARequest;
import dto.ETAResponse;

public class ETAService {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a");

    public ETAResponse searchBus(ETARequest request) {

        ETAResponse response = new ETAResponse();

        // Common Details
        response.setBusId(1);
        response.setBusNumber("TN72A1234");

        response.setSource(request.getSource());
        response.setDestination(request.getDestination());

        /*
         * Bus Schedule
         *
         * Nagercoil -> Tirunelveli
         * Departure : 08:00 AM
         * Arrival   : 10:00 AM
         *
         * Tirunelveli -> Nagercoil
         * Departure : 10:15 AM
         * Arrival   : 12:15 PM
         */

        LocalTime currentTime = LocalTime.now();

        LocalTime trip1Departure = LocalTime.parse("08:00 AM", TIME_FORMAT);
        LocalTime trip1Arrival   = LocalTime.parse("10:00 AM", TIME_FORMAT);

        LocalTime trip2Departure = LocalTime.parse("10:15 AM", TIME_FORMAT);
        LocalTime trip2Arrival   = LocalTime.parse("12:15 PM", TIME_FORMAT);

        boolean isRunning =
                isBetween(currentTime, trip1Departure, trip1Arrival)
             || isBetween(currentTime, trip2Departure, trip2Arrival);

        if (isRunning) {

            response.setStatus("RUNNING");

            response.setSpeed(48.5);

            response.setCurrentStop("Aralvaimozhi");
            response.setCurrentLocation("Aralvaimozhi");
            response.setNextStop("Kavalkinaru");

            response.setDistanceToNextStop(6.0);

            response.setRemainingDistanceToSource(22.0);
            response.setRemainingDistanceToDestination(48.0);

            response.setRemainingTimeAndDistance("48 km | 55 mins");

            response.setEtaToSource(calculateETA(15));
            response.setEtaToDestination(calculateETA(55));

            response.setLatitude(8.3221);
            response.setLongitude(77.6045);

            response.setLastUpdated(getCurrentTime());

        } else {

            response.setStatus("SCHEDULED");

            /*
             * Find where the bus is waiting
             */
            if (currentTime.isBefore(trip1Departure)) {

                response.setStartingFrom("Nagercoil");
                response.setDepartureTime("08:00 AM");
                response.setBusArrivalTimeAtSource("08:00 AM");
                response.setBusDestinationArrivalTime("10:00 AM");

            } else if (currentTime.isAfter(trip1Arrival)
                    && currentTime.isBefore(trip2Departure)) {

                response.setStartingFrom("Tirunelveli");
                response.setDepartureTime("10:15 AM");
                response.setBusArrivalTimeAtSource("10:15 AM");
                response.setBusDestinationArrivalTime("12:15 PM");

            } else {

                // Next day's first trip
                response.setStartingFrom("Nagercoil");
                response.setDepartureTime("08:00 AM");
                response.setBusArrivalTimeAtSource("08:00 AM");
                response.setBusDestinationArrivalTime("10:00 AM");
            }

            response.setSpeed(0);

            response.setCurrentStop("");
            response.setCurrentLocation("");
            response.setNextStop("");

            response.setDistanceToNextStop(0);

            response.setRemainingDistanceToSource(0);
            response.setRemainingDistanceToDestination(0);

            response.setRemainingTimeAndDistance("");

            response.setEtaToSource("");
            response.setEtaToDestination("");

            response.setLatitude(0);
            response.setLongitude(0);

            response.setLastUpdated(getCurrentTime());
        }

        return response;
    }

    /*
     * Checks whether current time is between departure and arrival
     */
    private boolean isBetween(LocalTime current,
                              LocalTime departure,
                              LocalTime arrival) {

        return !current.isBefore(departure)
                && !current.isAfter(arrival);
    }

    /*
     * Calculates ETA
     */
    private String calculateETA(int minutes) {

        return LocalTime.now()
                .plusMinutes(minutes)
                .format(TIME_FORMAT);
    }

    /*
     * Returns Current Time
     */
    private String getCurrentTime() {

        return LocalTime.now().format(TIME_FORMAT);
    }
}