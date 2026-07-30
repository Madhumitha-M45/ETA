package dto;

public class RouteStopDTO {
    private String stopName;
    private double remainingDistance;
    private String eta;

    public RouteStopDTO() {}

    public RouteStopDTO(String stopName, double remainingDistance, String eta) {
        this.stopName = stopName;
        this.remainingDistance = remainingDistance;
        this.eta = eta;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public double getRemainingDistance() {
        return remainingDistance;
    }

    public void setRemainingDistance(double remainingDistance) {
        this.remainingDistance = remainingDistance;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }
}