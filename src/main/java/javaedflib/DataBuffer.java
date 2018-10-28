package javaedflib;

import jdk.jfr.Timespan;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


class DataBuffer {
    private BinFileIO binFileIO;
    private FileHeader fileHeader;
    private Map<Integer, Channel> channels = new HashMap<>();

    DataBuffer(String path) throws IOException {
        binFileIO = new BinFileIO(path);
        fileHeader = binFileIO.ReadFileHeader();
        InitializeChannels();

/*

*/
    }

    private void InitializeChannels() throws IOException {
        Map<Integer, ChannelHeader> channelHeaders = binFileIO.ReadChannelHeaders(fileHeader.getNumberOfChannels()) ;
        int startbyte=fileHeader.getHeaderSize()+1;
        int offset=startbyte;
        int lenght;
        int byteNum;
        switch (fileHeader.getVersion()) {

            case "0":
                byteNum=2;
                break;

            case "255":
                byteNum=3;
                break;

                default: byteNum=2;
        }



        for (int i = 0; i < channelHeaders.size(); i++) {

            channels.put(i, new Channel(channelHeaders.get(i)));
        }
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
        channels.forEach((key,value)->System.out.println("Channel[" + key + "] label : " + value.getName() + " SampleNumber : " + value.getSampleNumber()));
    }

    void printChannelData (int ChannelNum) {
        Channel C=channels.get(0);
        Float[] f=binFileIO.readChannelData(C.getSampleNumber(),binFileIO.getDataLength(),fileHeader.getHeaderSize()+1);
        for (int i=0; i<f.length; i++) {
            System.out.println(i+".sample: "+f[i].toString());

        }
    }

}
