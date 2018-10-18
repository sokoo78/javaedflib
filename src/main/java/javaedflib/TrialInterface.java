/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaedflib;

import javaedflib.Layouts.BIOSEMI_ABC_128_MAPPING;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
//import electrode.BIOSEMI_ABC_128_MAPPING;

//import streamingeegexperiment.EEGData;
//import streamingeegexperiment.FIRFilter;
//import streamingeegexperiment.FIRFilterGPU;

/**
 *
 * @author juhasz
 */
public class TrialInterface {

    private Map<String, float[]> csvChannels;

    /**
     * Specifies where the input data is coming from.
     * 
     * Possible values: 
     *      - FILE: input file (EDF/BDF formats)
     *      - INSTRUMENT: live measurement data from Biosemi EEG device
     *      - SIMULATION: programmatic EEG data (e.g. simulated potential from head models, etc.)
     */
    public enum InputType {
        FILE, INSTRUMENT, SIMULATION
    };

    // input format
    public enum FileFormatType {
        EDF, EDF_PLUS, BDF, BDF_PLUS, CSV
    }

    InputType inputType;
    FileFormatType fileFormat;
    File inputFile;

    Header header;
//    BDFHeader bdfHeader;

//    Map<String, EDFSignalHeader> edfSignalHeaders; 
    public void setInput(File inputfile) throws FileNotFoundException, IOException {
        // store measurement data file and set descriptive parameters
        if (inputfile == null) {
            throw new IllegalArgumentException("Measurement data file is null.");
        }

        this.inputFile = inputfile;
        inputType = InputType.FILE;
        RandomAccessFile f = new RandomAccessFile(inputfile, "r");
        if (inputfile.getName().toLowerCase().endsWith(".edf") ) {
            // process EDF input file header data and create header data structures
            fileFormat = FileFormatType.EDF;
            // create EDF header object and signal headers for each channel so we don't need to do it every time we access the data  
            header = Header.create(f, FileFormatType.EDF);
        } else if (inputfile.getName().toLowerCase().endsWith(".bdf")) {
            // process BDF input file header data and create header data structures
            fileFormat = FileFormatType.BDF;
            // create BDF header object and signal headers for each channel so we don't need to do it every time we access the data  
            header = Header.create(f, FileFormatType.BDF);
        } else if (inputfile.getName().toLowerCase().endsWith(".csv")) {
            fileFormat = FileFormatType.CSV;
            try {
                csvChannels = readCSV(inputfile.getAbsolutePath(), "old");
            } catch (IOException ex) {
                Logger.getLogger(TrialInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else if (inputfile.getName().toLowerCase().endsWith(".txt")) {
            fileFormat = FileFormatType.CSV;
            try {
                csvChannels = readCSV(inputfile.getAbsolutePath(), "raw");
            } catch (IOException ex) {
                Logger.getLogger(TrialInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /* TODO:
        EDFLib file open sequence
        
        check edf file  -- returns header
        check if continuous file
        save header parameters
        check annotations
        
        then set signal header params 
        
        */
        
    }

    public Header getHeader() {
//        if ((inputType.equals(FileFormatType.BDF)) || (inputType.equals(FileFormatType.BDF)))
//            return bdfHeader;
//        if ((inputType.equals(FileFormatType.EDF)) || (inputType.equals(FileFormatType.EDF)))
            return header;
    }

    int getSignalDigitalMaximum(String label) {
        return header.getTriggerHeaders().get(label).getDigitalMaximum();
    }

    public int[] getTriggerData(long start, long end) {
        if (fileFormat == FileFormatType.CSV)
            return new int[]{0};
        else
            return header.readTriggers(SignalHeader.SIGNAL_TYPES.EVENT, "Status", start, end);
//        return getChannelData(SignalHeader.SIGNAL_TYPES.EVENT, "Status", start, end) ;
    }

    public FileFormatType getFileFormatType() {
        return fileFormat;
    }
    
    /**
     * Returns the next segment of data from an EEG data file as channels x samples array.
     * The number of samples is the same as the length of one data record, 
     * or defaults to 1 second.
     * It is assumed that the underlying inputstream is already open and stored in the Header instance.
     * This methods is intended to read data sequentially, record-by-record from the file
     * Returns -1 when end of file is reached
     * @return 
     */
    public float[][] readSegment() throws IOException{
        return header.readSegment();
    }
    
    /**
     * Get measurement data from the given channel from specified start position
     * to end.
     *
     * @param channelLabel
     * @param start
     * @param end
     * @return
     */
    public float[] getChannelData(SignalHeader.SIGNAL_TYPES signalType, String channelLabel, long start, long end) {
        float[] records = null;
        if (inputType == InputType.FILE) {
            if (fileFormat == FileFormatType.EDF) {
                // access the EDF file 
                // get channel
                //  EDFSignalHeader signalHeader = edfHeader.getEdfSignalHeaders().get(channelLabel); 

//                long dataRecordPosition = edfHeader.getDataRecordPosition(signalHeader.getIndex());
//                records = signalHeader.getDataRecords(dataRecordPosition, start, end);
                //       records = edfHeader.getDigitalSamples(0, start, end);
                records = header.getDigitalSamples(signalType, channelLabel, start, end);

                /*
                 // timestamp:
                 number of records   (eg 10)
                 duration of data records (1 sec)
                 samples per record  (600)
                        
                 then
                 number of samples = 10 * 1 * 600
                 sampling frequency = 600 / 1 Hz
                 time delta = 1 / 600 (start adding this to start time)
                 */
            } else if (fileFormat == FileFormatType.BDF) {
//                records = bdfHeader.readDigitalSamples(channelLabel, start, end);
                records = header.getDigitalSamples(signalType, channelLabel, start, end);
            } else if (fileFormat == FileFormatType.CSV) {
//                String filename = "tapping_avg.csv";
//            String filename = "tapping_avg_Szilvia.csv";            
//            String filename = "Kornel_Fulop_billentes_111201_153135_whole_tapping_avg_446.csv";
                records = Arrays.copyOfRange(csvChannels.get(channelLabel), (int)start, (int)end);             
//                if (channelLabel.equals("C10"))
//                    Arrays.fill(records, 40);
//                else
//                    Arrays.fill(records, (float)Math.random()*0.5f);
            }
        }
        // *** not quite OK as we do not keep the timestamps with the data 
//        float[] filteredData = new float[records.length];
//        FIRFilter filter = new FIRFilter(FIRFilter.coeffsBetaBand2048Hz); 
//        for (int i = 0; i < records.length; i++) {
//            filteredData[i] = filter.getOutput(records[i]);            
//        }
//        return filteredData; 
//        FIRFilterGPU<Float> f = new FIRFilterGPU<>();
//        float a = f.getOutput(2.8f);
    //    float[] filteredData = new float[records.length];
    //    FIRFilterGPU filter = new FIRFilterGPU(FIRFilter.coeffsBetaBand2048Hz); 
//        for (int i = 0; i < records.length; i++) {
//            filteredData[i] = filter.getOutput(records[i]);            
//        }        
//        FIRFilterGPU<double[]> g = new FIRFilterGPU<>();
//        double[] b = g.getOutput(new double[]{2.8});
        
//        return filter.transferTest(records);
        return records;
    }

    public float[] getEEGPotentials(long position) {
        float values[] = new float[getHeader().getNumberOfSignals()];
        values = header.getSignalValues(position);
        return values;
    }

    public List<Channel> getChannels(long sampleStart, long sampleEnd) {
        // read samples for all channels in the specified sample interval        
        List<Channel> channels = header.getSamplesTotal(sampleStart, sampleEnd);
        return channels;
    }
        
        
    public String[] getSignalLabel() {
        if (inputType == InputType.FILE) {
            if (fileFormat == FileFormatType.EDF) {
                return header.getSignalLabel();
            } else if (fileFormat == FileFormatType.BDF) {
                return header.getSignalLabel();
            } else if (fileFormat == FileFormatType.CSV) {
                Set<String> labels = new BIOSEMI_ABC_128_MAPPING().keySet();
                List<String> l = new ArrayList<>(labels);
                Collections.sort(l, new Comparator<String>(){
                    @Override
                    public int compare(String o1, String o2) {
                        String c1 = o1.substring(0, 1);
                        String c2 = o2.substring(0, 1);
                        int n1 = Integer.valueOf(o1.substring(1, o1.length()));
                        int n2 = Integer.valueOf(o2.substring(1, o2.length()));
                        
                        if (c1.compareTo(c2) < 0) 
                            return -1;
                        else if (c1.compareTo(c2) > 0) 
                            return 1;
                        else // c1 == c2
                        if (n1 == n2)
                            return 0;
                        else if (n1 < n2) 
                            return -1;
                        else if (n1 > n2)
                            return 1;
                        return 0;
                    }
                    
                });
                return l.toArray(new String[]{});
            }
        }
        return null;
    }

    public int getNumberOfSignals() {
        if (fileFormat == FileFormatType.EDF) {
                return header.getNumberOfSignals();
            } else if (fileFormat == FileFormatType.BDF) {
                return header.getNumberOfSignals();
            } else if (fileFormat == FileFormatType.CSV) {
                if (csvChannels != null)
                    return csvChannels.keySet().size();
                else 
                    return 0;
            }
        return 0;
    }    
    
    public long getNumberOfSamples(String label) {

        if (inputType == InputType.FILE) {
            if (fileFormat == FileFormatType.EDF) {
                return header.getNumberOfSamples(label);
            } else if (fileFormat == FileFormatType.BDF) {
                return header.getNumberOfSamples(label);
            } else if (fileFormat == FileFormatType.CSV) {
                if (csvChannels != null)
                    return csvChannels.get(getSignalLabel()[0]).length;
                else 
                    return 0;
            }
        }
        return -1;
    }

    /**
     * this method is designed for reading one averaged epoch in ASCII format.
     * electrodes in columns, rows are samples in temporal order
     * @param filename
     * @return
     * @throws IOException 
     */
    private Map<String, float[]> readCSV(String filename, String type) throws IOException {
        Map<String, float[]> channels = new HashMap<>();
        try (BufferedReader input = new BufferedReader(new FileReader(filename))) {
            List<float[]> samples = new ArrayList<>();
            String regexp = "";
            if (type.equals("raw"))
                regexp = "[ \\t]"; // space and tabs as deliminters
            if (type.equals("old"))  // old SiCAL type potential files
                regexp = ",";
            
            String line = null;
            int noSamples = 0;
            if (type.equals("old")){
                String headerline = input.readLine();
            }
            while ((line = input.readLine()) != null) {
                // one line contains 1 sample from each of the 128 electrodes
                String[] words = line.split(regexp);
                int size = words.length;
                if (type.equals("old")){   
                    size -=2;
                }             
                float[] data = new float[size];
                for (int i = 0; i < data.length; i++) {
                    if (type.equals("old")){
                        data[i] = Float.valueOf(words[i+2].trim());
                    }
                    if (type.equals("raw")){
                        data[i] = Float.valueOf(words[i].trim());
                    }
                }
                samples.add(data);
                noSamples++;
            }
            // now file is read, data stored in temporary list.
            // create channel data structure. 
            String[] labels = getSignalLabel();
            for (int chan = 0; chan < 128; chan++) {
                float[] s = new float[noSamples];
                for (int i = 0; i < noSamples; i++) {
                    s[i] = samples.get(i)[chan];
                }
                channels.put(labels[chan], s);
            }
        }
        return channels;
    }
    
//    public float[] getChannelsOneData(long time) {
//
//        if (inputType == InputType.FILE) {
//            if (fileFormat == FileFormatType.EDF) {
//
//                String[] labels = header.getSignalLabel();
//                float[] data = new float[labels.length];
//                for (int i = 0; i < labels.length; i++) {
//                    data[i] = header.readOneData(labels[i], time);
//                }
//                return data;
//            } else if (fileFormat == FileFormatType.BDF) {
//
//                String[] labels = header.getSignalLabel();
//                float[] data = new float[labels.length];
//                for (int i = 0; i < labels.length; i++) {
//                    data[i] = header.readOneData(labels[i], time);
//                }
//                return data;
//            }
//        }
//        return null;
//    }

}
