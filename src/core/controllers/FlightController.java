/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.controllers.services.FlightServices;
import core.models.Location;
import core.models.Plane;
import core.models.flight.Delay;
import core.models.flight.Flight;
import core.models.person.Passenger;
import core.models.storage.FlightStorage;
import core.models.storage.LocationStorage;
import core.models.storage.PassengerStorage;
import core.models.storage.PlaneStorage;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 *
 * @author Admin
 */
public class FlightController implements FlightServices{
   
    private final FlightStorage flightStorage = FlightStorage.getInstance(); 
    private final PlaneStorage planeStorage = PlaneStorage.getInstance();
    private final LocationStorage locationStorage = LocationStorage.getInstance();
    private final PassengerStorage passengerStorage = PassengerStorage.getInstance();

    private final Pattern FLIGHT_ID_PATTERN = Pattern.compile("^[A-Z]{3}\\d{3}$");
    @Override
    public Response createFlight(String flightId,String planeId,
                                        String departureLocationId, String arrivalLocationId,
                                        String scaleLocationId,
                                        String year,String month, String day, String hour, String minutes,
                                        String leg1HoursStr, String leg1MinutesStr,
                                        String leg2HoursStr, String leg2MinutesStr) { 
        try {
            if (flightId == null || flightId.trim().isEmpty()) {
                return new Response("Flight ID must not be empty.", Status.BAD_REQUEST);
            }
            flightId = flightId.trim().toUpperCase();
            if (!FLIGHT_ID_PATTERN.matcher(flightId).matches()) {
                return new Response("Flight ID must follow the format XXXYYY (e.g., AAA123).", Status.BAD_REQUEST);
            }
            if (flightStorage.flightIdExists(flightId)) {
                return new Response("A flight with ID '" + flightId + "' already exists.", Status.BAD_REQUEST);
            }
            if (planeId == null || planeId.trim().isEmpty()) {
                return new Response("Plane ID must not be empty.", Status.BAD_REQUEST);
            }
            Plane plane = planeStorage.getPlane(planeId.trim().toUpperCase());
            if (plane == null) {
                return new Response("Plane with ID '" + planeId + "' not found.", Status.NOT_FOUND);
            }
            if (departureLocationId == null || departureLocationId.trim().isEmpty()) {
                return new Response("Departure location ID must not be empty.", Status.BAD_REQUEST);
            }
            Location departureLocation = locationStorage.getLocation(departureLocationId.trim().toUpperCase());
            if (departureLocation == null) {
                return new Response("Departure location with ID '" + departureLocationId + "' not found.", Status.NOT_FOUND);
            }
            if (arrivalLocationId == null || arrivalLocationId.trim().isEmpty()) {
                return new Response("Arrival location ID must not be empty.", Status.BAD_REQUEST);
            }
            Location arrivalLocation = locationStorage.getLocation(arrivalLocationId.trim().toUpperCase());
            if (arrivalLocation == null) {
                return new Response("Arrival location with ID '" + arrivalLocationId + "' not found.", Status.NOT_FOUND);
            }

            if (departureLocation.getAirportId().equals(arrivalLocation.getAirportId())) {
                return new Response("Departure and arrival locations cannot be the same.", Status.BAD_REQUEST);
            }
            Location scaleLocation = null;
            if (scaleLocationId != null && !scaleLocationId.trim().isEmpty() && !scaleLocationId.equalsIgnoreCase("Location") /* Handle default combobox text */) {
                scaleLocation = locationStorage.getLocation(scaleLocationId.trim().toUpperCase());
                if (scaleLocation == null) {
                    return new Response("Scale location with ID '" + scaleLocationId + "' not found.", Status.NOT_FOUND);
                }
                if (scaleLocation.getAirportId().equals(departureLocation.getAirportId()) ||
                    scaleLocation.getAirportId().equals(arrivalLocation.getAirportId())) {
                    return new Response("Scale location cannot be the same as departure or arrival location.", Status.BAD_REQUEST);
                }
            }
            LocalDateTime departureDateTime;
            try{
                departureDateTime = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minutes));
                
            }catch(DateTimeParseException e){
                return new Response("Date must be in the format yyyy/mm/dd/hh:mm",Status.BAD_REQUEST);
            }
            if (departureDateTime == null) {
                return new Response("Departure date and time must not be empty.", Status.BAD_REQUEST);
            }
            int leg1Hours, leg1Minutes, leg2Hours = 0, leg2Minutes = 0;
            try {
                leg1Hours = Integer.parseInt(leg1HoursStr.trim());
                leg1Minutes = Integer.parseInt(leg1MinutesStr.trim());
                if (leg1Hours < 0 || leg1Minutes < 0 || (leg1Hours == 0 && leg1Minutes == 0)) {
                    return new Response("Duration for the first leg of the flight must be greater than 00:00.", Status.BAD_REQUEST); 
                }
                if (leg1Minutes >= 60) return new Response("Minutes for the first leg must be less than 60.", Status.BAD_REQUEST);


                if (scaleLocation != null) {
                    leg2Hours = Integer.parseInt(leg2HoursStr.trim());
                    leg2Minutes = Integer.parseInt(leg2MinutesStr.trim());
                    if (leg2Hours < 0 || leg2Minutes < 0 || (leg2Hours == 0 && leg2Minutes == 0)) {
                        return new Response("Duration for the second leg (scale to arrival) of the flight must be greater than 00:00.", Status.BAD_REQUEST); 
                    }
                    if (leg2Minutes >= 60) return new Response("Minutes for the second leg must be less than 60.", Status.BAD_REQUEST);
                } else { // No scale [cite: 31]
                    if (leg2HoursStr != null && !leg2HoursStr.trim().isEmpty() && Integer.parseInt(leg2HoursStr.trim()) != 0) {
                         return new Response("Scale duration hours must be 0 if no scale location is provided.", Status.BAD_REQUEST);
                    }
                     if (leg2MinutesStr != null && !leg2MinutesStr.trim().isEmpty() && Integer.parseInt(leg2MinutesStr.trim()) != 0) {
                         return new Response("Scale duration minutes must be 0 if no scale location is provided.", Status.BAD_REQUEST);
                    }
                    leg2Hours = 0; 
                    leg2Minutes = 0;
                }

            } catch (NumberFormatException ex) {
                return new Response("Durations must be valid numbers.", Status.BAD_REQUEST);
            }

