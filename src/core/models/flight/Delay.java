/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.flight;

/**
 *
 * @author Admin
 */
public class Delay {
    private Flight flight;

    public Delay(Flight flight) {
        this.flight = flight;
    }
    
    public void delay(int hours, int minutes) {
        flight.setDepartureDate(flight.getDepartureDate().plusHours(hours).plusMinutes(minutes));
    }
   
}
