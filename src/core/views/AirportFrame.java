/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package core.views;

import core.controllers.FlightController;
import core.controllers.LocationController;
import core.controllers.PassengerController;
import core.controllers.PlaneController;
import core.controllers.tables.FlightTableController;
import core.controllers.tables.LocationTableController;
import core.controllers.tables.MyFlightsTableController;
import core.controllers.tables.PassengerTableController;
import core.controllers.tables.PlaneTableController;
import core.controllers.utils.Response;
import core.controllers.utils.Status;
import core.models.flight.Flight;
import core.models.storage.FlightStorage;
import core.models.storage.LocationStorage;
import core.models.storage.PassengerStorage;
import core.models.storage.PlaneStorage;
import core.patterns.Observer;
import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author edangulo
 */
public class AirportFrame extends javax.swing.JFrame implements Observer {

    private int x, y;
    private DefaultTableModel allPassengersTableModel;
    private DefaultTableModel allPlanesTableModel;
    private DefaultTableModel allLocationsTableModel;
    private DefaultTableModel allFlightsTableModel;
    private DefaultTableModel myFlightsTableModel;

    public AirportFrame() {
        initComponents(); // Esto crea los JTables

        // --- INICIALIZAR TableModels DESPUÉS DE initComponents() ---
        this.allPassengersTableModel = (DefaultTableModel) allPassengersTable.getModel();
        this.allPlanesTableModel = (DefaultTableModel) allPlanesTable.getModel();
        this.allLocationsTableModel = (DefaultTableModel) allLocationsTable.getModel();
        this.allFlightsTableModel = (DefaultTableModel) allFlightsTable.getModel();
        this.myFlightsTableModel = (DefaultTableModel) myFlightsTable.getModel();

        // Las siguientes llamadas parecen ser para poblar ComboBoxes, lo cual está bien.
        // Considera si estos también deberían actualizarse con el patrón Observer
        // o si una carga inicial es suficiente.
        PassengerController.storageDownload(userSelect);
        FlightController.storageDownload(addToFlightSelectionComboBox);
        FlightController.storageDownload(delayFlightIdComboBox);
        PlaneController.storageDownload(flightPlaneComboBox);
        LocationController.storageDownload(flightDepartureLocationComboBox);
        LocationController.storageDownload(flightArrivalLocationComboBox);
        LocationController.storageDownload(flightScaleLocationComboBox);

        // this.passengers = new ArrayList<>(); // Comentado
        // this.planes = new ArrayList<>();     // Comentado
        // this.locations = new ArrayList<>();  // Comentado
        // this.flights = new ArrayList<>();    // Comentado

        this.setBackground(new Color(0, 0, 0, 0));
        this.setLocationRelativeTo(null);

        this.generateMonths();
        this.generateDays();
        this.generateHours();
        this.generateMinutes();
        this.blockPanels();

        // --- 3. REGISTRAR AirportFrame COMO OBSERVADOR ---
        PassengerStorage.getInstance().registerObserver(this);
        PlaneStorage.getInstance().registerObserver(this);
        LocationStorage.getInstance().registerObserver(this);
        FlightStorage.getInstance().registerObserver(this);

        // --- CARGA INICIAL DE DATOS EN TABLAS ---
        // Es bueno cargar los datos una vez que la UI está lista y se ha registrado como observador.
        refreshAllPassengersTableData();
        refreshAllPlanesTableData();
        refreshAllLocationsTableData();
        refreshAllFlightsTableData();
        // refreshMyFlightsTableData(); // Implementa esto si es necesario
    }
  @Override
    public void update() {
        System.out.println("AirportFrame (Observer): Notificación recibida. Actualizando tablas...");
        // Llamamos a todos los métodos de refresco.
        // En una implementación más avanzada, podrías tener formas de saber qué cambió
        // para solo refrescar la tabla necesaria, pero esto es un buen comienzo.
        refreshAllPassengersTableData();
        refreshAllPlanesTableData();
        refreshAllLocationsTableData();
        refreshAllFlightsTableData();
        // refreshMyFlightsTableData(); // Si también necesitas que esta tabla se actualice automáticamente
    }
      // --- Métodos privados para bloquear paneles y generar ComboBoxes (tu código existente) ---
    private void blockPanels() {
        // Tu lógica actual para habilitar/deshabilitar pestañas según el tipo de usuario
        // Administrador: todas excepto Update Info (idx 5), Add to Flight (idx 6), Show my Flights (idx 7)
        // Usuario: Show all Flights (idx 9->8), Show all Locations (idx 11->10), Update Info (idx 5), Add to Flight (idx 6), Show my Flights (idx 7)
        // Los índices pueden cambiar si se añaden o quitan pestañas. Revisa los índices en jTabbedPane1.
        // Por ahora, dejo tu lógica original, pero asegúrate de que los índices sean correctos.
        // La lógica actual de administratorActionPerformed y userActionPerformed parece manejar esto.
        // Lo importante es que al inicio se bloqueen correctamente.
        for (int i = 1; i < jTabbedPane1.getTabCount(); i++) {
            // Pestañas que podrían estar inicialmente deshabilitadas o depender del tipo de usuario
            // La lógica de habilitación/deshabilitación ya está en administratorActionPerformed y userActionPerformed
            // Podrías querer un estado inicial aquí o llamar a uno de esos métodos.
            // Por ahora, la mantendré como la tenías, asumiendo que funciona con los radio buttons.
             if (i != 9 && i != 11) { // Show all flights (idx 8), Show all locations (idx 10)
                 jTabbedPane1.setEnabledAt(i, false);
             }
        }
         // Si quieres un estado por defecto (ej. ninguna opción de admin/user seleccionada al inicio)
        if (!administrator.isSelected() && !user.isSelected()) {
            for (int i = 1; i < jTabbedPane1.getTabCount(); i++) {
                jTabbedPane1.setEnabledAt(i, false);
            }
        }
    }
    private void refreshAllFlightsTableData() {
        System.out.println("AirportFrame: Refrescando tabla de vuelos...");
        allFlightsTableModel.setRowCount(0);
        // Asume que FlightController.getAllFlights() existe y devuelve lista ordenada por fecha
        Response response = FlightController.getAllFlights(); 
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (response.getStatus() == Status.SUCCESS && response.getData() != null) {
            ArrayList<Flight> flightsList = (ArrayList<Flight>) response.getData();
            for (Flight flight : flightsList) {
                // Columnas definidas en tu initComponents para allFlightsTable:
                // "ID", "Departure Airport ID", "Arrival Airport ID", "Scale Airport ID",
                // "Departure Date", "Arrival Date", "Plane ID", "Number Passengers"
                int numPassengers = 0; // TODO: Necesitas una forma de obtener esto del modelo Flight
                                       // ej. flight.getPassengers().size()

                allFlightsTableModel.addRow(new Object[]{
                    flight.getId(),
                    flight.getDepartureLocation() != null ? flight.getDepartureLocation().getAirportId() : "N/A",
                    flight.getArrivalLocation() != null ? flight.getArrivalLocation().getAirportId() : "N/A",
                    flight.getScaleLocation() != null ? flight.getScaleLocation().getAirportId() : "N/A",
                    flight.getDepartureDate() != null ? flight.getDepartureDate().format(dateTimeFormatter) : "N/A",
                    flight.getArrivalDate() != null ? flight.getArrivalDate().format(dateTimeFormatter) : "N/A", // Asume que existe getArrivalDate
                    flight.getPlane() != null ? flight.getPlane().getId() : "N/A",
                    numPassengers
                });
            }
        } else if (response.getStatus() != Status.SUCCESS && response.getData() == null && response.getMessage().contains("No flights found")) {
            System.out.println("AirportFrame: No se encontraron vuelos para mostrar.");
        } else if (response.getStatus() != Status.SUCCESS) {
             JOptionPane.showMessageDialog(this, "Error refrescando tabla de vuelos: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("AirportFrame: Tabla de vuelos refrescada.");
    }

    private void refreshMyFlightsTableData() {
        System.out.println("AirportFrame: Refrescando tabla de 'Mis Vuelos'...");
        myFlightsTableModel.setRowCount(0);
        String selectedPassengerId = updateInfoPassengerIdTextField.getText(); // O de donde obtengas el ID del pasajero logueado/seleccionado

        if (selectedPassengerId == null || selectedPassengerId.trim().isEmpty() || userSelect.getSelectedIndex() == 0) {
            // Si no hay un pasajero seleccionado (asumiendo "Select User" es el item 0)
            // o el ID está vacío, no hay nada que mostrar.
             System.out.println("AirportFrame: No hay pasajero seleccionado para mostrar 'Mis Vuelos'.");
            return;
        }
        
        // Necesitarás un método en PassengerController o FlightController
        // ej. PassengerController.getFlightsForPassenger(String passengerId)
        // Este método debe devolver los vuelos ordenados por fecha como pide el parcial.
        Response response = PassengerController.getFlightsForPassenger(selectedPassengerId); // DEBES CREAR ESTE MÉTODO EN EL CONTROLADOR
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (response.getStatus() == Status.SUCCESS && response.getData() != null) {
            ArrayList<Flight> flightsList = (ArrayList<Flight>) response.getData();
            for (Flight flight : flightsList) {
                 // Columnas definidas en tu initComponents para myFlightsTable:
                 // "ID", "Departure Date", "Arrival Date"
                myFlightsTableModel.addRow(new Object[]{
                    flight.getId(),
                    flight.getDepartureDate() != null ? flight.getDepartureDate().format(dateTimeFormatter) : "N/A",
                    flight.getArrivalDate() != null ? flight.getArrivalDate().format(dateTimeFormatter) : "N/A" // Asume que existe getArrivalDate
                });
            }
        } else if (response.getStatus() != Status.SUCCESS && response.getData() == null && response.getMessage().contains("No flights found for passenger")) {
            System.out.println("AirportFrame: No se encontraron vuelos para el pasajero seleccionado.");
        } else if (response.getStatus() != Status.SUCCESS) {
             JOptionPane.showMessageDialog(this, "Error refrescando 'Mis Vuelos': " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("AirportFrame: Tabla 'Mis Vuelos' refrescada.");
    }

    private void generateMonths() {
        for (int i = 1; i < 13; i++) {
            MONTH.addItem("" + i);
            MONTH1.addItem("" + i);
            MONTH5.addItem("" + i);
        }
    }

    private void generateDays() {
        for (int i = 1; i < 32; i++) {
            DAY.addItem("" + i);
            DAY1.addItem("" + i);
            DAY5.addItem("" + i);
        }
    }

    private void generateHours() {
        for (int i = 0; i < 24; i++) {
            MONTH2.addItem("" + i);
            MONTH3.addItem("" + i);
            MONTH4.addItem("" + i);
            delayFlightHoursComboBox.addItem("" + i);
        }
    }

    private void generateMinutes() {
        for (int i = 0; i < 60; i++) {
            DAY2.addItem("" + i);
            DAY3.addItem("" + i);
            DAY4.addItem("" + i);
            delayFlightMinutesComboBox.addItem("" + i);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelRound1 = new core.views.PanelRound();
        panelRound2 = new core.views.PanelRound();
        jButton13 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        user = new javax.swing.JRadioButton();
        administrator = new javax.swing.JRadioButton();
        userSelect = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        passengerPhoneCountryCodeTextField = new javax.swing.JTextField();
        passengerIdTextField = new javax.swing.JTextField();
        passengerBirthYearTextField = new javax.swing.JTextField();
        passengerCountryTextField = new javax.swing.JTextField();
        passengerPhoneNumberTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        passengerLastNameTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        MONTH = new javax.swing.JComboBox<>();
        passengerFirstNameTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        DAY = new javax.swing.JComboBox<>();
        registerPassengerButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        airplaneIdTextField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        airplaneBrandTextField = new javax.swing.JTextField();
        airplaneModelTextField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        airplaneMaxCapacityTextField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        airplaneAirlineTextField = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        createAirplaneButton = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        locationAirportIdTextField = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        locationAirportNameTextField = new javax.swing.JTextField();
        locationAirportCityTextField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        locationAirportCountryTextField = new javax.swing.JTextField();
        locationAirportLatitudeTextField = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        locationAirportLongitudeTextField = new javax.swing.JTextField();
        createLocationButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        flightIdTextField = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        flightPlaneComboBox = new javax.swing.JComboBox<>();
        flightDepartureLocationComboBox = new javax.swing.JComboBox<>();
        jLabel24 = new javax.swing.JLabel();
        flightArrivalLocationComboBox = new javax.swing.JComboBox<>();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        flightScaleLocationComboBox = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        flightDepartureYearTextField = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        MONTH1 = new javax.swing.JComboBox<>();
        jLabel31 = new javax.swing.JLabel();
        DAY1 = new javax.swing.JComboBox<>();
        jLabel32 = new javax.swing.JLabel();
        MONTH2 = new javax.swing.JComboBox<>();
        jLabel33 = new javax.swing.JLabel();
        DAY2 = new javax.swing.JComboBox<>();
        MONTH3 = new javax.swing.JComboBox<>();
        jLabel34 = new javax.swing.JLabel();
        DAY3 = new javax.swing.JComboBox<>();
        jLabel35 = new javax.swing.JLabel();
        MONTH4 = new javax.swing.JComboBox<>();
        DAY4 = new javax.swing.JComboBox<>();
        createFlightButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        updateInfoPassengerIdTextField = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        updateInfoFirstNameTextField = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        updateInfoLastNameTextField = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        updateInfoBirthYearTextField = new javax.swing.JTextField();
        MONTH5 = new javax.swing.JComboBox<>();
        DAY5 = new javax.swing.JComboBox<>();
        updateInfoPhoneNumberTextField = new javax.swing.JTextField();
        jLabel40 = new javax.swing.JLabel();
        updateInfoPhoneAreaCodeTextField = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        updateInfoCountryTextField = new javax.swing.JTextField();
        updatePassengerInfoButton = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        addToFlightPassengerIdTextField = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        addToFlightSelectionComboBox = new javax.swing.JComboBox<>();
        addPassengerToFlightButton = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        myFlightsTable = new javax.swing.JTable();
        refreshMyFlightsTableButton = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        allPassengersTable = new javax.swing.JTable();
        refreshAllPassengersTableButton = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        allFlightsTable = new javax.swing.JTable();
        refreshAllFlightsTableButton = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        refreshAllPlanesTableButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        allPlanesTable = new javax.swing.JTable();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        allLocationsTable = new javax.swing.JTable();
        refreshAllLocationsTableButton = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        delayFlightHoursComboBox = new javax.swing.JComboBox<>();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        delayFlightIdComboBox = new javax.swing.JComboBox<>();
        jLabel48 = new javax.swing.JLabel();
        delayFlightMinutesComboBox = new javax.swing.JComboBox<>();
        delayFlightButton = new javax.swing.JButton();
        panelRound3 = new core.views.PanelRound();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        panelRound1.setRadius(40);
        panelRound1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelRound2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                panelRound2MouseDragged(evt);
            }
        });
        panelRound2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panelRound2MousePressed(evt);
            }
        });

