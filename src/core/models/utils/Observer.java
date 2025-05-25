/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package core.models.utils;

/**
 *
 * @author brayan
 */
public interface Observer {
       /**
     * Método llamado por el Sujeto cuando su estado cambia.
     * El observador debe implementar la lógica para reaccionar a esta actualización.
     */
    void update();
}
