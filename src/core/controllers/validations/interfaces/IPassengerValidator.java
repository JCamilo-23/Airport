package core.controllers.validations.interfaces;

import core.controllers.utils.Response; 

import core.models.storage.interfaces.IPassengerStorage;

public interface IPassengerValidator {

    Response validatePassengerBase(String firstName, String lastName,
                                       String yearStr, String monthStr, String dayStr,
                                       String phoneCodeStr, String phoneNumberStr, String country);

    Response validateAndParsePassengerId(String idStr);
    
    Response validateAndParseDateOfBirth(String yearStr, String monthStr, String dayStr);

    Response validateAndParsePhoneNumber(String phoneCodeStr, String phoneNumberStr);

    Response checkPassengerIdExists(long id, IPassengerStorage passengerStorage);
}
