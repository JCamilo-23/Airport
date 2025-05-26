package core.controllers.validations.interfaces; 

import core.controllers.utils.Response;

public interface IPlaneValidator {

    Response validatePlaneId(String idStr);

    Response validateBrand(String brand);

    Response validateModel(String model);

    Response validateAndParseMaxCapacity(String maxCapacityStr);

    Response validateAirline(String airline);
}