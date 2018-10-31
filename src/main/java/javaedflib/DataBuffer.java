package javaedflib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class DataBuffer {
    private BinFileIO binFileIO;
    private FileHeader fileHeader;
    private Map<Integer, Channel> channels = new HashMap<>();


    DataBuffer(String path) {
        binFileIO = new BinFileIO(path);
        fileHeader = binFileIO.ReadFileHeader();
        InitializeChannels();
    }

    private void InitializeChannels() {
        Map<Integer, ChannelHeader> channelHeaders = binFileIO.ReadChannelHeaders(fileHeader.getNumberOfChannels());
        int timeSlotOffset = 0;
        for (int channelNumber = 0; channelNumber < channelHeaders.size(); channelNumber++) {
            channels.put(channelNumber, new Channel(channelHeaders.get(channelNumber), timeSlotOffset));
            timeSlotOffset += channelHeaders.get(channelNumber).getNumberOfSamples() * binFileIO.getSignalDataLength();
        }
        binFileIO.setTimeSlotSize(timeSlotOffset);
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

    private String GetFileVersion() {
        var version = fileHeader.getVersion();
        if (version.equals("0"))
            return "EDF";
        if (version.equals("255"))
            return "BDF";
        return "Unknown";
    }

    void WriteFileHeader(String path) throws IOException {
        binFileIO.WriteFileHeader(fileHeader, path);

        Map<Integer, ChannelHeader> channelHeaders = new HashMap<>();
        channels.forEach((channelNumber, channel) -> channelHeaders.put(channelNumber, channel.getChannelHeader()));
        binFileIO.WriteChannelHeaders(channelHeaders, path);
    }

    void PrintChannelLabels() {
        channels.forEach((channelNumber, channel) -> System.out.println(new StringBuilder()
                .append("Channel[").append(channelNumber).append("] label : ")
                .append(channel.getName()).append(" SampleNumber : ").append(channel.getNumberOfSamples())
                .append(" Offset in time slot: ").append(channel.getTimeSlotOffset()).toString()));
    }

    private int getChannelOffsetInTimeSlot(int channelNumber) {
        int offset = 0;

            offset = channels.get(channelNumber).getTimeSlotOffset();

        return offset;
    }

    void readChannelData (int channelNumber, int startTimeSlot, int endTimeSlot) {
        int sampleNumber = channels.get(channelNumber).getNumberOfSamples();
        if (endTimeSlot == 0 || endTimeSlot > fileHeader.getNumberOfDataRecords())
            endTimeSlot = fileHeader.getNumberOfDataRecords();

        int timeFrame = endTimeSlot - startTimeSlot;
        int offset = fileHeader.getHeaderSize() + getChannelOffsetInTimeSlot(channelNumber)
                + (startTimeSlot * channels.get(channelNumber).getTimeSlotOffset());

      //  System.out.println("Sample Number: "+sampleNumber+" Offset: "+offset);
        float[] signals = binFileIO.readChannelData(sampleNumber, offset, timeFrame);
        channels.get(channelNumber).setSignals(signals);
    }

    void printChannelData (int channelNumber) {
        var signals = channels.get(channelNumber).getSignals();
        for (int signal = 0; signal < signals.length; signal++) {
            System.out.printf("%d. sample: %s%n", signal, signals[signal]);
        }
    }


    void readAllChannelData (int startTimeSlot, int endTimeSlot) {
        for (int channel = 0; channel < channels.size(); channel++)
        {
           // if (channel == 5) continue;
            readChannelData(channel, startTimeSlot, endTimeSlot);}

    }

    void printAllChannelData () {
        for (int channel = 0; channel < channels.size(); channel++) {
            var signals = channels.get(channel).getSignals();
            for (int signal = 0; signal < signals.length; signal++) {
                System.out.printf("%d. channel | %d. sample: %s%n", channel, signal, signals[signal]);
            }
        }
    }
}