        jButton13.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jButton13.setText("X");
        jButton13.setBorderPainted(false);
        jButton13.setContentAreaFilled(false);
        jButton13.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelRound2Layout = new javax.swing.GroupLayout(panelRound2);
        panelRound2.setLayout(panelRound2Layout);
        panelRound2Layout.setHorizontalGroup(
            panelRound2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRound2Layout.createSequentialGroup()
                .addContainerGap(1083, Short.MAX_VALUE)
                .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );
        panelRound2Layout.setVerticalGroup(
            panelRound2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound2Layout.createSequentialGroup()
                .addComponent(jButton13)
                .addGap(0, 12, Short.MAX_VALUE))
        );

        panelRound1.add(panelRound2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1150, -1));

        jTabbedPane1.setFont(new java.awt.Font("Yu Gothic UI", 0, 14)); // NOI18N

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        user.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        user.setText("User");
        user.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userActionPerformed(evt);
            }
        });
        jPanel1.add(user, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 230, -1, -1));

        administrator.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        administrator.setText("Administrator");
        administrator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                administratorActionPerformed(evt);
            }
        });
        jPanel1.add(administrator, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 164, -1, -1));

        userSelect.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        userSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select User" }));
        userSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userSelectActionPerformed(evt);
            }
        });
        jPanel1.add(userSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 300, 130, -1));

        jTabbedPane1.addTab("Administration", jPanel1);

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel1.setText("Country:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 400, -1, -1));

        jLabel2.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel2.setText("ID:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 90, -1, -1));

        jLabel3.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel3.setText("First Name:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 160, -1, -1));

        jLabel4.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel4.setText("Last Name:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 220, -1, -1));

        jLabel5.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel5.setText("Birthdate:");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 280, -1, -1));

        jLabel6.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel6.setText("+");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 340, 20, -1));

        passengerPhoneCountryCodeTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(passengerPhoneCountryCodeTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 340, 50, -1));

        passengerIdTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(passengerIdTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 90, 130, -1));

        passengerBirthYearTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(passengerBirthYearTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 280, 90, -1));

        passengerCountryTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(passengerCountryTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 400, 130, -1));

        passengerPhoneNumberTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel2.add(passengerPhoneNumberTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 340, 130, -1));

        jLabel7.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel7.setText("Phone:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 340, -1, -1));

        jLabel8.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel8.setText("-");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 280, 30, -1));

        passengerLastNameTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        passengerLastNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passengerLastNameTextFieldActionPerformed(evt);
            }
        });
        jPanel2.add(passengerLastNameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 220, 130, -1));

        jLabel9.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel9.setText("-");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 340, 30, -1));

        MONTH.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        MONTH.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Month" }));
        jPanel2.add(MONTH, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 280, -1, -1));

        passengerFirstNameTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        passengerFirstNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passengerFirstNameTextFieldActionPerformed(evt);
            }
        });
        jPanel2.add(passengerFirstNameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 160, 130, -1));

        jLabel10.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel10.setText("-");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 280, 30, -1));

        DAY.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DAY.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Day" }));
        jPanel2.add(DAY, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 280, -1, -1));

        registerPassengerButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        registerPassengerButton.setText("Register");
        registerPassengerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerPassengerButtonActionPerformed(evt);
            }
        });
        jPanel2.add(registerPassengerButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 470, -1, -1));

        jTabbedPane1.addTab("Passenger registration", jPanel2);

        jPanel3.setLayout(null);

        jLabel11.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel11.setText("ID:");
        jPanel3.add(jLabel11);
        jLabel11.setBounds(53, 96, 22, 25);

        airplaneIdTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel3.add(airplaneIdTextField);
        airplaneIdTextField.setBounds(180, 93, 130, 31);

        jLabel12.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel12.setText("Brand:");
        jPanel3.add(jLabel12);
        jLabel12.setBounds(53, 157, 50, 25);

        airplaneBrandTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel3.add(airplaneBrandTextField);
        airplaneBrandTextField.setBounds(180, 154, 130, 31);

        airplaneModelTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel3.add(airplaneModelTextField);
        airplaneModelTextField.setBounds(180, 213, 130, 31);

        jLabel13.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel13.setText("Model:");
        jPanel3.add(jLabel13);
        jLabel13.setBounds(53, 216, 55, 25);

        airplaneMaxCapacityTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel3.add(airplaneMaxCapacityTextField);
        airplaneMaxCapacityTextField.setBounds(180, 273, 130, 31);

        jLabel14.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel14.setText("Max Capacity:");
        jPanel3.add(jLabel14);
        jLabel14.setBounds(53, 276, 109, 25);

        airplaneAirlineTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jPanel3.add(airplaneAirlineTextField);
        airplaneAirlineTextField.setBounds(180, 333, 130, 31);

        jLabel15.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel15.setText("Airline:");
        jPanel3.add(jLabel15);
        jLabel15.setBounds(53, 336, 70, 25);

        createAirplaneButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        createAirplaneButton.setText("Create");
        createAirplaneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createAirplaneButtonActionPerformed(evt);
            }
        });
        jPanel3.add(createAirplaneButton);
        createAirplaneButton.setBounds(490, 480, 120, 40);

        jTabbedPane1.addTab("Airplane registration", jPanel3);

        jLabel16.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel16.setText("Airport ID:");

        locationAirportIdTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel17.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel17.setText("Airport name:");

        locationAirportNameTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        locationAirportCityTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel18.setText("Airport city:");

        jLabel19.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel19.setText("Airport country:");

        locationAirportCountryTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        locationAirportLatitudeTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel20.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel20.setText("Airport latitude:");

        jLabel21.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel21.setText("Airport longitude:");

        locationAirportLongitudeTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        createLocationButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        createLocationButton.setText("Create");
        createLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLocationButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20)
                            .addComponent(jLabel21))
                        .addGap(80, 80, 80)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(locationAirportLongitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(locationAirportIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(locationAirportNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(locationAirportCityTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(locationAirportCountryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(locationAirportLatitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(515, 515, 515)
                        .addComponent(createLocationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(515, 515, 515))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(36, 36, 36)
                        .addComponent(jLabel17)
                        .addGap(34, 34, 34)
                        .addComponent(jLabel18)
                        .addGap(35, 35, 35)
                        .addComponent(jLabel19)
                        .addGap(35, 35, 35)
                        .addComponent(jLabel20))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(locationAirportIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(locationAirportNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(locationAirportCityTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(locationAirportCountryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(locationAirportLatitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(44, 44, 44)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(locationAirportLongitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addComponent(createLocationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47))
        );

        jTabbedPane1.addTab("Location registration", jPanel13);

        jLabel22.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel22.setText("ID:");

        flightIdTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel23.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel23.setText("Plane:");

        flightPlaneComboBox.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        flightPlaneComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Plane" }));
        flightPlaneComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flightPlaneComboBoxActionPerformed(evt);
            }
        });

        flightDepartureLocationComboBox.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        flightDepartureLocationComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Location" }));

        jLabel24.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel24.setText("Departure location:");

        flightArrivalLocationComboBox.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        flightArrivalLocationComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Location" }));

        jLabel25.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel25.setText("Arrival location:");

        jLabel26.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel26.setText("Scale location:");

        flightScaleLocationComboBox.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        flightScaleLocationComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Location" }));

        jLabel27.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel27.setText("Duration:");

        jLabel28.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel28.setText("Duration:");

        jLabel29.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel29.setText("Departure date:");

        flightDepartureYearTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel30.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel30.setText("-");

        MONTH1.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        MONTH1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Month" }));

        jLabel31.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel31.setText("-");

        DAY1.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DAY1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Day" }));

        jLabel32.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel32.setText("-");

        MONTH2.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        MONTH2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hour" }));

        jLabel33.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel33.setText("-");

        DAY2.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DAY2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Minute" }));

        MONTH3.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        MONTH3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hour" }));

        jLabel34.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel34.setText("-");

        DAY3.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DAY3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Minute" }));

        jLabel35.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel35.setText("-");

        MONTH4.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        MONTH4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hour" }));

        DAY4.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DAY4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Minute" }));

        createFlightButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        createFlightButton.setText("Create");
        createFlightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createFlightButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(flightScaleLocationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(flightArrivalLocationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addGap(46, 46, 46)
                        .addComponent(flightDepartureLocationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22)
                            .addComponent(jLabel23))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(flightIdTextField)
                            .addComponent(flightPlaneComboBox, 0, 130, Short.MAX_VALUE))))
                .addGap(45, 45, 45)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel27)
                    .addComponent(jLabel28)
                    .addComponent(jLabel29))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(flightDepartureYearTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(MONTH1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(DAY1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(MONTH2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(DAY2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(30, 30, 30))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(MONTH3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addComponent(DAY3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(MONTH4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addComponent(DAY4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(createFlightButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(530, 530, 530))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel22))
                    .addComponent(flightIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(flightPlaneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(MONTH2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32)
                    .addComponent(jLabel33)
                    .addComponent(DAY2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel24)
                                .addComponent(flightDepartureLocationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel29))
                            .addComponent(flightDepartureYearTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(MONTH1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel30)
                            .addComponent(jLabel31)
                            .addComponent(DAY1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel25)
                                .addComponent(flightArrivalLocationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel28))
                            .addComponent(MONTH3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel34)
                            .addComponent(DAY3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(34, 34, 34)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(MONTH4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel35)
                            .addComponent(DAY4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel26)
                                .addComponent(flightScaleLocationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel27)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 138, Short.MAX_VALUE)
                .addComponent(createFlightButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );

        jTabbedPane1.addTab("Flight registration", jPanel4);

        jLabel36.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel36.setText("ID:");

        updateInfoPassengerIdTextField.setEditable(false);
        updateInfoPassengerIdTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        updateInfoPassengerIdTextField.setEnabled(false);
        updateInfoPassengerIdTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateInfoPassengerIdTextFieldActionPerformed(evt);
            }
        });

        jLabel37.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel37.setText("First Name:");

        updateInfoFirstNameTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel38.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel38.setText("Last Name:");

        updateInfoLastNameTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel39.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel39.setText("Birthdate:");

        updateInfoBirthYearTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        MONTH5.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        MONTH5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Month" }));

        DAY5.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        DAY5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Day" }));

        updateInfoPhoneNumberTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel40.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel40.setText("-");

        updateInfoPhoneAreaCodeTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        jLabel41.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel41.setText("+");

        jLabel42.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel42.setText("Phone:");

        jLabel43.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel43.setText("Country:");

        updateInfoCountryTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N

        updatePassengerInfoButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        updatePassengerInfoButton.setText("Update");
        updatePassengerInfoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updatePassengerInfoButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel36)
                                .addGap(108, 108, 108)
                                .addComponent(updateInfoPassengerIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel37)
                                .addGap(41, 41, 41)
                                .addComponent(updateInfoFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel38)
                                .addGap(43, 43, 43)
                                .addComponent(updateInfoLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel39)
                                .addGap(55, 55, 55)
                                .addComponent(updateInfoBirthYearTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(MONTH5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(DAY5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel42)
                                .addGap(56, 56, 56)
                                .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(updateInfoPhoneAreaCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(20, 20, 20)
                                .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(updateInfoPhoneNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel43)
                                .addGap(63, 63, 63)
                                .addComponent(updateInfoCountryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(507, 507, 507)
                        .addComponent(updatePassengerInfoButton)))
                .addContainerGap(610, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addComponent(updateInfoPassengerIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel37)
                    .addComponent(updateInfoFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel38)
                    .addComponent(updateInfoLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel39)
                    .addComponent(updateInfoBirthYearTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MONTH5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DAY5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel42)
                    .addComponent(jLabel41)
                    .addComponent(updateInfoPhoneAreaCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel40)
                    .addComponent(updateInfoPhoneNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel43)
                    .addComponent(updateInfoCountryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(updatePassengerInfoButton)
                .addGap(113, 113, 113))
        );

        jTabbedPane1.addTab("Update info", jPanel5);

        addToFlightPassengerIdTextField.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        addToFlightPassengerIdTextField.setEnabled(false);

        jLabel44.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel44.setText("ID:");

        jLabel45.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel45.setText("Flight:");

        addToFlightSelectionComboBox.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        addToFlightSelectionComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Flight" }));
        addToFlightSelectionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToFlightSelectionComboBoxActionPerformed(evt);
            }
        });

        addPassengerToFlightButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        addPassengerToFlightButton.setText("Add");
        addPassengerToFlightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPassengerToFlightButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel44)
                    .addComponent(jLabel45))
                .addGap(79, 79, 79)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addToFlightSelectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addToFlightPassengerIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(889, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addPassengerToFlightButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(509, 509, 509))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel44))
                    .addComponent(addToFlightPassengerIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel45)
                    .addComponent(addToFlightSelectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 304, Short.MAX_VALUE)
                .addComponent(addPassengerToFlightButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(85, 85, 85))
        );

        jTabbedPane1.addTab("Add to flight", jPanel6);

        myFlightsTable.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        myFlightsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Departure Date", "Arrival Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(myFlightsTable);

        refreshMyFlightsTableButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        refreshMyFlightsTableButton.setText("Refresh");
        refreshMyFlightsTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshMyFlightsTableButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(269, 269, 269)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 590, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(353, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(refreshMyFlightsTableButton)
                .addGap(527, 527, 527))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addComponent(refreshMyFlightsTableButton)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Show my flights", jPanel7);

        allPassengersTable.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        allPassengersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Birthdate", "Age", "Phone", "Country", "Num Flight"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(allPassengersTable);

        refreshAllPassengersTableButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        refreshAllPassengersTableButton.setText("Refresh");
        refreshAllPassengersTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAllPassengersTableButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(489, 489, 489)
                        .addComponent(refreshAllPassengersTableButton))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1078, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(87, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(88, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(refreshAllPassengersTableButton)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Show all passengers", jPanel8);

        allFlightsTable.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        allFlightsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Departure Airport ID", "Arrival Airport ID", "Scale Airport ID", "Departure Date", "Arrival Date", "Plane ID", "Number Passengers"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(allFlightsTable);

        refreshAllFlightsTableButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        refreshAllFlightsTableButton.setText("Refresh");
        refreshAllFlightsTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAllFlightsTableButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(521, 521, 521)
                        .addComponent(refreshAllFlightsTableButton)))
                .addContainerGap(83, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(refreshAllFlightsTableButton)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Show all flights", jPanel9);

        refreshAllPlanesTableButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        refreshAllPlanesTableButton.setText("Refresh");
        refreshAllPlanesTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAllPlanesTableButtonActionPerformed(evt);
            }
        });

        allPlanesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Brand", "Model", "Max Capacity", "Airline", "Number Flights"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(allPlanesTable);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(508, 508, 508)
                        .addComponent(refreshAllPlanesTableButton))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(145, 145, 145)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 816, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(251, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(61, Short.MAX_VALUE)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(refreshAllPlanesTableButton)
                .addGap(17, 17, 17))
        );

        jTabbedPane1.addTab("Show all planes", jPanel10);

        allLocationsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Airport ID", "Airport Name", "City", "Country"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(allLocationsTable);

        refreshAllLocationsTableButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        refreshAllLocationsTableButton.setText("Refresh");
        refreshAllLocationsTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAllLocationsTableButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(508, 508, 508)
                        .addComponent(refreshAllLocationsTableButton))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(226, 226, 226)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 652, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(334, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(64, Short.MAX_VALUE)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(refreshAllLocationsTableButton)
                .addGap(17, 17, 17))
        );

        jTabbedPane1.addTab("Show all locations", jPanel11);

        delayFlightHoursComboBox.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        delayFlightHoursComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hour" }));

        jLabel46.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel46.setText("Hours:");

        jLabel47.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel47.setText("ID:");

        delayFlightIdComboBox.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        delayFlightIdComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ID" }));

        jLabel48.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel48.setText("Minutes:");

        delayFlightMinutesComboBox.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        delayFlightMinutesComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Minute" }));

        delayFlightButton.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        delayFlightButton.setText("Delay");
        delayFlightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delayFlightButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(94, 94, 94)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel48)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(delayFlightMinutesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel47)
                            .addComponent(jLabel46))
                        .addGap(79, 79, 79)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(delayFlightHoursComboBox, 0, 167, Short.MAX_VALUE)
                            .addComponent(delayFlightIdComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(820, 820, 820))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(delayFlightButton)
                .addGap(531, 531, 531))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel47)
                    .addComponent(delayFlightIdComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(delayFlightHoursComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel48)
                    .addComponent(delayFlightMinutesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 315, Short.MAX_VALUE)
                .addComponent(delayFlightButton)
                .addGap(33, 33, 33))
        );

        jTabbedPane1.addTab("Delay flight", jPanel12);

        panelRound1.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 1150, 620));

        javax.swing.GroupLayout panelRound3Layout = new javax.swing.GroupLayout(panelRound3);
        panelRound3.setLayout(panelRound3Layout);
        panelRound3Layout.setHorizontalGroup(
            panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1150, Short.MAX_VALUE)
        );
        panelRound3Layout.setVerticalGroup(
            panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 36, Short.MAX_VALUE)
        );

        panelRound1.add(panelRound3, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 660, 1150, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRound1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRound1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void panelRound2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelRound2MousePressed
        x = evt.getX();
        y = evt.getY();
    }//GEN-LAST:event_panelRound2MousePressed

    private void panelRound2MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelRound2MouseDragged
        this.setLocation(this.getLocation().x + evt.getX() - x, this.getLocation().y + evt.getY() - y);
    }//GEN-LAST:event_panelRound2MouseDragged

    private void administratorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_administratorActionPerformed
        if (user.isSelected()) {
            user.setSelected(false);
            userSelect.setSelectedIndex(0);

        }
        for (int i = 1; i < jTabbedPane1.getTabCount(); i++) {
                jTabbedPane1.setEnabledAt(i, true);
        }
        jTabbedPane1.setEnabledAt(5, false);
        jTabbedPane1.setEnabledAt(6, false);
        jTabbedPane1.setEnabledAt(7, false);
    }//GEN-LAST:event_administratorActionPerformed

    private void userActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userActionPerformed
        if (administrator.isSelected()) {
            administrator.setSelected(false);
        }
        for (int i = 1; i < jTabbedPane1.getTabCount(); i++) {

            jTabbedPane1.setEnabledAt(i, false);

        }
        jTabbedPane1.setEnabledAt(9, true);
        jTabbedPane1.setEnabledAt(5, true);
        jTabbedPane1.setEnabledAt(6, true);
        jTabbedPane1.setEnabledAt(7, true);
        jTabbedPane1.setEnabledAt(11, true);
    }//GEN-LAST:event_userActionPerformed

    private void registerPassengerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerPassengerButtonActionPerformed
        // TODO add your handling code here:
        String id = passengerIdTextField.getText();
        String firstname = passengerFirstNameTextField.getText();
        String lastname = passengerLastNameTextField.getText();
        String year = passengerBirthYearTextField.getText();
        String month = MONTH.getItemAt(MONTH.getSelectedIndex());
        String day = DAY.getItemAt(DAY.getSelectedIndex());
        String phoneCode = passengerPhoneCountryCodeTextField.getText();
        String phone = passengerPhoneNumberTextField.getText();
        String country = passengerCountryTextField.getText();
        

        Response response =PassengerController.registerPassenger(id, firstname, lastname, year, month, day, phoneCode, phone, country); 
        
        if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Response Message", JOptionPane.INFORMATION_MESSAGE);
            passengerIdTextField.setText("");
            passengerFirstNameTextField.setText("");
            passengerLastNameTextField.setText("");
            passengerBirthYearTextField.setText("");
            passengerPhoneCountryCodeTextField.setText("");
            passengerPhoneNumberTextField.setText("");
            passengerCountryTextField.setText("");
            this.userSelect.addItem(id);
        }
    }//GEN-LAST:event_registerPassengerButtonActionPerformed

    private void createAirplaneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createAirplaneButtonActionPerformed
        // TODO add your handling code here:
        String id = airplaneIdTextField.getText();
        String brand = airplaneBrandTextField.getText();
        String model = airplaneModelTextField.getText();
        String maxCapacity = airplaneMaxCapacityTextField.getText();
        String airline = airplaneAirlineTextField.getText();

        Response response = PlaneController.createPlane(id, brand, model, maxCapacity, airline);
        
         if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Response Message", JOptionPane.INFORMATION_MESSAGE);
            airplaneIdTextField.setText("");
            airplaneBrandTextField.setText("");
            airplaneModelTextField.setText("");
            airplaneMaxCapacityTextField.setText("");
            airplaneAirlineTextField.setText("");

            this.flightPlaneComboBox.addItem(id);
        }
         
        
    }//GEN-LAST:event_createAirplaneButtonActionPerformed

    private void createLocationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLocationButtonActionPerformed
        // TODO add your handling code here:
        String id = locationAirportIdTextField.getText();
        String name = locationAirportNameTextField.getText();
        String city = locationAirportCityTextField.getText();
        String country = locationAirportCountryTextField.getText();
        String latitude = locationAirportLatitudeTextField.getText();
        String longitude = locationAirportLongitudeTextField.getText();

        Response response = LocationController.createLocation(id, name, city, country, latitude, longitude);
        
         if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Response Message", JOptionPane.INFORMATION_MESSAGE);
            
            locationAirportIdTextField.setText("");
            locationAirportNameTextField.setText("");
            locationAirportCityTextField.setText("");
            locationAirportCountryTextField.setText("");
            locationAirportLatitudeTextField.setText("");
            locationAirportLongitudeTextField.setText("");
            
            this.flightDepartureLocationComboBox.addItem(id);
            this.flightArrivalLocationComboBox.addItem(id);
            this.flightScaleLocationComboBox.addItem(id);
        } 
    }//GEN-LAST:event_createLocationButtonActionPerformed

    private void createFlightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createFlightButtonActionPerformed
        // TODO add your handling code here:
        String id = flightIdTextField.getText();
        String planeId = flightPlaneComboBox.getItemAt(flightPlaneComboBox.getSelectedIndex());
        String departureLocationId = flightDepartureLocationComboBox.getItemAt(flightDepartureLocationComboBox.getSelectedIndex());
        String arrivalLocationId = flightArrivalLocationComboBox.getItemAt(flightArrivalLocationComboBox.getSelectedIndex());
        String scaleLocationId = flightScaleLocationComboBox.getItemAt(flightScaleLocationComboBox.getSelectedIndex());
        String year = flightDepartureYearTextField.getText();
        String month = MONTH1.getItemAt(MONTH1.getSelectedIndex());
        String day = DAY1.getItemAt(DAY1.getSelectedIndex());
        String hour = MONTH2.getItemAt(MONTH2.getSelectedIndex());
        String minutes = DAY2.getItemAt(DAY2.getSelectedIndex());
        String hoursDurationsArrival =MONTH3.getItemAt(MONTH3.getSelectedIndex());
        String minutesDurationsArrival = DAY3.getItemAt(DAY3.getSelectedIndex());
        String hoursDurationsScale = MONTH4.getItemAt(MONTH4.getSelectedIndex());
        String minutesDurationsScale = DAY4.getItemAt(DAY4.getSelectedIndex());

        Response response = FlightController.createFlight(id, planeId, departureLocationId, arrivalLocationId, scaleLocationId, year, month, day, hour, minutes, hoursDurationsArrival, minutesDurationsArrival, hoursDurationsScale, minutesDurationsScale);
        
        if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Response Message", JOptionPane.INFORMATION_MESSAGE);
            
            flightIdTextField.setText("");
            flightDepartureYearTextField.setText("");
            
            this.addToFlightSelectionComboBox.addItem(id);
        }
    }//GEN-LAST:event_createFlightButtonActionPerformed

    private void updatePassengerInfoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updatePassengerInfoButtonActionPerformed
        // TODO add your handling code here:
        String idStr = updateInfoPassengerIdTextField.getText();
        String firstName = updateInfoFirstNameTextField.getText();
        String lastName = updateInfoLastNameTextField.getText();
        String yearStr = updateInfoBirthYearTextField.getText();
        String monthStr = MONTH5.getItemAt(MONTH5.getSelectedIndex()); 
        String dayStr = DAY5.getItemAt(DAY5.getSelectedIndex());     
        String phoneCodeStr = updateInfoPhoneAreaCodeTextField.getText();
        String phoneNumberStr = updateInfoPhoneNumberTextField.getText();
        String country = updateInfoCountryTextField.getText();
    
        if (idStr == null || idStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Passenger ID is missing. Please select a passenger first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Response response = PassengerController.updatePassenger(
            idStr,
            firstName,
            lastName,
            yearStr,
            monthStr,
            dayStr,
            phoneCodeStr,
            phoneNumberStr,
            country
        );
    
        if (response.getStatus() >= 500) { 
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) { 
            JOptionPane.showMessageDialog(this, response.getMessage(), "Validation Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else { 
            JOptionPane.showMessageDialog(this, response.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_updatePassengerInfoButtonActionPerformed

    private void addPassengerToFlightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPassengerToFlightButtonActionPerformed
        // TODO add your handling code here:
        String passengerId = addToFlightPassengerIdTextField.getText();
        String flightId = addToFlightSelectionComboBox.getItemAt(addToFlightSelectionComboBox.getSelectedIndex());
        
        Response response = PassengerController.asignFlight(passengerId, flightId);
        
         if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Response Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_addPassengerToFlightButtonActionPerformed

    private void delayFlightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delayFlightButtonActionPerformed
        // TODO add your handling code here:
        String flightId = delayFlightIdComboBox.getItemAt(delayFlightIdComboBox.getSelectedIndex());
        String hours = delayFlightHoursComboBox.getItemAt(delayFlightHoursComboBox.getSelectedIndex());
        String minutes = delayFlightMinutesComboBox.getItemAt(delayFlightMinutesComboBox.getSelectedIndex());
        
        Response response = FlightController.delayFlight(flightId, hours, minutes);
        if (response.getStatus() >= 500) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.ERROR_MESSAGE);
        } else if (response.getStatus() >= 400) {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Error " + response.getStatus(), JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, response.getMessage(), "Response Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_delayFlightButtonActionPerformed

    private void refreshMyFlightsTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshMyFlightsTableButtonActionPerformed
        MyFlightsTableController controller = new MyFlightsTableController(myFlightsTable,userSelect);
        controller.refreshTable();
    }//GEN-LAST:event_refreshMyFlightsTableButtonActionPerformed

    private void refreshAllPassengersTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAllPassengersTableButtonActionPerformed
        PassengerTableController controller = new PassengerTableController(allPassengersTable);
        controller.refreshTable();
    }//GEN-LAST:event_refreshAllPassengersTableButtonActionPerformed

    private void refreshAllFlightsTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAllFlightsTableButtonActionPerformed
        FlightTableController controller = new FlightTableController(allFlightsTable);
        controller.refreshTable();
    }//GEN-LAST:event_refreshAllFlightsTableButtonActionPerformed

    private void refreshAllPlanesTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAllPlanesTableButtonActionPerformed
        PlaneTableController controller = new PlaneTableController(allPlanesTable);
        controller.refreshTable();
    }//GEN-LAST:event_refreshAllPlanesTableButtonActionPerformed

    private void refreshAllLocationsTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAllLocationsTableButtonActionPerformed
        LocationTableController controller = new LocationTableController(allLocationsTable);
        controller.refreshTable();
    }//GEN-LAST:event_refreshAllLocationsTableButtonActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton13ActionPerformed

    private void userSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userSelectActionPerformed
        try {
            String id = userSelect.getSelectedItem().toString();
            if (! id.equals(userSelect.getItemAt(0))) {
                updateInfoPassengerIdTextField.setText(id);
                addToFlightPassengerIdTextField.setText(id);
            }
            else{
                updateInfoPassengerIdTextField.setText("");
                addToFlightPassengerIdTextField.setText("");
            }
        } catch (Exception e) {
        }
    }//GEN-LAST:event_userSelectActionPerformed

    private void passengerFirstNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passengerFirstNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passengerFirstNameTextFieldActionPerformed

    private void passengerLastNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passengerLastNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passengerLastNameTextFieldActionPerformed

    private void updateInfoPassengerIdTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateInfoPassengerIdTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_updateInfoPassengerIdTextFieldActionPerformed

    private void addToFlightSelectionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToFlightSelectionComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addToFlightSelectionComboBoxActionPerformed

    private void flightPlaneComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flightPlaneComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_flightPlaneComboBoxActionPerformed
    
    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> DAY;
    private javax.swing.JComboBox<String> DAY1;
    private javax.swing.JComboBox<String> DAY2;
    private javax.swing.JComboBox<String> DAY3;
    private javax.swing.JComboBox<String> DAY4;
    private javax.swing.JComboBox<String> DAY5;
    private javax.swing.JComboBox<String> MONTH;
    private javax.swing.JComboBox<String> MONTH1;
    private javax.swing.JComboBox<String> MONTH2;
    private javax.swing.JComboBox<String> MONTH3;
    private javax.swing.JComboBox<String> MONTH4;
    private javax.swing.JComboBox<String> MONTH5;
    private javax.swing.JButton addPassengerToFlightButton;
    private javax.swing.JTextField addToFlightPassengerIdTextField;
    private javax.swing.JComboBox<String> addToFlightSelectionComboBox;
    private javax.swing.JRadioButton administrator;
    private javax.swing.JTextField airplaneAirlineTextField;
    private javax.swing.JTextField airplaneBrandTextField;
    private javax.swing.JTextField airplaneIdTextField;
    private javax.swing.JTextField airplaneMaxCapacityTextField;
    private javax.swing.JTextField airplaneModelTextField;
    private javax.swing.JTable allFlightsTable;
    private javax.swing.JTable allLocationsTable;
    private javax.swing.JTable allPassengersTable;
    private javax.swing.JTable allPlanesTable;
    private javax.swing.JButton createAirplaneButton;
    private javax.swing.JButton createFlightButton;
    private javax.swing.JButton createLocationButton;
    private javax.swing.JButton delayFlightButton;
    private javax.swing.JComboBox<String> delayFlightHoursComboBox;
    private javax.swing.JComboBox<String> delayFlightIdComboBox;
    private javax.swing.JComboBox<String> delayFlightMinutesComboBox;
    private javax.swing.JComboBox<String> flightArrivalLocationComboBox;
    private javax.swing.JComboBox<String> flightDepartureLocationComboBox;
    private javax.swing.JTextField flightDepartureYearTextField;
    private javax.swing.JTextField flightIdTextField;
    private javax.swing.JComboBox<String> flightPlaneComboBox;
    private javax.swing.JComboBox<String> flightScaleLocationComboBox;
    private javax.swing.JButton jButton13;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField locationAirportCityTextField;
    private javax.swing.JTextField locationAirportCountryTextField;
    private javax.swing.JTextField locationAirportIdTextField;
    private javax.swing.JTextField locationAirportLatitudeTextField;
    private javax.swing.JTextField locationAirportLongitudeTextField;
    private javax.swing.JTextField locationAirportNameTextField;
    private javax.swing.JTable myFlightsTable;
    private core.views.PanelRound panelRound1;
    private core.views.PanelRound panelRound2;
    private core.views.PanelRound panelRound3;
    private javax.swing.JTextField passengerBirthYearTextField;
    private javax.swing.JTextField passengerCountryTextField;
    private javax.swing.JTextField passengerFirstNameTextField;
    private javax.swing.JTextField passengerIdTextField;
    private javax.swing.JTextField passengerLastNameTextField;
    private javax.swing.JTextField passengerPhoneCountryCodeTextField;
    private javax.swing.JTextField passengerPhoneNumberTextField;
    private javax.swing.JButton refreshAllFlightsTableButton;
    private javax.swing.JButton refreshAllLocationsTableButton;
    private javax.swing.JButton refreshAllPassengersTableButton;
    private javax.swing.JButton refreshAllPlanesTableButton;
    private javax.swing.JButton refreshMyFlightsTableButton;
    private javax.swing.JButton registerPassengerButton;
    private javax.swing.JTextField updateInfoBirthYearTextField;
    private javax.swing.JTextField updateInfoCountryTextField;
    private javax.swing.JTextField updateInfoFirstNameTextField;
    private javax.swing.JTextField updateInfoLastNameTextField;
    private javax.swing.JTextField updateInfoPassengerIdTextField;
    private javax.swing.JTextField updateInfoPhoneAreaCodeTextField;
    private javax.swing.JTextField updateInfoPhoneNumberTextField;
    private javax.swing.JButton updatePassengerInfoButton;
    private javax.swing.JRadioButton user;
    private javax.swing.JComboBox<String> userSelect;
    // End of variables declaration//GEN-END:variables

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void refreshAllPassengersTableData() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void refreshAllLocationsTableData() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void refreshAllPlanesTableData() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
