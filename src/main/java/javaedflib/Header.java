package javaedflib;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javaedflib.TrialInterface.*;

/**
 * Class representing an EDF/BDF measurement file.
// * @see http://www.biosemi.com/faq/file_format.htm  and 
// * @see http://www.edfplus.info/specs/edf.html
 * @author juhasz
 */
public class Header{
    
    /// fields holding the EDF/BDF header content ->
    //==========================================================
    /** EDF/BDF Identification code */
    private String version;
    /** EDF/BDF Local subject identification (80 chars)*/
    private String patientInfo;
    /** EDF/BDF Local recording identification (80 chars)*/
    private String recordingInfo;
    /** EDF/BDF Start date of recording */
    private String startDate;
    /** EDF/BDF Start time of recording */
    private String startTime;
    /** EDF/BDF Number of bytes in header (N+1)*256 where N is the number of channels*/
    private int headerSize;
    /** Reserved in EDF, Version of data format ("BIOSEMI" vs "24BIT") in BDF. */
    private String reserved;
    /** EDF/BDF Number of data records "-1" if unknown */
    private int numberOfDataRecords;
    /** EDF/BDF Duration of a data record, in seconds or in nanoseconds? CHECK*/
    private long durationOfDataRecords;//duration in nanoseconds
    /** EDF/BDF Number of channels (N) in data record */
    private int numberOfSignals;
    
    //// fields for internal processing and representation
    //=========================================================
    private int dataOffset;   //file position of the first data record
    private long totalSamplesInDataRecord;
    private static final int NANOSEC = 1000000000;
    private String[] signalLabels;

    //private String fileName = null;
    private RandomAccessFile inputFile;
    //private Calendar start = null;
    //private byte[] unprocessedHeader = new byte[256];
    //private String[] ChannelLabel=null;
  //  private SignalHeader[] signalHeaders = null;
//    private long[] signalDataOffsetInRecord = null;
    
    private long marker;    // used for keeping track of data records in segment read mode

    
    /** main table holding all the signals in the file as (channel label, Header) key-value pairs */
    private Map<String, SignalHeader> signalHeaders;
    
    // specific map for signal groups. This is the key entry point for finding specific signal lists (eg EEG or ECG signals)
    private Map<SignalHeader.SIGNAL_TYPES, List<SignalHeader>> signalMap;
    private Map<String, SignalHeader> triggerHeaders;
    
    private FileFormatType fileFormat;
    private int DATA_SIZE;  // 2 (bytes) for EDF, 3 (bytes) for BDF
    private boolean edgeFound;
    private int sampleRate;
    private FileFormatType encoding;


    private long recordSize;
    /**
     * Creates an EDF header object by parsing the header record of the specified binary input file.
     * For details of the EDF data file format consult the specification at 
     * http://www.teuniz.net/edfbrowser/edf%20format%20description.html
     * 
     * @param inputFile
     * @return 
     */
    static Header create(RandomAccessFile inputFile, FileFormatType format) {
        
        if (inputFile == null){
            throw new IllegalArgumentException("Input data file is null");
        }
        Header header = null;
        
        /*
         8 ascii : version of this data format (0)
         80 ascii : local patient identification
         80 ascii : local recording identification
         8 ascii : start date of recording (dd.mm.yy)
         8 ascii : start time of recording (hh.mm.ss)
         8 ascii : number of bytes in header record
         44 ascii : reserved
         8 ascii : number of data records (-1 if unknown)
         8 ascii : duration of a data record, in seconds
         4 ascii : number of signals (ns) in data record
         */
            
        FileChannel inChannel = inputFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(256);
        try {
            inChannel.read(buffer);
            // allocate temporary buffers for data processing
            byte[] version = new byte[8];
            byte[] patientInfo = new byte[80];
            byte[] recordingInfo = new byte[80];
            byte[] startDate = new byte[8];
            byte[] startTime = new byte[8];
            byte[] headerSize = new byte[8];
            byte[] reserved = new byte[44];
            byte[] numberOfDataRecords = new byte[8];
            byte[] durationOfDataRecords = new byte[8];
            byte[] numberOfSignals = new byte[4];
//            byte[] unprocessedHeader = new byte[256];
//
//            buffer.flip();
//            buffer.get(unprocessedHeader);

            buffer.flip();
            buffer.get(version);
            buffer.get(patientInfo);
            buffer.get(recordingInfo);
            buffer.get(startDate);
            buffer.get(startTime);
            buffer.get(headerSize);
            buffer.get(reserved);
//            buffer.position(236);
            buffer.get(numberOfDataRecords);
            buffer.get(durationOfDataRecords);
            buffer.get(numberOfSignals);

            int encodingType = version[0] & 0xFF;
            
//            inChannel.position (0);
            header = new Header(
                    inputFile,
                    format,
                    encodingType,
                    new String(version).trim(),
                    new String(patientInfo).trim(),
                    new String(recordingInfo).trim(),
                    new String(startDate).trim(),
                    new String(startTime).trim(),
                    Integer.valueOf(new String(headerSize).trim()),
                    new String(reserved).trim(),
                    Integer.valueOf(new String(numberOfDataRecords).trim()),
                    Float.valueOf(new String(durationOfDataRecords).trim()),
                    Integer.valueOf(new String(numberOfSignals).trim())
            );
        } catch (IOException | BufferUnderflowException | IndexOutOfBoundsException ex) {
            Logger.getLogger(Header.class.getName()).log(Level.SEVERE, null, ex);
        }
        buffer.clear();

        return header;
    }


