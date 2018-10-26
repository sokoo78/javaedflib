package javaedflib;

import java.io.IOException;
import java.util.Map;

class DataBuffer {
    private BinFileIO binFileIO;
    private FileHeader fileHeader;
    private Map<Integer, ChannelHeader> channelHeaders;
    private Object[][][] signals;

    DataBuffer(String path) throws IOException {
        binFileIO = new BinFileIO(path);
        fileHeader = binFileIO.ReadFileHeader();
        channelHeaders = binFileIO.ReadChannelHeaders(fileHeader.getNumberOfChannels());
    }

    void ReadRecords(int start, int length) {
        signals = binFileIO.ReadRecords(fileHeader.getHeaderSize(), fileHeader.getNumberOfChannels(), fileHeader.getNumberOfDataRecords(), start, length);
    }

    void PrintRecords(int start, int length) {
        for (int channel = 0; channel < fileHeader.getNumberOfChannels(); channel++ ) {
            for (int time = start; time < time + length; time++) {
                for (int sample = 0; sample < fileHeader.getNumberOfDataRecords(); sample++) {
                    System.out.println(signals[channel][time][sample]);
                }
            }
        }
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
        channelHeaders.forEach((key,value)->System.out.println("Channel[" + key + "] label : " + value.getLabelOfChannel()));
    }
}
