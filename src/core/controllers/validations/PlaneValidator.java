package core.controllers.validations;

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.validations.interfaces.IPlaneValidator;
import core.models.storage.interfaces.IPlaneStorage;

public class PlaneValidator implements IPlaneValidator {

    private final IPlaneStorage planeStorage;

    public PlaneValidator(IPlaneStorage planeStorage) {
        this.planeStorage = planeStorage;
    }

    @Override
    public Response validatePlaneId(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return new Response("Airplane ID must not be empty.", Status.BAD_REQUEST);
        }
        String trimmedId = idStr.trim().toUpperCase();
        if (!trimmedId.matches("[A-Z]{2}\\d{5}")) {
            return new Response("Airplane ID must follow the format XXYYYYY (e.g., AA12345).", Status.BAD_REQUEST);
        }
        if (planeStorage.planeIdExists(trimmedId)) { // Asume que IPlaneStorage tiene este método
            return new Response("An airplane with the ID '" + trimmedId + "' already exists.", Status.BAD_REQUEST);
        }
        return new Response("Plane ID is valid.", Status.SUCCESS, trimmedId);
    }

    @Override
    public Response validateBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            return new Response("Airplane brand must not be empty.", Status.BAD_REQUEST);
        }
        return null; // Válido
    }

    @Override
    public Response validateModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            return new Response("Airplane model must not be empty.", Status.BAD_REQUEST);
        }
        return null; // Válido
    }

    @Override
    public Response validateAndParseMaxCapacity(String maxCapacityStr) {
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
        return new Response("Max capacity is valid.", Status.SUCCESS, maxCapacity);
    }

    @Override
    public Response validateAirline(String airline) {
        if (airline == null || airline.trim().isEmpty()) {
            return new Response("Airline must not be empty.", Status.BAD_REQUEST);
        }
        return null; 
    }
}