/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.calculate;

import core.models.person.Passenger;
import java.time.LocalDate;
import java.time.Period;

/**
 *
 * @author Andrea Osio Amaya
 */
public class PassengerCalculate {
    private final Passenger passenger;

    public PassengerCalculate(Passenger passenger) {
        this.passenger = passenger;
    }
    public int calculateAge() {
        return Period.between(passenger.getBirthDate(), LocalDate.now()).getYears();
    }
    
    public int getNumFlights() {
        return passenger.getFlights().size();
    }
}
