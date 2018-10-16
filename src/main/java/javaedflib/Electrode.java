/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaedflib;

import javafx.geometry.Point3D;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author juhasz
 */
public class Electrode {
    private int index;  
    private String type;
    private String label;
    private Set<String> alternativeLabels;
    private Point3D position;
    private double value;

    public Electrode() {
        alternativeLabels = new TreeSet<>();
    }

    public Electrode(String label) {
        this.label = label;
        alternativeLabels = new TreeSet<>();
    }
    
    public Electrode(String label, Point3D pos) {
        this.label = label;
        this.position = pos;
        alternativeLabels = new TreeSet<>();
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Point3D getPosition() {
        return position;
    }

    public void setPosition(Point3D position) {
        this.position = position;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
    
    public void addAlternativeLabel(String label){
        alternativeLabels.add(label);
    }

    public Set<String> getAlternativeLabels() {
        return alternativeLabels;
    }

    public int getIndex() {
        return index;
    }
                
}
