/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.services.LocationServices;
import core.models.Location;
import core.models.storage.LocationStorage;
import core.models.storage.interfaces.ILocationStorage;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author Admin
 */
public class LocationController implements LocationServices{

    private static ILocationStorage locationStorage = (ILocationStorage) LocationStorage.getInstance();

    
    private static final Pattern AIRPORT_ID_PATTERN = Pattern.compile("[A-Z]{3}");

   
    @Override
    public boolean hasAtMostFourDecimalPlaces(String valueStr) {
        if (valueStr.contains(".")) {
            if (valueStr.toLowerCase().contains("e")) {
                return false;
            }
            String decimalPart = valueStr.substring(valueStr.indexOf(".") + 1);
            return decimalPart.length() <= 4 && decimalPart.matches("\\d+");
        }
        return true;
    }

    private Response validateLocationData(String airportId, String airportName, String airportCity,
                                                 String airportCountry, String latitudeStr, String longitudeStr) {
        if (airportId == null || airportId.trim().isEmpty()) {
            return new Response("Airport ID must not be empty.", Status.BAD_REQUEST);
        }
        String trimmedAirportId = airportId.trim().toUpperCase();
        if (!AIRPORT_ID_PATTERN.matcher(trimmedAirportId).matches()) {
            return new Response("Airport ID must be exactly 3 uppercase letters (e.g., JFK).", Status.BAD_REQUEST);
        }

        if (airportName == null || airportName.trim().isEmpty()) {
            return new Response("Airport name must not be empty.", Status.BAD_REQUEST);
        }
        if (airportCity == null || airportCity.trim().isEmpty()) {
            return new Response("Airport city must not be empty.", Status.BAD_REQUEST);
        }
        if (airportCountry == null || airportCountry.trim().isEmpty()) {
            return new Response("Airport country must not be empty.", Status.BAD_REQUEST);
        }

        if (latitudeStr == null || latitudeStr.trim().isEmpty()) {
            return new Response("Latitude must not be empty.", Status.BAD_REQUEST);
        }
        double latitude;
        try {
            String trimmedLatitudeStr = latitudeStr.trim();
            if (!hasAtMostFourDecimalPlaces(trimmedLatitudeStr)) {
                return new Response("Latitude must have at most 4 decimal places.", Status.BAD_REQUEST);
            }
            latitude = Double.parseDouble(trimmedLatitudeStr);
            if (latitude < -90.0 || latitude > 90.0) {
                return new Response("Latitude must be between -90.0 and 90.0.", Status.BAD_REQUEST);
            }
        } catch (NumberFormatException ex) {
            return new Response("Latitude must be a valid number.", Status.BAD_REQUEST);
        }

        if (longitudeStr == null || longitudeStr.trim().isEmpty()) {
            return new Response("Longitude must not be empty.", Status.BAD_REQUEST);
        }
        double longitude;
        try {
            String trimmedLongitudeStr = longitudeStr.trim();
            if (!hasAtMostFourDecimalPlaces(trimmedLongitudeStr)) {
                return new Response("Longitude must have at most 4 decimal places.", Status.BAD_REQUEST);
            }
            longitude = Double.parseDouble(trimmedLongitudeStr);
            if (longitude < -180.0 || longitude > 180.0) {
                return new Response("Longitude must be between -180.0 and 180.0.", Status.BAD_REQUEST);
            }
        } catch (NumberFormatException ex) {
            return new Response("Longitude must be a valid number.", Status.BAD_REQUEST);
        }
        return null; 
    }
    @Override
    public Response createLocation(String airportIdStr, String airportName, String airportCity, String airportCountry, String latitudeStr, String longitudeStr) {
        try {
            if (airportIdStr == null || airportIdStr.trim().isEmpty()) {
                return new Response("Airport ID must not be empty.", Status.BAD_REQUEST);
            }
            String airportId = airportIdStr.trim().toUpperCase();
            if (!AIRPORT_ID_PATTERN.matcher(airportId).matches()) {
                return new Response("Airport ID must be exactly 3 uppercase letters (e.g., JFK).", Status.BAD_REQUEST);
            }
            if (locationStorage.LocationIdExists(airportId)) { // Usa el método de la interfaz
                return new Response("An airport with ID '" + airportId + "' already exists.", Status.BAD_REQUEST);
            }

            Response validationResponse = validateLocationData(airportId, airportName, airportCity, airportCountry, latitudeStr, longitudeStr);
            if (validationResponse != null) {
                if (validationResponse.getMessage().contains("Airport ID") && !AIRPORT_ID_PATTERN.matcher(airportId).matches()) {
                     return new Response("Airport ID must be exactly 3 uppercase letters (e.g., JFK).", Status.BAD_REQUEST);
                }
                return validationResponse;
            }

            double latitude = Double.parseDouble(latitudeStr.trim());
            double longitude = Double.parseDouble(longitudeStr.trim());

            Location newLocation = new Location(airportId, airportName.trim(), airportCity.trim(),
                                                airportCountry.trim(), latitude, longitude);

            if (!locationStorage.addLocation(newLocation)) {
                return new Response("Failed to add location, ID might already exist or another storage error occurred.", Status.INTERNAL_SERVER_ERROR);
            }

            try {
                Location locationCopy = (Location) newLocation.clone();
                return new Response("Location created successfully.", Status.CREATED, locationCopy);
            } catch (Exception e) {
                System.err.println("Cloning not supported for Location: " + e.getMessage());
                return new Response("Location created, but failed to clone the response object.", Status.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception ex) {
            System.err.println("Unexpected error in createLocation: " + ex.getMessage());
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Response getAllLocations() {
        try {
            ArrayList<Location> locations = locationStorage.getLocations();

            if (locations == null) { // El storage no debería devolver null
                 locations = new ArrayList<>();
            }
             if (locations.isEmpty()){
                 return new Response("No locations found.", Status.NOT_FOUND, new ArrayList<Location>());
            }
            ArrayList<Location> locationCopies = new ArrayList<>();
            for (Location loc : locations) {
                try {
                    locationCopies.add((Location) loc.clone()); 
                } catch (Exception e) {
                    System.err.println("Error cloning location with ID " + loc.getAirportId() + ": " + e.getMessage());
                }
            }
            return new Response("Locations retrieved successfully.", Status.SUCCESS, locationCopies);
        } catch (Exception ex) {
            System.err.println("Unexpected error in getAllLocations: " + ex.getMessage());
            
            return new Response("An unexpected server error occurred while retrieving locations.", Status.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public Response getLocationDisplayInfoForComboBox() {
        try {
        
            ArrayList<Location> locations = locationStorage.getLocations();
            ArrayList<String[]> locationDisplayInfo = new ArrayList<>();
            
            if (locations == null) { locations = new ArrayList<>(); }

            for (Location loc : locations) {
                locationDisplayInfo.add(new String[]{
                        loc.getAirportId(), // Valor
                        loc.getAirportId() + " - " + loc.getAirportName() // Texto a mostrar
                });
            }

            if (locationDisplayInfo.isEmpty() && locations.isEmpty()) {
                 return new Response("No locations found for ComboBox.", Status.NOT_FOUND, new ArrayList<String[]>());
            }
            return new Response("Location info for ComboBox retrieved.", Status.SUCCESS, locationDisplayInfo);
        } catch (Exception ex) {
            System.err.println("Unexpected error in getLocationDisplayInfoForComboBox: " + ex.getMessage());
         
            return new Response("Error retrieving location info for ComboBox.", Status.INTERNAL_SERVER_ERROR);
        }
    }
}

