package javaedflib;

import java.io.IOException;
import java.util.Map;

class DataBuffer {
    private BinFileIO binFileIO;
    private FileHeader fileHeader;
    private Map<Integer, ChannelHeader> channelHeaders;

    DataBuffer(String path) throws IOException {
        binFileIO = new BinFileIO(path);
        fileHeader = binFileIO.ReadFileHeader();
        channelHeaders = binFileIO.ReadChannelHeaders(fileHeader.getNumberOfChannels());
    }

    String GetFileVersion() {
        var version = fileHeader.getVersion();
        if (version.equals("0"))
            return "EDF";
        if (version.equals("255"))
            return "BDF";
        return "Unknown";
    }

    void PrintHeader() {
        System.out.println("File version:   " + GetFileVersion());
        System.out.println("Patient info:   " + fileHeader.getPatientInfo());
        System.out.println("Recording info: " + fileHeader.getRecordingInfo());
        System.out.println("Start date:     " + fileHeader.getStartDate());
        System.out.println("Start time:     " + fileHeader.getStartTime());
        System.out.println("Header size:    " + fileHeader.getHeaderSize() + " bytes");
        System.out.println("Data format:    " + fileHeader.getReserved());
        System.out.println("Number of records:  " + fileHeader.getNumberOfDataRecords());
        System.out.println("Length of records:  " + fileHeader.getDurationOfDataRecords() + " sec");
        System.out.println("Number of channels: " + fileHeader.getNumberOfChannels());
    }

    void WriteHeader(String path) throws IOException {
        binFileIO.WriteFileHeader(fileHeader, path);
        binFileIO.WriteChannelHeaders(channelHeaders, path);
    }

    void PrintChannelLabels() {
        channelHeaders.forEach((k,v)->System.out.println("Channel: " + k + " - Label : " + v.getPhysicalMinimum()));
    }
}
