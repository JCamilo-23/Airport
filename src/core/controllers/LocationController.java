/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.models.Location;

/**
 *
 * @author Admin
 */
public class LocationController {
    public static Response createLocation(String airportId, String airportName, String airportCity, String airportCountry, String latitudeStr, String longitudeStr){
         try {
            // 1. Airport ID Validation
            if (airportId == null || airportId.trim().isEmpty()) {
                return new Response("Airport ID must not be empty.", Status.BAD_REQUEST);
            }
            airportId = airportId.trim().toUpperCase(); // Normalize
            if (!AIRPORT_ID_PATTERN.matcher(airportId).matches()) {
                return new Response("Airport ID must be exactly 3 uppercase letters (e.g., JFK).", Status.BAD_REQUEST);
            }

            //AirportStorage storage = AirportStorage.getInstance();
//            if (storage.airportIdExists(airportId)) {
//                return new Response("An airport with ID '" + airportId + "' already exists.", Status.CONFLICT);
//            }

            // 2. Airport Name Validation
            if (airportName == null || airportName.trim().isEmpty()) {
                return new Response("Airport name must not be empty.", Status.BAD_REQUEST);
            }

            // 3. Airport City Validation
            if (airportCity == null || airportCity.trim().isEmpty()) {
                return new Response("Airport city must not be empty.", Status.BAD_REQUEST);
            }

            // 4. Airport Country Validation
            if (airportCountry == null || airportCountry.trim().isEmpty()) {
                return new Response("Airport country must not be empty.", Status.BAD_REQUEST);
            }

            // 5. Latitude Validation
            if (latitudeStr == null || latitudeStr.trim().isEmpty()) {
                return new Response("Latitude must not be empty.", Status.BAD_REQUEST);
            }
            double latitude;
            try {
                // Trim before parsing
                String trimmedLatitudeStr = latitudeStr.trim();
                if (!hasAtMostFourDecimalPlaces(trimmedLatitudeStr)) {
                     return new Response("Latitude must have at most 4 decimal places and be a valid number format.", Status.BAD_REQUEST);
                }
                latitude = Double.parseDouble(trimmedLatitudeStr);
                if (latitude < -90.0 || latitude > 90.0) {
                    return new Response("Latitude must be between -90.0 and 90.0.", Status.BAD_REQUEST);
                }
            } catch (NumberFormatException ex) {
                return new Response("Latitude must be a valid number.", Status.BAD_REQUEST);
            }

            // 6. Longitude Validation
            if (longitudeStr == null || longitudeStr.trim().isEmpty()) {
                return new Response("Longitude must not be empty.", Status.BAD_REQUEST);
            }
            double longitude;
            try {
                // Trim before parsing
                String trimmedLongitudeStr = longitudeStr.trim();
                 if (!hasAtMostFourDecimalPlaces(trimmedLongitudeStr)) {
                    return new Response("Longitude must have at most 4 decimal places and be a valid number format.", Status.BAD_REQUEST);
                }
                longitude = Double.parseDouble(trimmedLongitudeStr);
                if (longitude < -180.0 || longitude > 180.0) {
                    return new Response("Longitude must be between -180.0 and 180.0.", Status.BAD_REQUEST);
                }
            } catch (NumberFormatException ex) {
                return new Response("Longitude must be a valid number.", Status.BAD_REQUEST);
            }

            // If all validations pass:
            Location newLocation = new Location(airportId, airportName.trim(), airportCity.trim(), 
                                             airportCountry.trim(), latitude, longitude);

//            if (!storage.addAirport(newLocation)) {
//                // Should be caught by airportIdExists, but as a fallback
//                return new Response("Error saving airport. ID conflict might have occurred.", Status.CONFLICT);
//            }

            return new Response("Airport created successfully.", Status.CREATED, newLocation);

        } catch (Exception ex) {
            // Log ex.printStackTrace(); for debugging
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
}

