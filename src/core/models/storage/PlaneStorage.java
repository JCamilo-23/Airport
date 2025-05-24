/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

import core.models.Plane;
import java.util.ArrayList;

/**
 *
 * @author brayan
 */
public class PlaneStorage {
    private static PlaneStorage instance;
    private ArrayList<Plane> planes;

    private PlaneStorage() {
        this.planes = new ArrayList<>();
    }

    public static PlaneStorage getInstance() {
        if (instance == null) {
            instance = new PlaneStorage();
        }
        return instance;
    }

    public boolean addPlane(Plane plane) {
        for (Plane p : this.planes) {
            if (p.getId() == plane.getId()) {
                return false;
            }
        }
        this.planes.add(plane);
        return true;
    }

    public Plane getPlane(String id) {
        for (Plane p : this.planes) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public boolean deletePlane(String id) {
        for (Plane plane : this.planes) {
            if (plane.getId() == id) {
                this.planes.remove(plane);
                return true;
            }
        }
        return false;
    }

    public boolean planeIdExists(String id) {
        for (Plane p : this.planes) {
            if (p.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Plane> getPlanes() {
        return planes;
    }
}