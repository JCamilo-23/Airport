/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

import core.models.person.Passenger;
import core.models.storage.interfaces.IPassengerStorage;
import core.models.utils.Observer;
import core.models.utils.Subject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author brayan
 */

    public class PassengerStorage implements Subject, IPassengerStorage {

    private static PassengerStorage instance;
    private ArrayList<Passenger> passengers;
    // Usar ArrayList<Observer> directamente para el tipo del campo
    private final ArrayList<Observer> observers;

    private PassengerStorage() {
        this.passengers = new ArrayList<>();
        this.observers = new ArrayList<>(); // Inicializar la lista de observadores
    }

    public static synchronized PassengerStorage getInstance() {
        if (instance == null) {
            instance = new PassengerStorage();
        }
        return instance;
    }

     @Override
    public ArrayList<Passenger> getPassengers() {
        ArrayList<Passenger> sortedPassengers = new ArrayList<>(this.passengers);
        Collections.sort(sortedPassengers, Comparator.comparingLong(Passenger::getId));
        return sortedPassengers;
    }
    
    @Override
    public void registerObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers() {
        ArrayList<Observer> observersCopy = new ArrayList<>(this.observers);
        System.out.println("PassengerStorage: Notificando a " + observersCopy.size() + " observador(es)...");
        for (Observer observer : observersCopy) {
            observer.update();
        }
    }

    public boolean addPassenger(Passenger passenger) {
        if (passenger == null || passengerIdExists(passenger.getId())) {
            return false;
        }
        this.passengers.add(passenger);
        System.out.println("PassengerStorage: Pasajero agregado. ID: " + passenger.getId());
        notifyObservers(); // Notificar
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

    public Passenger getPassengerById(long id) {
        return getPassenger(id);
    }

    public boolean deletePassenger(long id) {
        Passenger passengerToRemove = null;
        for (Passenger passenger : this.passengers) {
            if (passenger.getId() == id) {
                passengerToRemove = passenger;
                break;
            }
        }
        if (passengerToRemove != null) {
            this.passengers.remove(passengerToRemove);
            System.out.println("PassengerStorage: Pasajero eliminado. ID: " + id);
            notifyObservers();
            return true;
        }
        return false;
    }

    public boolean updatePassenger(Passenger passengerToUpdate) {
        if (passengerToUpdate == null) {
            return false;
        }
        for (int i = 0; i < this.passengers.size(); i++) {
            if (this.passengers.get(i).getId() == passengerToUpdate.getId()) {
                this.passengers.set(i, passengerToUpdate);
                System.out.println("PassengerStorage: Pasajero actualizado. ID: " + passengerToUpdate.getId());
                notifyObservers(); 
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
}