/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

import com.formdev.flatlaf.FlatDarkLaf;
import core.models.storage.LoadStorage;
import core.views.AirportFrame;
import javax.swing.UIManager;

/**
 *
 * @author Admin
 */
public class Main {
    public static void main(String args[]) {
        System.setProperty("flatlaf.useNativeLibrary", "false");
        LoadStorage storage = new LoadStorage();
        storage.loadAllData();
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AirportFrame().setVisible(true);
            }
        });
    }
}
