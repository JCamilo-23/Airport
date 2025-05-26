/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.calculate;

import core.models.flight.Flight;
import java.time.LocalDateTime;

/**
 *
 * @author Admin
 */
public class CalculateArrivalDate{
    
    private final Flight flight;

    public CalculateArrivalDate(Flight flight) {
        this.flight = flight;
    }
    
    public LocalDateTime calculateArrivalDate() {
        return flight.getDepartureDate().plusHours(flight.getHoursDurationScale()).plusHours(flight.getHoursDurationArrival()).plusMinutes(flight.getMinutesDurationScale()).plusMinutes(flight.getMinutesDurationArrival());
    }
}
