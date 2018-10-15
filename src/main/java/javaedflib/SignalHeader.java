package javaedflib;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javaedflib.TrialInterface.FileFormatType;

/**
 * Class representing one channel in and EDF/BDF file
// * @see http://www.biosemi.com/faq/file_format.htm  and 
// * @see http://www.edfplus.info/specs/edf.html
 * @author juhasz
 */
public class SignalHeader {

    public enum SIGNAL_TYPES {DISTANCE, AREA, VOLUME, DURATION, VELOCITY,
    MASS, ANGLE, PERCENTAGE, MONEY_VALUE, EEG, SEEG, ECG, EOG, ERG, EMG, MEG, MCG, EP, 
    TEMPERATURE, RESPIRATION, OXYGEN_SATURATION, LIGHT, SOUND, SOUND_PRESSURE_LEVEL, 
    EVENT, ANNOTATION, UNKNOWN};
    private RandomAccessFile inputFile;
    private final FileFormatType fileFormat;

    private int index;     // store the signal index in the EDF file structure for future reference
    
    /** EDF/BDF Labels of the channels (16 bytes)
     * label has a 'type' 'space' 'sensor specification' format.
     * This label stores the original label found in the file.
     * It is split during processing into separate type and sensor name data
     * //@see http://www.edfplus.info/specs/edftexts.html#signals
     */
    private String label;
    /** EDF/BDF Transducer type (80 bytes) */
    private String transducerType;
    /** EDF/BDF  Physical dimension of channels (e.g uV, Ohm, etc)*/
    private String physicalDimension;
    /** EDF/BDF Physical minimum in units of physical dimension (BDF: -262144, EDF: -32768) */
    private double physicalMinimum;
    /** EDF/BDF Physical maximum in units of physical dimension (BDF: 262143, EDF: 32767) */
    private double physicalMaximum;
    /** EDF/BDF  */
    private int digitalMinimum;
    /** EDF/BDF  */
    private int digitalMaximum;
    /** EDF/BDF Pre-filtering description (80 bytes), e.g. BDF: "HP:DC; LP:410" or EDF: "HP:0,16; LP:500" 	 */
    private String prefilteringInfo;
    /** EDF/BDF  Number of samples in each data record. (Sample-rate if Duration of data record = "1") */
    private long numberOfSamples;
    /** EDF/BDF Reserved (32 bytes) */
    private String reserved;

    private long signalDataOffset;
    