            Flight newFlight;
            if (scaleLocation != null) {
                newFlight = new Flight(flightId, plane, departureLocation, scaleLocation, arrivalLocation,
                                       departureDateTime, leg2Hours, leg2Minutes, leg1Hours, leg1Minutes);
            } else {
                newFlight = new Flight(flightId, plane, departureLocation, arrivalLocation,
                                       departureDateTime, leg1Hours, leg1Minutes);
            }

            if (!flightStorage.addFlight(newFlight)) {
                return new Response("Failed to save the flight. ID conflict might have occurred.", Status.BAD_REQUEST);
            }
            try {
                Flight flightCopy = (Flight) newFlight.clone();
                return new Response("Flight created successfully.", Status.CREATED, flightCopy);
            } catch (Exception e) {
                System.err.println("Cloning not supported for Flight: " + e.getMessage());
                return new Response("Flight created, but failed to clone the response object.", Status.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Response getAllFlights() {
        try {
            ArrayList<Flight> flights = flightStorage.getFlights();

            if (flights == null) {
                flights = new ArrayList<>();
            }

            if (flights.isEmpty()) {
                return new Response("No flights found.", Status.NOT_FOUND, new ArrayList<Flight>());
            }

            ArrayList<Flight> flightCopies = new ArrayList<>();
            for (Flight f : flights) {
                try {
                    flightCopies.add((Flight) f.clone());
                } catch (Exception e) {
                    System.err.println("Error cloning flight with ID " + f.getId() + ": " + e.getMessage());
                }
            }
            return new Response("Flights retrieved successfully.", Status.SUCCESS, flightCopies);
        } catch (Exception ex) {
            System.err.println("Unexpected error in getAllFlights: " + ex.getMessage());
            ex.printStackTrace();
            return new Response("An unexpected server error occurred while retrieving flights.", Status.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Response addPassengerToFlight(String flightId, long passengerId) {
        try {
            if (flightId == null || flightId.trim().isEmpty()) {
                return new Response("Flight ID must not be empty.", Status.BAD_REQUEST);
            }
            Flight flight = flightStorage.getFlight(flightId.trim().toUpperCase());
            if (flight == null) {
                return new Response("Flight with ID '" + flightId + "' not found.", Status.NOT_FOUND); // [cite: 33]
            }

            Passenger passenger = passengerStorage.getPassenger(passengerId);
            if (passenger == null) {
                return new Response("Passenger with ID '" + passengerId + "' not found.", Status.NOT_FOUND);
            }

            if (flight.getNumPassengers() >= flight.getPlane().getMaxCapacity()) {
                return new Response("Flight is full. Cannot add more passengers.", Status.BAD_REQUEST);
            }

            flight.addPassenger(passenger); 
            passenger.addFlight(flight);   
            
            return new Response("Passenger added to flight successfully.", Status.OK);

        } catch (Exception ex) {
            // Log ex.printStackTrace();
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Response delayFlight(String flightId, String hoursStr, String minutesStr) {
        try {
            if (flightId == null || flightId.trim().isEmpty()) {
                return new Response("Flight ID must not be empty.", Status.BAD_REQUEST);
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

            Delay delay = new Delay(flight);
            delay.delay(hours, minutes);
  
            return new Response("Flight delayed successfully.", Status.OK); 
            
        } catch (Exception ex) {
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public Response getFlightsForPassenger(long passengerId) {
        try {
            Passenger passenger = passengerStorage.getPassenger(passengerId);
            if (passenger == null) {
                return new Response("Passenger with ID '" + passengerId + "' not found.", Status.NOT_FOUND);
            }

            ArrayList<Flight> flights = new ArrayList<>(passenger.getFlights());

            flights.sort(Comparator.comparing(Flight::getDepartureDate));

    
            ArrayList<Flight> flightCopies = new ArrayList<>();
            for (Flight flight : flights) {
//               flightCopies.add(flight.clone()); // Assuming Flight has a clone() method
            }
            return new Response("Passenger flights retrieved successfully.", Status.OK, flightCopies);

        } catch (Exception ex) {
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }

}