    private Header(RandomAccessFile inputFile, FileFormatType format, int encodingType, String version, String patientInfo, String recordingInfo,
            String startDate, String startTime, int headerSize, String reserved, 
            int numberOfDataRecords, float durationOfDataRecords,
            int numberOfSignals) {

        signalMap = new HashMap<SignalHeader.SIGNAL_TYPES, List<SignalHeader>>();
        
        this.inputFile = inputFile;
        this.fileFormat = format;
        if (format == FileFormatType.EDF || format == FileFormatType.EDF_PLUS) {
            DATA_SIZE = 2;
        } else if (format == FileFormatType.BDF || format == FileFormatType.BDF_PLUS) {
            DATA_SIZE = 3;
        }
        if (encodingType == 255){
            this.encoding = FileFormatType.BDF; 
        }else if ((char) encodingType == '0'){
            this.encoding = FileFormatType.EDF;
        }
            
        this.version = version;
        this.patientInfo = patientInfo;
        this.recordingInfo = recordingInfo;
        this.startDate = startDate;
        this.startTime = startTime;
        this.headerSize = headerSize;
        this.reserved = reserved;
        this.numberOfDataRecords = numberOfDataRecords;
        this.durationOfDataRecords = (long)(durationOfDataRecords*NANOSEC);
        this.numberOfSignals = numberOfSignals;
        
        this.dataOffset=256+256*numberOfSignals;
        this.signalHeaders = new HashMap<>();
        this.triggerHeaders = new HashMap<>();
        // create and store signal headers 
        this.signalLabels = new String[numberOfSignals];
        recordSize = 0;
        for (int i = 0; i < numberOfSignals; i++) {
            // find start of signal header, get data
            //sampleRate = 0;
            SignalHeader signalHeader = SignalHeader.create(inputFile, format, numberOfSignals, i);
            // add signal to the signal group map
            SignalHeader.SIGNAL_TYPES sigType = signalHeader.getSignalType();
            if (signalMap.containsKey(sigType)){
                // know
                signalMap.get(sigType).add(signalHeader);
            }else{
                //add new List
                List<SignalHeader> list = new ArrayList<>();
                list.add(signalHeader);
                signalMap.put(sigType, list);
            }

            String label = signalHeader.getLabel();

            // find largest samples per second value for display sample rate
            if (signalHeader.getNumberOfSamples() > sampleRate)
                sampleRate = (int)signalHeader.getNumberOfSamples();

//                System.out.println(i + " -- " + label);
//                if (format == FileFormatType.EDF || format == FileFormatType.EDF_PLUS){
//                    if (label.equals("EDF Annotations"))
//                        System.out.println("Annotation");
//                }
//                if (format == FileFormatType.BDF || format == FileFormatType.BDF_PLUS){
//                    if (label.equals("BDF Annotations"))
//                        System.out.println("Annotation");
//                }
//
//                if (label.equals("Status"))
//                    System.out.println("Status");
            signalHeader.setSignalDataOffset(totalSamplesInDataRecord * DATA_SIZE);
            totalSamplesInDataRecord += signalHeader.getNumberOfSamples();
            // compute the length of the full data record including all channels in bytes
            recordSize += signalHeader.getNumberOfSamples() * DATA_SIZE;

//                if (label.startsWith("EX")){
//                    continue;
//                }else if (label.toLowerCase().startsWith("status")){
//                    triggerHeaders.put(label, signalHeader);
//                }
            signalHeaders.put(label, signalHeader);
            signalLabels[i]=label;
        }
        // compute and store data record length (offset)
    }



//    public Map<String, SignalHeader> getEdfSignalHeaders() {
//        return signalHeaders;
//    }

    

    public Calendar getStart(){
        byte[] tmp = new byte[2];

//        tmp[0] = startDate[6];
//        tmp[1] = startDate[7];
//        int year = new Integer(new String(tmp).trim()).intValue();
//        if(year >= 85 && year <= 99){
//            year += 1900;
//        }else{
//            year += 2000;
//        }
//
//        tmp[0] = startDate[3];
//        tmp[1] = startDate[4];
//        int month = new Integer(new String(tmp).trim()).intValue();
//
//        tmp[0] = startDate[1];
//        tmp[1] = startDate[2];
//        int day = new Integer(new String(tmp).trim()).intValue();
//
//        tmp[0] = startTime[0];
//        tmp[1] = startTime[1];
//        int hour = new Integer(new String(tmp).trim()).intValue();
//
//        tmp[0] = startTime[2];
//        tmp[1] = startTime[3];
//        int minute = new Integer(new String(tmp).trim()).intValue();
//
//        tmp[0] = startTime[4];
//        tmp[1] = startTime[5];
//        int second = new Integer(new String(tmp).trim()).intValue();
//
//        Calendar result = Calendar.getInstance();
//        result.set(year,month,day,hour,minute,second);

// ******* TEMPORARY FIX        
        return Calendar.getInstance();// result;
    }

//    public  byte[] getUnprocessedHeader(){
//        return unprocessedHeader;
//    }

//    long getDataRecordPosition(int index) {
//        return 256 + numberOfSignals * 256;
//    }
    
    float[][] readSegment() throws IOException{
        if (inputFile == null)
            throw new IllegalStateException();
        
        // start at first record if we only start. Successive reads should work from an advanced marker position
        if (marker == 0){
            inputFile.seek(dataOffset);     // position to first data record
            marker = dataOffset;
        }
        // create new sample buffer
        // we assume uniform channel sample rates and allocate buffer only once
        int sampleSize = 1; // default to byte
        if (fileFormat == FileFormatType.EDF)
            sampleSize = 2; // 2 bytes/sample
        if (fileFormat == FileFormatType.BDF)
            sampleSize = 3; // 3 bytes/sample        
        int recordLength = (int) (/*getDurationOfDataRecords() * */ getMaxSampleRate());
        byte[] byteBuffer = new byte[sampleSize * recordLength];
                        
        // start reading the data record for the channels
        int count;
        String[] labels = getSignalLabel();
        // prepare output array
        float[][] segment = new float[labels.length][];   // add samples later
        
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
             
            count = inputFile.read(byteBuffer);
            if (count != -1) {
                // convert segment to float array
                float[] data = convert(byteBuffer, recordLength, fileFormat, getSignalHeaders().get(label));
                // add to segment array
                segment[i] = data;
            } else {
                segment = null;
                break;
            }
        }
        
