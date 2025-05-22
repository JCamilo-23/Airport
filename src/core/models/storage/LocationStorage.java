/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

import core.models.Location;
import java.util.ArrayList;

/**
 *
 * @author braya
 */
public class LocationStorage {
    // Instancia Singleton
    private static LocationStorage instance;

    private ArrayList<Location> locations;

    private LocationStorage() {
        this.locations = new ArrayList<>();
    }

    public static LocationStorage getInstance() {
        if (instance == null) {
            instance = new LocationStorage();
        }
        return instance;
    }

    public boolean addLocation(Location location) {
        for (Location l : this.locations) {
            if (l.getAirportId() == location.getAirportId()) {
                return false; 
            }
        }
        this.locations.add(location);
        return true;
    }

    public Location getLocation(String id) {
        for (Location loc : this.locations) {
            if (loc.getAirportId() == id) {
                return loc;
            }
        }
        return null; // Location not found
    }

    public boolean deleteLocation(String id) {
        for (Location location : this.locations) {
            if (location.getAirportId() == id) {
                this.locations.remove(location);
                return true;
            }
        }
        return false; 
    }

    public boolean LocationIdExists(String id) {
        for (Location loc : this.locations) {
            if (loc.getAirportId() == id) {
                return true;
            }
        }
        return false;
    }
}