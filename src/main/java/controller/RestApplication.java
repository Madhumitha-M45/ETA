package controller;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class RestApplication extends Application {
    // Keeps it completely annotation-driven. JAX-RS automatically 
    // scans and registers @Path annotated controllers.
}