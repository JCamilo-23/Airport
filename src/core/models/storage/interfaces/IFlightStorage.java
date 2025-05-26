/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package core.models.storage.interfaces;

import core.models.flight.Flight;
import java.util.ArrayList;

/**
 *
 * @author brayan
 */
public interface IFlightStorage {
    boolean addFlight(Flight flight);
    Flight getFlight(String id);
    boolean updateFlight(Flight flight);
    ArrayList<Flight> getFlights();
    boolean flightIdExists(String id);
}
