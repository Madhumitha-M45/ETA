package dto;

public class ETARequest {

    private String source;
    private String destination;

    // Default Constructor
    public ETARequest() {
    }

    // Parameterized Constructor
    public ETARequest(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    // Getter for source
    public String getSource() {
        return source;
    }

    // Setter for source
    public void setSource(String source) {
        this.source = source;
    }

    // Getter for destination
    public String getDestination() {
        return destination;
    }

    // Setter for destination
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "ETARequest [source=" + source + ", destination=" + destination + "]";
    }
}