/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.calculate;

import core.models.person.Passenger;

/**
 *
 * @author Andrea Osio Amaya
 */
public class PassengerFormats {
    private final Passenger passenger;

    public PassengerFormats(Passenger passenger) {
        this.passenger = passenger;
    }
    
    public String getFullname() {
        return passenger.getFirstname() + " " + passenger.getLastname();
    }
    
    public String generateFullPhone() {
        return "+" + passenger.getCountryPhoneCode() + " " + passenger.getPhone();
    }
}
