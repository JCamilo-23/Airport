/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.models.Plane;

/**
 *
 * @author Admin
 */
public class PlaneController {
    public static Response createPlane(String idStr, String brand, String model, String maxCapacityStr, String airline){
        try{
            // 1. Airplane ID validation
            if (idStr == null || idStr.trim().isEmpty()) {
                return new Response("Airplane ID must not be empty.", Status.BAD_REQUEST);
            }
            
            idStr = idStr.trim().toUpperCase(); // Normalize ID to uppercase
            if (!idStr.matches("[A-Z]{2}\\d{5}")) {
                return new Response("Airplane ID must follow the format XXYYYYY (e.g., AA12345).", Status.BAD_REQUEST);
            }

            // Check ID uniqueness
           // AirplaneStorage storage = AirplaneStorage.getInstance(); // Make sure AirplaneStorage exists
            //if (storage.airplaneIdExists(idStr)) {
            //    return new Response("An airplane with the ID '" + idStr + "' already exists.", Status.CONFLICT);
            //}

            // 2. Brand validation
            if (brand == null || brand.trim().isEmpty()) {
                return new Response("Airplane brand must not be empty.", Status.BAD_REQUEST);
            }

            // 3. Model validation
            if (model == null || model.trim().isEmpty()) {
                return new Response("Airplane model must not be empty.", Status.BAD_REQUEST);
            }

            // 4. Max Capacity validation
            if (maxCapacityStr == null || maxCapacityStr.trim().isEmpty()) {
                return new Response("Maximum capacity must not be empty.", Status.BAD_REQUEST);
            }
            int maxCapacity;
            try {
                maxCapacity = Integer.parseInt(maxCapacityStr.trim());
                if (maxCapacity <= 0) {
                    return new Response("Maximum capacity must be a positive number.", Status.BAD_REQUEST);
                }
            } catch (NumberFormatException ex) {
                return new Response("Maximum capacity must be a valid number.", Status.BAD_REQUEST);
            }

            // 5. Airline validation
            if (airline == null || airline.trim().isEmpty()) {
                return new Response("Airline must not be empty.", Status.BAD_REQUEST);
            }

            // If all validations pass, create and save the airplane
            Plane newAirplane = new Plane(idStr, brand.trim(), model.trim(), maxCapacity, airline.trim());
            
            //if (!storage.addAirplane(newAirplane)) {
                // This case should ideally be caught by airplaneIdExists, but as a fallback
            //    return new Response("Error saving airplane. The ID might have been registered simultaneously.", Status.CONFLICT);
           // }

            return new Response("Airplane created successfully.", Status.CREATED, newAirplane);

        } catch (Exception ex) {
            // Log the error for debugging, e.g., ex.printStackTrace();
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    
    }
    
}
