/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers.tables;

import core.models.calculate.PassengerCalculate;
import core.models.calculate.PassengerFormats;
import core.models.person.Passenger;
import core.models.storage.PassengerStorage;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author brayan
 */
public class PassengerTableController {
    private JTable passengerTable;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PassengerTableController(JTable passengerTable) {
        this.passengerTable = passengerTable;
    }

    public void refreshTable() {
        if (this.passengerTable == null) {
            System.err.println("Error: La JTable para pasajeros no ha sido asignada al controlador.");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) this.passengerTable.getModel();
        model.setRowCount(0);
        ArrayList<Passenger> allPassengers = PassengerStorage.getInstance().getPassengers(); //

        if (allPassengers != null) {
            for (Passenger passenger : allPassengers) {
                if (passenger != null) {
                    PassengerFormats format = new PassengerFormats(passenger);
                    String id = String.valueOf(passenger.getId());
                    String name = format.getFullname();
                    String birthdate = (passenger.getBirthDate() != null) ?
                                       passenger.getBirthDate().format(dateFormatter) : "N/A";
                    
                    String ageStr = "N/A";
                    if (passenger.getBirthDate() != null) {
                        try {
                            PassengerCalculate calculate = new PassengerCalculate(passenger);
                            
                            ageStr = ""+calculate.calculateAge();
                        } catch (Exception e) {
                            System.err.println("No se pudo determinar la edad para el pasajero " + id + ": " + e.getMessage());
                        }
                    }

                    String phone = format.generateFullPhone();
                    String country = passenger.getCountry() != null ? passenger.getCountry() : "N/A";
                    int numFlights = (passenger.getFlights() != null) ? passenger.getFlights().size() : 0;

                    model.addRow(new Object[]{id, name.trim(), birthdate, ageStr, phone, country, numFlights});
                }
            }
        } else {
             System.err.println("No se pudieron obtener los pasajeros del almacenamiento (lista nula).");
        }
    }
}
