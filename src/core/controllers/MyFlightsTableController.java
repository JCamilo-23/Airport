/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.models.flight.Flight;
import core.models.person.Passenger;
import core.models.storage.PassengerStorage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author brayan
 */
public class MyFlightsTableController {
    private JTable myFlightsTable;
    private JComboBox<String> userSelectComboBox; 
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MyFlightsTableController(JTable myFlightsTable, JComboBox<String> userSelectComboBox) {
        this.myFlightsTable = myFlightsTable;
        this.userSelectComboBox = userSelectComboBox;
    }

    public void refreshTable() {
        if (this.myFlightsTable == null || this.userSelectComboBox == null) {
            System.err.println("Error: La JTable 'Mis Vuelos' o el JComboBox de selección de usuario no han sido asignados.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) this.myFlightsTable.getModel();
        model.setRowCount(0);

        String selectedUserStr = null;
        if (userSelectComboBox.getSelectedIndex() > 0) { 
            selectedUserStr = (String) userSelectComboBox.getSelectedItem();
        }

        if (selectedUserStr == null || selectedUserStr.equals("Select User") || selectedUserStr.trim().isEmpty()) {
            return; 
        }

        try {
            long passengerId = Long.parseLong(selectedUserStr);
            Passenger passenger = PassengerStorage.getInstance().getPassenger(passengerId); //

            if (passenger != null && passenger.getFlights() != null) {
                for (Flight flight : passenger.getFlights()) {
                    if (flight != null) {
                        String flightId = flight.getId();
                        String departureDateStr = (flight.getDepartureDate() != null) ?
                                                  flight.getDepartureDate().format(dateTimeFormatter) : "N/A";

                        String arrivalDateStr = "N/A";
                        if (flight.getDepartureDate() != null && flight.getDurationHoursArrival() != null && flight.getDurationMinutesArrival() != null) {
                           try {
                               LocalDateTime arrivalDateTime = flight.getDepartureDate()
                                   .plusHours((long) flight.getDurationHoursArrival())
                                   .plusMinutes((long) flight.getDurationMinutesArrival());
                               arrivalDateStr = arrivalDateTime.format(dateTimeFormatter);
                           } catch (Exception e) {
                               System.err.println("Error calculando fecha de llegada para mis vuelos, vuelo " + flight.getId() + ": " + e.getMessage());
                           }
                        }
                        model.addRow(new Object[]{flightId, departureDateStr, arrivalDateStr});
                    }
                }
            } else if (passenger == null) {
            }
        } catch (NumberFormatException e) {
             System.err.println("Error al convertir ID de pasajero para 'Mis Vuelos': " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ocurrió un error al refrescar 'Mis Vuelos': " + e.getMessage());
            e.printStackTrace();
        }
    }
}
