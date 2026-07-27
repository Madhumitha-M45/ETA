package dto;

public class ETAResponse {

    private int busId;
    private String busNumber;
    private String status;

    private double speed;

    private String currentStop;
    private String currentLocation;
    private String nextStop;

    private double distanceToNextStop;

    private String remainingTimeAndDistance;

    private double remainingDistanceToSource;
    private String etaToSource;

    private double remainingDistanceToDestination;
    private String etaToDestination;

    private String startingFrom;
    private String departureTime;

    private String busArrivalTimeAtSource;
    private String busDestinationArrivalTime;

    private String lastUpdated;

    private String source;
    private String destination;

    private double latitude;
    private double longitude;

    public ETAResponse() {
    }

    public int getBusId() {
        return busId;
    }

    public void setBusId(int busId) {
        this.busId = busId;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getCurrentStop() {
        return currentStop;
    }

    public void setCurrentStop(String currentStop) {
        this.currentStop = currentStop;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getNextStop() {
        return nextStop;
    }

    public void setNextStop(String nextStop) {
        this.nextStop = nextStop;
    }

    public double getDistanceToNextStop() {
        return distanceToNextStop;
    }

    public void setDistanceToNextStop(double distanceToNextStop) {
        this.distanceToNextStop = distanceToNextStop;
    }

    public String getRemainingTimeAndDistance() {
        return remainingTimeAndDistance;
    }

    public void setRemainingTimeAndDistance(String remainingTimeAndDistance) {
        this.remainingTimeAndDistance = remainingTimeAndDistance;
    }

    public double getRemainingDistanceToSource() {
        return remainingDistanceToSource;
    }

    public void setRemainingDistanceToSource(double remainingDistanceToSource) {
        this.remainingDistanceToSource = remainingDistanceToSource;
    }

    public String getEtaToSource() {
        return etaToSource;
    }

    public void setEtaToSource(String etaToSource) {
        this.etaToSource = etaToSource;
    }

    public double getRemainingDistanceToDestination() {
        return remainingDistanceToDestination;
    }

    public void setRemainingDistanceToDestination(double remainingDistanceToDestination) {
        this.remainingDistanceToDestination = remainingDistanceToDestination;
    }

    public String getEtaToDestination() {
        return etaToDestination;
    }

    public void setEtaToDestination(String etaToDestination) {
        this.etaToDestination = etaToDestination;
    }

    public String getStartingFrom() {
        return startingFrom;
    }

    public void setStartingFrom(String startingFrom) {
        this.startingFrom = startingFrom;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getBusArrivalTimeAtSource() {
        return busArrivalTimeAtSource;
    }

    public void setBusArrivalTimeAtSource(String busArrivalTimeAtSource) {
        this.busArrivalTimeAtSource = busArrivalTimeAtSource;
    }

    public String getBusDestinationArrivalTime() {
        return busDestinationArrivalTime;
    }

    public void setBusDestinationArrivalTime(String busDestinationArrivalTime) {
        this.busDestinationArrivalTime = busDestinationArrivalTime;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}