/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaedflib;

import javaedflib.Layouts.BIOSEMI_ABC_128_MAPPING;
import javafx.geometry.Point3D;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author juhasz
 */
public class Cap {
    
    private String configuration; // e.g. 10-10
    private List<Electrode> electrodes;
    private Map<String, Electrode> electrodeMap;
    private Map<String, List<String>> electrodeGroups;
    private Map<Integer, String[]> positionToLabelMap;
    
    public Cap(){
        electrodes = new ArrayList<>();
        electrodeMap = new HashMap<>();
        electrodeGroups = new HashMap<>();
    }
    
    // static factory methods to generate common electrode cap configurations
    public static Cap getStandard_10_20_Cap(int numElectrodes){
        Cap cap = new Cap();
        cap.setConfiguration("10/20");
        for (int i = 0; i < numElectrodes; i++) {
            cap.addElectrode(new Electrode());            
        }
        return cap;
    }

    public static Cap getStandard_10_10_Cap(int numElectrodes){
        Cap cap = new Cap();
        cap.setConfiguration("10/10");
        for (int i = 0; i < numElectrodes; i++) {
            cap.addElectrode(new Electrode());            
        }
        return cap;
    }
    
    public static Cap getStandard_10_5_Cap(int numElectrodes){
        Cap cap = new Cap();
        cap.setConfiguration("10/5");
//        cap.setElectrodes(new STANDARD_10_5_MAPPING());            
        return cap;
    }

