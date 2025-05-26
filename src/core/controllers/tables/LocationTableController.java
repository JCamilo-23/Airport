/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers.tables;

import core.models.Location;
import core.models.storage.LocationStorage;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author braya
 */
public class LocationTableController {
    private JTable locationTable;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public LocationTableController(JTable locationTable) {
        this.locationTable = locationTable;
    }

    public void refreshTable() {
        if (this.locationTable == null) {
            System.err.println("Error: La JTable para pasajeros no ha sido asignada al controlador.");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) this.locationTable.getModel();
        model.setRowCount(0);
        ArrayList<Location> alllocations = LocationStorage.getInstance().getLocations(); //

        if (alllocations != null) {
            for (Location location : alllocations) {
                if (location != null) {
                    String id = location.getAirportId();
                    String airportName = location.getAirportName();
                    String airportCity = location.getAirportCity();
                    String airportCountry = location.getAirportCountry();
                    
                    model.addRow(new Object[]{id, airportName, airportCity,airportCountry});
                }
            }
        } else {
             System.err.println("No se pudieron obtener los pasajeros del almacenamiento (lista nula).");
        }
    }
}
