/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

import core.models.Location;
import core.models.storage.interfaces.ILocationStorage;
import core.models.utils.Observer;
import core.models.utils.Subject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author brayan
 */
    public class LocationStorage implements Subject, ILocationStorage   { 
    // Instancia Singleton
    private static LocationStorage instance;
    private ArrayList<Location> locations;
    private final ArrayList<Observer> observers; 

    private LocationStorage() {
        this.locations = new ArrayList<>();
        this.observers = new ArrayList<>(); 
    }

    public static synchronized LocationStorage getInstance() { 
        if (instance == null) {
            instance = new LocationStorage();
        }
        return instance;
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
    public ArrayList<Location> getLocations() {
    ArrayList<Location> sortedLocations = new ArrayList<>(this.locations);
    if (sortedLocations.size() > 1) { 
        Collections.sort(sortedLocations, Comparator.comparing(Location::getAirportId));
    }
    return sortedLocations;
}
    @Override
    public void notifyObservers() {
        ArrayList<Observer> observersCopy = new ArrayList<>(this.observers);
        System.out.println("LocationStorage: Notificando a " + observersCopy.size() + " observador(es)...");
        for (Observer observer : observersCopy) {
            observer.update();
        }
    }
    // Fin de métodos de Subject

    public boolean addLocation(Location location) {
        if (location == null || location.getAirportId() == null) {
            return false;
        }
        if (LocationIdExists(location.getAirportId())) {
            return false;
        }
        this.locations.add(location);
        System.out.println("LocationStorage: Localización agregada. ID: " + location.getAirportId());
        notifyObservers(); 
        return true;
    }

    public Location getLocation(String id) {
        if (id == null) return null;
        for (Location loc : this.locations) {
            if (id.equals(loc.getAirportId())) {
                return loc;
            }
        }
        return null;
    }

    public boolean deleteLocation(String id) {
        if (id == null) return false;
        Location locationToRemove = null;
        for (Location location : this.locations) {
            if (id.equals(location.getAirportId())) {
                locationToRemove = location;
                break;
            }
        }
        if (locationToRemove != null) {
            this.locations.remove(locationToRemove);
            System.out.println("LocationStorage: Localización eliminada. ID: " + id);
            notifyObservers(); // 4. Notificar después de eliminar
            return true;
        }
        return false;
    }

    public boolean LocationIdExists(String id) {
        if (id == null) return false;
        for (Location loc : this.locations) {
            if (id.equals(loc.getAirportId())) {
                return true;
            }
        }
        return false;
    }
}