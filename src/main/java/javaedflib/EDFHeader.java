package javaedflib;

public class EDFHeader {
    /** EDF/BDF Identification code */
    protected String version;
    /** EDF/BDF Local subject identification (80 chars)*/
    protected String patientInfo;
    /** EDF/BDF Local recording identification (80 chars)*/
    protected String recordingInfo;
    /** EDF/BDF Start date of recording */
    protected String startDate;
    /** EDF/BDF Start time of recording */
    protected String startTime;
    /** EDF/BDF Number of bytes in header (N+1)*256 where N is the number of channels*/
    protected int headerSize;
    /** Reserved in EDF, Version of data format ("BIOSEMI" vs "24BIT") in BDF. */
    protected String reserved;
    /** EDF/BDF Number of data records "-1" if unknown */
    protected int numberOfDataRecords;
    /** EDF/BDF Duration of a data record, in seconds */
    protected long durationOfDataRecordsInSec;
    /** EDF/BDF Number of channels (N) in data record */
    protected int numberOfSignals;
}
