package core.controllers;

import core.controllers.services.PassengerServices;
import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.validations.PassengerValidator;
import core.models.calculate.PassengerFormats;
import core.models.flight.Flight;
import core.models.person.Passenger;
import core.models.storage.FlightStorage;
import core.models.storage.PassengerStorage;
import core.models.storage.interfaces.IFlightStorage;
import core.models.storage.interfaces.IPassengerStorage;

import java.time.LocalDate;
import java.util.ArrayList;

public class PassengerController implements PassengerServices {

    private final IPassengerStorage passengerStorage = (IPassengerStorage) PassengerStorage.getInstance();
    private final IFlightStorage flightStorage = (IFlightStorage) FlightStorage.getInstance();
    private final PassengerValidator passengerValidator;

    public PassengerController() {
        this.passengerValidator = new PassengerValidator();
    }

    @Override
    public Response registerPassenger(String idStr, String firstName, String lastName,
                                      String yearStr, String monthStr, String dayStr,
                                      String phoneCodeStr, String phoneNumberStr, String country) {
        try {
            // Validate and parse ID
            Response idValidationResponse = passengerValidator.validateAndParsePassengerId(idStr);
            if (idValidationResponse.getStatus() != Status.SUCCESS) {
                return idValidationResponse;
            }
            long id = (long) idValidationResponse.getObject();

            // Check if passenger ID already exists
            Response idExistsResponse = passengerValidator.checkPassengerIdExists(id, passengerStorage);
            if (idExistsResponse != null) { // If not null, it's an error response
                return idExistsResponse;
            }

            // Validate base passenger 
            Response baseValidationResponse = passengerValidator.validatePassengerBase(
                firstName, lastName, yearStr, monthStr, dayStr, phoneCodeStr, phoneNumberStr, country);
            if (baseValidationResponse.getStatus() != Status.SUCCESS) {
                return baseValidationResponse;
            }
            
            // Retrieve parsed date and phone from validator responses (if they were put in  field)
            Response dateParseResponse = passengerValidator.validateAndParseDateOfBirth(yearStr, monthStr, dayStr);
            LocalDate dateOfBirth = (LocalDate) dateParseResponse.getObject(); // Assumes  is LocalDate

            Response phoneParseResponse = passengerValidator.validateAndParsePhoneNumber(phoneCodeStr, phoneNumberStr);
            Object[] phoneParts = (Object[]) phoneParseResponse.getObject(); // Assumes  is Object[]{int, long}
            int phoneCode = (int) phoneParts[0];
            long phoneNumber = (long) phoneParts[1];


            Passenger newPassenger = new Passenger(id, firstName.trim(), lastName.trim(), dateOfBirth, phoneCode, phoneNumber, country.trim());

            if (!passengerStorage.addPassenger(newPassenger)) {
                return new Response("Error saving passenger, ID might be duplicated or another issue occurred.", Status.INTERNAL_SERVER_ERROR);
            }
            
            // Clone for response (Prototype Pattern)
            Passenger passengerCopy = (Passenger) newPassenger.clone(); 
            return new Response("Passenger created successfully.", Status.CREATED, passengerCopy);

        }catch (Exception e) {
            System.err.println("Cloning not supported for Passenger: " + e.getMessage());
            return new Response("Passenger created, but failed to clone the response object.", Status.INTERNAL_SERVER_ERROR);
        }
        
    }

