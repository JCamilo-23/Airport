/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package core.controllers.services;

import core.controllers.utils.Response;

/**
 *
 * @author Andrea Osio Amaya
 */
public interface PlaneServices {
     Response createPlane(String idStr, String brand, String model, String maxCapacityStr, String airline);
     
     Response getAllPlanes();
}
