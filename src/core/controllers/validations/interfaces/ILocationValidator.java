package core.controllers.validations.interfaces; 

import core.controllers.utils.Response;

public interface ILocationValidator {

    Response validateAndCheckAirportId(String airportIdStr);

    Response validateAirportName(String airportName);

    Response validateAirportCity(String airportCity);

    Response validateAirportCountry(String airportCountry);

    Response validateAndParseLatitude(String latitudeStr);

    Response validateAndParseLongitude(String longitudeStr);
}
