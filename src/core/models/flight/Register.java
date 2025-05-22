/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.flight;

import core.models.flight.Flight;
import core.models.person.Passenger;
import java.time.LocalDateTime;

/**
 *
 * @author Admin
 */
public class Register{
    
    private Flight flight;

    public Register(Flight flight) {
        this.flight = flight;
    }
    
    public void addPassenger(Passenger passenger) {
        flight.passengers.add(passenger);
    }
}