        return segment;
    }
    
    private float[] convert(byte[] buffer, int recordLength, FileFormatType fileFormat, SignalHeader signal){
        float[] samples = new float[recordLength];
        if (fileFormat == FileFormatType.EDF || fileFormat == FileFormatType.EDF_PLUS) {
            throw new UnsupportedOperationException();
        }
        if (fileFormat == FileFormatType.BDF || fileFormat == FileFormatType.BDF_PLUS) {
            for (int i = 0; i < recordLength; i++) {
                int value = (buffer[i * 3 + 2] << 24) & 0xff000000
                        | (buffer[i * 3 + 1] << 16) & 0x00ff0000
                        | (buffer[i * 3] << 8) & 0x0000ff00
                        | (0) & 0x000000ff;
                samples[i] = (float) (signal.getPhysicalMinimum()
                        + ((signal.getPhysicalMaximum() - signal.getPhysicalMinimum())
                        * (value / 256 - signal.getDigitalMinimum()))
                        / (double) ((signal.getDigitalMaximum() - signal.getDigitalMinimum())));

                if (signal.getLabel().equalsIgnoreCase("Status")){
                    samples[i] = (buffer[i*3] & 0xFF) | ((buffer[i*3 + 1] & 0xFF) << 8) | ((buffer[i*3 + 2]) << 16);
                }
            }
        }
        return samples;
    }
    
    float[] getDigitalSamples(SignalHeader.SIGNAL_TYPES signalType, String label, long start, long end) {
        /*
        EDFLib read data sequence
        
        read N samples from channel K
        
        get file and channel K header
        get sample count        smp_in_file = hdr->edfparam[channel].smp_per_record * hdr->datarecords;
        
        adjust n if out of range // over file end
        
        compute and jump to data offset 
        
            file = hdr->file_hdl;

            offset = hdr->hdrsize;
            offset += (hdr->edfparam[channel].sample_pntr / hdr->edfparam[channel].smp_per_record) * hdr->recordsize;
            offset += hdr->edfparam[channel].buf_offset;
            offset += ((hdr->edfparam[channel].sample_pntr % hdr->edfparam[channel].smp_per_record) * bytes_per_smpl);

        seek to offset 
            sample_pntr = hdr->edfparam[channel].sample_pntr;
            samples_end = sample_pntr + n;
            smp_per_record = hdr->edfparam[channel].smp_per_record;
            jump = hdr->recordsize - (smp_per_record * bytes_per_smpl);
          // void* asd = malloc(jump);  // modification to increase read speed
          if(hdr->edf)
            {
              for(i=0; i<n; i++)
              {
                if(!(sample_pntr%smp_per_record))
                {
                  if(i)
                  {
                    fseeko(file, jump, SEEK_CUR);
                    // fread(asd, 1, jump, file);   // modification to increase read speed
                  }
                }

                var.four[0] = fgetc(file);
                tmp = fgetc(file);
                if(tmp==EOF)
                {
                  return(-1);
                }
                var.four[1] = tmp;

                buf[i] = var.two_signed[0];

                sample_pntr++;
              }
            }
        
            hdr->edfparam[channel].sample_pntr = sample_pntr;

            return(n);
       */       
        
        
        //end = 200;
        SignalHeader signal = signalHeaders.get(label);
        if (signal == null)
            return null;
        
        FileChannel inChannel = inputFile.getChannel();
        // allocate buffer for 1 complete signal data record 
        int sampleRate = (int) signal.getNumberOfSamples();
        ByteBuffer buffer = ByteBuffer.allocateDirect(sampleRate * DATA_SIZE);  
//        buffer.order(ByteOrder.nativeOrder());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        // access this buffer later as 16-bit data (EDF only, BDF will use 3 bytes!)
        //ShortBuffer shortBuffer = buffer.asShortBuffer();
        buffer.clear();
        // create buffer for result samples for current channel
        
        // TODO: check that number of samples to read is within the file limits
        int durationInSamples = (int)((end - start));
//        float data[] = new float[durationInSamples];
    //    ------------------------------------------------
        float data[] = new float[durationInSamples];
        
        
////// TODO position is not yet correct !!!!!!!!!!
        // get header size
        long dataRecordsOffset = dataOffset; //256 + numberOfSignals * 256; // start position of first data record
        dataRecordsOffset += (((int)(start)) / signal.getNumberOfSamples()) * recordSize;
        dataRecordsOffset += signal.getSignalDataOffset(); 
//        dataRecordsOffset += signal.getIndex() * signal.getNumberOfSamples() * DATA_SIZE;
//        System.out.println("dataRecordsOffset: " + signal.getSignalDataOffset());
        int firstChunkStartOffset =  (int) (start % signal.getNumberOfSamples());
        int samplesToReadFromFirstChunk = sampleRate - firstChunkStartOffset;
        // we leave the dataRecordOffset to always point to the first sample of a chunk
//        dataRecordsOffset += firstChunkStartOffset * DATA_SIZE;
        // only work in the first record first!
        int chanIndex = signal.getIndex();
        // now we set the correct start position taking the within chunk offset into consideration
        long readPosition = dataRecordsOffset + firstChunkStartOffset * DATA_SIZE ;//+ start + chanIndex * getNumberOfSamples(label)*2;
        
        // change buffer size to match the amount of data we read from the curent data record
//        int bytesToRead = (int)(start % signal.getNumberOfSamples()) * DATA_SIZE;
//        buffer = ByteBuffer.allocate(bytesToRead);
//        shortBuffer = buffer.asShortBuffer();
//        buffer.clear();
        //correct this section not to lose data already fetched into the buffer when going into the next iterration of the 'k' loop
        try {
            
//            int jump = (int)recordSize - (int)(signal.getNumberOfSamples() * DATA_SIZE);
            int sampleCounter = 0;
            int recordCount = 0;
            while (sampleCounter < (end-start)){
                inChannel.position(readPosition);
//            while (sampleCounter < durationInSamples){
//                if ((((firstChunkStartOffset + sampleCounter) % (sampleRate)) == 0) 
//                        && (sampleCounter > 0)){
//                    readPosition += recordSize;// - sampleRate*DATA_SIZE;
//                    inChannel.position(readPosition );
////                    jump += jump;
//                }
                // read a full buffer of samples
                buffer.clear();
                ByteBuffer tempBuffer = ByteBuffer.allocateDirect(samplesToReadFromFirstChunk * DATA_SIZE);
            
                //tempBuffer.clear();
//                int dataLengthInBuffer = inChannel.read(buffer);
                int dataLengthInBuffer = inChannel.read(tempBuffer);
                tempBuffer.flip();
                buffer.put(tempBuffer);
                buffer.flip();
//                buffer.position(0);
//                buffer.limit(dataLengthInBuffer);

                // the rest of the chunks are read normally, starting from the first sample
                firstChunkStartOffset = 0;
                samplesToReadFromFirstChunk = sampleRate;
//                System.out.println("buffer: " + buffer.toString());
//                System.out.println("shortbuffer: " + shortBuffer.toString());
                if (label.equals("Status")){
                    // find 0 -- -1 transitions, leading edge of the tap trigger
                    edgeFound = false;
/*                    int prev = buffer.getShort();
                    for (int i = 1; i < shortBuffer.capacity(); i++) {
                        int value = buffer.getShort() ;
//                        if (i%50 == 0)
//                            System.out.println("");
//                        System.out.print(value + " ");
                        if ((value - prev) == -1){
                            System.out.println("trigger edge found at: " + (start + k + i) +
                                    " position: " + position +
                                    " /" + start + "/" + k +"/" + i);
                        }
                        prev = value;
//                        if (! edgeFound && (value == -2)){
//                            System.out.println("trigger edge found at: " + (start + k + i));
//                            edgeFound = true;
//                        }else if (value != -2){
//                            edgeFound = false;
//                        }
                    }                        
*/
                    if (fileFormat == FileFormatType.EDF || fileFormat == FileFormatType.EDF_PLUS) {                     
//                        int s = (int)(k % signal.getNumberOfSamples());
                        if (dataLengthInBuffer == -1)
                            continue;
                        for (int i = 0; i < buffer.capacity()/2; i++) {
                            data[sampleCounter] = (buffer.get(i*2) & 0xFF) | ((buffer.get(i*2 + 1) & 0xFF) << 8);
                            sampleCounter ++;
                        }                         
//                        for (int i = 0; i < dataLengthInBuffer/DATA_SIZE; i++) {
////                        for (int i = 0; (i < dataLengthInBuffer/DATA_SIZE) && (sampleCounter < durationInSamples); i++) {
//                            if (sampleCounter >= durationInSamples)
//                                break;
//                            data[sampleCounter] = (buffer.getShort());
//                            sampleCounter ++;
//                        }
                    }else if (fileFormat == FileFormatType.BDF || fileFormat == FileFormatType.BDF_PLUS){
                        for (int i = 0; i < buffer.capacity()/3; i++) {
                            data[sampleCounter] = (buffer.get(i*3) & 0xFF) | ((buffer.get(i*3 + 1) & 0xFF) << 8) | ((buffer.get(i*3 + 2)) << 16);
                            sampleCounter ++;
                        }                        
                    }                    
                }else{
                    // convert data
//                SignalHeader signal = (SignalHeader)signal;
                    if (fileFormat == FileFormatType.EDF || fileFormat == FileFormatType.EDF_PLUS) {   
//                        int s = (int)((dataRecordsOffset + sampleCounter*DATA_SIZE) % (sampleRate*DATA_SIZE));
//                        for (int i = s; (i < shortBuffer.capacity()) && (k < end); i++) {
                        for (int i = 0; i < dataLengthInBuffer/DATA_SIZE; i++) {
//                            System.out.println("sample counter: " + sampleCounter + " i: " + i);
                            if (sampleCounter >= durationInSamples)
                                break;
                            
                            data[sampleCounter] = (float) (signal.getPhysicalMinimum()
                                    + (float) ((signal.getPhysicalMaximum() - signal.getPhysicalMinimum())
                                    * (buffer.getShort() - signal.getDigitalMinimum()))
                                    / (float) ((signal.getDigitalMaximum() - signal.getDigitalMinimum())));
                            sampleCounter++;
                        }
                    }else if (fileFormat == FileFormatType.BDF || fileFormat == FileFormatType.BDF_PLUS){
                        double gain = (double) (signal.getPhysicalMaximum() - signal.getPhysicalMinimum()) /
                                    (double) (signal.getDigitalMaximum() - signal.getDigitalMinimum());
                        // print signal reading debug info
//                        if (signal.getIndex() == 0){
//                            System.out.println("readPosition: " + readPosition);
//                            System.out.println("dataLengthInBuffer: " + dataLengthInBuffer);
//                        }
                        
                        
                        for (int i = 0; i < dataLengthInBuffer/3; i++) {
                            if (sampleCounter >= durationInSamples) //data.length)
                                break;
//                            int value = (buffer.get(i * 3) & 0xFF) << 8
//                                    + (buffer.get(i * 3 + 1) & 0xFF) * 256
//                                    + (buffer.get(i * 3 + 2) & 0xFF) * 256 * 256;
                            int value = (buffer.get(i * 3 + 2) << 24) & 0xff000000
                                    | (buffer.get(i * 3 + 1) << 16) & 0x00ff0000
                                    | (buffer.get(i * 3) << 8) & 0x0000ff00
                                    | (0) & 0x000000ff;
                            data[sampleCounter] = (float) (signal.getPhysicalMinimum()
                                    + ((signal.getPhysicalMaximum() - signal.getPhysicalMinimum())
                                    * (value/256 - signal.getDigitalMinimum()))
                                    / (double) ((signal.getDigitalMaximum() - signal.getDigitalMinimum())));
//                            if (sampleCounter < 4*2048)
//                                System.out.println(String.format(
//                                        "%d:%x:%x:%x:%d:%d:%d:%.3f",
//                                        sampleCounter,
//                                        buffer.get(i*3),
//                                        buffer.get(i*3+1),
//                                        buffer.get(i*3+2),
//                                        buffer.get(i*3),
//                                        buffer.get(i*3+1),
//                                        buffer.get(i*3+2),
//                                        data[sampleCounter])
//                                );
//                            System.err.println(data[sampleCounter]);
//                            data[sampleCounter] = (float) (signal.getPhysicalMinimum()
//                                    + (float) ((signal.getPhysicalMaximum() - signal.getPhysicalMinimum())
//                                    * (value - signal.getDigitalMinimum()))
//                                    / (float) ((signal.getDigitalMaximum() - signal.getDigitalMinimum())));
                            sampleCounter++;
                        }                        
                    }
                }
//                float a = Pmin   +  (Pmax - Pmin) * (D - Dmin) / (Dmax - Dmin);
//                k += shortBuffer.capacity();
//                k += k % (signal.getNumberOfSamples());
                recordCount++;
                readPosition = dataRecordsOffset + recordCount * totalSamplesInDataRecord * DATA_SIZE;
//                inChannel.position(readPosition );
                tempBuffer.clear();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Header.class.getName()).log(Level.SEVERE, null, ex);
        }

//        short[] s = null;
//        if (shortBuffer.hasArray())
//            s = shortBuffer.array();
//        
//        int x = shortBuffer.get(0);
//        int y = shortBuffer.get(1);
//        int z = shortBuffer.get(2);
//        
//        EDFSignalHeader temp = edfSignalHeaders.get("EEG Fp1-REF1");
//        double Pmin = temp.getPhysicalMinimum();
//        double Pmax = temp.getPhysicalMaximum();
//        double Dmin = temp.getDigitalMinimum();
//        double Dmax = temp.getDigitalMaximum();
////        float a = Pmin   +  (Pmax - Pmin) * (D - Dmin) / (Dmax - Dmin)
//        double a = Pmin  +  (Pmax - Pmin) * (x - Dmin) / (Dmax - Dmin);
//        double b = Pmin  +  (Pmax - Pmin) * (y - Dmin) / (Dmax - Dmin);
//        double c = Pmin  +  (Pmax - Pmin) * (z - Dmin) / (Dmax - Dmin);
        return data;
    }   

    public int[] readTriggers(SignalHeader.SIGNAL_TYPES signalType, String label, long start, long end) {       
        /*
        EDFLib read data sequence
        
        read N samples from channel K
        
        get file and channel K header
        get sample count        smp_in_file = hdr->edfparam[channel].smp_per_record * hdr->datarecords;
        
        adjust n if out of range // over file end
        
        compute and jump to data offset 
        
            file = hdr->file_hdl;

            offset = hdr->hdrsize;
            offset += (hdr->edfparam[channel].sample_pntr / hdr->edfparam[channel].smp_per_record) * hdr->recordsize;
            offset += hdr->edfparam[channel].buf_offset;
            offset += ((hdr->edfparam[channel].sample_pntr % hdr->edfparam[channel].smp_per_record) * bytes_per_smpl);

        seek to offset 
            sample_pntr = hdr->edfparam[channel].sample_pntr;
            samples_end = sample_pntr + n;
            smp_per_record = hdr->edfparam[channel].smp_per_record;
            jump = hdr->recordsize - (smp_per_record * bytes_per_smpl);
          if(hdr->edf)
            {
              for(i=0; i<n; i++)
              {
                if(!(sample_pntr%smp_per_record))
                {
                  if(i)
                  {
                    fseeko(file, jump, SEEK_CUR);
                  }
                }

                var.four[0] = fgetc(file);
                tmp = fgetc(file);
                if(tmp==EOF)
                {
                  return(-1);
                }
                var.four[1] = tmp;

                buf[i] = var.two_signed[0];

                sample_pntr++;
              }
            }
        
            hdr->edfparam[channel].sample_pntr = sample_pntr;

            return(n);
       */       
        
        
        //end = 200;
        SignalHeader signal = signalHeaders.get(label);
        if (signal == null)
            return null;
        
        FileChannel inChannel = inputFile.getChannel();
        // allocate buffer for 1 complete signal data record 
        int sampleRate = (int) signal.getNumberOfSamples();
        ByteBuffer buffer = ByteBuffer.allocate(sampleRate * DATA_SIZE);  
//        buffer.order(ByteOrder.nativeOrder());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        // access this buffer later as 16-bit data (EDF only, BDF will use 3 bytes!)
        ShortBuffer shortBuffer = buffer.asShortBuffer();
        buffer.clear();
        // create buffer for result samples for current channel
        
        // TODO: check that number of samples to read is within the file limits
        int durationInSamples = (int)((end - start));
//        float data[] = new float[durationInSamples];
    //    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        int data[] = new int[durationInSamples];
        
        
////// TODO position is not yet correct !!!!!!!!!!
        // get header size
        long dataRecordsOffset = dataOffset; //256 + numberOfSignals * 256; // start position of first data record
        dataRecordsOffset += (((int)(start)) / signal.getNumberOfSamples()) * recordSize;
        dataRecordsOffset += signal.getSignalDataOffset(); 
//        dataRecordsOffset += signal.getIndex() * signal.getNumberOfSamples() * DATA_SIZE;
//        System.out.println("dataRecordsOffset: " + signal.getSignalDataOffset());
        int firstChunkStartOffset =  (int) (start % signal.getNumberOfSamples());
        int samplesToReadFromFirstChunk = sampleRate - firstChunkStartOffset;
        // we leave the dataRecordOffset to always point to the first sample of a chunk
//        dataRecordsOffset += firstChunkStartOffset * DATA_SIZE;
        // only work in the first record first!
        int chanIndex = signal.getIndex();
        // now we set the correct start position taking the within chunk offset into consideration
        long readPosition = dataRecordsOffset + firstChunkStartOffset * DATA_SIZE ;//+ start + chanIndex * getNumberOfSamples(label)*2;
        
        // change buffer size to match the amount of data we read from the curent data record
//        int bytesToRead = (int)(start % signal.getNumberOfSamples()) * DATA_SIZE;
//        buffer = ByteBuffer.allocate(bytesToRead);
//        shortBuffer = buffer.asShortBuffer();
//        buffer.clear();
        //correct this section not to lose data already fetched into the buffer when going into the next iterration of the 'k' loop
        try {
            
//            int jump = (int)recordSize - (int)(signal.getNumberOfSamples() * DATA_SIZE);
            int sampleCounter = 0;
            int recordCount = 0;
            while (sampleCounter < (end-start)){
                inChannel.position(readPosition);
                ByteBuffer tempBuffer = ByteBuffer.allocate(samplesToReadFromFirstChunk * DATA_SIZE);
//            while (sampleCounter < durationInSamples){
//                if ((((firstChunkStartOffset + sampleCounter) % (sampleRate)) == 0) 
//                        && (sampleCounter > 0)){
//                    readPosition += recordSize;// - sampleRate*DATA_SIZE;
//                    inChannel.position(readPosition );
////                    jump += jump;
//                }
                // read a full buffer of samples
                buffer.clear();
                tempBuffer.clear();
                int dataLengthInBuffer = inChannel.read(tempBuffer);
                tempBuffer.flip();
                buffer.put(tempBuffer);
                buffer.flip();
                
                // the rest of the chunks are read normally, starting from the first sample
                firstChunkStartOffset = 0;
                samplesToReadFromFirstChunk = sampleRate;
//                System.out.println("buffer: " + buffer.toString());
//                System.out.println("shortbuffer: " + shortBuffer.toString());
                if (label.equals("Status")){
                    // find 0 -- -1 transitions, leading edge of the tap trigger
                    edgeFound = false;
/*                    int prev = buffer.getShort();
                    for (int i = 1; i < shortBuffer.capacity(); i++) {
                        int value = buffer.getShort() ;
//                        if (i%50 == 0)
//                            System.out.println("");
//                        System.out.print(value + " ");
                        if ((value - prev) == -1){
                            System.out.println("trigger edge found at: " + (start + k + i) +
                                    " position: " + position +
                                    " /" + start + "/" + k +"/" + i);
                        }
                        prev = value;
//                        if (! edgeFound && (value == -2)){
//                            System.out.println("trigger edge found at: " + (start + k + i));
//                            edgeFound = true;
//                        }else if (value != -2){
//                            edgeFound = false;
//                        }
                    }                        
*/
                    if (fileFormat == FileFormatType.EDF || fileFormat == FileFormatType.EDF_PLUS) {                     
//                        int s = (int)(k % signal.getNumberOfSamples());
                        if (dataLengthInBuffer == -1)
                            continue;
                        
                        for (int i = 0; i < dataLengthInBuffer/DATA_SIZE; i++) {
//                        for (int i = 0; (i < dataLengthInBuffer/DATA_SIZE) && (sampleCounter < durationInSamples); i++) {
                            if (sampleCounter >= durationInSamples)
                                break;
                            data[sampleCounter] = (buffer.getShort());
                            sampleCounter ++;
                        }
                    }else if (fileFormat == FileFormatType.BDF || fileFormat == FileFormatType.BDF_PLUS){
                        for (int i = 0; i < buffer.capacity()/3; i++) {
                            data[sampleCounter] = (buffer.get(i*3) & 0xFF) | ((buffer.get(i*3 + 1) & 0xFF) << 8) | ((buffer.get(i*3 + 2)) << 16);
                            sampleCounter ++;
                        }                        
                    }
                    
                }//                float a = Pmin   +  (Pmax - Pmin) * (D - Dmin) / (Dmax - Dmin);
//                k += shortBuffer.capacity();
//                k += k % (signal.getNumberOfSamples());
                recordCount++;
                readPosition = dataRecordsOffset + recordCount * totalSamplesInDataRecord * DATA_SIZE;
//                inChannel.position(readPosition );
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Header.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }   


    public float[] readDigitalSamples(String label, long start, long end) {

        SignalHeader signalHeader = (SignalHeader)signalHeaders.get(label);

        long startDataRecordIndex = (start / durationOfDataRecords);
        long endDataRecordIndex = (end / durationOfDataRecords);
        double sampleLength = (((double) durationOfDataRecords) / (double) signalHeader.getNumberOfSamples());
        int startSampleOffset = (int) Math.floor((start % (durationOfDataRecords)) / sampleLength);
        int endSampleOffset = (int) Math.floor((end % (durationOfDataRecords)) / sampleLength);



//         long recordPosition = header.getDataStartPositionInFile() + numberOfRecord * header.getTotalNumberOfSamplesPerRecord() * 2 +
//                header.getSignalDataOffsetInRecord(signalHeader.getCurrentNumberOfSignal());
//        long startingSampleInFirstRecord = Math.round(((double) (startSecond % header.getDurationOfDataRecords())) *
//                (double) signalHeader.getNumberOfSamples() / (double) header.getDurationOfDataRecords());
//        long numberSamplesToRead = Math.round((double) numberOfSeconds * (double) signalHeader.getNumberOfSamples() / (double) header.getDurationOfDataRecords());
//        long numberOfSamplesToReadInFirstRecord = numberSamplesToRead;
//
//        if((numberOfSamplesToReadInFirstRecord + startingSampleInFirstRecord > signalHeader.getNumberOfSamples())){
//            numberOfSamplesToReadInFirstRecord = signalHeader.getNumberOfSamples() - startingSampleInFirstRecord;
//        }
//        
        long numberSamplesToRead = (endDataRecordIndex - startDataRecordIndex) + 1;
        if (numberSamplesToRead == 0) {
            numberSamplesToRead = 1;
        }

        int numberOfData = (int) ((endDataRecordIndex - startDataRecordIndex + 1) * signalHeader.getNumberOfSamples());

        FileChannel inChannel = inputFile.getChannel();
        ByteBuffer outBuffer = ByteBuffer.allocate((int) (numberOfData) * DATA_SIZE);
        ByteBuffer inBuffer = ByteBuffer.allocate((int) signalHeader.getNumberOfSamples() * 2);
        outBuffer.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = outBuffer.asShortBuffer();


        if (numberOfData == 0) {
            numberOfData = 1;
        }

        int numberOfResultData = (int) (numberOfData - startSampleOffset - (signalHeader.getNumberOfSamples() - endSampleOffset));
        if (numberOfResultData == 0) {
            numberOfResultData = 1;
        }
        short[] result = new short[numberOfResultData];
        int numberSamplesToReadAtOnce = 0;


        long recordPosition = startDataRecordIndex * totalSamplesInDataRecord * DATA_SIZE + dataOffset + signalHeader.getSignalDataOffset() * 2;
        numberSamplesToReadAtOnce = (int) signalHeader.getNumberOfSamples();



        byte[] dataToRead = new byte[numberSamplesToReadAtOnce * 2];
        while (numberSamplesToRead > 0) {


            try {
                inChannel.position(recordPosition);
                inChannel.read(inBuffer);
                inBuffer.flip();
                inBuffer.get(dataToRead);

                for (int i = 0; i < dataToRead.length; i++) {
                    outBuffer.put(dataToRead[i]);
                }

//                 outBuffer.put(0,dataToRead[0]);
//                 outBuffer.put(2,dataToRead[2]);
                /*input.seek(recordPosition + startingSampleInFirstRecord * 2);
                 input.readFully(dataToRead);
                 buffer.put(dataToRead);*/
            } catch (EOFException eof) {
                break;
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            numberSamplesToRead -= 1;
//            if(numberSamplesToRead<0)
//                numberSamplesToRead=0;
//            

            recordPosition = recordPosition + totalSamplesInDataRecord * 2;
            inBuffer.clear();
        }
//       shortBuffer.get(result);
        float[] physresult;

  
        int index = 0;
        for (int i = startSampleOffset; i < (result.length + endSampleOffset); i++) {
            result[index] = shortBuffer.get(i);

            index++;

        }
//       shortBuffer.get(result,startSampleOffset,result.length);



        physresult = new float[result.length];
        // [(physical miniumum) + (digital value in the data record - digital minimum) x (physical maximum - physical minimum) / (digital maximum - digital minimum)]
        for (int idxi = 0; idxi < result.length; idxi++) {
            physresult[idxi] = (float) (signalHeader.getPhysicalMinimum() + (result[idxi] - signalHeader.getDigitalMinimum()) * (signalHeader.getPhysicalMaximum() - signalHeader.getPhysicalMinimum()) / (signalHeader.getDigitalMaximum() - signalHeader.getDigitalMinimum()));
        }

        return physresult;


    }


    public float readOneData(String label, long start) {

        SignalHeader signalHeader = (SignalHeader)signalHeaders.get(label);

        long startDataRecordIndex = (start / durationOfDataRecords);
        long endDataRecordIndex = (start / durationOfDataRecords);
        double sampleLength = (((double) durationOfDataRecords) / (double) signalHeader.getNumberOfSamples());
        int startSampleOffset = (int) Math.floor((start % (durationOfDataRecords)) / sampleLength);
        int endSampleOffset = (int) Math.floor((start % (durationOfDataRecords)) / sampleLength);


//         long recordPosition = header.getDataStartPositionInFile() + numberOfRecord * header.getTotalNumberOfSamplesPerRecord() * 2 +
//                header.getSignalDataOffsetInRecord(signalHeader.getCurrentNumberOfSignal());
//        long startingSampleInFirstRecord = Math.round(((double) (startSecond % header.getDurationOfDataRecords())) *
//                (double) signalHeader.getNumberOfSamples() / (double) header.getDurationOfDataRecords());
//        long numberSamplesToRead = Math.round((double) numberOfSeconds * (double) signalHeader.getNumberOfSamples() / (double) header.getDurationOfDataRecords());
//        long numberOfSamplesToReadInFirstRecord = numberSamplesToRead;
//
//        if((numberOfSamplesToReadInFirstRecord + startingSampleInFirstRecord > signalHeader.getNumberOfSamples())){
//            numberOfSamplesToReadInFirstRecord = signalHeader.getNumberOfSamples() - startingSampleInFirstRecord;
//        }
//        
        long numberSamplesToRead = (endDataRecordIndex - startDataRecordIndex) + 1;
        if (numberSamplesToRead == 0) {
            numberSamplesToRead = 1;
        }

        int numberOfData = (int) ((endDataRecordIndex - startDataRecordIndex + 1) * signalHeader.getNumberOfSamples());

        FileChannel inChannel = inputFile.getChannel();
        ByteBuffer outBuffer = ByteBuffer.allocate((int) (numberOfData) * DATA_SIZE);
        ByteBuffer inBuffer = ByteBuffer.allocate((int) signalHeader.getNumberOfSamples() * DATA_SIZE);
        outBuffer.order(ByteOrder.nativeOrder());
         ShortBuffer shortBuffer = outBuffer.asShortBuffer();

        if (numberOfData == 0) {
            numberOfData = 1;
        }

        int numberOfResultData = (int) (numberOfData - startSampleOffset - (signalHeader.getNumberOfSamples() - endSampleOffset));
        if (numberOfResultData == 0) {
            numberOfResultData = 1;
        }

        int numberSamplesToReadAtOnce = 0;

        long recordPosition = startDataRecordIndex * totalSamplesInDataRecord * DATA_SIZE+ dataOffset + signalHeader.getSignalDataOffset() * DATA_SIZE;
        numberSamplesToReadAtOnce = (int) signalHeader.getNumberOfSamples();

        byte[] dataToRead = new byte[numberSamplesToReadAtOnce * DATA_SIZE];
        while (numberSamplesToRead > 0) {
            try {
                inChannel.position(recordPosition);
                inChannel.read(inBuffer);
                inBuffer.flip();
                inBuffer.get(dataToRead);

                for (int i = 0; i < dataToRead.length; i++) {
                    outBuffer.put(dataToRead[i]);
                }

//                 outBuffer.put(0,dataToRead[0]);
//                 outBuffer.put(2,dataToRead[2]);
                /*input.seek(recordPosition + startingSampleInFirstRecord * 2);
                 input.readFully(dataToRead);
                 buffer.put(dataToRead);*/
            } catch (EOFException eof) {
                break;
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            numberSamplesToRead -= 1;
//            if(numberSamplesToRead<0)
//                numberSamplesToRead=0;
//            

            recordPosition = recordPosition + totalSamplesInDataRecord * 2;
            inBuffer.clear();
        }
//       shortBuffer.get(result);

//            int a=outBuffer.get(i) & 0xFF;
//            int b=outBuffer.get(i+1) & 0xFF;
//            int c=outBuffer.get(i+2) & 0xFF;
//            b<<=8;
//            c<<=16;
//            
//            result3[index]=a+b+c;
        short result = shortBuffer.get(startSampleOffset);




        // [(physical miniumum) + (digital value in the data record - digital minimum) x (physical maximum - physical minimum) / (digital maximum - digital minimum)]

        float physresult = (float) (signalHeader.getPhysicalMinimum() + (result - signalHeader.getDigitalMinimum()) * (signalHeader.getPhysicalMaximum() - 
                signalHeader.getPhysicalMinimum()) / (signalHeader.getDigitalMaximum() - signalHeader.getDigitalMinimum()));


        return physresult;



    }


    public float getDurationOfDataRecords() {
        return durationOfDataRecords;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public long getNumberOfDataRecords() {
        return numberOfDataRecords;
    }

    public int getNumberOfSignals() {
//        return numberOfSignals;
        return signalHeaders.values().size();
    }

    public String getPatientInfo() {
        return patientInfo;
    }

    public String getRecordingInfo() {
        return recordingInfo;
    }

    public String getReserved() {
        return reserved;
    }

    public String[] getSignalLabel() {
        return signalLabels;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, SignalHeader> getSignalHeaders() {
        return signalHeaders;
    }
    
    public Map<String, SignalHeader> getTriggerHeaders() {
        return triggerHeaders;
    }

    public long getNumberOfSamples(String label) {
        return signalHeaders.get(label).getNumberOfSamples();
    }
    
    float[] getSignalValues(long position) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getMaxSampleRate() {
        sampleRate = 0;
        for (Iterator it = signalHeaders.values().iterator(); it.hasNext();) {
                SignalHeader signal = (SignalHeader) it.next();
                int tempRate = getSampleRate(signal.getLabel());
                if (tempRate > sampleRate)
                    sampleRate = tempRate;
            }
        return sampleRate;
    }
    
    public int getSampleRate(String label) {
        // get number of samples for specified channel and use it to calculate the number of samples per second
        // conversion from nanosec to sec is required as duration is already in nanosec
        return (int) (NANOSEC * signalHeaders.get(label).getNumberOfSamples() / durationOfDataRecords);
    }

    public List<Channel_old> getSamplesTotal(long sampleStart, long sampleEnd) {
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
        List<Channel_old> channels = new ArrayList<>();
        // read 1 second for all channels, than the next and so on for improved file access performance
        // prepare data structure for returning samples
        for (int i = 0; i < signalLabels.length; i++) {
            Channel_old chan = new Channel_old();
            chan.label = signalLabels[i];
            if (chan.label.equals("Status"))
                chan.trigger = new int[(int)(sampleEnd - sampleStart)];
            else
                chan.data = new float[(int)(sampleEnd - sampleStart)];
            channels.add(chan); 
        }
        
//        SignalHeader signal = signalHeaders.get(label);
//        if (signal == null)
//            return null;
        
        FileChannel inChannel = inputFile.getChannel();
        // allocate buffer for 1 complete signal data record 
//        int sampleRate = (int) signal.getNumberOfSamples();
        // allocate large enough buffer for holding 1 second of samples byte data for all channels 
        ByteBuffer buffer = ByteBuffer.allocate((int) ((sampleEnd - sampleStart) * DATA_SIZE * numberOfSignals));  
//        buffer.order(ByteOrder.nativeOrder());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        // access this buffer later as 16-bit data (EDF only, BDF will use 3 bytes!)
        ShortBuffer shortBuffer = buffer.asShortBuffer();
        buffer.clear();
        // create buffer for result samples for current channel
        
        // TODO: check that number of samples to read is within the file limits
        int durationInSamples = (int)((sampleEnd - sampleStart));
//        float data[] = new float[durationInSamples];
    //    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        //float data[] = new float[durationInSamples];
        
        
////// TODO position is not yet correct !!!!!!!!!!
        // get header size
        long dataRecordsOffset = dataOffset; //256 + numberOfSignals * 256; // start position of first data record
//        dataRecordsOffset += (((int)(sampleStart)) / signal.getNumberOfSamples()) * recordSize;
//        dataRecordsOffset += signal.getSignalDataOffset(); 
////        dataRecordsOffset += signal.getIndex() * signal.getNumberOfSamples() * DATA_SIZE;
////        System.out.println("dataRecordsOffset: " + signal.getSignalDataOffset());
//        int firstChunkStartOffset =  (int) (start % signal.getNumberOfSamples());
//        int samplesToReadFromFirstChunk = sampleRate - firstChunkStartOffset;
//        // we leave the dataRecordOffset to always point to the first sample of a chunk
////        dataRecordsOffset += firstChunkStartOffset * DATA_SIZE;
//        // only work in the first record first!
//        int chanIndex = signal.getIndex();
        // now we set the correct start position taking the within chunk offset into consideration
        long readPosition = dataRecordsOffset;// + firstChunkStartOffset * DATA_SIZE ;//+ start + chanIndex * getNumberOfSamples(label)*2;
        
        // change buffer size to match the amount of data we read from the curent data record
//        int bytesToRead = (int)(start % signal.getNumberOfSamples()) * DATA_SIZE;
//        buffer = ByteBuffer.allocate(bytesToRead);
//        shortBuffer = buffer.asShortBuffer();
//        buffer.clear();
        //correct this section not to lose data already fetched into the buffer when going into the next iterration of the 'k' loop
        try {
            
//            int jump = (int)recordSize - (int)(signal.getNumberOfSamples() * DATA_SIZE);
            int sampleCounter = 0;
            int recordCount = 0;
            int bufferPointer = 0;
            inChannel.position(readPosition);
            while (sampleCounter < (sampleEnd-sampleStart)){
                // read a full buffer of samples
                buffer.clear();
//                ByteBuffer tempBuffer = ByteBuffer.allocate(samplesToReadFromFirstChunk * DATA_SIZE);
            
                //tempBuffer.clear();
                int dataLengthInBuffer = inChannel.read(buffer);
//                int dataLengthInBuffer = inChannel.read(tempBuffer);
//                tempBuffer.flip();
//                buffer.put(tempBuffer);
                buffer.flip();
//                buffer.position(0);
//                buffer.limit(dataLengthInBuffer);

                // now copy data to channels in a loop
                for (int i = 0; i < numberOfSignals; i++) {
                    SignalHeader signal = signalHeaders.get(channels.get(i).label);
                    // the rest of the chunks are read normally, starting from the first sample
//                    firstChunkStartOffset = 0;
//                    samplesToReadFromFirstChunk = sampleRate;
//                System.out.println("buffer: " + buffer.toString());
//                System.out.println("shortbuffer: " + shortBuffer.toString());
                    if (channels.get(i).label.equals("Status")) {
                        int samplesPerRecord = getSampleRate(channels.get(i).label);
                        if (fileFormat == FileFormatType.EDF || fileFormat == FileFormatType.EDF_PLUS) {
                            if (dataLengthInBuffer == -1) {
                                continue;
                            }
                            for (int j = 0; j < samplesPerRecord; j++) {
                                channels.get(i).trigger[j] = (buffer.get((bufferPointer+j) * 2) & 0xFF) | ((buffer.get((bufferPointer+j) * 2 + 1) & 0xFF) << 8);                               
                                
                            }
                            bufferPointer += samplesPerRecord;
                        } else if (fileFormat == FileFormatType.BDF || fileFormat == FileFormatType.BDF_PLUS) {
                            for (int j = 0; j < samplesPerRecord; j++) {
                                channels.get(i).trigger[j] = (buffer.get((bufferPointer+j) * 3) & 0xFF) | ((buffer.get((bufferPointer+j) * 3 + 1) & 0xFF) << 8) | ((buffer.get((bufferPointer+j) * 3 + 2)) << 16);
                                
                            }
                            bufferPointer += samplesPerRecord;
                        }

                    } else // convert data
                        //                SignalHeader signal = (SignalHeader)signal;
                        if (fileFormat == FileFormatType.EDF || fileFormat == FileFormatType.EDF_PLUS) {
                            int samplesPerRecord = getSampleRate(channels.get(i).label);
    //                        int s = (int)((dataRecordsOffset + sampleCounter*DATA_SIZE) % (sampleRate*DATA_SIZE));
    //                        for (int i = s; (i < shortBuffer.capacity()) && (k < end); i++) {
                            for (int j = 0; j < samplesPerRecord; j++) {
                                  int value = (buffer.get((bufferPointer + j) * 2) & 0xFF) | ((buffer.get((bufferPointer + j) * 2 + 1) & 0xFF) << 8);
    //                            System.out.println("sample counter: " + sampleCounter + " i: " + i);
                                if (sampleCounter >= samplesPerRecord) {
                                    break;
                                }

                                channels.get(i).data[j] = (float) (signal.getPhysicalMinimum()
                                        + (float) ((signal.getPhysicalMaximum() - signal.getPhysicalMinimum())
                                        * (value - signal.getDigitalMinimum()))
                                        / (float) ((signal.getDigitalMaximum() - signal.getDigitalMinimum())));
                                
                            }
                            bufferPointer += samplesPerRecord;
                        } else if (fileFormat == FileFormatType.BDF || fileFormat == FileFormatType.BDF_PLUS) {
                            int samplesPerRecord = getSampleRate(channels.get(i).label);
                            for (int j = 0; j < samplesPerRecord; j++) {
                                int value = (buffer.get((bufferPointer + j) * 3) & 0xFF) | ((buffer.get((bufferPointer + j) * 3 + 1) & 0xFF) << 8) | ((buffer.get((bufferPointer + j) * 3 + 2)) << 16);
                                if (sampleCounter >= samplesPerRecord) {
                                    break;
                                }

                                channels.get(i).data[j] = (float) (signal.getPhysicalMinimum()
                                        + (float) ((signal.getPhysicalMaximum() - signal.getPhysicalMinimum())
                                        * (value - signal.getDigitalMinimum()))
                                        / (float) ((signal.getDigitalMaximum() - signal.getDigitalMinimum())));                                
                            }
                            bufferPointer += samplesPerRecord;
                        }
                    recordCount++;
                }
                sampleCounter = bufferPointer/numberOfSignals;
                readPosition += recordSize;                
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Header.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return channels;
    }

}
