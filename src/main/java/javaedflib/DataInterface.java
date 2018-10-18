package javaedflib;

import java.io.IOException;

class DataInterface {
    private DataReader dataReader;
    private FileHeader fileHeader;

    DataInterface(String path) throws IOException {
        dataReader = new DataReader(path);
        fileHeader = dataReader.GetHeader();
    }

    String GetFileType() {
        return fileHeader.getVersion();
    }

    void PrintHeader() {
        System.out.println(fileHeader.getVersion());
        System.out.println(fileHeader.getPatientInfo());
        System.out.println(fileHeader.getRecordingInfo());
        System.out.println(fileHeader.getStartDate());
        System.out.println(fileHeader.getStartTime());
        System.out.println(fileHeader.getHeaderSize());
        System.out.println(fileHeader.getReserved());
        System.out.println(fileHeader.getNumberOfDataRecords());
        System.out.println(fileHeader.getDurationOfDataRecords());
        System.out.println(fileHeader.getNumberOfSignals());
    }
}