    private SIGNAL_TYPES signalType;
    /** stores the signal sensor label without the type information, e.g. A1*/
    private String signalLabel;
    private double standardDeviation;

    
    public static SignalHeader create(RandomAccessFile inputFile, FileFormatType format,
            int numSignals, int signalIndex) {

        /*
         * ns * 16 ascii : ns * label (e.g. EEG FpzCz or Body temp)
         ns * 80 ascii : ns * transducer type (e.g. AgAgCl electrode)
         ns * 8 ascii : ns * physical dimension (e.g. uV or degreeC)
         ns * 8 ascii : ns * physical minimum (e.g. -500 or 34)
         ns * 8 ascii : ns * physical maximum (e.g. 500 or 40)
         ns * 8 ascii : ns * digital minimum (e.g. -2048)
         ns * 8 ascii : ns * digital maximum (e.g. 2047)
         ns * 80 ascii : ns * prefilteringInfo (e.g. HP:0.1Hz LP:75Hz)
         ns * 8 ascii : ns * numberOfSamples in each data record
         ns * 32 ascii : ns * reserved
         */
        
        SignalHeader edfSignalHeader = null;
        
        int position = 0;
        FileChannel inChannel = inputFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(256 * numSignals);
        buffer.clear();
        try {
            inChannel.position(256);
            inChannel.read(buffer);
            buffer.flip();

            // allocate temporary buffers for parsing the header
            byte[] label = new byte[16];
            byte[] transducerType = new byte[80];
            byte[] physicalDimension = new byte[8];
            byte[] physicalMinimum = new byte[8];
            byte[] physicalMaximum = new byte[8];
            byte[] digitalMinimum = new byte[8];
            byte[] digitalMaximum = new byte[8];
            byte[] prefilteringInfo = new byte[80];
            byte[] numberOfSamples = new byte[8];
            byte[] reserved = new byte[32];

            buffer.position(position + signalIndex * 16);
            buffer.get(label);
            position += numSignals * 16;

            buffer.position(position + signalIndex * 80);
            buffer.get(transducerType);
            position += numSignals * 80;

            buffer.position(position + signalIndex * 8);
            buffer.get(physicalDimension);
            position += numSignals * 8;

            buffer.position(position + signalIndex * 8);
            buffer.get(physicalMinimum);
            position += numSignals * 8;

            buffer.position(position + signalIndex * 8);
            buffer.get(physicalMaximum);
            position += numSignals * 8;

            buffer.position(position + signalIndex * 8);
            buffer.get(digitalMinimum);
            position += numSignals * 8;

            buffer.position(position + signalIndex * 8);
            buffer.get(digitalMaximum);
            position += numSignals * 8;

            buffer.position(position + signalIndex * 80);
            buffer.get(prefilteringInfo);
            position += numSignals * 80;

            buffer.position(position + signalIndex * 8);
            buffer.get(numberOfSamples);
            position += numSignals * 8;

            buffer.position(position + signalIndex * 32);
            buffer.get(reserved);
            position += numSignals * 32;
            
//buffer.position(position + currentNumberOfSignal * 8);

            edfSignalHeader = new SignalHeader(
                    inputFile,
                    format,
                    signalIndex,
                    new String(label).trim(), 
                    new String(transducerType).trim(), 
                    new String(physicalDimension).trim(), 
                    new Double(new String(physicalMinimum).trim()).doubleValue(), 
                    new Double(new String(physicalMaximum).trim()).doubleValue(), 
                    new Long(new String(digitalMinimum).trim()).intValue(), 
                    new Long(new String(digitalMaximum).trim()).intValue(), 
                    new String(prefilteringInfo).trim(), 
                    new Long(new String(numberOfSamples).trim()).longValue(),
                    new String(reserved).trim()
            );
            
        } catch (IOException ex) {
            Logger.getLogger(SignalHeader.class.getName()).log(Level.SEVERE, null, ex);
        }catch(BufferUnderflowException ex){
            Logger.getLogger(SignalHeader.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IndexOutOfBoundsException ex){
            Logger.getLogger(SignalHeader.class.getName()).log(Level.SEVERE, null, ex);
        }
        buffer.clear();
//        inChannel.position(0);
        
        return edfSignalHeader;
    }


    private SignalHeader(RandomAccessFile inputFile, FileFormatType format, int signalID, String label, String transducerType, String physicalDimension, double physicalMinimum, 
            double physicalMaximum, int digitalMinimum, int digitalMaximum, String prefilteringInfo, long numberOfSamples, String reserved) {
        
        this.inputFile = inputFile;
        this.fileFormat = format;
        this.index = signalID;
        this.label = label;
        // parse label for type and specification
        String[] labelWords = null;
        //special case for EDF/BDF Annotation label
        if (label.equals("") || label.equals("")){
            labelWords = new String[]{label};
            signalType = SIGNAL_TYPES.ANNOTATION;
        }
        else 
            labelWords = label.split(" ");
        if (labelWords != null){
            if (labelWords.length == 1 ){
                signalLabel = labelWords[0];
                signalType = SIGNAL_TYPES.UNKNOWN;
            }
            if (labelWords.length == 2 ){
                signalType = parseSignalType(labelWords);                
                signalLabel = labelWords[1];
            }            
        }
        this.transducerType = transducerType;
        this.physicalDimension = physicalDimension;
        this.physicalMinimum = physicalMinimum;
        this.physicalMaximum = physicalMaximum;
        this.digitalMinimum = digitalMinimum;
        this.digitalMaximum = digitalMaximum;
        this.prefilteringInfo = prefilteringInfo;
        this.numberOfSamples = numberOfSamples;
        this.reserved = reserved;
    }

    private SIGNAL_TYPES parseSignalType(String[] labelWords) {
        String type = labelWords[0];
        if (type.equals("Dist"))
            signalType = SIGNAL_TYPES.DISTANCE;
        else if (type.equals("Area"))
            signalType = SIGNAL_TYPES.AREA;
        else if (type.equals("Vol"))
            signalType = SIGNAL_TYPES.VOLUME;
        else if (type.equals("Dur"))
            signalType = SIGNAL_TYPES.DURATION;
        else if (type.equals("Vel"))
            signalType = SIGNAL_TYPES.VELOCITY;
        else if (type.equals("Mass"))
            signalType = SIGNAL_TYPES.MASS;
        else if (type.equals("Angle"))
            signalType = SIGNAL_TYPES.ANGLE;
        else if (type.equals("%"))
            signalType = SIGNAL_TYPES.PERCENTAGE;
        else if (type.equals("Value"))
            signalType = SIGNAL_TYPES.MONEY_VALUE;
        else if (type.equals("EEG"))
            signalType = SIGNAL_TYPES.EEG;
        else if (type.equals("ECG"))
            signalType = SIGNAL_TYPES.ECG;
        else if (type.equals("EOG"))
            signalType = SIGNAL_TYPES.EOG;
        else if (type.equals("ERG"))
            signalType = SIGNAL_TYPES.ERG;
        else if (type.equals("EMG"))
            signalType = SIGNAL_TYPES.EMG;
        else if (type.equals("MEG"))
            signalType = SIGNAL_TYPES.MEG;
        else if (type.equals("MCG"))
            signalType = SIGNAL_TYPES.MCG;
        else if (type.equals("EP"))
            signalType = SIGNAL_TYPES.EP;
        else if (type.equals("Temp"))
            signalType = SIGNAL_TYPES.TEMPERATURE;
        else if (type.equals("Resp"))
            signalType = SIGNAL_TYPES.RESPIRATION;
        else if (type.equals("SaO2"))
            signalType = SIGNAL_TYPES.OXYGEN_SATURATION;
        else if (type.equals("Light"))
            signalType = SIGNAL_TYPES.LIGHT;
        else if (type.equals("Event") || type.equals("Status"))
            signalType = SIGNAL_TYPES.EVENT;
        else if (type.equals("Sound")){
            signalType = SIGNAL_TYPES.SOUND;
            if (labelWords[1].equals("SPL"))
                signalType = SIGNAL_TYPES.SOUND_PRESSURE_LEVEL;
        }
        return signalType;
    }

    SIGNAL_TYPES getSignalType() {
        return signalType;
    }
    
    public int getIndex(){
        return index;
    }
    
    public String getLabel() {
        return label;
    }
    public String getTransducerType() {
        return transducerType;
    }
    public String getPhysicalDimension() {
        return physicalDimension;
    }
    public double getPhysicalMinimum() {
        return physicalMinimum;
    }
    public double getPhysicalMaximum() {
        return physicalMaximum;
    }
    public int getDigitalMinimum() {
        return digitalMinimum;
    }
    public int getDigitalMaximum() {
        return digitalMaximum;
    }
    public String getPrefiltering() {
        return prefilteringInfo;
    }
    public long getNumberOfSamples() {
        return numberOfSamples;
    }
    
    public String getReserved() {
        return reserved;
    }

    //    float[] getDataRecords(long dataRecordPosition, long start, long end) {
    //
    //        FileChannel inChannel = inputFile.getChannel();
    //        ByteBuffer buffer = ByteBuffer.allocate((int)(end - start) * 4);
    //        buffer.clear();
    //
    //        float data[] = new float[(int)(end - start)];
    //
    //////// TODO position is not yet corrct !!!!!!!!!!
    //
    //        long position = dataRecordPosition + start * numberOfSamples;
    //        try {
    //            inChannel.position(position);
    //            while (position < end){
    //                inChannel.read(buffer);
    //                buffer.flip();
    //                // convert data
    //                position += ...
    //            }
    //        }
    //        return data;
    //    }
    public long getSignalDataOffset() {
        return signalDataOffset;
    }

    public void setSignalDataOffset(long signalDataOffset) {
        this.signalDataOffset = signalDataOffset;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }
    


    
}
