/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

/**
 *
 * @author brayan
 */

import core.models.flight.Flight;
import core.models.storage.interfaces.IFlightStorage;
import core.patterns.Observer;
import core.patterns.Subject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FlightStorage implements Subject, IFlightStorage { // Implementaciones correctas
    private static FlightStorage instance;
    private ArrayList<Flight> flights;
    private final ArrayList<Observer> observers;

    private FlightStorage() {
        this.flights = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public static synchronized FlightStorage getInstance() {
        if (instance == null) {
            instance = new FlightStorage();
        }
        return instance;
    }

    // Métodos de Subject (register, remove, notify) - Se ven bien
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
        System.out.println("FlightStorage: Notificando a " + observersCopy.size() + " observador(es)...");
        for (Observer observer : observersCopy) {
            observer.update();
        }
    }

    @Override 
    public boolean addFlight(Flight flight) {
        if (flight == null || flight.getId() == null) {
            return false;
        }
        if (flightIdExists(flight.getId())) { 
            return false;
        }
        this.flights.add(flight);
        System.out.println("FlightStorage: Vuelo agregado. ID: " + flight.getId());
        notifyObservers(); 
        return true;
    }

    @Override 
    public Flight getFlight(String id) {
        if (id == null) return null;
        for (Flight f : this.flights) {
            if (id.equals(f.getId())) { 
                return f;
            }
        }
        return null;
    }

    public boolean deleteFlight(String id) {
        if (id == null) return false;
        Flight flightToRemove = null;
        for (Flight flight : this.flights) {
            if (id.equals(flight.getId())) { // Correcto
                flightToRemove = flight;
                break;
            }
        }
        if (flightToRemove != null) {
            this.flights.remove(flightToRemove);
            System.out.println("FlightStorage: Vuelo eliminado. ID: " + id);
            notifyObservers(); // Correcto: Notificar después de eliminar
            return true;
        }
        return false;
    }

    
    public boolean flightIdExists(String id) {
        if (id == null) return false;
        for (Flight f : this.flights) {
            if (id.equals(f.getId())) { // Correcto
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ArrayList<Flight> getFlights() {
        ArrayList<Flight> sortedFlights = new ArrayList<>(this.flights);
        // Requisito del parcial: Los vuelos se deben obtener de manera ordenada
        // (respecto a su fecha de salida, de los más antiguos a los más nuevos).
        // Asegúrate que tu clase Flight tenga un método getDepartureDate()
        // que devuelva un tipo comparable (como LocalDateTime).
        if (sortedFlights.size() > 1) { // Solo ordenar si hay más de un elemento
             Collections.sort(sortedFlights, Comparator.comparing(Flight::getDepartureDate));
        }
        return sortedFlights; // Devuelve la copia ordenada
    }

    @Override
    public boolean updateFlight(Flight flightToUpdate) {
        if (flightToUpdate == null || flightToUpdate.getId() == null) {
            return false;
        }
        for (int i = 0; i < this.flights.size(); i++) {
            if (flightToUpdate.getId().equals(this.flights.get(i).getId())) {
                this.flights.set(i, flightToUpdate);
                System.out.println("FlightStorage: Vuelo actualizado/retrasado. ID: " + flightToUpdate.getId());
                notifyObservers(); // ¡Correcto y crucial! Notificar después de actualizar
                return true;
            }
        }
        return false; // No se encontró para actualizar
    }
}