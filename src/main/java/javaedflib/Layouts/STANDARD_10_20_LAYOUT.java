/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaedflib.Layouts;

import java.util.HashMap;
import javafx.geometry.Point3D;

/**
 *
 * @author juhasz
 */
public class STANDARD_10_20_LAYOUT extends HashMap<String, Point3D> {

    private static final String ELECTRODE_MAP
            = ""; 


    // See http://eeg.sourceforge.net/doc_m2html/bioelectromagnetism/elec_1020all_cart.html 


    public STANDARD_10_20_LAYOUT() {
        for (String line : ELECTRODE_MAP.split("\n")) {
            String[] items = line.split(" +");
            double x = Double.valueOf(items[1].trim());
            double y = Double.valueOf(items[2].trim());
            double z = Double.valueOf(items[3].trim());
            put(items[0].trim(), new Point3D(x,y,z));
        }
    }

    public static void main(String[] args) {
        STANDARD_10_20_LAYOUT cap = new STANDARD_10_20_LAYOUT();
        System.out.println(cap.size());
        for (String key : cap.keySet()) {
            System.out.println(key + ": " + cap.get(key));
        }
    }
}

