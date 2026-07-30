package model;

public class RouteStop {
    private String stopName;
    private Double distanceFromPrevious;
    private Double distance;
    private Double distanceFromStart;

    public RouteStop() {}

    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }

    public Double getDistanceFromPrevious() { return distanceFromPrevious; }
    public void setDistanceFromPrevious(Double distanceFromPrevious) { this.distanceFromPrevious = distanceFromPrevious; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Double getDistanceFromStart() { return distanceFromStart; }
    public void setDistanceFromStart(Double distanceFromStart) { this.distanceFromStart = distanceFromStart; }
}