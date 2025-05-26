/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package core.controllers.services;

import core.controllers.utils.Response;

/**
 *
 * @author Andrea Osio Amaya
 */
public interface PassengerServices {
Response registerPassenger(String idStr,String firstName, String lastName, 
                                        String yearStr, String monthStr, 
                                        String dayStr, String phoneCodeStr, 
                                        String phoneNumberStr, String country);

Response updatePassenger(String idStrToUpdate, String newFirstName, String newLastName,
                                           String newYearStr, String newMonthStr, String newDayStr,
                                           String newPhoneCodeStr, String newPhoneNumberStr, String newCountry);
Response getAllPassengers();

Response asignFlight(String passengerIdStr, String flightIdFromComboBox);

Response getFlightsForPassenger(String passengerIdStr);

Response getPassengerDisplayInfoForComboBox();
}
