/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.services.PassengerServices;
import core.models.calculate.PassengerFormats;
import core.models.flight.Flight;
import core.models.person.Passenger;
import core.models.storage.FlightStorage;
import core.models.storage.PassengerStorage;
import core.models.storage.interfaces.IFlightStorage;
import core.models.storage.interfaces.IPassengerStorage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 *
 * @author Admin
 */
public class PassengerController implements PassengerServices{
    // DIP: Usar interfaces para las dependencias de 
    private static IPassengerStorage passengerStorage = (IPassengerStorage) PassengerStorage.getInstance();
    private static IFlightStorage flightStorage = (IFlightStorage) FlightStorage.getInstance();

    private static Response validatePassengerBaseData(String firstName, String lastName,
                                                      String yearStr, String monthStr, String dayStr,
                                                      String phoneCodeStr, String phoneNumberStr, String country) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return new Response("First name must not be empty.", Status.BAD_REQUEST);
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            return new Response("Last name must not be empty.", Status.BAD_REQUEST);
        }

        if (yearStr == null || yearStr.trim().isEmpty() || monthStr == null || monthStr.trim().isEmpty() || dayStr == null || dayStr.trim().isEmpty()) {
            return new Response("All date of birth fields (year, month, day) must not be empty.", Status.BAD_REQUEST);
        }
        try {
            String localMonthStr = monthStr.trim();
            String localDayStr = dayStr.trim();
            if (localMonthStr.length() == 1) localMonthStr = "0" + localMonthStr;
            if (localDayStr.length() == 1) localDayStr = "0" + localDayStr;
            DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate.parse(yearStr.trim() + "/" + localMonthStr + "/" + localDayStr, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            return new Response("Date of birth is not valid. Use yyyy/MM/dd format and ensure it's a real date.", Status.BAD_REQUEST);
        } catch (NumberFormatException ex) {
            return new Response("Year, month, and day for birthdate must be valid numbers.", Status.BAD_REQUEST);
        }

        if (phoneCodeStr == null || phoneCodeStr.trim().isEmpty()) {
            return new Response("Phone code must not be empty.", Status.BAD_REQUEST);
        }
        if (phoneCodeStr.trim().length() > 3) {
            return new Response("Phone code must have at most 3 digits.", Status.BAD_REQUEST);
        }
        try {
            int phoneCodeVal = Integer.parseInt(phoneCodeStr.trim());
            if (phoneCodeVal < 0) {
                return new Response("Phone code must be greater than or equal to 0.", Status.BAD_REQUEST);
            }
        } catch (NumberFormatException ex) {
            return new Response("Phone code must be numeric.", Status.BAD_REQUEST);
        }

        if (phoneNumberStr == null || phoneNumberStr.trim().isEmpty()) {
            return new Response("Phone number must not be empty.", Status.BAD_REQUEST);
        }
        if (phoneNumberStr.trim().length() > 11) {
            return new Response("Phone number must have at most 11 digits.", Status.BAD_REQUEST);
        }
        try {
            long phoneNumVal = Long.parseLong(phoneNumberStr.trim());
            if (phoneNumVal < 0) {
                return new Response("Phone number must be greater than or equal to 0.", Status.BAD_REQUEST);
            }
        } catch (NumberFormatException ex) {
            return new Response("Phone number must be numeric.", Status.BAD_REQUEST);
        }

        if (country == null || country.trim().isEmpty()) {
            return new Response("Country must not be empty.", Status.BAD_REQUEST);
        }
        return null; // Validación base exitosa
    }
    @Override
    public  Response registerPassenger(String idStr, String firstName, String lastName, String yearStr, String monthStr, String dayStr, String phoneCodeStr, String phoneNumberStr, String country) {
        try {
            // Validación específica del ID para el registro
            if (idStr == null || idStr.trim().isEmpty()) {
                return new Response("Passenger ID must not be empty.", Status.BAD_REQUEST);
            }
            if (idStr.trim().length() > 15) {
                return new Response("Passenger ID must have at most 15 digits.", Status.BAD_REQUEST);
            }
            long id;
            try {
                id = Long.parseLong(idStr.trim());
                if (id < 0) {
                    return new Response("Passenger ID must be greater than or equal to 0.", Status.BAD_REQUEST);
                }
            } catch (NumberFormatException ex) {
                return new Response("Passenger ID must be numeric.", Status.BAD_REQUEST);
            }

            if (passengerStorage.passengerIdExists(id)) {
                return new Response("A passenger with the provided ID " + id + " already exists.", Status.BAD_REQUEST);
            }

            Response baseValidationResponse = validatePassengerBaseData(firstName, lastName, yearStr, monthStr, dayStr, phoneCodeStr, phoneNumberStr, country);
            if (baseValidationResponse != null) {
                return baseValidationResponse;
            }

            String localMonthStr = monthStr.trim();
            if (localMonthStr.length() == 1) localMonthStr = "0" + localMonthStr;
            String localDayStr = dayStr.trim();
            if (localDayStr.length() == 1) localDayStr = "0" + localDayStr;
            DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate dateOfBirth = LocalDate.parse(yearStr.trim() + "/" + localMonthStr + "/" + localDayStr, DATE_FORMATTER);
            int phoneCode = Integer.parseInt(phoneCodeStr.trim());
            long phoneNumber = Long.parseLong(phoneNumberStr.trim());

            Passenger newPassenger = new Passenger(id, firstName.trim(), lastName.trim(), dateOfBirth, phoneCode, phoneNumber, country.trim());

            if (!passengerStorage.addPassenger(newPassenger)) {
                return new Response("Error saving passenger, ID might be duplicated or another issue occurred.", Status.INTERNAL_SERVER_ERROR);
            }
            try {
                Passenger passengerCopy = (Passenger) newPassenger.clone(); // Patrón Prototype
                return new Response("Passenger created successfully.", Status.CREATED, passengerCopy);

            } catch (Exception e) {
                System.err.println("Cloning not supported for Passenger: " + e.getMessage());
                return new Response("Passenger created, but failed to clone the response object.", Status.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception ex) {
            System.err.println("Unexpected error in registerPassenger: " + ex.getMessage());
            return new Response("Unexpected server error: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Response updatePassenger(String idStrToUpdate, String newFirstName, String newLastName,
                                           String newYearStr, String newMonthStr, String newDayStr,
                                           String newPhoneCodeStr, String newPhoneNumberStr, String newCountry) {
        try {
            if (idStrToUpdate == null || idStrToUpdate.trim().isEmpty()) {
                return new Response("Passenger ID for update must not be empty.", Status.BAD_REQUEST);
            }
            long idToUpdate;
            try {
                idToUpdate = Long.parseLong(idStrToUpdate.trim());
            } catch (NumberFormatException ex) {
                return new Response("Passenger ID for update must be numeric.", Status.BAD_REQUEST);
            }

            Passenger passengerToUpdate = passengerStorage.getPassengerById(idToUpdate);
            if (passengerToUpdate == null) {
                return new Response("Passenger with ID " + idToUpdate + " not found.", Status.NOT_FOUND);
            }

            Response baseValidationResponse = validatePassengerBaseData(newFirstName, newLastName, newYearStr, newMonthStr, newDayStr, newPhoneCodeStr, newPhoneNumberStr, newCountry);
            if (baseValidationResponse != null) {
                return baseValidationResponse;
            }

            String localMonthStr = newMonthStr.trim();
            if (localMonthStr.length() == 1) localMonthStr = "0" + localMonthStr;
            String localDayStr = newDayStr.trim();
            if (localDayStr.length() == 1) localDayStr = "0" + localDayStr;
            DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate newDateOfBirth = LocalDate.parse(newYearStr.trim() + "/" + localMonthStr + "/" + localDayStr, DATE_FORMATTER);
            int newPhoneCode = Integer.parseInt(newPhoneCodeStr.trim());
            long newPhoneNumber = Long.parseLong(newPhoneNumberStr.trim());

            passengerToUpdate.setFirstname(newFirstName.trim());
            passengerToUpdate.setLastname(newLastName.trim());
            passengerToUpdate.setBirthDate(newDateOfBirth);
            passengerToUpdate.setCountryPhoneCode(newPhoneCode); // Asumiendo nombres de setters correctos en Passenger.java
            passengerToUpdate.setPhone(newPhoneNumber);         // Asumiendo nombres de setters correctos
            passengerToUpdate.setCountry(newCountry.trim());

            if (!passengerStorage.updatePassenger(passengerToUpdate)) {
                return new Response("Error updating passenger in storage.", Status.INTERNAL_SERVER_ERROR);
            }

            Passenger passengerCopy = (Passenger) passengerToUpdate.clone(); // Patrón Prototype
            return new Response("Passenger updated successfully.", Status.SUCCESS, passengerCopy);

        } catch (Exception ex) {    
            System.err.println("Unexpected error in updatePassenger: " + ex.getMessage());
            return new Response("Unexpected server error during update: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Response getAllPassengers() {
        try {
            // IPassengerStorage.getPassengers() debe devolver la lista ordenada por ID
            ArrayList<Passenger> passengers = passengerStorage.getPassengers();

            if (passengers == null) { // Storage no debería devolver null
                passengers = new ArrayList<>(); // Tratar como lista vacía para evitar NullPointerException
            }
            
            if (passengers.isEmpty()) {
                return new Response("No passengers found.", Status.NOT_FOUND, new ArrayList<Passenger>());
            }

            ArrayList<Passenger> passengerCopies = new ArrayList<>();
            for (Passenger p : passengers) {
                try {
                    passengerCopies.add((Passenger) p.clone()); // Patrón Prototype
                } catch (Exception e) {
                    System.err.println("Error cloning passenger with ID " + p.getId() + ": " + e.getMessage());
                    // Considerar cómo manejar esto: omitir, o devolver error si la clonación es crítica.
                }
            }
            return new Response("Passengers retrieved successfully.", Status.SUCCESS, passengerCopies);
        } catch (Exception ex) {
            System.err.println("Unexpected error in getAllPassengers: " + ex.getMessage());
            return new Response("An unexpected server error occurred while retrieving passengers.", Status.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Response asignFlight(String passengerIdStr, String flightIdFromComboBox) {
        try {
            if (passengerIdStr == null || passengerIdStr.trim().isEmpty()) {
                return new Response("Passenger ID must not be empty.", Status.BAD_REQUEST);
            }
            long passengerId;
            try {
                passengerId = Long.parseLong(passengerIdStr.trim());
            } catch (NumberFormatException e) {
                return new Response("Passenger ID must be numeric.", Status.BAD_REQUEST);
            }

            if (flightIdFromComboBox == null || flightIdFromComboBox.trim().isEmpty() || flightIdFromComboBox.equalsIgnoreCase("Flight") || flightIdFromComboBox.equalsIgnoreCase("ID")) {
                return new Response("A valid Flight ID must be selected.", Status.BAD_REQUEST);
            }
            String flightId = flightIdFromComboBox.trim().toUpperCase();

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
            for(Flight f: passenger.getFlights()){
                if(f.getId().equals(flightId)){
                    return new Response("Passenger " + passengerId + " is already assigned to flight " + flightId + ".", Status.BAD_REQUEST);     
                }
            }

            flight.addPassenger(passenger);
            passenger.addFlight(flight);

            
                // Para que Observer funcione: los cambios en flight y passenger deben guardarse
                // y los métodos de guardado en Storage deben llamar a notifyObservers().
                boolean flightUpdatedOk = flightStorage.updateFlight(flight); 
                boolean passengerUpdatedOk = passengerStorage.updatePassenger(passenger);

                if (!flightUpdatedOk || !passengerUpdatedOk) {
                    System.err.println("Warning: Passenger/Flight assignment updated in memory, but failed to persist all changes to storage for observer notification.");
                    // Podrías considerar esto un error parcial o completo dependiendo de la criticidad.
                }
                return new Response("Passenger " + passengerId + " assigned to flight " + flightId + " successfully.", Status.SUCCESS);
        } catch (Exception ex) {
            System.err.println("Unexpected error in asignFlight: " + ex.getMessage());
            return new Response("An unexpected server error occurred during flight assignment: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Response getFlightsForPassenger(String passengerIdStr) {
        try {
            if (passengerIdStr == null || passengerIdStr.trim().isEmpty()) {
                return new Response("Passenger ID must not be empty for fetching flights.", Status.BAD_REQUEST);
            }
            long passengerId;
            try {
                passengerId = Long.parseLong(passengerIdStr.trim());
            } catch (NumberFormatException e) {
                return new Response("Passenger ID must be numeric.", Status.BAD_REQUEST);
            }

            Passenger passenger = passengerStorage.getPassengerById(passengerId);
            if (passenger == null) {
                return new Response("Passenger with ID " + passengerId + " not found.", Status.NOT_FOUND);
            }

            ArrayList<Flight> flights = passenger.getFlights(); // Passenger.getFlights() debería devolver una COPIA y ORDENADA por fecha

            if (flights == null) { // El getter del modelo no debería devolver null
                flights = new ArrayList<>();
            }

            if (flights.isEmpty()) {
                return new Response("No flights found for passenger " + passengerId + ".", Status.NOT_FOUND, new ArrayList<Flight>());
            }
            
            // Patrón Prototype: Devolver una lista de copias de Flight
            // Esto asume que Flight también es Cloneable y tiene un método clone()
            ArrayList<Flight> flightCopies = new ArrayList<>();
            for (Flight f : flights) {
                try {
                    flightCopies.add((Flight) f.clone());
                } catch (Exception e) {
                    System.err.println("Error cloning flight with ID " + f.getId() + ": " + e.getMessage());
                }
            }
            // El parcial pide que los vuelos de un pasajero estén ordenados por fecha de salida.
            // Esta ordenación debería hacerse en passenger.getFlights() o aquí si es necesario.
            // Si passenger.getFlights() ya los devuelve ordenados, no necesitas reordenar aquí.
            // Ejemplo de ordenamiento si no estuvieran ya ordenados:
            // Collections.sort(flightCopies, Comparator.comparing(Flight::getDepartureDate));

            return new Response("Flights for passenger " + passengerId + " retrieved successfully.", Status.SUCCESS, flightCopies);

        } catch (Exception ex) {
            System.err.println("Unexpected error in getFlightsForPassenger: " + ex.getMessage());
            return new Response("An unexpected error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response getPassengerDisplayInfoForComboBox() {
        try {
            // IPassengerStorage.getPassengers() debe devolver la lista ordenada por ID
            ArrayList<Passenger> passengers = passengerStorage.getPassengers();
            ArrayList<String[]> displayInfo = new ArrayList<>();

            if (passengers == null) { passengers = new ArrayList<>(); }
            
            for (Passenger p : passengers) {
                PassengerFormats format = new PassengerFormats(p);
                displayInfo.add(new String[]{String.valueOf(p.getId()), format.getFullname()});
            }
            
            if (displayInfo.isEmpty() && passengers.isEmpty()) {
                return new Response("No passengers found for ComboBox.", Status.NOT_FOUND, new ArrayList<String[]>());
            }
            return new Response("Passenger info for ComboBox retrieved.", Status.SUCCESS, displayInfo);
        } catch (Exception ex) {
            System.err.println("Unexpected error in getPassengerDisplayInfoForComboBox: " + ex.getMessage());
            return new Response("Error retrieving passenger info for ComboBox: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
}

