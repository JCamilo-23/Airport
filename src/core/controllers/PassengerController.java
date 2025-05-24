/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.models.flight.Flight;
import core.models.person.Passenger;
import core.models.storage.FlightStorage;
import core.models.storage.PassengerStorage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.swing.JComboBox;

/**
 *
 * @author Admin
 */
public class PassengerController{
    public static Response registerPassenger(String idStr, String firstName, String lastName, String yearStr, String monthStr, String dayStr, String phoneCodeStr,String phoneNumberStr, String country){
        
        try{
            long id;
            LocalDate dateOfBirth;
            int phoneCode;
            long phoneNumber;

            // 1. Passenger ID validation
            if (idStr == null || idStr.trim().isEmpty()) {
                return new Response("Passenger ID must not be empty.", Status.BAD_REQUEST);
            }
            if (idStr.length() > 15) {
                return new Response("Passenger ID must have at most 15 digits.", Status.BAD_REQUEST);
            }
            try {
                id = Long.parseLong(idStr);
                if (id < 0) {
                    return new Response("Passenger ID must be greater than or equal to 0.", Status.BAD_REQUEST);
                }
            } catch (NumberFormatException ex) {
                return new Response("Passenger ID must be numeric.", Status.BAD_REQUEST);
            }

            // Verify ID uniqueness
            PassengerStorage storage = PassengerStorage.getInstance();
            if (storage.passengerIdExists(id)) {
                return new Response("A passenger with the provided ID already exists.", Status.BAD_REQUEST);
            }

            if (yearStr == null || yearStr.trim().isEmpty()) {
                return new Response("Year must not be empty.", Status.BAD_REQUEST);
            }
            if (monthStr == null || monthStr.trim().isEmpty()) {
                return new Response("Month must not be empty.", Status.BAD_REQUEST);
            }
            if (dayStr == null || dayStr.trim().isEmpty()) {
                return new Response("Date of birth must not be empty.", Status.BAD_REQUEST);
            }
            
            try {
                if (monthStr.length() == 1){
                    monthStr = "0"+monthStr;
                }
                if (dayStr.length() == 1){
                    dayStr = "0"+dayStr;
                }
                
                DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                dateOfBirth = LocalDate.parse(yearStr+"/"+monthStr+"/"+dayStr, DATE_FORMATTER);
                
            } catch (DateTimeParseException ex) {
                return new Response("Date of birth is not valid. Use YYYY-MM-DD format.", Status.BAD_REQUEST);
            }

            // 3. Phone code validation
            if (phoneCodeStr == null || phoneCodeStr.trim().isEmpty()) {
                return new Response("Phone code must not be empty.", Status.BAD_REQUEST);
            }
            if (phoneCodeStr.length() > 3) {
                return new Response("Phone code must have at most 3 digits.", Status.BAD_REQUEST);
            }
            try {
                phoneCode = Integer.parseInt(phoneCodeStr);
                if (phoneCode < 0) {
                    return new Response("Phone code must be greater than or equal to 0.", Status.BAD_REQUEST);
                }
            } catch (NumberFormatException ex) {
                return new Response("Phone code must be numeric.", Status.BAD_REQUEST);
            }

            // 4. Phone number validation
            if (phoneNumberStr == null || phoneNumberStr.trim().isEmpty()) {
                return new Response("Phone number must not be empty.", Status.BAD_REQUEST);
            }
            if (phoneNumberStr.length() > 11) {
                return new Response("Phone number must have at most 11 digits.", Status.BAD_REQUEST);
            }
            try {
                phoneNumber = Long.parseLong(phoneNumberStr);
                if (phoneNumber < 0) {
                    return new Response("Phone number must be greater than or equal to 0.", Status.BAD_REQUEST);
                }
            } catch (NumberFormatException ex) {
                return new Response("Phone number must be numeric.", Status.BAD_REQUEST);
            }

            // 5. Validation of other fields (not empty)
            if (firstName == null || firstName.trim().isEmpty()) {
                return new Response("First name must not be empty.", Status.BAD_REQUEST);
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                return new Response("Last name must not be empty.", Status.BAD_REQUEST);
            }
            if (country == null || country.trim().isEmpty()) {
                return new Response("Country must not be empty.", Status.BAD_REQUEST);
            }
            // You could add more specific email format validation here if needed.


            // If all validations pass, create and save the passenger
            Passenger newPassenger = new Passenger(id, firstName.trim(), lastName.trim(), dateOfBirth, phoneCode, phoneNumber, country.trim());
            
            if (!storage.addPassenger(newPassenger)) {
                // This case was already covered by passengerIdExists, but it's a double-check.
                return new Response("Error saving passenger, ID might be duplicated.", Status.BAD_REQUEST);
            }

            return new Response("Passenger created successfully.", Status.CREATED, newPassenger);

        } catch (Exception ex) {
            return new Response("Unexpected server error: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    public static Response asignFlight(String passengerId, String flightId){
        FlightStorage storage = FlightStorage.getInstance();
        //Verify the existance of the flight
        Flight flight = storage.getFlight(flightId);
        if (flight == null){
            return new Response("Flight with ID "+flightId+" not found",Status.BAD_REQUEST);
        }
        //Verify de capacity of the flight
        if(flight.getNumPassengers() == flight.getPlane().getMaxCapacity()){
            return new Response("Flight is on max capacity",Status.BAD_REQUEST);
        }
        long passengerID = Long.parseLong(passengerId);
        flight.addPassenger(PassengerStorage.getInstance().getPassenger(passengerID));
        return new Response("Flight asign correctly",Status.CREATED);
    }
    public static void storageDownload(JComboBox jbox){
        PassengerStorage storage = PassengerStorage.getInstance();
        for (Passenger s : storage.getPassengers()) {
            jbox.addItem(""+s.getId());
        }
    }
}