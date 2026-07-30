package controller;

import dto.ETARequest;
import dto.ETAResponse;
import service.ETAService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/bus")
public class Controller {

    private final ETAService etaService = new ETAService();

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    
    public List<ETAResponse> searchBus(ETARequest request) {
    	System.out.println("Controller Called");
        return etaService.searchBus(request);
    }
}