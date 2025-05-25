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
    public class LocationStorage implements Subject, ILocationStorage   { // 1. Implementar Subject
    // Instancia Singleton
    private static LocationStorage instance;
    private ArrayList<Location> locations;
    private final ArrayList<Observer> observers; // 2. Añadir lista de observadores

    private LocationStorage() {
        this.locations = new ArrayList<>();
        this.observers = new ArrayList<>(); // Inicializar la lista de observadores
    }

    public static synchronized LocationStorage getInstance() { // Sincronizado
        if (instance == null) {
            instance = new LocationStorage();
        }
        return instance;
    }

    // 3. Implementación de los métodos de Subject
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
    
    @Override // Añade si getLocations() está en ILocationStorage
    public ArrayList<Location> getLocations() {
    ArrayList<Location> sortedLocations = new ArrayList<>(this.locations);
    if (sortedLocations.size() > 1) { // Solo ordenar si hay más de un elemento
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
        // Usar el método LocationIdExists para la lógica de unicidad
        if (LocationIdExists(location.getAirportId())) {
            return false;
        }
        this.locations.add(location);
        System.out.println("LocationStorage: Localización agregada. ID: " + location.getAirportId());
        notifyObservers(); // 4. Notificar después de agregar
        return true;
    }

    public Location getLocation(String id) {
        if (id == null) return null;
        for (Location loc : this.locations) {
            // Corrección: Usar .equals() para comparar Strings
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
            // Corrección: Usar .equals() para comparar Strings
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
            // Corrección: Usar .equals() para comparar Strings
            if (id.equals(loc.getAirportId())) {
                return true;
            }
        }
        return false;
    }


    // Si necesitas un método para actualizar localizaciones, se vería así:
    // public boolean updateLocation(Location locationToUpdate) {
    //     if (locationToUpdate == null || locationToUpdate.getAirportId() == null) {
    //         return false;
    //     }
    //     for (int i = 0; i < this.locations.size(); i++) {
    //         if (locationToUpdate.getAirportId().equals(this.locations.get(i).getAirportId())) {
    //             this.locations.set(i, locationToUpdate);
    //             System.out.println("LocationStorage: Localización actualizada. ID: " + locationToUpdate.getAirportId());
    //             notifyObservers(); // 4. Notificar después de actualizar
    //             return true;
    //         }
    //     }
    //     return false;
    // }
}