/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.utils.Response;
import core.controllers.utils.Status;
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
import javax.swing.JComboBox;

/**
 *
 * @author Admin
 */
public class FlightController {
    // In a real application, these would be obtained through a proper mechanism
    // (e.g., dependency injection, singleton getInstance()).
    private static FlightStorage flightStorage = FlightStorage.getInstance(); // Assuming getInstance() exists
    private static PlaneStorage planeStorage = PlaneStorage.getInstance();
    private static LocationStorage locationStorage = LocationStorage.getInstance();
    private static PassengerStorage passengerStorage = PassengerStorage.getInstance();

    private static final Pattern FLIGHT_ID_PATTERN = Pattern.compile("^[A-Z]{3}\\d{3}$");

    /**
     * Creates a new flight.
     * The durations are interpreted as:
     * - leg1: Departure to Scale (if scale exists) or Departure to Arrival (if no scale)
     * - leg2: Scale to Arrival (only if scale exists)
     *
     * [cite: 28, 29, 30, 31, 32]
     */
    public static Response createFlight(String flightId, String planeId,
                                        String departureLocationId, String arrivalLocationId,
                                        String scaleLocationId, // Can be null or empty if no scale
                                        String year,String month, String day, String hour, String minutes,
                                        String leg1HoursStr, String leg1MinutesStr,
                                        String leg2HoursStr, String leg2MinutesStr) { 
        try {
            // 1. Flight ID Validation
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

            // 2. Plane Validation
            if (planeId == null || planeId.trim().isEmpty()) {
                return new Response("Plane ID must not be empty.", Status.BAD_REQUEST);
            }
            Plane plane = planeStorage.getPlane(planeId.trim().toUpperCase());
            if (plane == null) {
                return new Response("Plane with ID '" + planeId + "' not found.", Status.NOT_FOUND);
            }

            // 3. Departure Location Validation [cite: 29]
            if (departureLocationId == null || departureLocationId.trim().isEmpty()) {
                return new Response("Departure location ID must not be empty.", Status.BAD_REQUEST);
            }
            Location departureLocation = locationStorage.getLocation(departureLocationId.trim().toUpperCase());
            if (departureLocation == null) {
                return new Response("Departure location with ID '" + departureLocationId + "' not found.", Status.NOT_FOUND);
            }

            // 4. Arrival Location Validation [cite: 29]
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

            // 5. Scale Location Validation (Optional) [cite: 30]
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

            // 6. Departure Date Validation
            LocalDateTime departureDateTime;
            try{
                departureDateTime = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minutes));
                
            }catch(DateTimeParseException e){
                return new Response("Date must be in the format yyyy/mm/dd/hh:mm",Status.BAD_REQUEST);
            }
            if (departureDateTime == null) {
                return new Response("Departure date and time must not be empty.", Status.BAD_REQUEST);
            }
            // 7. Duration Parsing and Validation
            int leg1Hours, leg1Minutes, leg2Hours = 0, leg2Minutes = 0;
            try {
                leg1Hours = Integer.parseInt(leg1HoursStr.trim());
                leg1Minutes = Integer.parseInt(leg1MinutesStr.trim());
                if (leg1Hours < 0 || leg1Minutes < 0 || (leg1Hours == 0 && leg1Minutes == 0)) {
                    return new Response("Duration for the first leg of the flight must be greater than 00:00.", Status.BAD_REQUEST); // [cite: 32]
                }
                if (leg1Minutes >= 60) return new Response("Minutes for the first leg must be less than 60.", Status.BAD_REQUEST);


                if (scaleLocation != null) {
                    leg2Hours = Integer.parseInt(leg2HoursStr.trim());
                    leg2Minutes = Integer.parseInt(leg2MinutesStr.trim());
                    if (leg2Hours < 0 || leg2Minutes < 0 || (leg2Hours == 0 && leg2Minutes == 0)) {
                        return new Response("Duration for the second leg (scale to arrival) of the flight must be greater than 00:00.", Status.BAD_REQUEST); // [cite: 32]
                    }
                    if (leg2Minutes >= 60) return new Response("Minutes for the second leg must be less than 60.", Status.BAD_REQUEST);
                } else { // No scale [cite: 31]
                    if (leg2HoursStr != null && !leg2HoursStr.trim().isEmpty() && Integer.parseInt(leg2HoursStr.trim()) != 0) {
                         return new Response("Scale duration hours must be 0 if no scale location is provided.", Status.BAD_REQUEST);
                    }
                     if (leg2MinutesStr != null && !leg2MinutesStr.trim().isEmpty() && Integer.parseInt(leg2MinutesStr.trim()) != 0) {
                         return new Response("Scale duration minutes must be 0 if no scale location is provided.", Status.BAD_REQUEST);
                    }
                    leg2Hours = 0; // Explicitly set to 0 if no scale
                    leg2Minutes = 0;
                }

            } catch (NumberFormatException ex) {
                return new Response("Durations must be valid numbers.", Status.BAD_REQUEST);
            }

