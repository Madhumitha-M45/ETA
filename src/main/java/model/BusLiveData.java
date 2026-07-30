package model;

public class BusLiveData {
    private String busId;
    private String busStatus;
    private Double speed;
    private Double journeyProgress;
    private Double latitude;
    private Double longitude;
    private String currentStop;
    private String nextStop;
    private String timestamp;
    private Double distanceToNextStop;

    public BusLiveData() {}

    public String getBusId() { return busId; }
    public void setBusId(String busId) { this.busId = busId; }

    public String getBusStatus() { return busStatus; }
    public void setBusStatus(String busStatus) { this.busStatus = busStatus; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Double getJourneyProgress() { return journeyProgress; }
    public void setJourneyProgress(Double journeyProgress) { this.journeyProgress = journeyProgress; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCurrentStop() { return currentStop; }
    public void setCurrentStop(String currentStop) { this.currentStop = currentStop; }

    public String getNextStop() { return nextStop; }
    public void setNextStop(String nextStop) { this.nextStop = nextStop; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Double getDistanceToNextStop() { return distanceToNextStop; }
    public void setDistanceToNextStop(Double distanceToNextStop) { this.distanceToNextStop = distanceToNextStop; }
}