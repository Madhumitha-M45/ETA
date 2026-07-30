package service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import dto.ETARequest;
import dto.ETAResponse;
import dto.RouteStopDTO;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ETAService {

    private static final String CONNECTION_STRING = "mongodb://192.168.1.171:27017";
    private static final String DATABASE_NAME = "bus_tracking_db";
    private static final MongoClient client = MongoClients.create(CONNECTION_STRING);

    public List<ETAResponse> searchBus(ETARequest request) {
        List<ETAResponse> responsesList = new ArrayList<>();
        try {
            MongoDatabase database = client.getDatabase(DATABASE_NAME);
            MongoCollection<Document> scheduleCollection = database.getCollection("bus_schedule");
            MongoCollection<Document> liveCollection = database.getCollection("bus_live_data");
            MongoCollection<Document> busCollection = database.getCollection("buses");

            String boardingStop = request.getBoardingStop();
            String destinationStop = request.getDestinationStop();

            if (boardingStop == null || destinationStop == null) {
                return responsesList;
            }

            Pattern boardingPattern = Pattern.compile("^" + Pattern.quote(boardingStop.trim()) + "$", Pattern.CASE_INSENSITIVE);
            Pattern destinationPattern = Pattern.compile("^" + Pattern.quote(destinationStop.trim()) + "$", Pattern.CASE_INSENSITIVE);

            List<Document> schedules = scheduleCollection.find(
                Filters.and(
                    Filters.regex("routeStops.stopName", boardingPattern),
                    Filters.regex("routeStops.stopName", destinationPattern)
                )
            ).into(new ArrayList<>());

            for (Document schedule : schedules) {
                List<Document> rawRouteStops = (List<Document>) schedule.get("routeStops");
                if (rawRouteStops == null) continue;

                int boardingIndex = -1;
                int destinationIndex = -1;
                double sourceDistance = 0.0;
                double destinationDistance = 0.0;

                double cumulativeDist = 0.0;
                List<Double> stopCumulativeDistances = new ArrayList<>();

                for (int i = 0; i < rawRouteStops.size(); i++) {
                    Document stopDoc = rawRouteStops.get(i);
                    String name = stopDoc.getString("stopName");

                    double distVal = 0.0;
                    if (stopDoc.get("distanceFromPrevious") != null) {
                        distVal = ((Number) stopDoc.get("distanceFromPrevious")).doubleValue();
                    } else if (stopDoc.get("distance") != null) {
                        distVal = ((Number) stopDoc.get("distance")).doubleValue();
                    }

                    cumulativeDist += distVal;
                    stopCumulativeDistances.add(cumulativeDist);

                    if (name != null && name.equalsIgnoreCase(boardingStop.trim())) {
                        boardingIndex = i;
                        sourceDistance = cumulativeDist;
                    }

                    if (name != null && name.equalsIgnoreCase(destinationStop.trim())) {
                        destinationIndex = i;
                        destinationDistance = cumulativeDist;
                    }
                }

                if (boardingIndex == -1 || destinationIndex == -1 || boardingIndex >= destinationIndex) {
                    continue;
                }

                ETAResponse response = new ETAResponse();
                String busId = schedule.getString("busId");

                response.setBusId(busId);
                response.setBoardingStop(boardingStop);
                response.setDestinationStop(destinationStop);
                response.setStartingFrom(schedule.getString("startLocation"));
                
                // Format railway time "15:00" to "03:00 PM"
                response.setDepartureTime(ETACalculator.format12HourTime(schedule.getString("departureTime")));

                double totalDistance = schedule.get("totalDistance") != null 
                        ? ((Number) schedule.get("totalDistance")).doubleValue() 
                        : cumulativeDist;

                Document liveData = liveCollection.find(Filters.eq("busId", busId)).first();

                if (liveData != null) {
                    String status = liveData.getString("busStatus");
                    double speed = liveData.get("speed") != null ? ((Number) liveData.get("speed")).doubleValue() : 0.0;
                    double journeyProgress = liveData.get("journeyProgress") != null ? ((Number) liveData.get("journeyProgress")).doubleValue() : 0.0;

                    double lat = liveData.get("latitude") != null ? ((Number) liveData.get("latitude")).doubleValue() : 0.0;
                    double lng = liveData.get("longitude") != null ? ((Number) liveData.get("longitude")).doubleValue() : 0.0;
                    String currentStop = liveData.getString("currentStop");
                    String nextStop = liveData.getString("nextStop");
                    String timestamp = liveData.getString("timestamp");

                    double distanceToNextStop = liveData.get("distanceToNextStop") != null 
                            ? ((Number) liveData.get("distanceToNextStop")).doubleValue() : 0.0;

                    response.setLatitude(lat);
                    response.setLongitude(lng);
                    response.setCurrentStop(currentStop);
                    response.setNextStop(nextStop);
                    response.setSpeed(speed);
                    response.setLastUpdated(timestamp != null ? timestamp : "2026-07-29 10:00:00");
                    response.setDistanceToNextStop(Math.round(distanceToNextStop * 10.0) / 10.0);

                    double currentBusDistance = ETACalculator.calculateCurrentBusDistance(journeyProgress, totalDistance);

                    double remainingToBoarding = ETACalculator.calculateRemainingDistance(sourceDistance, currentBusDistance);
                    double remainingToDestination = ETACalculator.calculateRemainingDistance(destinationDistance, currentBusDistance);

                    // Ensure upcoming stops use real telemetry distance
                    if (remainingToBoarding == 0.0 && nextStop != null && nextStop.equalsIgnoreCase(boardingStop)) {
                        remainingToBoarding = distanceToNextStop;
                    }
                    if (remainingToDestination == 0.0 && nextStop != null && nextStop.equalsIgnoreCase(destinationStop)) {
                        remainingToDestination = distanceToNextStop;
                    }

                    long etaBoardingMins = ETACalculator.calculateETAMinutes(remainingToBoarding, speed);
                    long etaDestMins = ETACalculator.calculateETAMinutes(remainingToDestination, speed);

                    response.setRemainingDistanceToBoardingStop(Math.round(remainingToBoarding * 10.0) / 10.0);
                    response.setRemainingDistanceToDestination(Math.round(remainingToDestination * 10.0) / 10.0);

                    response.setEtaToBoardingStop(ETACalculator.formatETAString(etaBoardingMins));
                    response.setBusArrivalTimeAtBoardingStop(ETACalculator.calculateArrivalTime(timestamp, etaBoardingMins));

                    response.setEtaToDestinationStop(ETACalculator.formatETAString(etaDestMins));
                    response.setBusArrivalTimeAtDestinationStop(ETACalculator.calculateArrivalTime(timestamp, etaDestMins));

                    if ("RUNNING".equalsIgnoreCase(status) && speed == 0.0) {
                        status = "WAITING AT STOP";
                    }
                    response.setStatus(status);

                    // Locate index of current stop in schedule
                    int currentStopIndex = -1;
                    for (int i = 0; i < rawRouteStops.size(); i++) {
                        String name = rawRouteStops.get(i).getString("stopName");
                        if (name != null && name.equalsIgnoreCase(currentStop)) {
                            currentStopIndex = i;
                            break;
                        }
                    }

                    // Build routeStops list
                    List<RouteStopDTO> formattedStops = new ArrayList<>();
                    for (int i = 0; i < rawRouteStops.size(); i++) {
                        Document stopDoc = rawRouteStops.get(i);
                        String stopName = stopDoc.getString("stopName");
                        double stopAbsDist = stopCumulativeDistances.get(i);

                        double remDist = ETACalculator.calculateRemainingDistance(stopAbsDist, currentBusDistance);

                        // If distance evaluates to 0.0 for next stop, fallback to distanceToNextStop
                        if (remDist == 0.0 && nextStop != null && nextStop.equalsIgnoreCase(stopName)) {
                            remDist = distanceToNextStop;
                        }

                        remDist = Math.round(remDist * 10.0) / 10.0;

                        String stopEtaStr;
                        if (currentStop != null && currentStop.equalsIgnoreCase(stopName)) {
                            stopEtaStr = "Current";
                            remDist = 0.0;
                        } else if (currentStopIndex != -1 && i < currentStopIndex) {
                            stopEtaStr = "Passed";
                            remDist = 0.0;
                        } else {
                            long stopEtaMins = ETACalculator.calculateETAMinutes(remDist, speed);
                            stopEtaStr = ETACalculator.formatETAString(stopEtaMins);
                        }

                        formattedStops.add(new RouteStopDTO(stopName, remDist, stopEtaStr));
                    }
                    response.setRouteStops(formattedStops);

                } else {
                    response.setStatus("OFFLINE");
                    response.setEtaToBoardingStop("1 mins");
                    response.setEtaToDestinationStop("1 mins");
                    response.setBusArrivalTimeAtBoardingStop("12:00 PM");
                    response.setBusArrivalTimeAtDestinationStop("12:00 PM");
                }

                Document bus = busCollection.find(Filters.eq("busId", busId)).first();
                if (bus != null) {
                    response.setBusNumber(bus.getString("busNumber"));
                }

                responsesList.add(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return responsesList;
    }
}