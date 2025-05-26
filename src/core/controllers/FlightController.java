package core.controllers; 

import core.controllers.services.FlightServices;
import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.validations.FlightValidator;
import core.models.flight.Delay;
import core.models.flight.Flight;
import core.models.Location;
import core.models.Plane;
import core.models.person.Passenger;
import core.models.storage.FlightStorage;
import core.models.storage.LocationStorage;
import core.models.storage.PassengerStorage;
import core.models.storage.PlaneStorage;
import core.models.storage.interfaces.IFlightStorage;
import core.models.storage.interfaces.ILocationStorage;
import core.models.storage.interfaces.IPassengerStorage;
import core.models.storage.interfaces.IPlaneStorage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

public class FlightController implements FlightServices {

    private final IPassengerStorage passengerStorage = (IPassengerStorage) PassengerStorage.getInstance();
    private final IFlightStorage flightStorage = (IFlightStorage) FlightStorage.getInstance();
    private final ILocationStorage locationStorage = (ILocationStorage) LocationStorage.getInstance();
        private final IPlaneStorage planeStorage = (IPlaneStorage) PlaneStorage.getInstance();
    private final FlightValidator flightValidator;

    public FlightController() {
        this.flightValidator = new FlightValidator(flightStorage,planeStorage,locationStorage);
    }

    @Override
    public Response createFlight(String flightIdStr, String planeIdStr,
                                 String departureLocationIdStr, String arrivalLocationIdStr,
                                 String scaleLocationIdStr,
                                 String yearStr, String monthStr, String dayStr, String hourStr, String minutesStr,
                                 String leg1HoursStr, String leg1MinutesStr,
                                 String leg2HoursStr, String leg2MinutesStr) {
        try {
            Response idResponse = flightValidator.validateFlightId(flightIdStr);
            if (idResponse.getStatus() != Status.SUCCESS) return idResponse;
            String validatedFlightId = (String) idResponse.getObject();

            Response planeResponse = flightValidator.validateAndGetPlane(planeIdStr);
            if (planeResponse.getStatus() != Status.SUCCESS) return planeResponse;
            Plane plane = (Plane) planeResponse.getObject();

            Response depLocResponse = flightValidator.validateAndGetLocation(departureLocationIdStr, "Departure");
            if (depLocResponse.getStatus() != Status.SUCCESS) return depLocResponse;
            Location departureLocation = (Location) depLocResponse.getObject();

            Response arrLocResponse = flightValidator.validateAndGetLocation(arrivalLocationIdStr, "Arrival");
            if (arrLocResponse.getStatus() != Status.SUCCESS) return arrLocResponse;
            Location arrivalLocation = (Location) arrLocResponse.getObject();
            
            Response depArrValidationResponse = flightValidator.validateDepartureAndArrival(departureLocation, arrivalLocation);
            if (depArrValidationResponse.getStatus() != Status.SUCCESS) return depArrValidationResponse;

            Response scaleLocResponse = flightValidator.validateAndGetLocation(scaleLocationIdStr, "Scale");
            if (scaleLocResponse.getStatus() == Status.NOT_FOUND) return scaleLocResponse; 
            Location scaleLocation = (Location) scaleLocResponse.getObject(); 

            Response scaleValidationResponse = flightValidator.validateScaleLocation(scaleLocation, departureLocation, arrivalLocation);
            if (scaleValidationResponse.getStatus() != Status.SUCCESS) return scaleValidationResponse;

            Response dateTimeResponse = flightValidator.validateAndParseDepartureDateTime(yearStr, monthStr, dayStr, hourStr, minutesStr);
            if (dateTimeResponse.getStatus() != Status.SUCCESS) return dateTimeResponse;
            LocalDateTime departureDateTime = (LocalDateTime) dateTimeResponse.getObject();

            Response leg1Response = flightValidator.validateAndParseFlightLegDuration(leg1HoursStr, leg1MinutesStr, "first leg", false, null);
            if (leg1Response.getStatus() != Status.SUCCESS) return leg1Response;
            int[] leg1Duration = (int[]) leg1Response.getObject();

            Response leg2Response = flightValidator.validateAndParseFlightLegDuration(leg2HoursStr, leg2MinutesStr, "second leg", true, scaleLocation);
            if (leg2Response.getStatus() != Status.SUCCESS) return leg2Response;
            int[] leg2Duration = (int[]) leg2Response.getObject();

            Flight newFlight;
            if (scaleLocation != null) {
                newFlight = new Flight(validatedFlightId, plane, departureLocation, scaleLocation, arrivalLocation,
                                       departureDateTime, leg2Duration[0], leg2Duration[1], leg1Duration[0], leg1Duration[1]);
            } else {
                newFlight = new Flight(validatedFlightId, plane, departureLocation, arrivalLocation,
                                       departureDateTime, leg1Duration[0], leg1Duration[1]);
            }

            if (!flightStorage.addFlight(newFlight)) {
                return new Response("Failed to save the flight. Storage error or conflict.", Status.INTERNAL_SERVER_ERROR);
            }

            Flight flightCopy = (Flight) newFlight.clone(); 
            return new Response("Flight created successfully.", Status.CREATED, flightCopy);

        }catch (Exception e) {
            System.err.println("Cloning not supported for Flight: " + e.getMessage());
            return new Response("Flight created, but failed to clone the response object.", Status.INTERNAL_SERVER_ERROR);
        }
        
    }

