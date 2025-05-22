/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.flight;

import core.models.flight.Flight;
import java.time.LocalDateTime;

/**
 *
 * @author Admin
 */
public class ArrivalDate{
    
    private Flight flight;

    public ArrivalDate(Flight flight) {
        this.flight = flight;
    }
    
    public LocalDateTime calculateArrivalDate() {
        return flight.departureDate.plusHours(flight.hoursDurationScale).plusHours(flight.hoursDurationArrival).plusMinutes(flight.minutesDurationScale).plusMinutes(flight.minutesDurationArrival);
    }
}
