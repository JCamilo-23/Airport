package core.controllers;

import core.controllers.services.PlaneServices;
import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.validations.PlaneValidator;
import core.models.Plane;
import core.models.storage.PlaneStorage;
import core.models.storage.interfaces.IPlaneStorage;
import java.util.ArrayList;

public class PlaneController implements PlaneServices {

    private final IPlaneStorage planeStorage = (IPlaneStorage) PlaneStorage.getInstance();
    private final PlaneValidator planeValidator;

    public PlaneController() {
        this.planeValidator = new PlaneValidator(this.planeStorage);
    }

    @Override
    public Response createPlane(String idStr, String brand, String model, String maxCapacityStr, String airline) {
        try {
            Response idValidationResponse = planeValidator.validatePlaneId(idStr);
            if (idValidationResponse.getStatus() != Status.SUCCESS) {
                return idValidationResponse;
            }
            String validatedId = (String) idValidationResponse.getObject();

            Response brandValidationResponse = planeValidator.validateBrand(brand);
            if (brandValidationResponse != null) return brandValidationResponse;

            Response modelValidationResponse = planeValidator.validateModel(model);
            if (modelValidationResponse != null) return modelValidationResponse;

            Response capacityValidationResponse = planeValidator.validateAndParseMaxCapacity(maxCapacityStr);
            if (capacityValidationResponse.getStatus() != Status.SUCCESS) {
                return capacityValidationResponse;
            }
            int maxCapacity = (int) capacityValidationResponse.getObject();

            Response airlineValidationResponse = planeValidator.validateAirline(airline);
            if (airlineValidationResponse != null) return airlineValidationResponse;

            Plane newAirplane = new Plane(validatedId, brand.trim(), model.trim(), maxCapacity, airline.trim());

            if (!planeStorage.addPlane(newAirplane)) { 
                return new Response("Error saving airplane to storage.", Status.INTERNAL_SERVER_ERROR);
            }

            Plane airplaneCopy = (Plane) newAirplane.clone(); 
            return new Response("Plane created successfully.", Status.CREATED, airplaneCopy); // Mensaje corregido

        }catch (Exception e) {
             System.err.println("Cloning not supported for Plane: " + e.getMessage());
             return new Response("Plane created, but failed to clone the response object.", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response getAllPlanes() {
        try {
            ArrayList<Plane> planes = planeStorage.getPlanes(); // Usa la instancia inyectada

            if (planes == null) {
                planes = new ArrayList<>();
            }

            if (planes.isEmpty()) {
                return new Response("No planes found.", Status.NOT_FOUND, new ArrayList<Plane>());
            }

            ArrayList<Plane> planeCopies = new ArrayList<>();
            for (Plane p : planes) {
                planeCopies.add((Plane) p.clone());
            }
            return new Response("Planes retrieved successfully.", Status.SUCCESS, planeCopies);
        } catch (Exception e) {
            System.err.println("Cloning error in getAllPlanes: " + e.getMessage());
            return new Response("Error retrieving planes: cloning failed.", Status.INTERNAL_SERVER_ERROR);
        }
    }
}