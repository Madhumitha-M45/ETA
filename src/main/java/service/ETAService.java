package service;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import dto.ETARequest;
import dto.ETAResponse;
import dto.RouteStopDTO;
import model.Bus;
import model.BusLiveData;
import model.BusSchedule;
import model.RouteStop;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
public class ETAService {

    private static final String CONNECTION_STRING = "mongodb://192.168.1.171:27017";
    private static final String DATABASE_NAME = "bus_tracking_db";

    private static final CodecRegistry pojoCodecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build())
    );

    private static final MongoClient client = MongoClients.create(CONNECTION_STRING);

    public List<ETAResponse> searchBus(ETARequest request) {
        List<ETAResponse> responsesList = new ArrayList<>();
        if (request == null) {
            return responsesList;
        }

        try {
            MongoDatabase database = client.getDatabase(DATABASE_NAME).withCodecRegistry(pojoCodecRegistry);

            MongoCollection<BusSchedule> scheduleCollection = database.getCollection("bus_schedule", BusSchedule.class);
            MongoCollection<BusLiveData> liveCollection = database.getCollection("bus_live_data", BusLiveData.class);
            MongoCollection<Bus> busCollection = database.getCollection("buses", Bus.class);

            String boardingStop = request.getBoardingStop();
            String destinationStop = request.getDestinationStop();
            String userTravelTime = request.getTravelTime(); // Accepts "2:00 PM", "02:00 PM", "14:00", etc.

            if (boardingStop == null || destinationStop == null || boardingStop.trim().isEmpty() || destinationStop.trim().isEmpty()) {
                return responsesList;
            }

            Pattern boardingPattern = Pattern.compile("^" + Pattern.quote(boardingStop.trim()) + "$", Pattern.CASE_INSENSITIVE);
            Pattern destinationPattern = Pattern.compile("^" + Pattern.quote(destinationStop.trim()) + "$", Pattern.CASE_INSENSITIVE);

            List<BusSchedule> schedules = scheduleCollection.find(
                Filters.and(
                    Filters.regex("routeStops.stopName", boardingPattern),
                    Filters.regex("routeStops.stopName", destinationPattern)
                )
            ).into(new ArrayList<>());

            LocalTime parsedUserTime = parseLocalTime(userTravelTime);

            for (BusSchedule schedule : schedules) {
                if (parsedUserTime != null && schedule.getDepartureTime() != null) {
                    LocalTime busDeparture = parseLocalTime(schedule.getDepartureTime());
                    if (busDeparture != null && busDeparture.isBefore(parsedUserTime)) {
                        continue;
                    }
                }

                List<RouteStop> rawRouteStops = schedule.getRouteStops();
                if (rawRouteStops == null || rawRouteStops.isEmpty()) continue;

                double scheduleTotalDistance = schedule.getTotalDistance() != null ? schedule.getTotalDistance() : 0.0;
                List<Double> stopCumulativeDistances = buildCumulativeDistances(rawRouteStops, scheduleTotalDistance);
                
                int boardingIndex = -1;
                int destinationIndex = -1;
                for (int i = 0; i < rawRouteStops.size(); i++) {
                    RouteStop stopObj = rawRouteStops.get(i);
                    String name = stopObj != null ? stopObj.getStopName() : null;
                    if (name != null && name.equalsIgnoreCase(boardingStop.trim())) {
                        boardingIndex = i;
                    }
                    if (name != null && name.equalsIgnoreCase(destinationStop.trim())) {
                        destinationIndex = i;
                    }
                }
                if (boardingIndex == -1 || destinationIndex == -1 || boardingIndex >= destinationIndex) {
                    continue;
                }

                ETAResponse response = new ETAResponse();
                String busId = schedule.getBusId();

                response.setBusId(busId);
                response.setBoardingStop(boardingStop.trim());
                response.setDestinationStop(destinationStop.trim());
                response.setStartingFrom(schedule.getStartLocation());
                response.setDepartureTime(ETACalculator.format12HourTime(schedule.getDepartureTime()));

                BusLiveData liveData = liveCollection.find(Filters.eq("busId", busId)).first();
                if (liveData != null) {
                    String status = liveData.getBusStatus();
                    double speed = liveData.getSpeed() != null ? liveData.getSpeed() : 0.0;
                    double journeyProgress = liveData.getJourneyProgress() != null ? liveData.getJourneyProgress() : 0.0;
                    double lat = liveData.getLatitude() != null ? liveData.getLatitude() : 0.0;
                    double lng = liveData.getLongitude() != null ? liveData.getLongitude() : 0.0;
                    String currentStop = liveData.getCurrentStop();
                    String nextStop = liveData.getNextStop();
                    String timestamp = liveData.getTimestamp();
                    double distanceToNextStop = liveData.getDistanceToNextStop() != null ? liveData.getDistanceToNextStop() : 0.0;

                    response.setLatitude(lat);
                    response.setLongitude(lng);
                    response.setCurrentStop(currentStop);
                    response.setNextStop(nextStop);
                    response.setSpeed(speed);
                    response.setLastUpdated(timestamp != null ? timestamp : "2026-07-30 12:00:00");
                    response.setDistanceToNextStop(ETACalculator.roundDistance(distanceToNextStop));

                    if ("RUNNING".equalsIgnoreCase(status) && speed == 0.0) {
                        status = "WAITING AT STOP";
                    }
                    response.setStatus(status);

                    int currentStopIndex = -1;
                    for (int i = 0; i < rawRouteStops.size(); i++) {
                        String name = rawRouteStops.get(i) != null ? rawRouteStops.get(i).getStopName() : null;
                        if (name != null && name.equalsIgnoreCase(currentStop)) {
                            currentStopIndex = i;
                            break;
                        }
                    }

                    List<RouteStopDTO> formattedStops = new ArrayList<>();

                    for (int i = 0; i < rawRouteStops.size(); i++) {
                        RouteStop stopObj = rawRouteStops.get(i);
                        String stopName = stopObj != null ? stopObj.getStopName() : "Unknown";

                        double remDist = calculateStopRemainingDistance(
                                i, currentStopIndex, distanceToNextStop, 
                                stopCumulativeDistances, journeyProgress, scheduleTotalDistance
                        );

                        remDist = ETACalculator.roundDistance(remDist);
                        String stopEtaStr;

                        if (currentStopIndex != -1 && i < currentStopIndex) {
                            stopEtaStr = "Passed";
                        } else if (currentStopIndex != -1 && i == currentStopIndex) {
                            stopEtaStr = "Current";
                        } else {
                            long stopEta = ETACalculator.calculateETAMinutes(remDist, speed);
                            stopEtaStr = ETACalculator.formatETAString(stopEta);
                        }

                        formattedStops.add(new RouteStopDTO(stopName, remDist, stopEtaStr));
                    }

                    response.setRouteStops(formattedStops);

                    // Boarding & Destination Calculations
                    double remBoardingDist = calculateStopRemainingDistance(
                            boardingIndex, currentStopIndex, distanceToNextStop, 
                            stopCumulativeDistances, journeyProgress, scheduleTotalDistance
                    );
                    remBoardingDist = ETACalculator.roundDistance(remBoardingDist);
                    response.setRemainingDistanceToBoardingStop(remBoardingDist);

                    long etaBoardingMins = ETACalculator.calculateETAMinutes(remBoardingDist, speed);
                    response.setEtaToBoardingStop(ETACalculator.formatETAString(etaBoardingMins));
                    response.setBusArrivalTimeAtBoardingStop(ETACalculator.calculateArrivalTime(timestamp, etaBoardingMins));

                    double remDestDist = calculateStopRemainingDistance(
                            destinationIndex, currentStopIndex, distanceToNextStop, 
                            stopCumulativeDistances, journeyProgress, scheduleTotalDistance
                    );
                    remDestDist = ETACalculator.roundDistance(remDestDist);
                    response.setRemainingDistanceToDestination(remDestDist);

                    long etaDestMins = ETACalculator.calculateETAMinutes(remDestDist, speed);
                    response.setEtaToDestinationStop(ETACalculator.formatETAString(etaDestMins));
                    response.setBusArrivalTimeAtDestinationStop(ETACalculator.calculateArrivalTime(timestamp, etaDestMins));

                } else {
                    response.setStatus("OFFLINE");
                    response.setEtaToBoardingStop("N/A");
                    response.setEtaToDestinationStop("N/A");
                    response.setBusArrivalTimeAtBoardingStop("N/A");
                    response.setBusArrivalTimeAtDestinationStop("N/A");
                }

                Bus bus = busCollection.find(Filters.eq("busId", busId)).first();
                if (bus != null) {
                    response.setBusNumber(bus.getBusNumber());
                }

                responsesList.add(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return responsesList;
    }
    private LocalTime parseLocalTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        
        String cleanTime = timeStr.trim().toUpperCase();

        String[] patterns = {
            "h:mm a", "hh:mm a", "h:mm:ss a", "hh:mm:ss a",
            "h:mma", "hh:mma",
            "H:mm", "HH:mm", "H:mm:ss", "HH:mm:ss"
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern(pattern)
                        .toFormatter(Locale.ENGLISH);
                return LocalTime.parse(cleanTime, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private List<Double> buildCumulativeDistances(List<RouteStop> rawRouteStops, double scheduleTotalDistance) {
        List<Double> cumulativeList = new ArrayList<>();
        double runningTotal = 0.0;

        for (int i = 0; i < rawRouteStops.size(); i++) {
            RouteStop stop = rawRouteStops.get(i);
            
            if (stop == null) {
                cumulativeList.add(runningTotal);
                continue;
            }
            Double distFromStart = stop.getDistanceFromStart();
            if (distFromStart != null && distFromStart > 0.0) {
                runningTotal = distFromStart;
                cumulativeList.add(runningTotal);
                continue;
            }
            Double distFromPrev = stop.getDistanceFromPrevious() != null ? stop.getDistanceFromPrevious() : stop.getDistance();
            if (distFromPrev != null && distFromPrev > 0.0) {
                runningTotal += distFromPrev;
                cumulativeList.add(runningTotal);
                continue;
            }
            if (i > 0 && scheduleTotalDistance > runningTotal) {
                int remainingStops = rawRouteStops.size() - i;
                double estimatedSegment = (scheduleTotalDistance - runningTotal) / remainingStops;
                runningTotal += estimatedSegment;
            }

            cumulativeList.add(runningTotal);
        }

        return cumulativeList;
    }

    private double calculateStopRemainingDistance(
            int targetIndex, 
            int currentStopIndex, 
            double distanceToNextStop, 
            List<Double> stopCumulativeDistances, 
            double journeyProgress, 
            double totalDistance) {

        if (targetIndex < 0 || targetIndex >= stopCumulativeDistances.size()) return 0.0;

        if (currentStopIndex == -1) {
            double currentBusDist = ETACalculator.calculateCurrentBusDistance(journeyProgress, totalDistance);
            return Math.max(0.0, stopCumulativeDistances.get(targetIndex) - currentBusDist);
        }

        if (targetIndex <= currentStopIndex) {
            return 0.0; 
        } else if (targetIndex == currentStopIndex + 1) {
            return distanceToNextStop; 
        } else {
            double nextStopDistance = stopCumulativeDistances.get(currentStopIndex + 1);
            double targetStopDistance = stopCumulativeDistances.get(targetIndex);
            return distanceToNextStop + Math.max(0.0, targetStopDistance - nextStopDistance);
        }
    }
}