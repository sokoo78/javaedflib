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

    void readChannelData (int channelNumber, int startTimeSlot, int endTimeSlot) {
        /*
        *
        * */

        int sampleNumber = channels.get(channelNumber).getNumberOfSamples();

        if (endTimeSlot == 0 || endTimeSlot > fileHeader.getNumberOfDataRecords())
            endTimeSlot = fileHeader.getNumberOfDataRecords();

        int timeFrame = endTimeSlot - startTimeSlot;
        int offset = fileHeader.getHeaderSize() + getChannelOffsetInTimeSlot(channelNumber)
                + (startTimeSlot * channels.get(channelNumber).getTimeSlotOffset());
        float[] signals = binFileIO.readChannelData(sampleNumber, offset, timeFrame);
        channels.get(channelNumber).setSignals(signals);
    }

    private int getChannelOffsetInTimeSlot(int channelNumber) {
        return channels.get(channelNumber).getTimeSlotOffset();
    }

    void printChannelData (int channelNumber) {
        var signals = channels.get(channelNumber).getSignals();
        for (int signal = 0; signal < signals.length; signal++) {
            System.out.printf("%d. digital sample: %s | physical sample: %s%n", signal, signals[signal],
                    channels.get(channelNumber).sampleFromDigitalToPhysical(signals[signal]),
                    channels.get(channelNumber).getChannelHeader().getPhysicalDimension());
        }
    }

    void readAllChannelData (int startTimeSlot, int endTimeSlot) {
        int i;
        int timeFrameDataOffset;
        int[] channelPos = new int[channels.size()];
        int offset = fileHeader.getHeaderSize() + (startTimeSlot * binFileIO.getTimeSlotSize());
        if (endTimeSlot == 0 || endTimeSlot > fileHeader.getNumberOfDataRecords())
            endTimeSlot = fileHeader.getNumberOfDataRecords();
        int length = (endTimeSlot-startTimeSlot)*binFileIO.getTimeSlotSize();
        float[] timeFrameData=binFileIO.readTimeFrame(offset,length);
        timeFrameDataOffset=0;
        for (int timeSlot=0; timeSlot<endTimeSlot-startTimeSlot;timeSlot++) {
            for (int channel = 0; channel < channels.size(); channel++) {
            i=0;
            float[] channelData=new float[channels.get(channel).getNumberOfSamples()];
            while (i<channelData.length){

            channelData[i]=timeFrameData[timeFrameDataOffset];
                i++;
                timeFrameDataOffset++;
            }
            if (channels.get(channel).getSignals()==null) {
                channels.get(channel).setSignals(channelData);
            } else {
                channels.get(channel).addSignals(channelData);
            }
            }
        }
    }

    void writeAllChannelData(String path) throws  IOException {
        try {
            float[] dataToWrite;
            int dataLength = 0;
            int position = 0;
            int timeSlot;
            for (int i = 0; i < channels.size(); i++) {
                dataLength += channels.get(i).getNumberOfSamples();
            }
            dataLength = dataLength * fileHeader.getNumberOfDataRecords() * binFileIO.getSignalDataLength();
            //dataToWrite = new float[dataLength/2];
            dataToWrite = new float[dataLength];
            int[] channelPosition=new int[channels.size()];
            for (int cp=0; cp < channelPosition.length; cp++) {
                channelPosition[cp]=0;
            }

            for (int datarecords=0; datarecords<fileHeader.getNumberOfDataRecords();datarecords++) { //datarecord
                for (int channel = 0; channel < channels.size(); channel++) {                       //channel
                    var signals = channels.get(channel).getSignalsTimeFrame(channelPosition[channel],channels.get(channel).getNumberOfSamples());
                    for (int signal = 0; signal < signals.length; signal++) {
                        //channels.get(channel).sampleFromDigitalToPhysical(signals[signal])
                        dataToWrite[position] = signals[signal];
                        position++;
                    }
                    channelPosition[channel]+=channels.get(channel).getNumberOfSamples();
                }

            }

            binFileIO.writeAllChannelData(dataToWrite, path);
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }


    void printAllChannelData () {
        byte[] bytes=new byte[binFileIO.getSignalDataLength()];
        for (int channel = 0; channel < channels.size(); channel++) {
            var signals = channels.get(channel).getSignals();
            for (int signal = 0; signal < signals.length; signal++) {
                bytes=binFileIO.digitalSignalToBytes(channels.get(channel).sampleFromPhysicalToDigital(channels.get(channel).sampleFromDigitalToPhysical(signals[signal])),binFileIO.getSignalDataLength());
                System.out.printf("%d. channel | %d. digital sample: %s | physical sample: %s %s | converted digital sample: %s| ", channel, signal,
                        signals[signal], channels.get(channel).sampleFromDigitalToPhysical(signals[signal]),
                        channels.get(channel).getChannelHeader().getPhysicalDimension(),
                        channels.get(channel).sampleFromPhysicalToDigital((channels.get(channel).sampleFromDigitalToPhysical(signals[signal]))) );
                System.out.printf("Bytes: ");
                for (int b=0; b<binFileIO.getSignalDataLength();b++) {
                    System.out.printf("%d. byte : %d | ",b,bytes[b]);


                }
System.out.printf("%n");


                }



        }
    }

}
