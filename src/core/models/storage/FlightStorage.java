/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

/**
 *
 * @author braya
 */

import core.models.flight.Flight;
import java.util.ArrayList;

 public class FlightStorage {
    // Instancia Singleton
    private static FlightStorage instance;

    private ArrayList<Flight> flights;

    private FlightStorage() {
        this.flights = new ArrayList<>();
    }

    public static FlightStorage getInstance() {
        if (instance == null) {
            instance = new FlightStorage();
        }
        return instance;
    }

    public boolean addFlight(Flight flight) {
        for (Flight f : this.flights) {
            if (f.getId() == flight.getId()) {
                return false; 
            }
        }
        this.flights.add(flight);
        return true;
    }

    public Flight getFlight(String id) {
        for (Flight f : this.flights) {
            if (f.getId() == id) {
                return f;
            }
        }
        return null; // Flight not found
    }

    public boolean deleteFlight(String id) {
        for (Flight flight : this.flights) {
            if (flight.getId() == id) {
                this.flights.remove(flight);
                return true; 
            }
        }
        return false;        
    }
    public boolean flightIdExists(String id) {
        for (Flight f : this.flights) {
            if (f.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Flight> getFlights() {
        return flights;
    }
}