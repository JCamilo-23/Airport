/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package core.models.storage.interfaces;

import core.models.Location;
import java.util.ArrayList;

/**
 *
 * @author braya
 */
public interface ILocationStorage {
    boolean addLocation(Location location);
    Location getLocation(String id); 
    boolean LocationIdExists(String id);
    ArrayList<Location> getLocations(); 
    
}

