package core.controllers.validations;

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.validations.interfaces.IPassengerValidator;
import core.models.storage.interfaces.IPassengerStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PassengerValidator implements IPassengerValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public Response validatePassengerBase(String firstName, String lastName,
                                              String yearStr, String monthStr, String dayStr,
                                              String phoneCodeStr, String phoneNumberStr, String country) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return new Response("First name must not be empty.", Status.BAD_REQUEST);
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            return new Response("Last name must not be empty.", Status.BAD_REQUEST);
        }

        // Date of birth validation (delegated)
        Response dateValidationResponse = validateAndParseDateOfBirth(yearStr, monthStr, dayStr);
        if (dateValidationResponse.getStatus() != Status.SUCCESS && dateValidationResponse.getObject() == null) { // Check if it's an error response
             return dateValidationResponse;
        }


        // Phone number validation (delegated)
        Response phoneValidationResponse = validateAndParsePhoneNumber(phoneCodeStr, phoneNumberStr);
        if (phoneValidationResponse.getStatus() != Status.SUCCESS) {
            return phoneValidationResponse;
        }
        
        if (country == null || country.trim().isEmpty()) {
            return new Response("Country must not be empty.", Status.BAD_REQUEST);
        }
        return new Response("Base data validation successful.", Status.SUCCESS); // Indicates success
    }

    @Override
    public Response validateAndParsePassengerId(String idStr) {
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
        return new Response("Passenger ID parsed successfully.", Status.SUCCESS, id); // Return parsed ID in data
    }
    
    @Override
    public Response validateAndParseDateOfBirth(String yearStr, String monthStr, String dayStr) {
        if (yearStr == null || yearStr.trim().isEmpty() || 
            monthStr == null || monthStr.trim().isEmpty() || 
            dayStr == null || dayStr.trim().isEmpty()) {
            return new Response("All date of birth fields (year, month, day) must not be empty.", Status.BAD_REQUEST);
        }
        try {
            String localMonthStr = monthStr.trim();
            String localDayStr = dayStr.trim();
            if (localMonthStr.length() == 1) localMonthStr = "0" + localMonthStr;
            if (localDayStr.length() == 1) localDayStr = "0" + localDayStr;
            
            LocalDate parsedDate = LocalDate.parse(yearStr.trim() + "/" + localMonthStr + "/" + localDayStr, DATE_FORMATTER);
            return new Response("Date of birth parsed successfully.", Status.SUCCESS, parsedDate);
        } catch (DateTimeParseException ex) {
            return new Response("Date of birth is not valid. Use yyyy/MM/dd format and ensure it's a real date.", Status.BAD_REQUEST);
        } catch (NumberFormatException ex) { // Should be caught by DateTimeParseException mostly, but good to have
            return new Response("Year, month, and day for birthdate must be valid numbers.", Status.BAD_REQUEST);
        }
    }

    @Override
    public Response validateAndParsePhoneNumber(String phoneCodeStr, String phoneNumberStr) {
        // Phone Code Validation
        if (phoneCodeStr == null || phoneCodeStr.trim().isEmpty()) {
            return new Response("Phone code must not be empty.", Status.BAD_REQUEST);
        }
        if (phoneCodeStr.trim().length() > 3) {
            return new Response("Phone code must have at most 3 digits.", Status.BAD_REQUEST);
        }
        int phoneCodeVal;
        try {
            phoneCodeVal = Integer.parseInt(phoneCodeStr.trim());
            if (phoneCodeVal < 0) {
                return new Response("Phone code must be greater than or equal to 0.", Status.BAD_REQUEST);
            }
        } catch (NumberFormatException ex) {
            return new Response("Phone code must be numeric.", Status.BAD_REQUEST);
        }

        // Phone Number Validation
        if (phoneNumberStr == null || phoneNumberStr.trim().isEmpty()) {
            return new Response("Phone number must not be empty.", Status.BAD_REQUEST);
        }
        if (phoneNumberStr.trim().length() > 11) { // Max 11 digits based on original code
            return new Response("Phone number must have at most 11 digits.", Status.BAD_REQUEST);
        }
        long phoneNumVal;
        try {
            phoneNumVal = Long.parseLong(phoneNumberStr.trim());
            if (phoneNumVal < 0) {
                return new Response("Phone number must be greater than or equal to 0.", Status.BAD_REQUEST);
            }
        } catch (NumberFormatException ex) {
            return new Response("Phone number must be numeric.", Status.BAD_REQUEST);
        }
        return new Response("Phone number validated successfully.", Status.SUCCESS, new Object[]{phoneCodeVal, phoneNumVal});
    }


    @Override
    public Response checkPassengerIdExists(long id, IPassengerStorage passengerStorage) {
        if (passengerStorage.passengerIdExists(id)) {
            return new Response("A passenger with the provided ID " + id + " already exists.", Status.BAD_REQUEST);
        }
        return null; 
    }
}
