package core.controllers; // Or core.services.implementations if you rename it

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.services.LocationServices; // Assuming this is your service interface
import core.controllers.validations.LocationValidator;
import core.models.Location;
import core.models.storage.LocationStorage;
import core.models.storage.interfaces.ILocationStorage;

import java.util.ArrayList;

public class LocationController implements LocationServices {

    private final ILocationStorage locationStorage = (ILocationStorage) LocationStorage.getInstance();
    private final LocationValidator locationValidator;

    public LocationController() {
        this.locationValidator = new LocationValidator(locationStorage);
    }

    @Override
    public Response createLocation(String airportIdStr, String airportName, String airportCity,
                                   String airportCountry, String latitudeStr, String longitudeStr) {
        try {
            Response idValidationResponse = locationValidator.validateAndCheckAirportId(airportIdStr);
            if (idValidationResponse.getStatus() != Status.SUCCESS) {
                return idValidationResponse;
            }
            String validatedAirportId = (String) idValidationResponse.getObject();

            Response nameValidationResponse = locationValidator.validateAirportName(airportName);
            if (nameValidationResponse != null) return nameValidationResponse; // Null means valid

            Response cityValidationResponse = locationValidator.validateAirportCity(airportCity);
            if (cityValidationResponse != null) return cityValidationResponse;

            Response countryValidationResponse = locationValidator.validateAirportCountry(airportCountry);
            if (countryValidationResponse != null) return countryValidationResponse;

            Response latitudeValidationResponse = locationValidator.validateAndParseLatitude(latitudeStr);
            if (latitudeValidationResponse.getStatus() != Status.SUCCESS) {
                return latitudeValidationResponse;
            }
            double latitude = (double) latitudeValidationResponse.getObject();

            Response longitudeValidationResponse = locationValidator.validateAndParseLongitude(longitudeStr);
            if (longitudeValidationResponse.getStatus() != Status.SUCCESS) {
                return longitudeValidationResponse;
            }
            double longitude = (double) longitudeValidationResponse.getObject();

            Location newLocation = new Location(validatedAirportId, airportName.trim(), airportCity.trim(),
                                                airportCountry.trim(), latitude, longitude);

            if (!locationStorage.addLocation(newLocation)) {
                return new Response("Failed to add location. Storage error occurred.", Status.INTERNAL_SERVER_ERROR);
            }

            Location locationCopy = (Location) newLocation.clone(); // Assumes Location implements Cloneable
            return new Response("Location created successfully.", Status.CREATED, locationCopy);

        }catch (Exception e) {
            System.err.println("Cloning not supported for Location: " + e.getMessage());
 
            return new Response("Location created, but failed to clone the response object.", Status.INTERNAL_SERVER_ERROR);
        }
  
        
    }

    @Override
    public Response getAllLocations() {
        try {
            ArrayList<Location> locations = locationStorage.getLocations();

            if (locations == null) {
                locations = new ArrayList<>();
            }
            if (locations.isEmpty()) {
                return new Response("No locations found.", Status.NOT_FOUND, new ArrayList<Location>());
            }

            ArrayList<Location> locationCopies = new ArrayList<>();
            for (Location loc : locations) {
                locationCopies.add((Location) loc.clone()); // Assumes Location implements Cloneable
            }
            return new Response("Locations retrieved successfully.", Status.SUCCESS, locationCopies);
        } catch (Exception e) {
            System.err.println("Cloning error in getAllLocations: " + e.getMessage());
            return new Response("Error retrieving locations: cloning failed for one or more locations.", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response getLocationDisplayInfoForComboBox() {
        try {
            ArrayList<Location> locations = locationStorage.getLocations();
            ArrayList<String[]> locationDisplayInfo = new ArrayList<>();

            if (locations == null) {
                locations = new ArrayList<>();
            }

            for (Location loc : locations) {
                // Array: [ValueForComboBox, TextToDisplayInComboBox]
                locationDisplayInfo.add(new String[]{
                        loc.getAirportId(), // Value
                        loc.getAirportId() + " - " + loc.getAirportName() // Display Text
                });
            }

            if (locationDisplayInfo.isEmpty() && locations.isEmpty()) { // Check if truly no locations
                return new Response("No locations found for ComboBox.", Status.NOT_FOUND, new ArrayList<String[]>());
            }
            return new Response("Location info for ComboBox retrieved.", Status.SUCCESS, locationDisplayInfo);
        } catch (Exception ex) {
            System.err.println("Unexpected error in getLocationDisplayInfoForComboBox: " + ex.getMessage());
            ex.printStackTrace();
            return new Response("Error retrieving location info for ComboBox: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
}
