/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers.tables;

import core.models.flight.Flight;
import core.models.storage.FlightStorage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author brayan
 */
public class FlightTableController {
    private JTable flightTable;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FlightTableController(JTable flightTable) {
        this.flightTable = flightTable;
    }

    public void refreshTable() {
        if (this.flightTable == null) {
            System.err.println("Error: La JTable para vuelos no ha sido asignada al controlador.");
            return;
        }
        DefaultTableModel model = (DefaultTableModel) this.flightTable.getModel();
        model.setRowCount(0);
        ArrayList<Flight> allFlights;
        if (FlightStorage.getInstance() != null && FlightStorage.getInstance().getFlights() != null ) { 
             allFlights = FlightStorage.getInstance().getFlights();
        } else {
            System.err.println("FlightStorage o getFlights() no disponible. La tabla de vuelos no se puede refrescar desde el storage.");
            allFlights = new ArrayList<>(); 
        }


        if (allFlights != null) {
            for (Flight flight : allFlights) {
                if (flight != null) {
                    String flightId = flight.getId();
                    String depAirportId = (flight.getDepartureLocation() != null && flight.getDepartureLocation().getAirportId() != null) ?
                                          flight.getDepartureLocation().getAirportId() : "N/A";
                    String arrAirportId = (flight.getArrivalLocation() != null && flight.getArrivalLocation().getAirportId() != null) ?
                                          flight.getArrivalLocation().getAirportId() : "N/A";
                    String scaleAirportId = (flight.getScaleLocation() != null && flight.getScaleLocation().getAirportId() != null) ?
                                            flight.getScaleLocation().getAirportId() : "-";
                    String depDateStr = (flight.getDepartureDate() != null) ?
                                        flight.getDepartureDate().format(dateTimeFormatter) : "N/A";

                    String arrDateStr = "N/A";
//                    if (flight.getDepartureDate() != null && flight.getHoursDurationArrival() != null && flight.getMinutesDurationArrival() != null) {
                       try {
                           LocalDateTime arrivalDateTime = flight.getDepartureDate()
                               .plusHours((long) flight.getHoursDurationArrival())
                               .plusMinutes((long) flight.getMinutesDurationArrival());
                           arrDateStr = arrivalDateTime.format(dateTimeFormatter);
                       } catch (Exception e) {
                           System.err.println("Error calculando fecha de llegada para vuelo " + flight.getId() + ": " + e.getMessage());
                       }
                    

                    String planeIdStr = (flight.getPlane() != null && flight.getPlane().getId() != null) ?
                                        flight.getPlane().getId() : "N/A";
                    int numPassengers = flight.getNumPassengers();
                    model.addRow(new Object[]{
                        flightId, depAirportId, arrAirportId, scaleAirportId,
                        depDateStr, arrDateStr, planeIdStr, numPassengers
                    });
                }
            }
        }
    }
}
