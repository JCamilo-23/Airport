package core.controllers.validations.interfaces;

import core.controllers.utils.Response;

import core.models.Location;

public interface IFlightValidator {

    Response validateFlightId(String flightId);

    Response validateAndGetPlane(String planeId);

    Response validateAndGetLocation(String locationId, String locationType); // type like "Departure", "Arrival", "Scale"

    Response validateDepartureAndArrival(Location departureLocation, Location arrivalLocation);
    
    Response validateScaleLocation(Location scaleLocation, Location departureLocation, Location arrivalLocation);

    Response validateAndParseDepartureDateTime(String year, String month, String day, String hour, String minutes);

    Response validateAndParseFlightLegDuration(String hoursStr, String minutesStr, String legName, boolean isOptional, Location scaleLocationForOptionalCheck);
}