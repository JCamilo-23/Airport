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
public interface FlightServices {
    Response createFlight(String flightId, String planeId,
                          String departureLocationId, String arrivalLocationId,
                          String scaleLocationId,
                          String year, String month, String day, String hour, String minutes,
                          String leg1HoursStr, String leg1MinutesStr,
                          String leg2HoursStr, String leg2MinutesStr);

    Response getAllFlights();

    Response addPassengerToFlight(String flightId, long passengerId);

    Response delayFlight(String flightId, String hoursStr, String minutesStr);

    Response getFlightsForPassenger(long passengerId);
}

