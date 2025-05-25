/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package core.patterns;

/**
 *
 * @author brayan
 */
public interface Subject {
    /**
     * Registra un observador para que reciba notificaciones de este sujeto.
     * @param observer El observador a registrar.
     */
    void registerObserver(Observer observer);

    /**
     * Elimina un observador de la lista de notificaciones de este sujeto.
     * @param observer El observador a eliminar.
     */
    void removeObserver(Observer observer);

    /**
     * Notifica a todos los observadores registrados que el estado del sujeto ha cambiado.
     */
    void notifyObservers();
}
