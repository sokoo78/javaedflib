package javaedflib;

import java.io.IOException;

class DataBuffer {
    private BinFileIO binFileIO;
    private FileHeader fileHeader;

    DataBuffer(String path) throws IOException {
        binFileIO = new BinFileIO(path);
        fileHeader = binFileIO.ReadHeader();
    }

    String GetFileType() {
        return fileHeader.getVersion();
    }

    void PrintFileTypeName() {
        if (fileHeader.getVersion().equals("0       "))
            System.out.println("EDF");
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

    void WriteHeader(String path) throws IOException {
        binFileIO.WriteHeader(fileHeader, path);
    }
}