    public static Cap getBiosemi_ABC_Cap(int numElectrodes){
        Cap cap = new Cap();
        cap.setConfiguration("ABC");

        switch (numElectrodes){
            case 128: cap.setElectrodes(new BIOSEMI_ABC_128_MAPPING());
                
                // map electrode positions to labels in a circular fashion
                Map<Integer, String[]> positionToLabelMap = new HashMap<>();
                positionToLabelMap.put(0, new String[]{"A1"});

                positionToLabelMap.put(1, new String[]{"A2", "B1", "C1", "D1", "D15"});
                positionToLabelMap.put(2, new String[]{"A3", "B2", "B20", "C2", "C23", "D2", "D14", "D16"});
                positionToLabelMap.put(3, new String[]{"A4", "DRL", "B19", "B21", "B32", "C11", "C22", "C24", 
                    "D13", "D18", "D17", "CMS"});
                positionToLabelMap.put(4, new String[]{"A19", "A32", "B3", "B18", "B22", "B31", "C3", "C12", 
                    "C21", "C25", "D3", "D12", "D19", "D28", "A6", "A5"});
                positionToLabelMap.put(5, new String[]{"A20", "A31", "B4", "B17", "B23", "B30", "C4", "C13",
                    "C20", "C26", "D4", "D11", "D20", "D27", "A7", "A18"});
                positionToLabelMap.put(6, new String[]{"A21", "A30", "B5", "B13", "B16", "B24", "B29", "C5",
                    "C10", "C14", "C19", "C27", "C32", "D5", "D10", "D21", "D26", "D29", "A8", "A17"});
                positionToLabelMap.put(7, new String[]{"A22", "A29", "B6", "B12", "B15", "B25", "B28", "C6",
                    "C9", "C15", "C18", "C28", "C31", "D6", "D9", "D22", "D25", "D30", "A9", "A16"});
                positionToLabelMap.put(8, new String[]{"A23", "A28", "B7", "B11", "B14", "B26", "B27", "C7",
                    "C8", "C16", "C17", "C29", "C30", "D7", "D8", "D23", "D24", "D31", "A10", "A15"});
                positionToLabelMap.put(9, new String[]{"A24", "A27", "B8", "B10", "", "", "", "",
                    "", "", "", "", "", "", "", "", "", "D32", "A11", "A14"});
                positionToLabelMap.put(10, new String[]{"A25", "A26", "B9", "", "", "", "",
                    "", "", "", "", "", "", "", "", "", "", "", "A12", "A13"});
                cap.setPositionToLabelMap(positionToLabelMap);
                
                // create ROIs
//                cap.addElectrodeGroup("Occipital", "A18", "A19", "A20", "A21");
                // left and right hemisphere -- middle line electrodes are skipped
                cap.addElectrodeGroup("Left", "D1", "D15", 
                        "D2", "D14", "D16",
                        "C24", "D13", "D18", "D17", 
                        "C25", "D3", "D12", "D19", "D28", "A6", "A5",
                        "C26", "D4", "D11", "D20", "D27", "A7", "A18",
                        "C27", "C32", "D5", "D10", "D21", "D26", "D29", "A8", "A17",
                        "C28", "C31", "D6", "D9", "D22", "D25", "D30", "A9", "A16",
                        "C29", "C30", "D7", "D8", "D23", "D24", "D31", "A10", "A15",
                        "D32", "A11", "A14",
                        "A12", "A13");
                cap.addElectrodeGroup("Right", "C1", "B1",
                        "C2", "B20", "B2",
                        "C11", "B32", "B21", "B19",
                        "C12", "C3", "B31", "B22", "B18", "B3", "A32",
                        "C13", "C4", "B30", "B23", "B17", "B4", "A31",
                        "C14", "C10", "C5", "B29", "B24", "B16", "B13", "B5", "A30", 
                        "C15", "C9", "C6", "B28", "B25", "B15", "B12", "B6", "A29",
                        "C16", "C8", "C7", "B27", "B26", "B14", "B11", "B7", "A28",
                        "B10", "B8", "A27",
                        "B9", "A26");                
                break;
            case 160: break;
            case 256: break;
        }
        return cap;
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
    
    // util methods 
    // eg fitting electrodes on a head
    // creating custom configurations
    // operations on electrodes

    public void setConfiguration(String name) {
        this.configuration = name;
    }

    public void addElectrode(Electrode electrode) {
        electrodes.add(electrode);
        electrodeMap.put(electrode.getLabel(), electrode);
    }
    
    public int getNumElectrodes(){
        return electrodes.size();
    }

    public Electrode getElectrode(String label){
        return electrodeMap.get(label);
    }

    public List<Electrode> getElectrodes() {
        return electrodes;
    }
    
    public Map<String, Electrode> getElectrodeMap() {
        return electrodeMap;
    }
                
    public void setElectrodes(Map<String, Point3D> electrodeMap) {
        for (String label:electrodeMap.keySet()){
            Electrode e = new Electrode(label, electrodeMap.get(label));
//            this.electrodes.add(e);
            this.electrodeMap.put(label, e);
        }
        // temporary hack to add electrodes in Biosemi128 channel order
        for (int i = 0; i < 32; i++) {            
            this.electrodes.add( this.electrodeMap.get("A"+(i+1)) );    // A1-32
        }
        for (int i = 0; i < 32; i++) {            
            this.electrodes.add( this.electrodeMap.get("B"+(i+1)) );    // A1-32
        }
        for (int i = 0; i < 32; i++) {            
            this.electrodes.add( this.electrodeMap.get("C"+(i+1)) );    // A1-32
        }
        for (int i = 0; i < 32; i++) {            
            this.electrodes.add( this.electrodeMap.get("D"+(i+1)) );    // A1-32
        }
    }
    
    private void setElectrodeMap(Map electrodeMap) {
        this.electrodeMap = electrodeMap;
    }    
    
    public Set<String> getElectrodeGroupNames(){
        return electrodeGroups.keySet();
    }
    
    public void addElectrodeGroup(String groupName, String... electrodeLabels){
        if (electrodeGroups.containsKey(groupName))
//            throw new IllegalArgumentException("Electrode group exists already.");
            electrodeGroups.replace(groupName, Arrays.asList(electrodeLabels));
        else
            electrodeGroups.put(groupName, Arrays.asList(electrodeLabels));
    }
    
    public List<String> getElectrodeGroup(String groupName){
        return electrodeGroups.get(groupName);
    }
    
    public List<Electrode> getElectrodeGroupItems(String groupName){ 
        List<Electrode> temp = new ArrayList<>(electrodeGroups.get(groupName).size());
        for (String label : electrodeGroups.get(groupName)) {
            temp.add(electrodeMap.get(label));            
        }
        return temp;
    }
    
    public void mapElectrodesToSphere(double radius){
        // this only works for the 128-channel ABC layout!!!
        //=====================================================
        
        //Circle # electrodes
        //1	1
        //2	5
        //3	8
        //4	12  -2 ref electrodes
        //5	16
        //6	16
        //7	20
        //8	20
        //9	20
        //10	7
        //11	5
        int electrodeCounts[] = {1, 5, 8, 12, 16, 16, 20, 20, 20, 7, 5};

        // set A1
        double alpha = 0;
        
        // calculate electrodes on other circles
        for (int i = 1; i < electrodeCounts.length; i++) {
            // increase angle for current circle by 11.5 degrees
            alpha = i * 11.5 * Math.PI/180; 
            double z = radius * Math.cos(alpha);
//            System.out.println("mapping electrodes for circle " + (i + 1) + " alpha=" + z + " # of electrodes: " + electrodeCounts[i]);
            int numElectrodes = electrodeCounts[i];
            if (i > 8) 
                numElectrodes = 20;
            System.out.println("circle " + (i+1) + ", # of electrodes: " + electrodeCounts[i]);
            for (int j = 0; j < numElectrodes; j++) {
                // go around the circle in the given number of steps
                // if on the 4th circle, skip the two reference electrodes: 2, 12
                if (i==3 && (j==1 || j==11))
                    continue;
                if (i==9 && (j>3 && j<17))
                    continue;
                if (i==10 && (j>2 && j<18))
                    continue;

                double beta = j * 2 * Math.PI/numElectrodes; 
                double x = radius * Math.sin(alpha) * Math.sin(beta);
                double y = -radius * Math.sin(alpha) * Math.cos(beta);
                System.out.println("Electrode: " + positionToLabelMap.get(i)[j] + " index: " + (j+1) + 
                        " " + x + " " + y + " " + z);
//                System.out.println("Electrode: " + positionToLabelMap.get(i)[j]);
            }
        }
    }    

    private void mapElectrodesToSurface() {
//        Electrode mapping onto the head surface
//    
//        input: 
//         - the surface mesh as a vertex/face list - need face info for constructing triangles
//         - mapping rule: layout
//         - fiducial points
//
//        output: electrode list with proper coordinates
//
//        algorithm: 
//
//        1   find fiducial points, establish origin and axis directions 
//        2   place Cz: find half length between nasion and inion, find point
//        3   divide the front and back halflength into the required steps
//            - calculate arc lengths 
//            - find electrodes on the middle line - keep these electrodes as references to each circle
//        4   place electrodes on each circle
//
//        for each step, we need to find 
//            - either the intersection of a line and the triangle
//            - a point that is a given distance from another one on the surface 
//

         
    }
    private void setPositionToLabelMap(Map<Integer, String[]> positionToLabelMap) {
        this.positionToLabelMap = positionToLabelMap;
    }  
    
    public Map<Integer, String[]> getPositionToLabelMap() {
        return positionToLabelMap;
        }
    
    public static void main(String[] args) {
        double radius = 100;
        Cap cap = Cap.getBiosemi_ABC_Cap(128);
        cap.mapElectrodesToSphere(radius);

        // testing groups
        cap.addElectrodeGroup("Occipital", "A18", "A19", "A20", "A21");
        cap.addElectrodeGroup("Left", "C1", "C2");
        System.out.println("Electrode Groups:");
        for (String name : cap.getElectrodeGroupNames()) {
            System.out.println(name);
            for (String label : cap.getElectrodeGroup(name)) {
                System.out.print(label + " ");
            }
            System.out.println("");
            System.out.println("Electrodes: ");
            for (Electrode e : cap.getElectrodeGroupItems(name)) {
                System.out.println(e.getLabel() + " - " + e.getPosition() + " ");
            }
        }
    }

}
