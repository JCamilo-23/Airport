/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

import core.models.person.Passenger;
import java.util.ArrayList;

/**
 *
 * @author braya
 */

public class PassengerStorage {
    // Instancia Singleton
    private static PassengerStorage instance;
    
    // Atributos del Storage
    private ArrayList<Passenger> passengers;
    
    private PassengerStorage() {
        this.passengers = new ArrayList<>();
    }
    
    public static PassengerStorage getInstance() {
        if (instance == null) {
            instance = new PassengerStorage();
        }
        return instance;
    }
    
    public boolean addPassenger(Passenger passenger) {
        for (Passenger p : this.passengers) {
            if (p.getId() == passenger.getId()) {
                return false;
            }
        }
        this.passengers.add(passenger);
        return true;
    }
    
    public Passenger getPassenger(long id) {
       for (Passenger passenger : this.passengers) {
            if (passenger.getId() == id) {
                return passenger;
            }
        }
        return null;
    }
    
    public boolean deletePassenger(long id) {
        for (Passenger passenger : this.passengers) {
            if (passenger.getId() == id) {
                this.passengers.remove(passenger);
                return true;
            }
        }
        return false;
    }


   public boolean passengerIdExists(long id) {
        for (Passenger p : this.passengers) {
            if (p.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Passenger> getPassengers() {
        return passengers;
    }
   
   
}