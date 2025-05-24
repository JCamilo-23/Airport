/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.models.Plane;
import core.models.flight.Flight;
import core.models.storage.FlightStorage;
import core.models.storage.PlaneStorage;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author brayan
 */
public class PlaneTableController {
    private JTable planeTable;

    public PlaneTableController(JTable planeTable) {
        this.planeTable = planeTable;
    }

    public void refreshTable() {
        if (this.planeTable == null) {
            System.err.println("Error: La JTable para aviones no ha sido asignada al controlador.");
            return;
        }
        DefaultTableModel model = (DefaultTableModel) this.planeTable.getModel();
        model.setRowCount(0);
        ArrayList<Plane> allPlanes = PlaneStorage.getInstance().getPlanes(); 
        ArrayList<Flight> allFlights = FlightStorage.getInstance().getFlights();


        if (allPlanes != null) {
            for (Plane plane : allPlanes) {
                if (plane != null) {
                    int numFlightsForPlane = 0;
                    if (allFlights != null && plane.getId() != null) {
                        for (Flight flight : allFlights) {
                            if (flight != null && flight.getPlane() != null && flight.getPlane().getId() != null &&
                                flight.getPlane().getId().equals(plane.getId())) {
                                numFlightsForPlane++;
                            }
                        }
                    }
                    model.addRow(new Object[]{
                        plane.getId() != null ? plane.getId() : "N/A",
                        plane.getBrand() != null ? plane.getBrand() : "N/A",
                        plane.getModel() != null ? plane.getModel() : "N/A",
                        plane.getMaxCapacity(),
                        plane.getAirline() != null ? plane.getAirline() : "N/A",
                        numFlightsForPlane
                    });
                }
            }
        } else {
            System.err.println("No se pudieron obtener los aviones del almacenamiento (lista nula).");
        }
    }
}