    @Override
    public Response getAllFlights() {
        try {
            ArrayList<Flight> flights = flightStorage.getFlights();
            if (flights == null) flights = new ArrayList<>();

            if (flights.isEmpty()) {
                return new Response("No flights found.", Status.NOT_FOUND, new ArrayList<Flight>());
            }

            ArrayList<Flight> flightCopies = new ArrayList<>();
            for (Flight f : flights) {
                flightCopies.add((Flight) f.clone()); 
            }
            return new Response("Flights retrieved successfully.", Status.SUCCESS, flightCopies);
        } catch (Exception e) {
            System.err.println("Cloning error in getAllFlights: " + e.getMessage());
            return new Response("Error retrieving flights: cloning failed for one or more flights.", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response addPassengerToFlight(String flightId, long passengerId) {
        try {
        
            if (flightId == null || flightId.trim().isEmpty()) {
                return new Response("Flight ID must not be empty for adding passenger.", Status.BAD_REQUEST);
            }
            
            if (passengerId <=0) {
                 return new Response("Passenger ID must be valid.", Status.BAD_REQUEST);
            }

            Flight flight = flightStorage.getFlight(flightId.trim().toUpperCase());
            if (flight == null) {
                return new Response("Flight with ID '" + flightId + "' not found.", Status.NOT_FOUND);
            }

            Passenger passenger = passengerStorage.getPassengerById(passengerId);
            if (passenger == null) {
                return new Response("Passenger with ID '" + passengerId + "' not found.", Status.NOT_FOUND);
            }

            if (flight.getPlane() == null) {
                 return new Response("Flight " + flightId + " does not have an assigned plane. Cannot determine capacity.", Status.INTERNAL_SERVER_ERROR);
            }
            if (flight.getNumPassengers() >= flight.getPlane().getMaxCapacity()) {
                return new Response("Flight is full. Cannot add more passengers.", Status.BAD_REQUEST);
            }

            if (passenger.getFlights().stream().anyMatch(f -> f.getId().equals(flight.getId()))) {
                return new Response("Passenger " + passengerId + " is already on flight " + flightId + ".", Status.BAD_REQUEST);
            }

            flight.addPassenger(passenger);
            passenger.addFlight(flight);

      
            boolean flightUpdated = flightStorage.updateFlight(flight);
            boolean passengerUpdated = passengerStorage.updatePassenger(passenger);

            if (!flightUpdated || !passengerUpdated) {
                
                System.err.println("Failed to fully persist passenger addition to flight. Data may be inconsistent.");
             
                return new Response("Error saving changes after adding passenger to flight.", Status.INTERNAL_SERVER_ERROR);
            }

            return new Response("Passenger added to flight successfully.", Status.SUCCESS);
        } catch (Exception ex) {
            System.err.println("Unexpected error in addPassengerToFlight service: " + ex.getMessage());
            ex.printStackTrace();
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response delayFlight(String flightId, String hoursStr, String minutesStr) {
        try {
            if (flightId == null || flightId.trim().isEmpty()) {
                return new Response("Flight ID must not be empty for delay.", Status.BAD_REQUEST);
            }
            Flight flight = flightStorage.getFlight(flightId.trim().toUpperCase());
            if (flight == null) {
                return new Response("Flight with ID '" + flightId + "' not found.", Status.NOT_FOUND);
            }

            int hours, minutes;
            try {
                hours = Integer.parseInt(hoursStr.trim());
                minutes = Integer.parseInt(minutesStr.trim());
            } catch (NumberFormatException ex) {
                return new Response("Delay hours and minutes must be valid numbers.", Status.BAD_REQUEST);
            }

            if (hours < 0 || minutes < 0 || (hours == 0 && minutes == 0)) {
                return new Response("Delay time must be greater than 00:00.", Status.BAD_REQUEST);
            }
            if (minutes >= 60) {
                return new Response("Delay minutes must be less than 60.", Status.BAD_REQUEST);
            }

            Delay flightDelay = new Delay(flight);
            flightDelay.delay(hours, minutes); 

            if (!flightStorage.updateFlight(flight)) { // Persist the changes
                return new Response("Failed to save flight delay.", Status.INTERNAL_SERVER_ERROR);
            }

            return new Response("Flight delayed successfully.", Status.SUCCESS);
        } catch (Exception ex) {
            System.err.println("Unexpected error in delayFlight service: " + ex.getMessage());
            ex.printStackTrace();
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response getFlightsForPassenger(long passengerId) {
        try {
            if (passengerId <=0) { // Basic validation
                 return new Response("Invalid Passenger ID.", Status.BAD_REQUEST);
            }
            Passenger passenger = passengerStorage.getPassengerById(passengerId);
            if (passenger == null) {
                return new Response("Passenger with ID '" + passengerId + "' not found.", Status.NOT_FOUND);
            }

            ArrayList<Flight> flights = new ArrayList<>(passenger.getFlights()); 
            flights.sort(Comparator.comparing(Flight::getDepartureDate)); 

            ArrayList<Flight> flightCopies = new ArrayList<>();
            for (Flight flight : flights) {
                flightCopies.add((Flight) flight.clone()); 
            }
            return new Response("Passenger flights retrieved successfully.", Status.SUCCESS, flightCopies);
        } catch (Exception e) {
            System.err.println("Cloning error in getFlightsForPassenger: " + e.getMessage());
            return new Response("Error retrieving passenger flights: cloning failed.", Status.INTERNAL_SERVER_ERROR);
        }
    }
}