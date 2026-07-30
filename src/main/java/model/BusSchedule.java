package model;

import java.util.List;

public class BusSchedule {
    private String busId;
    private String startLocation;
    private String departureTime;
    private Double totalDistance;
    private List<RouteStop> routeStops;

    public BusSchedule() {}

    public String getBusId() { return busId; }
    public void setBusId(String busId) { this.busId = busId; }

    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public Double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Double totalDistance) { this.totalDistance = totalDistance; }

    public List<RouteStop> getRouteStops() { return routeStops; }
    public void setRouteStops(List<RouteStop> routeStops) { this.routeStops = routeStops; }
}