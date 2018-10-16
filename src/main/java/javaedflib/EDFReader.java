/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaedflib;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author juhasz
 */
public class EDFReader {
    public static void main(String[] args) {
        try {
            TrialInterface source = new TrialInterface();
            String path = System.getProperty("user.dir") + "\\src\\main\\test_generator.edf";
            source.setInput(new File(path));
            
            Header header = source.getHeader();
            System.out.println("Header info:");
            System.out.println("Patient info: " + header.getPatientInfo());
            System.out.println("Recording info: " + header.getRecordingInfo());
            System.out.println("Start date: " + header.getStartDate());
            System.out.println("No of channels: " + header.getNumberOfSignals());
            
            System.out.println("Channel names: ");
            String[] labels = source.getSignalLabel();
            for (String label : labels) {
                System.out.println(label);
            }
            
            System.out.println("First 100 samples from channel 8");
            float[] channelData = source.getChannelData(SignalHeader.SIGNAL_TYPES.EEG, labels[7], 0, 100);
            for (int i = 0; i < channelData.length; i++) {
                System.out.println(channelData[i]);                
            }
        } catch (IOException ex) {
            Logger.getLogger(EDFReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
                
    }
}
