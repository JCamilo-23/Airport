/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

import core.models.Plane;
import core.models.utils.Observer;
import core.models.utils.Subject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import core.models.storage.interfaces.IPlaneStorage;
/**
 *
 * @author brayan
 */

    public class PlaneStorage implements Subject, IPlaneStorage {
    private static PlaneStorage instance;
    private ArrayList<Plane> planes;
    private final ArrayList<Observer> observers;

    private PlaneStorage() {
        this.planes = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public static synchronized PlaneStorage getInstance() { 
        if (instance == null) {
            instance = new PlaneStorage();
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
    public ArrayList<Plane> getPlanes() {
    ArrayList<Plane> planesCopy = new ArrayList<>(this.planes);
    Collections.sort(planesCopy, Comparator.comparing(Plane::getId));
    return planesCopy;
}
    @Override
    public void notifyObservers() {
        ArrayList<Observer> observersCopy = new ArrayList<>(this.observers); 
        System.out.println("PlaneStorage: Notificando a " + observersCopy.size() + " observador(es)...");
        for (Observer observer : observersCopy) {
            observer.update();
        }
    }
 

    public boolean addPlane(Plane plane) {
        if (plane == null || plane.getId() == null) { 
            return false;
        }
        
        if (planeIdExists(plane.getId())) {
            return false;
        }
        this.planes.add(plane);
        System.out.println("PlaneStorage: Avión agregado. ID: " + plane.getId());
        notifyObservers(); // 4. Notificar después de agregar
        return true;
    }

    public Plane getPlane(String id) {
        if (id == null) return null;
        for (Plane p : this.planes) {
            if (id.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }

    public boolean deletePlane(String id) {
        if (id == null) return false;
        Plane planeToRemove = null;
        for (Plane plane : this.planes) {
            if (id.equals(plane.getId())) {
                planeToRemove = plane;
                break;
            }
        }
        if (planeToRemove != null) {
            this.planes.remove(planeToRemove);
            System.out.println("PlaneStorage: Avión eliminado. ID: " + id);
            notifyObservers(); 
            return true;
        }
        return false;
    }

    public boolean planeIdExists(String id) {
        if (id == null) return false;
        for (Plane p : this.planes) {
            if (id.equals(p.getId())) {
                return true;
            }
        }
        return false;
    }
}