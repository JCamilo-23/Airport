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
public interface LocationServices {
    boolean hasAtMostFourDecimalPlaces(String valueStr);
    
   Response createLocation(String airportIdStr, String airportName, String airportCity, 
                                                String airportCountry, String latitudeStr, 
                                                String longitudeStr);
   
   Response getAllLocations();
   
   Response getLocationDisplayInfoForComboBox();
    
}
