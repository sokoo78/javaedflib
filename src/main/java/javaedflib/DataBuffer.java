package javaedflib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class DataBuffer {
    private BinFileIO binFileIO;
    private FileHeader fileHeader;
    private Map<Integer, Channel> channels = new HashMap<>();
    private int timeSlotSize;

    DataBuffer(String path) {
        binFileIO = new BinFileIO(path);
        fileHeader = binFileIO.ReadFileHeader();
        InitializeChannels();
    }

    private void InitializeChannels() {
        Map<Integer, ChannelHeader> channelHeaders = binFileIO.ReadChannelHeaders(fileHeader.getNumberOfChannels());
        int offset=0;
        for (int i = 0; i < channelHeaders.size(); i++) {
            channels.put(i, new Channel(channelHeaders.get(i), offset));
            offset+=channelHeaders.get(i).getNumberOfSamples() * binFileIO.getSignalDataLength();
        }
        timeSlotSize=offset;
    }

    private String GetFileVersion() {
        var version = fileHeader.getVersion();
        if (version.equals("0"))
            return "EDF";
        if (version.equals("255"))
            return "BDF";
        return "Unknown";
    }

    void PrintFileHeader() {
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

    void WriteFileHeader(String path) throws IOException {
        binFileIO.WriteFileHeader(fileHeader, path);

        Map<Integer, ChannelHeader> channelHeaders = new HashMap<>();
        channels.forEach((key,value) -> channelHeaders.put(key, value.getChannelHeader()));
        binFileIO.WriteChannelHeaders(channelHeaders, path);
    }

    void PrintChannelLabels() {
        channels.forEach((key,value)->System.out.println("Channel[" + key + "] label : " + value.getName() + " SampleNumber : " + value.getNumberOfSamples() + " Offset in timeslot: "+value.getTimeSlotOffset()));
    }

    void printChannelData (int channelNumber) {
        int sampleNumber = channels.get(channelNumber).getNumberOfSamples();
        int offset = fileHeader.getHeaderSize() + 1;
        Float[] signals = binFileIO.readChannelData(sampleNumber, offset);
        for (int i = 0; i < signals.length; i++) {
            System.out.println(i + 1 + ". sample: " + signals[i]);
        }
    }


}
