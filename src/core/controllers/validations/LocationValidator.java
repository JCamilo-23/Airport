package core.controllers.validations; 

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.validations.interfaces.ILocationValidator;
import core.models.storage.interfaces.ILocationStorage;
import java.util.regex.Pattern;

public class LocationValidator implements ILocationValidator {

    private final ILocationStorage locationStorage;
    private static final Pattern AIRPORT_ID_PATTERN = Pattern.compile("[A-Z]{3}");

    public LocationValidator(ILocationStorage locationStorage) {
        this.locationStorage = locationStorage;
    }

    private boolean hasAtMostFourDecimalPlaces(String valueStr) {
        if (valueStr.contains(".")) {
            if (valueStr.toLowerCase().contains("e")) {
                return false;
            }
            String decimalPart = valueStr.substring(valueStr.indexOf(".") + 1);
            return decimalPart.length() <= 4 && decimalPart.matches("\\d+"); 
        }
        return true; 
    }

    @Override
    public Response validateAndCheckAirportId(String airportIdStr) {
        if (airportIdStr == null || airportIdStr.trim().isEmpty()) {
            return new Response("Airport ID must not be empty.", Status.BAD_REQUEST);
        }
        String trimmedAirportId = airportIdStr.trim().toUpperCase();
        if (!AIRPORT_ID_PATTERN.matcher(trimmedAirportId).matches()) {
            return new Response("Airport ID must be exactly 3 uppercase letters (e.g., JFK).", Status.BAD_REQUEST);
        }
        if (locationStorage.LocationIdExists(trimmedAirportId)) { // Assumes ILocationStorage has this
            return new Response("An airport with ID '" + trimmedAirportId + "' already exists.", Status.BAD_REQUEST);
        }
        return new Response("Airport ID is valid and available.", Status.SUCCESS, trimmedAirportId);
    }

    @Override
    public Response validateAirportName(String airportName) {
        if (airportName == null || airportName.trim().isEmpty()) {
            return new Response("Airport name must not be empty.", Status.BAD_REQUEST);
        }
        return null; // Indicates valid
    }

    @Override
    public Response validateAirportCity(String airportCity) {
        if (airportCity == null || airportCity.trim().isEmpty()) {
            return new Response("Airport city must not be empty.", Status.BAD_REQUEST);
        }
        return null; // Indicates valid
    }

    @Override
    public Response validateAirportCountry(String airportCountry) {
        if (airportCountry == null || airportCountry.trim().isEmpty()) {
            return new Response("Airport country must not be empty.", Status.BAD_REQUEST);
        }
        return null; // Indicates valid
    }

    @Override
    public Response validateAndParseLatitude(String latitudeStr) {
        if (latitudeStr == null || latitudeStr.trim().isEmpty()) {
            return new Response("Latitude must not be empty.", Status.BAD_REQUEST);
        }
        double latitude;
        try {
            String trimmedLatitudeStr = latitudeStr.trim();
            if (!hasAtMostFourDecimalPlaces(trimmedLatitudeStr)) {
                return new Response("Latitude must have at most 4 decimal places and be a valid number.", Status.BAD_REQUEST);
            }
            latitude = Double.parseDouble(trimmedLatitudeStr);
            if (latitude < -90.0 || latitude > 90.0) {
                return new Response("Latitude must be between -90.0 and 90.0.", Status.BAD_REQUEST);
            }
        } catch (NumberFormatException ex) {
            return new Response("Latitude must be a valid number.", Status.BAD_REQUEST);
        }
        return new Response("Latitude is valid.", Status.SUCCESS, latitude);
    }

    @Override
    public Response validateAndParseLongitude(String longitudeStr) {
        if (longitudeStr == null || longitudeStr.trim().isEmpty()) {
            return new Response("Longitude must not be empty.", Status.BAD_REQUEST);
        }
        double longitude;
        try {
            String trimmedLongitudeStr = longitudeStr.trim();
            if (!hasAtMostFourDecimalPlaces(trimmedLongitudeStr)) {
                return new Response("Longitude must have at most 4 decimal places and be a valid number.", Status.BAD_REQUEST);
            }
            longitude = Double.parseDouble(trimmedLongitudeStr);
            if (longitude < -180.0 || longitude > 180.0) {
                return new Response("Longitude must be between -180.0 and 180.0.", Status.BAD_REQUEST);
            }
        } catch (NumberFormatException ex) {
            return new Response("Longitude must be a valid number.", Status.BAD_REQUEST);
        }
        return new Response("Longitude is valid.", Status.SUCCESS, longitude);
    }
}
