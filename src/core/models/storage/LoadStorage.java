/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

import core.models.Location;
import core.models.Plane;
import core.models.flight.Flight;
import core.models.person.Passenger;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author Admin
 */
public class LoadStorage {

    public void loadAllData() {
        loadLocations("json/locations.json"); 
        loadPlanes("json/planes.json");       
        loadPassengers("json/passengers.json"); 
        loadFlights("json/flights.json");     
    }

    public void loadLocations(String filename) {
        LocationStorage locationSto
                = LocationStorage.getInstance();
        try (FileReader reader = new FileReader(filename)) {
            JSONArray array = new JSONArray(new JSONTokener(reader));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String airportId = obj.getString("airportId");
                String airportName = obj.getString("airportName");
                String airportCity = obj.getString("airportCity");
                String airportCountry = obj.getString("airportCountry");
                double airportLatitude = obj.getDouble("airportLatitude");
                double airportLongitude = obj.getDouble("airportLongitude");

                Location location = new Location(airportId, airportName, airportCity, airportCountry, airportLatitude, airportLongitude);
                locationSto
                        .addLocation(location);
            }
            System.out.println("Locations loaded successfully: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading locations file (" + filename + "): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing locations JSON (" + filename + "): " + e.getMessage());
        }
    }

    public void loadPlanes(String filename) {
        PlaneStorage planeSto
                = PlaneStorage.getInstance();
        try (FileReader reader = new FileReader(filename)) {
            JSONArray array = new JSONArray(new JSONTokener(reader));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String id = obj.getString("id");
                String brand = obj.getString("brand");
                String model = obj.getString("model");
                int maxCapacity = obj.getInt("maxCapacity");
                String airline = obj.getString("airline");

                Plane plane = new Plane(id, brand, model, maxCapacity, airline);
                // Asegúrate de que PlaneStorage exista y tenga el método addPlane
                if (planeSto
                        != null) { // Solo para evitar NullPointerException si aún no lo has creado
                    planeSto
                            .addPlane(plane);
                } else {
                    System.err.println("PlaneStorage is null. Cannot add plane.");
                }
            }
            System.out.println("Planes loaded successfully: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading planes file (" + filename + "): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing planes JSON (" + filename + "): " + e.getMessage());
        }
    }

    public void loadPassengers(String filename) {
        PassengerStorage passengerSto = PassengerStorage.getInstance();
        try (FileReader reader = new FileReader(filename)) {
            JSONArray array = new JSONArray(new JSONTokener(reader));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                long id = obj.getLong("id");
                String firstname = obj.getString("firstname");
                String lastname = obj.getString("lastname");
                String birthDateStr = obj.getString("birthDate");
                int code = obj.getInt("countryPhoneCode");
                long phone = obj.getLong("phone");
                String country = obj.getString("country");

                LocalDate birthDate = LocalDate.parse(birthDateStr);

                Passenger p = new Passenger(id, firstname, lastname, birthDate, code, phone, country);
                passengerSto.addPassenger(p);
            }
            System.out.println("Passengers loaded successfully: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading passengers file (" + filename + "): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing passengers JSON (" + filename + "): " + e.getMessage());
        }
    }

    public void loadFlights(String filename) {
        FlightStorage flightSto = FlightStorage.getInstance();
        PlaneStorage planeSto = PlaneStorage.getInstance();
        LocationStorage locationSto = LocationStorage.getInstance();

        try (FileReader reader = new FileReader(filename)) {
            JSONArray array = new JSONArray(new JSONTokener(reader));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String id = obj.getString("id");
                String planeId = obj.getString("plane");
                String departureLocationId = obj.getString("departureLocation");
                String arrivalLocationId = obj.getString("arrivalLocation");
                String scaleLocationId = obj.isNull("scaleLocation") ? null : obj.getString("scaleLocation");
                String departureDateStr = obj.getString("departureDate");

                int hoursDurationArrival = obj.getInt("hoursDurationArrival");
                int minutesDurationArrival = obj.getInt("minutesDurationArrival");
                int hoursDurationScale = obj.getInt("hoursDurationScale");
                int minutesDurationScale = obj.getInt("minutesDurationScale");

                Plane plane = (planeSto != null) ? planeSto.getPlane(planeId) : null;
                Location departureLocation = locationSto.getLocation(departureLocationId);
                Location arrivalLocation = locationSto.getLocation(arrivalLocationId);
                Location scaleLocation = null;
                
                if (scaleLocationId != null) {
                    scaleLocation = locationSto.getLocation(scaleLocationId);
                }

                LocalDateTime departureDate = LocalDateTime.parse(departureDateStr);

                Flight flight;
                if (plane == null || departureLocation == null || arrivalLocation == null) {
                    System.err.println("Skipping flight " + id + " due to missing plane or location references. Plane: " + planeId + ", Departure: " + departureLocationId + ", Arrival: " + arrivalLocationId);
                    continue;
                }
                if (scaleLocationId != null && scaleLocation == null) {
                    System.err.println("Skipping flight " + id + " due to missing scale location reference: " + scaleLocationId);
                    
                }

                if (scaleLocation != null) {
                    flight = new Flight(id, plane, departureLocation, scaleLocation, arrivalLocation, departureDate,
                                        hoursDurationArrival, minutesDurationArrival, hoursDurationScale, minutesDurationScale);
                } else {
                    flight = new Flight(id, plane, departureLocation, arrivalLocation, departureDate,
                                        hoursDurationArrival, minutesDurationArrival);
                }
                flightSto.addFlight(flight);
            }
            System.out.println("Flights loaded successfully: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading flights file (" + filename + "): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing flights JSON (" + filename + "): " + e.getMessage() + ". Check data consistency.");
            e.printStackTrace();
        }
    }
}