            Flight newFlight;
            if (scaleLocation != null) {
                // Leg 1 is Departure -> Scale, Leg 2 is Scale -> Arrival
                // Flight constructor: (id, plane, dep, scale, arr, date, arrH, arrM, scaleH, scaleM)
                // Here, arrH/arrM maps to leg2, scaleH/scaleM maps to leg1 based on Flight model's calculateArrivalDate
                newFlight = new Flight(flightId, plane, departureLocation, scaleLocation, arrivalLocation,
                                       departureDateTime, leg2Hours, leg2Minutes, leg1Hours, leg1Minutes);
            } else {
                // Leg 1 is Departure -> Arrival. Leg 2 durations are 0.
                // Flight constructor: (id, plane, dep, arr, date, arrH, arrM)
                // Here, arrH/arrM maps to leg1. hoursDurationScale and minutesDurationScale in Flight model will be 0 by default or unused.
                newFlight = new Flight(flightId, plane, departureLocation, arrivalLocation,
                                       departureDateTime, leg1Hours, leg1Minutes);
            }

            if (!flightStorage.addFlight(newFlight)) {
                return new Response("Failed to save the flight. ID conflict might have occurred.", Status.BAD_REQUEST);
            }

            // Return a copy of the created flight [cite: 40]
            return new Response("Flight created successfully.", Status.CREATED); // Assuming Flight has a clone() method

        } catch (Exception ex) {
            // Log ex.printStackTrace(); for debugging
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds a passenger to a flight. [cite: 33]
     */
    public static Response addPassengerToFlight(String flightId, long passengerId) {
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

            // Check if plane capacity is exceeded
            if (flight.getNumPassengers() >= flight.getPlane().getMaxCapacity()) {
                return new Response("Flight is full. Cannot add more passengers.", Status.BAD_REQUEST);
            }

            flight.addPassenger(passenger); // Assumes this method exists in Flight model
            passenger.addFlight(flight);   // Assumes this method exists in Passenger model
            
            // Persist changes if necessary (e.g., flightStorage.updateFlight(flight))
            // Depending on how your storage and model interaction is designed.
            // If addPassenger directly modifies the stored object, an update call might be implicit or explicit.

            return new Response("Passenger added to flight successfully.", Status.OK);

        } catch (Exception ex) {
            // Log ex.printStackTrace();
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delays a flight by a specified amount of hours and minutes. [cite: 34]
     */
    public static Response delayFlight(String flightId, String hoursStr, String minutesStr) {
        try {
            if (flightId == null || flightId.trim().isEmpty()) {
                return new Response("Flight ID must not be empty.", Status.BAD_REQUEST);
            }
            Flight flight = flightStorage.getFlight(flightId.trim().toUpperCase());
            if (flight == null) {
                return new Response("Flight with ID '" + flightId + "' not found.", Status.NOT_FOUND); // [cite: 34]
            }

            int hours, minutes;
            try {
                hours = Integer.parseInt(hoursStr.trim());
                minutes = Integer.parseInt(minutesStr.trim());
            } catch (NumberFormatException ex) {
                return new Response("Delay hours and minutes must be valid numbers.", Status.BAD_REQUEST);
            }

            if (hours < 0 || minutes < 0 || (hours == 0 && minutes == 0)) {
                return new Response("Delay time must be greater than 00:00.", Status.BAD_REQUEST); // [cite: 34]
            }
            if (minutes >= 60) {
                 return new Response("Delay minutes must be less than 60.", Status.BAD_REQUEST);
            }


            Delay delay = new Delay(flight);
            delay.delay(hours, minutes);

            return new Response("Flight delayed successfully.", Status.OK); // Return updated flight (copy) [cite: 40]

        } catch (Exception ex) {
            // Log ex.printStackTrace();
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    public static Response getAllFlights() {
        try {
            ArrayList<Flight> flights = flightStorage.getFlights();
            if (flights == null) { // Should ideally return empty list from storage
                flights = new ArrayList<>();
            }
            // Sort flights by departure date [cite: 38]
            flights.sort(Comparator.comparing(Flight::getDepartureDate));

            // Return copies of flights [cite: 40]
//            ArrayList<Flight> flightCopies = new ArrayList<>();
//            for (Flight flight : flights) {
//                flightCopies.add(flight.clone()); // Assuming Flight has a clone() method
//            }
//            return new Response("Flights retrieved successfully.", Status.OK, flightCopies);
              return new Response("Flights retrieved successfully.", Status.OK);
        } catch (Exception ex) {
            // Log ex.printStackTrace();
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets all flights for a specific passenger, ordered by departure date (oldest to newest). [cite: 39]
     */
    public static Response getFlightsForPassenger(long passengerId) {
        try {
            Passenger passenger = passengerStorage.getPassenger(passengerId);
            if (passenger == null) {
                return new Response("Passenger with ID '" + passengerId + "' not found.", Status.NOT_FOUND);
            }

            ArrayList<Flight> flights = new ArrayList<>(passenger.getFlights()); // Get a mutable copy from passenger
            
            // Sort flights by departure date [cite: 39]
            flights.sort(Comparator.comparing(Flight::getDepartureDate));

            // Return copies of flights [cite: 40]
            ArrayList<Flight> flightCopies = new ArrayList<>();
            for (Flight flight : flights) {
//                flightCopies.add(flight.clone()); // Assuming Flight has a clone() method
            }
            return new Response("Passenger flights retrieved successfully.", Status.OK, flightCopies);

        } catch (Exception ex) {
            return new Response("An unexpected server error occurred: " + ex.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }
    public static void storageDownload(JComboBox jbox){
        FlightStorage storage = FlightStorage.getInstance();
        for (Flight f : storage.getFlights()) {
            jbox.addItem(""+f.getId());
        }
    }
}
