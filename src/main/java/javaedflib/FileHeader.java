package javaedflib;

class FileHeader {

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

    String getVersion() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }

    String getPatientInfo() {
        return patientInfo;
    }

    void setPatientInfo(String patientInfo) {
        this.patientInfo = patientInfo;
    }

    String getRecordingInfo() {
        return recordingInfo;
    }

    void setRecordingInfo(String recordingInfo) {
        this.recordingInfo = recordingInfo;
    }

    String getStartDate() {
        return startDate;
    }

    void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    String getStartTime() {
        return startTime;
    }

    void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    int getHeaderSize() {
        return headerSize;
    }

    void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    String getReserved() {
        return reserved;
    }

    void setReserved(String reserved) {
        this.reserved = reserved;
    }

    int getNumberOfDataRecords() {
        return numberOfDataRecords;
    }

    void setNumberOfDataRecords(int numberOfDataRecords) {
        this.numberOfDataRecords = numberOfDataRecords;
    }

    long getDurationOfDataRecords() {
        return durationOfDataRecords;
    }

    void setDurationOfDataRecords(long durationOfDataRecords) {
        this.durationOfDataRecords = durationOfDataRecords;
    }

    int getNumberOfSignals() {
        return numberOfSignals;
    }

    void setNumberOfSignals(int numberOfSignals) {
        this.numberOfSignals = numberOfSignals;
    }
}
