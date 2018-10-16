/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaedflib;

import javaedflib.Layouts.BIOSEMI_ABC_128_MAPPING;
import javaedflib.Layouts.STANDARD_10_10_LAYOUT;
import javaedflib.Layouts.STANDARD_10_20_LAYOUT;
import javaedflib.Layouts.STANDARD_10_5_LAYOUT;
import javafx.geometry.Point3D;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Helper class providing useful functionality when working with electrode caps
 * @author juhasz
 */
public class Caps {
 
    // static factory methods to generate common electrode cap configurations
    public static Cap getStandard_10_20_Cap(int numElectrodes){
        return makeCap("10/20", new STANDARD_10_20_LAYOUT());
    }

    public static Cap getStandard_10_10_Cap(int numElectrodes){
        return makeCap("10/10", new STANDARD_10_10_LAYOUT());
    }

    public static Cap getStandard_10_5_Cap(int numElectrodes){
        return makeCap("10/5", new STANDARD_10_5_LAYOUT());
    }

    public static Cap getBiosemi_ABC_Cap(int numElectrodes){
        return makeCap("ABC", new BIOSEMI_ABC_128_MAPPING() );
    }    

    public static Cap loadFromFile(String configuration, String filename) throws FileNotFoundException, IOException{
        Cap cap = new Cap();
        cap.setConfiguration(configuration);
        // read file
        String line = null;
        String[] electrodeTokens = null;

        BufferedReader electrodeReader = new BufferedReader(new FileReader(filename));
        //Read File Line By Line
        // assume the following file structure
        // chan_name; x_coord; y_coord; z_coord
        
        //Map electrodeMap = new HashMap();
        
        int index = 0;
        while ((line = electrodeReader.readLine()) != null) {
            electrodeTokens = line.split("[;\\s\\t]");

            String chanLabel = electrodeTokens[0].trim();
            Electrode e = new Electrode(
                    chanLabel,
                    new Point3D(
                            Double.valueOf(electrodeTokens[1].trim()),
                            Double.valueOf(electrodeTokens[2].trim()),
                            Double.valueOf(electrodeTokens[3].trim())
                    )
                );
            index++;
            cap.addElectrode(e);
        }         
//        cap.setElectrodes(electrodeMap);
        return cap;
    }    

    
    
    ///////////////////////////////////////////////////////// 
    // implementation methods
    private static Cap makeCap(String name, HashMap<String, Point3D> layout) {
        Cap cap = new Cap();
        cap.setConfiguration(name);
        cap.setElectrodes(layout);
        return cap;
    }
        
}
