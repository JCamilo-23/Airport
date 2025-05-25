/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package core.models.storage.interfaces;

import core.models.person.Passenger;
import java.util.ArrayList;

/**
 *
 * @author brayan
 */
public interface IPassengerStorage {
    boolean addPassenger(Passenger passenger);
    Passenger getPassengerById(long id); 
    boolean updatePassenger(Passenger passengerToUpdate);
    boolean passengerIdExists(long id);
    ArrayList<Passenger> getPassengers();
}
