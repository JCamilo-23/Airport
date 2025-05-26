package core.controllers.validations; // o core.controllers.validation

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.validations.interfaces.IFlightValidator;
import core.models.Location;
import core.models.Plane;
import core.models.storage.interfaces.IFlightStorage;
import core.models.storage.interfaces.ILocationStorage;
import core.models.storage.interfaces.IPlaneStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class FlightValidator implements IFlightValidator {

    private static final Pattern FLIGHT_ID_PATTERN = Pattern.compile("^[A-Z]{3}\\d{3}$");
    private final IFlightStorage flightStorage;
    private final IPlaneStorage planeStorage;
    private final ILocationStorage locationStorage;

    public FlightValidator(IFlightStorage flightStorage, IPlaneStorage planeStorage, ILocationStorage locationStorage) {
        this.flightStorage = flightStorage;
        this.planeStorage = planeStorage;
        this.locationStorage = locationStorage;
    }

    @Override
    public Response validateFlightId(String flightId) {
        if (flightId == null || flightId.trim().isEmpty()) {
            return new Response("Flight ID must not be empty.", Status.BAD_REQUEST);
        }
        String trimmedFlightId = flightId.trim().toUpperCase();
        if (!FLIGHT_ID_PATTERN.matcher(trimmedFlightId).matches()) {
            return new Response("Flight ID must follow the format XXXYYY (e.g., AAA123).", Status.BAD_REQUEST);
        }
        if (flightStorage.flightIdExists(trimmedFlightId)) {
            return new Response("A flight with ID '" + trimmedFlightId + "' already exists.", Status.BAD_REQUEST);
        }
        return new Response("Flight ID is valid.", Status.SUCCESS, trimmedFlightId);
    }

    @Override
    public Response validateAndGetPlane(String planeId) {
        if (planeId == null || planeId.trim().isEmpty()) {
            return new Response("Plane ID must not be empty.", Status.BAD_REQUEST);
        }
        Plane plane = planeStorage.getPlane(planeId.trim().toUpperCase());
        if (plane == null) {
            return new Response("Plane with ID '" + planeId.trim().toUpperCase() + "' not found.", Status.NOT_FOUND);
        }
        return new Response("Plane found.", Status.SUCCESS, plane);
    }

    @Override
    public Response validateAndGetLocation(String locationId, String locationType) {
        if (locationId == null || locationId.trim().isEmpty()) {
            if (locationType.equalsIgnoreCase("Scale") && (locationId == null || locationId.trim().isEmpty() || locationId.equalsIgnoreCase("Location"))) {
                // Scale location is optional and default combobox text "Location" implies no scale
                return new Response(locationType + " location not provided (optional).", Status.SUCCESS, null);
            }
            return new Response(locationType + " location ID must not be empty.", Status.BAD_REQUEST);
        }
        // Handle default combobox text for scale explicitly if it wasn't caught as empty
        if (locationType.equalsIgnoreCase("Scale") && locationId.equalsIgnoreCase("Location")){
            return new Response(locationType + " location not provided (optional).", Status.SUCCESS, null);
        }

        Location location = locationStorage.getLocation(locationId.trim().toUpperCase());
        if (location == null) {
            return new Response(locationType + " location with ID '" + locationId.trim().toUpperCase() + "' not found.", Status.NOT_FOUND);
        }
        return new Response(locationType + " location found.", Status.SUCCESS, location);
    }
    
    @Override
    public Response validateDepartureAndArrival(Location departureLocation, Location arrivalLocation) {
        if (departureLocation.getAirportId().equals(arrivalLocation.getAirportId())) {
            return new Response("Departure and arrival locations cannot be the same.", Status.BAD_REQUEST);
        }
        return new Response("Departure and arrival locations are valid.", Status.SUCCESS);
    }

    @Override
    public Response validateScaleLocation(Location scaleLocation, Location departureLocation, Location arrivalLocation) {
        if (scaleLocation != null) {
            if (scaleLocation.getAirportId().equals(departureLocation.getAirportId()) ||
                scaleLocation.getAirportId().equals(arrivalLocation.getAirportId())) {
                return new Response("Scale location cannot be the same as departure or arrival location.", Status.BAD_REQUEST);
            }
        }
        return new Response("Scale location is valid.", Status.SUCCESS);
    }


    @Override
    public Response validateAndParseDepartureDateTime(String yearStr, String monthStr, String dayStr, String hourStr, String minutesStr) {
        try {
            int year = Integer.parseInt(yearStr.trim());
            int month = Integer.parseInt(monthStr.trim());
            int day = Integer.parseInt(dayStr.trim());
            int hour = Integer.parseInt(hourStr.trim());
            int minutes = Integer.parseInt(minutesStr.trim());
            LocalDateTime departureDateTime = LocalDateTime.of(year, month, day, hour, minutes);
            return new Response("Departure date and time parsed successfully.", Status.SUCCESS, departureDateTime);
        } catch (DateTimeParseException | NumberFormatException e) {
            return new Response("Date/Time components must be valid numbers and form a valid date/time. Format YYYY/MM/DD HH:MM", Status.BAD_REQUEST);
        }
    }

    @Override
    public Response validateAndParseFlightLegDuration(String hoursStr, String minutesStr, String legName, boolean isOptional, Location scaleLocationForOptionalCheck) {
        int hours, minutes;
        try {
            // If it's the optional second leg and no scale location, duration must be effectively zero or not provided.
            if (isOptional && scaleLocationForOptionalCheck == null) {
                boolean hoursProvided = hoursStr != null && !hoursStr.trim().isEmpty();
                boolean minutesProvided = minutesStr != null && !minutesStr.trim().isEmpty();

                hours = hoursProvided ? Integer.parseInt(hoursStr.trim()) : 0;
                minutes = minutesProvided ? Integer.parseInt(minutesStr.trim()) : 0;

                if (hours != 0 || minutes != 0) {
                    return new Response("Duration for " + legName + " must be 00:00 if no scale location is provided.", Status.BAD_REQUEST);
                }
                 return new Response(legName + " duration is valid (00:00 as no scale).", Status.SUCCESS, new int[]{0, 0});
            }

            // Mandatory leg or optional second leg with a scale location
            if (hoursStr == null || hoursStr.trim().isEmpty() || minutesStr == null || minutesStr.trim().isEmpty()){
                return new Response("Hours and minutes for " + legName + " duration must not be empty.", Status.BAD_REQUEST);
            }

            hours = Integer.parseInt(hoursStr.trim());
            minutes = Integer.parseInt(minutesStr.trim());

        } catch (NumberFormatException ex) {
            return new Response("Durations for " + legName + " must be valid numbers.", Status.BAD_REQUEST);
        }

        if (hours < 0 || minutes < 0) {
            return new Response("Durations for " + legName + " cannot be negative.", Status.BAD_REQUEST);
        }
        if (minutes >= 60) {
            return new Response("Minutes for " + legName + " must be less than 60.", Status.BAD_REQUEST);
        }
        if (hours == 0 && minutes == 0 && (!isOptional || scaleLocationForOptionalCheck != null) ) { // Duration must be > 0 if it's the first leg or the second leg with a scale
             return new Response("Duration for " + legName + " must be greater than 00:00.", Status.BAD_REQUEST);
        }
        return new Response(legName + " duration parsed successfully.", Status.SUCCESS, new int[]{hours, minutes});
    }
}