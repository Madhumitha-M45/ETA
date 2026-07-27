package controller;

import java.io.BufferedReader;
import java.io.IOException;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import dto.ETARequest;
import dto.ETAResponse;
import service.ETAService;

@WebServlet("/bus")
public class Controller extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ETAService etaService = new ETAService();
    private final Gson gson = new Gson();

    // Browser Request (GET)
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Create dummy request
        ETARequest etaRequest = new ETARequest();
        etaRequest.setSource("Nagercoil");
        etaRequest.setDestination("Tirunelveli");

        // Call Service
        ETAResponse etaResponse = etaService.searchBus(etaRequest);

        // Convert response object to JSON
        String jsonResponse = gson.toJson(etaResponse);

        // Send JSON to browser
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(jsonResponse);
    }

    // Mobile App / Postman Request (POST)
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        try {

            BufferedReader reader = request.getReader();

            ETARequest etaRequest = gson.fromJson(reader, ETARequest.class);

            ETAResponse etaResponse = etaService.searchBus(etaRequest);

            String jsonResponse = gson.toJson(etaResponse);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(jsonResponse);

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                "{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}"
            );

            e.printStackTrace();
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        response.setStatus(HttpServletResponse.SC_OK);
    }
}