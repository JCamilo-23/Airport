/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package core.models.storage.interfaces;

import core.models.Plane;
import java.util.ArrayList;

/**
 *
 * @author brayan
 */
public interface IPlaneStorage {
    boolean addPlane(Plane plane);
    Plane getPlane(String id);
    boolean planeIdExists(String id);
    ArrayList<Plane> getPlanes();
}