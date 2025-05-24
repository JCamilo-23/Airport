/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

import core.models.Plane;
import core.patterns.Observer;
import core.patterns.Subject;
import java.util.ArrayList;

/**
 *
 * @author brayan
 */

    public class PlaneStorage implements Subject { 
    private static PlaneStorage instance;
    private ArrayList<Plane> planes;
    private final ArrayList<Observer> observers; // 2. Añadir lista de observadores (usando ArrayList)

    private PlaneStorage() {
        this.planes = new ArrayList<>();
        this.observers = new ArrayList<>(); // Inicializar la lista de observadores
    }

    public static synchronized PlaneStorage getInstance() { // Sincronizado para seguridad
        if (instance == null) {
            instance = new PlaneStorage();
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

    @Override
    public void notifyObservers() {
        ArrayList<Observer> observersCopy = new ArrayList<>(this.observers); // Copia para iteración segura
        System.out.println("PlaneStorage: Notificando a " + observersCopy.size() + " observador(es)...");
        for (Observer observer : observersCopy) {
            observer.update();
        }
    }
    // Fin de métodos de Subject

    public boolean addPlane(Plane plane) {
        if (plane == null || plane.getId() == null) { // Buena idea chequear si plane o su ID es null
            return false;
        }
        // Usar el método planeIdExists para la lógica de unicidad
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
            // Corrección: Usar .equals() para comparar Strings
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
            // Corrección: Usar .equals() para comparar Strings
            if (id.equals(plane.getId())) {
                planeToRemove = plane;
                break;
            }
        }
        if (planeToRemove != null) {
            this.planes.remove(planeToRemove);
            System.out.println("PlaneStorage: Avión eliminado. ID: " + id);
            notifyObservers(); // 4. Notificar después de eliminar
            return true;
        }
        return false;
    }

    public boolean planeIdExists(String id) {
        if (id == null) return false;
        for (Plane p : this.planes) {
            // Corrección: Usar .equals() para comparar Strings
            if (id.equals(p.getId())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Plane> getPlanes() {
        // Devolver una copia para proteger la lista interna y consistencia
        return new ArrayList<>(this.planes);
    }

    // Ejemplo de cómo se vería un método de actualización (si lo necesitas)
    // public boolean updatePlane(Plane planeToUpdate) {
    //     if (planeToUpdate == null || planeToUpdate.getId() == null) {
    //         return false;
    //     }
    //     for (int i = 0; i < this.planes.size(); i++) {
    //         if (planeToUpdate.getId().equals(this.planes.get(i).getId())) {
    //             this.planes.set(i, planeToUpdate); // Actualiza el avión en la lista
    //             System.out.println("PlaneStorage: Avión actualizado. ID: " + planeToUpdate.getId());
    //             notifyObservers(); // 4. Notificar después de actualizar
    //             return true;
    //         }
    //     }
    //     return false; // No se encontró el avión para actualizar
    // }
}