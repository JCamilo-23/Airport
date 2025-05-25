/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models;

import core.models.flight.Flight;
import core.models.utils.Add;
import java.util.ArrayList;

/**
 *
 * @author edangulo
 */
public class Plane implements Prototype<Plane>, Add{
    
    private final String id;
    private String brand;
    private String model;
    private final int maxCapacity;
    private String airline;
    private ArrayList<Flight> flights;

    public Plane(String id, String brand, String model, int maxCapacity, String airline) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.maxCapacity = maxCapacity;
        this.airline = airline;
        this.flights = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public String getAirline() {
        return airline;
    }

    public ArrayList<Flight> getFlights() {
        return flights;
    }
    
    public int getNumFlights() {
        return flights.size();
    }

    @Override
    public Plane clone() {
        Plane clone = new Plane(id, brand, model, maxCapacity, airline);
        clone.getFlights().addAll(flights);
        return clone;
    }

    @Override
    public void addFlight(Flight flight) {
        this.flights.add(flight);
    }
    
}