    @Override
    public Response updatePassenger(String idStrToUpdate, String newFirstName, String newLastName,
                                    String newYearStr, String newMonthStr, String newDayStr,
                                    String newPhoneCodeStr, String newPhoneNumberStr, String newCountry) {
        try {
            // Validate and parse ID for update
            Response idValidationResponse = passengerValidator.validateAndParsePassengerId(idStrToUpdate);
            if (idValidationResponse.getStatus() != Status.SUCCESS) {
                return idValidationResponse;
            }
            long idToUpdate = (long) idValidationResponse.getObject();

            Passenger passengerToUpdate = passengerStorage.getPassengerById(idToUpdate);
            if (passengerToUpdate == null) {
                return new Response("Passenger with ID " + idToUpdate + " not found.", Status.NOT_FOUND);
            }

            // Validate new base passenger 
            Response baseValidationResponse = passengerValidator.validatePassengerBase(
                newFirstName, newLastName, newYearStr, newMonthStr, newDayStr, 
                newPhoneCodeStr, newPhoneNumberStr, newCountry);
            if (baseValidationResponse.getStatus() != Status.SUCCESS) {
                return baseValidationResponse;
            }

            // Retrieve parsed date and phone
            Response dateParseResponse = passengerValidator.validateAndParseDateOfBirth(newYearStr, newMonthStr, newDayStr);
            LocalDate newDateOfBirth = (LocalDate) dateParseResponse.getObject();

            Response phoneParseResponse = passengerValidator.validateAndParsePhoneNumber(newPhoneCodeStr, newPhoneNumberStr);
            Object[] phoneParts = (Object[]) phoneParseResponse.getObject();
            int newPhoneCode = (int) phoneParts[0];
            long newPhoneNumber = (long) phoneParts[1];
            
            passengerToUpdate.setFirstname(newFirstName.trim());
            passengerToUpdate.setLastname(newLastName.trim());
            passengerToUpdate.setBirthDate(newDateOfBirth);
            passengerToUpdate.setCountryPhoneCode(newPhoneCode);
            passengerToUpdate.setPhone(newPhoneNumber);
            passengerToUpdate.setCountry(newCountry.trim());

            if (!passengerStorage.updatePassenger(passengerToUpdate)) {
                return new Response("Error updating passenger in storage.", Status.INTERNAL_SERVER_ERROR);
            }

            Passenger passengerCopy = (Passenger) passengerToUpdate.clone();
            return new Response("Passenger updated successfully.", Status.SUCCESS, passengerCopy);

        } catch (Exception e) {
            System.err.println("Cloning not supported for Passenger during update: " + e.getMessage());
            return new Response("Passenger updated, but failed to clone the response object.", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response getAllPassengers() {
        try {
            ArrayList<Passenger> passengers = passengerStorage.getPassengers(); // Assuming sorted by ID as per original comment

            if (passengers == null) { // Storage should ideally not return null
                passengers = new ArrayList<>(); 
            }
            
            if (passengers.isEmpty()) {
                return new Response("No passengers found.", Status.NOT_FOUND, new ArrayList<Passenger>());
            }

            ArrayList<Passenger> passengerCopies = new ArrayList<>();
            for (Passenger p : passengers) {
                try {
                    passengerCopies.add((Passenger) p.clone());
                } catch (Exception e) {
                    System.err.println("Error cloning passenger with ID " + p.getId() + " in getAllPassengers: " + e.getMessage());
                    // Decide handling: skip, add original (risky), or return error.
                    // For now, skipping the problematic one.
                }
            }
            return new Response("Passengers retrieved successfully.", Status.SUCCESS, passengerCopies);
        } catch (Exception ex) {
            System.err.println("Unexpected error in getAllPassengers: " + ex.getMessage());
            ex.printStackTrace();
            return new Response("An unexpected server error occurred while retrieving passengers.", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response assignFlight(String passengerIdStr, String flightIdFromComboBox) {
        try {
            Response passengerIdResponse = passengerValidator.validateAndParsePassengerId(passengerIdStr);
            if(passengerIdResponse.getStatus() != Status.SUCCESS){
                return passengerIdResponse;
            }
            long passengerId = (long) passengerIdResponse.getObject();

            if (flightIdFromComboBox == null || flightIdFromComboBox.trim().isEmpty() || 
                flightIdFromComboBox.equalsIgnoreCase("Flight") || flightIdFromComboBox.equalsIgnoreCase("ID")) { // Example placeholder check
                return new Response("A valid Flight ID must be selected/provided.", Status.BAD_REQUEST);
            }
            String flightId = flightIdFromComboBox.trim().toUpperCase(); // Assuming flight IDs are uppercase

            Flight flight = flightStorage.getFlight(flightId);
            if (flight == null) {
                return new Response("Flight with ID '" + flightId + "' not found.", Status.NOT_FOUND);
            }

            Passenger passenger = passengerStorage.getPassengerById(passengerId);
            if (passenger == null) {
                return new Response("Passenger with ID '" + passengerId + "' not found.", Status.NOT_FOUND);
            }

            if (flight.getNumPassengers() >= flight.getPlane().getMaxCapacity()) {
                return new Response("Flight " + flightId + " is full. Cannot add passenger " + passengerId + ".", Status.BAD_REQUEST);
            }
            
            // Check if passenger already assigned to this flight
            for(Flight f: passenger.getFlights()){ // Assumes passenger.getFlights() returns a list of flights passenger is on
                if(f.getId().equals(flightId)){ // Assumes Flight has getId()
                    return new Response("Passenger " + passengerId + " is already assigned to flight " + flightId + ".", Status.BAD_REQUEST);    
                }
            }

            flight.addPassenger(passenger); // Assumes these methods manage the bidirectional relationship
            passenger.addFlight(flight);

            // Persist changes
            boolean flightUpdatedOk = flightStorage.updateFlight(flight); 
            boolean passengerUpdatedOk = passengerStorage.updatePassenger(passenger);

            if (!flightUpdatedOk || !passengerUpdatedOk) {
                // This is a tricky situation.  might be inconsistent.
                // Consider transactional behavior or rollback if possible, or at least log severity.
                System.err.println("Warning: Passenger/Flight assignment updated in memory, but failed to persist all changes to storage.  might be inconsistent.");
                // Depending on requirements, this could be a full error.
                return new Response("Flight assignment partially failed during storage update.", Status.INTERNAL_SERVER_ERROR);
            }
            
            return new Response("Passenger " + passengerId + " assigned to flight " + flightId + " successfully.", Status.SUCCESS);
        } catch (Exception ex) {
            System.err.println("Unexpected error in assignFlight: " + ex.getMessage());
            ex.printStackTrace();
            return new Response("An unexpected server error occurred during flight assignment: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public Response getFlightsForPassenger(String passengerIdStr) {
        try {
            Response passengerIdResponse = passengerValidator.validateAndParsePassengerId(passengerIdStr);
            if(passengerIdResponse.getStatus() != Status.SUCCESS){
                return passengerIdResponse;
            }
            long passengerId = (long) passengerIdResponse.getObject();

            Passenger passenger = passengerStorage.getPassengerById(passengerId);
            if (passenger == null) {
                return new Response("Passenger with ID " + passengerId + " not found.", Status.NOT_FOUND);
            }

            ArrayList<Flight> flights = passenger.getFlights(); // Original comment: "debería devolver una COPIA y ORDENADA por fecha"

            if (flights == null) { flights = new ArrayList<>(); }

            if (flights.isEmpty()) {
                return new Response("No flights found for passenger " + passengerId + ".", Status.NOT_FOUND, new ArrayList<Flight>());
            }
            
            ArrayList<Flight> flightCopies = new ArrayList<>();
            for (Flight f : flights) {
                try {
                    flightCopies.add((Flight) f.clone()); // Assumes Flight is Cloneable
                } catch (Exception e) {
                     System.err.println("Error cloning flight with ID " + f.getId() + " in getFlightsForPassenger: " + e.getMessage());
                }
            }
            // Sorting by departure date: if passenger.getFlights() doesn't already do it.
            // Example: flightCopies.sort(Comparator.comparing(Flight::getDepartureDate)); // Assumes Flight has getDepartureDate()

            return new Response("Flights for passenger " + passengerId + " retrieved successfully.", Status.SUCCESS, flightCopies);

        } catch (Exception ex) {
            System.err.println("Unexpected error in getFlightsForPassenger: " + ex.getMessage());
            ex.printStackTrace();
            return new Response("An unexpected error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response getPassengerDisplayInfoForComboBox() {
        try {
            ArrayList<Passenger> passengers = passengerStorage.getPassengers(); // Assumes sorted by ID
            ArrayList<String[]> displayInfo = new ArrayList<>();

            if (passengers == null) { passengers = new ArrayList<>(); }

            for (Passenger p : passengers) {
                PassengerFormats format = new PassengerFormats(p);
                displayInfo.add(new String[]{String.valueOf(p.getId()), format.getFullname()}); // Assumes Passenger has getId() and getFullname()
            }
            
            if (displayInfo.isEmpty() && passengers.isEmpty()) { // Check if truly no passengers
                 return new Response("No passengers found for ComboBox.", Status.NOT_FOUND, new ArrayList<String[]>());
            }
            return new Response("Passenger info for ComboBox retrieved.", Status.SUCCESS, displayInfo);
        } catch (Exception ex) {
            System.err.println("Unexpected error in getPassengerDisplayInfoForComboBox: " + ex.getMessage());
            ex.printStackTrace();
            return new Response("Error retrieving passenger info for ComboBox: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
}
