package javaedflib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.IntStream;

class BinFileIO {
    private BinFile inputFile;
    private BinFile outputFile;
    private String fileType;
    private int signalByteSize;
    private int timeSlotSize;

    BinFileIO(String path) {
        inputFile = new BinFile(path);
        setFileType();
        setSignalByteSize();
    }

    FileHeader ReadFileHeader() {

        ByteBuffer binHeader = inputFile.ReadBytes(0, 256);

        byte[] version = new byte[8];
        byte[] patientInfo = new byte[80];
        byte[] recordingInfo = new byte[80];
        byte[] startDate = new byte[8];
        byte[] startTime = new byte[8];
        byte[] headerSize = new byte[8];
        byte[] reserved = new byte[44];
        byte[] numberOfDataRecords = new byte[8];
        byte[] durationOfDataRecords = new byte[8];
        byte[] numberOfSignals = new byte[4];

        binHeader.get(version);
        binHeader.get(patientInfo);
        binHeader.get(recordingInfo);
        binHeader.get(startDate);
        binHeader.get(startTime);
        binHeader.get(headerSize);
        binHeader.get(reserved);
        binHeader.get(numberOfDataRecords);
        binHeader.get(durationOfDataRecords);
        binHeader.get(numberOfSignals);

        FileHeader fileHeader = new FileHeader();
        fileHeader.setVersion(new String(version).trim());
        fileHeader.setPatientInfo(new String(patientInfo).trim());
        fileHeader.setRecordingInfo(new String(recordingInfo).trim());
        fileHeader.setStartDate(new String(startDate).trim());
        fileHeader.setStartTime(new String(startTime).trim());
        fileHeader.setHeaderSize(Integer.valueOf(new String(headerSize).trim()));
        fileHeader.setReserved(new String(reserved).trim());
        fileHeader.setNumberOfDataRecords(Integer.valueOf(new String(numberOfDataRecords).trim()));
        fileHeader.setDurationOfDataRecords(Long.valueOf(new String(durationOfDataRecords).trim()));
        fileHeader.setNumberOfChannels(Integer.valueOf(new String(numberOfSignals).trim()));

        return fileHeader;
    }

    Map<Integer, ChannelHeader> ReadChannelHeaders(int numberOfChannels) {
        Map<Integer, ChannelHeader> channelHeaders = new HashMap<>();
        ByteBuffer buffer = inputFile.ReadBytes(256, 256 * numberOfChannels);

        for (int channelIndex = 0;  channelIndex < numberOfChannels; channelIndex++) {

            int position = 0;

            byte[] label = new byte[16];
            byte[] transducerType = new byte[80];
            byte[] physicalDimension = new byte[8];
            byte[] physicalMinimum = new byte[8];
            byte[] physicalMaximum = new byte[8];
            byte[] digitalMinimum = new byte[8];
            byte[] digitalMaximum = new byte[8];
            byte[] preFilteringInfo = new byte[80];
            byte[] numberOfSamples = new byte[8];
            byte[] reserved = new byte[32];

            buffer.position(position + channelIndex * 16);
            buffer.get(label);
            position += numberOfChannels * 16;

            buffer.position(position + channelIndex * 80);
            buffer.get(transducerType);
            position += numberOfChannels * 80;

            buffer.position(position + channelIndex * 8);
            buffer.get(physicalDimension);
            position += numberOfChannels * 8;

            buffer.position(position + channelIndex * 8);
            buffer.get(physicalMinimum);
            position += numberOfChannels * 8;

            buffer.position(position + channelIndex * 8);
            buffer.get(physicalMaximum);
            position += numberOfChannels * 8;

            buffer.position(position + channelIndex * 8);
            buffer.get(digitalMinimum);
            position += numberOfChannels * 8;

            buffer.position(position + channelIndex * 8);
            buffer.get(digitalMaximum);
            position += numberOfChannels * 8;

            buffer.position(position + channelIndex * 80);
            buffer.get(preFilteringInfo);
            position += numberOfChannels * 80;

            buffer.position(position + channelIndex * 8);
            buffer.get(numberOfSamples);
            position += numberOfChannels * 8;

            buffer.position(position + channelIndex * 32);
            buffer.get(reserved);

            ChannelHeader channelHeader = new ChannelHeader();
            channelHeader.setLabelOfChannel(new String(label).trim());
            channelHeader.setTransducerType(new String(transducerType).trim());
            channelHeader.setPhysicalDimension(new String(physicalDimension).trim());
            channelHeader.setPhysicalMinimum(Double.valueOf(new String(physicalMinimum).trim()));
            channelHeader.setPhysicalMaximum(Double.valueOf(new String(physicalMaximum).trim()));
            channelHeader.setDigitalMinimum(Long.valueOf(new String(digitalMinimum).trim()).intValue());
            channelHeader.setDigitalMaximum(Long.valueOf(new String(digitalMaximum).trim()).intValue());
            channelHeader.setPreFilteringInfo(new String(preFilteringInfo).trim());
            channelHeader.setNumberOfSamples(Integer.valueOf(new String(numberOfSamples).trim()));
            channelHeader.setReserved(new String(reserved).trim());
            channelHeaders.put(channelIndex,channelHeader);
            buffer.rewind();
        }
        return channelHeaders;
    }

    void WriteFileHeader(FileHeader fileHeader, String path) throws IOException {

        byte[] version = fileHeader.getVersion().getBytes();
        byte[] patientInfo = fileHeader.getPatientInfo().getBytes();
        byte[] recordingInfo = fileHeader.getRecordingInfo().getBytes();
        byte[] startDate = fileHeader.getStartDate().getBytes();
        byte[] startTime = fileHeader.getStartTime().getBytes();
        byte[] headerSize = Integer.toString(fileHeader.getHeaderSize()).getBytes();
        byte[] reserved = fileHeader.getReserved().getBytes();
        byte[] numberOfDataRecords = Integer.toString(fileHeader.getNumberOfDataRecords()).getBytes();
        byte[] durationOfDataRecords = Long.toString(fileHeader.getDurationOfDataRecords()).getBytes();
        byte[] numberOfSignals = Integer.toString(fileHeader.getNumberOfChannels()).getBytes();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(version);
        outputStream.write(CreateSpaceByteArray(8 - version.length)); // Fill up remaining bytes with spaces
        outputStream.write(patientInfo);
        outputStream.write(CreateSpaceByteArray(80 - patientInfo.length));
        outputStream.write(recordingInfo);
        outputStream.write(CreateSpaceByteArray(80 - recordingInfo.length));
        outputStream.write(startDate);
        outputStream.write(CreateSpaceByteArray(8 - startDate.length));
        outputStream.write(startTime);
        outputStream.write(CreateSpaceByteArray(8 - startTime.length));
        outputStream.write(headerSize);
        outputStream.write(CreateSpaceByteArray(8 - headerSize.length));
        outputStream.write(reserved);
        outputStream.write(CreateSpaceByteArray(44 - reserved.length));
        outputStream.write(numberOfDataRecords);
        outputStream.write(CreateSpaceByteArray(8 - numberOfDataRecords.length));
        outputStream.write(durationOfDataRecords);
        outputStream.write(CreateSpaceByteArray(8 - durationOfDataRecords.length));
        outputStream.write(numberOfSignals);
        outputStream.write(CreateSpaceByteArray(4 - numberOfSignals.length));

        byte[] binaryHeader = outputStream.toByteArray();
        outputFile = new BinFile(path);
        outputFile.WriteBytes(binaryHeader, StandardOpenOption.WRITE);
    }

    void WriteChannelHeaders(Map<Integer, ChannelHeader> channelHeaders, String path) throws IOException {

        DecimalFormat formatter = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        formatter.setMaximumFractionDigits(340);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int channelIndex = 0;  channelIndex < channelHeaders.size(); channelIndex++) {
            byte[] label = channelHeaders.get(channelIndex).getLabelOfChannel().getBytes();
            outputStream.write(label);
            outputStream.write(CreateSpaceByteArray(16 - label.length)); // Fill up remaining bytes with spaces
        }
        for (int channelIndex = 0;  channelIndex < channelHeaders.size(); channelIndex++) {
            byte[] transducerType = channelHeaders.get(channelIndex).getTransducerType().getBytes();
            outputStream.write(transducerType);
            outputStream.write(CreateSpaceByteArray(80 - transducerType.length));
        }
        for (int channelIndex = 0;  channelIndex < channelHeaders.size(); channelIndex++) {
            byte[] physicalDimension = channelHeaders.get(channelIndex).getPhysicalDimension().getBytes();
            outputStream.write(physicalDimension);
            outputStream.write(CreateSpaceByteArray(8 - physicalDimension.length));
        }
        for (int channelIndex = 0;  channelIndex < channelHeaders.size(); channelIndex++) {
            byte[] physicalMinimum = formatter.format(channelHeaders.get(channelIndex).getPhysicalMinimum()).getBytes();
            outputStream.write(physicalMinimum);
            outputStream.write(CreateSpaceByteArray(8 - physicalMinimum.length));
        }
        for (int channelIndex = 0;  channelIndex < channelHeaders.size(); channelIndex++) {
            byte[] physicalMaximum = formatter.format(channelHeaders.get(channelIndex).getPhysicalMaximum()).getBytes();
            outputStream.write(physicalMaximum);
            outputStream.write(CreateSpaceByteArray(8 - physicalMaximum.length));
        }
        for (int channelIndex = 0;  channelIndex < channelHeaders.size(); channelIndex++) {
            byte[] digitalMinimum = Integer.toString(channelHeaders.get(channelIndex).getDigitalMinimum()).getBytes();
            outputStream.write(digitalMinimum);
            outputStream.write(CreateSpaceByteArray(8 - digitalMinimum.length));
        }
        for (int channelIndex = 0;  channelIndex < channelHeaders.size(); channelIndex++) {
            byte[] digitalMaximum = Integer.toString(channelHeaders.get(channelIndex).getDigitalMaximum()).getBytes();
            outputStream.write(digitalMaximum);
            outputStream.write(CreateSpaceByteArray(8 - digitalMaximum.length));
        }
        for (int channelIndex = 0;  channelIndex < channelHeaders.size(); channelIndex++) {
            byte[] preFilteringInfo = channelHeaders.get(channelIndex).getPreFilteringInfo().getBytes();
            outputStream.write(preFilteringInfo);
            outputStream.write(CreateSpaceByteArray(80 - preFilteringInfo.length));
        }
        for (int channelIndex = 0;  channelIndex < channelHeaders.size(); channelIndex++) {
            byte[] numberOfSamples = Long.toString(channelHeaders.get(channelIndex).getNumberOfSamples()).getBytes();
            outputStream.write(numberOfSamples);
            outputStream.write(CreateSpaceByteArray(8 - numberOfSamples.length));
        }
        for (int channelIndex = 0;  channelIndex < channelHeaders.size(); channelIndex++) {
            byte[] reserved = channelHeaders.get(channelIndex).getReserved().getBytes();
            outputStream.write(reserved);
            outputStream.write(CreateSpaceByteArray(32 - reserved.length));
        }

        // TODO: data lengths to be checked and rounded as necessary
        byte[] binaryHeaders = outputStream.toByteArray();
        outputFile = new BinFile(path);
        outputFile.WriteBytes(binaryHeaders, StandardOpenOption.APPEND);
    }

    float[] readChannelData(int sampleNumber, int offset, int timeFrame)  {
        float value;
        byte[] bytes = new byte[signalByteSize];
        int length = sampleNumber * signalByteSize;
        ByteBuffer sampleBytes = ByteBuffer.allocate(length);
        sampleBytes.order(ByteOrder.LITTLE_ENDIAN);
        float[] values = new float[sampleNumber * timeFrame];
        int signalArraySize = 0;
        for (int i = 0; i < timeFrame; i++) {
            sampleBytes = inputFile.ReadBytes(offset, length);
            for (int j = 0; j < sampleNumber; j++) {
                sampleBytes.get(bytes);
                value = getSignalValue(bytes);
                values[signalArraySize] = value;
                signalArraySize++;
            }
            offset += timeSlotSize;
            sampleBytes.rewind();
        }
        sampleBytes.clear();
        return values;
    }

    float[] readTimeFrame(int offset, int length)  {
        byte[] bytes = new byte[signalByteSize];
        int signalArraySize=0;
        ByteBuffer timeFrameBytes = ByteBuffer.allocate(length);
        timeFrameBytes.order(ByteOrder.LITTLE_ENDIAN);
        float[] values = new float[length/2];
        timeFrameBytes=inputFile.ReadBytes(offset,length);
        for (int i=0; i<values.length; i++) {
            timeFrameBytes.get(bytes);
            float value = getSignalValue(bytes);
            values[signalArraySize]=value;
            signalArraySize++;
        }
        return values;
    }

    private float getSignalValue(byte[] bytes) {
        float value;
        switch(signalByteSize) {
            case 2: // EDF format
                value = (bytes[0] & 255) | ((bytes[1] & 255) << 8);
                break;
            case 3: // BDF format
                value = (bytes[0] & 255) | ((bytes[1] & 255) << 8) | ((bytes[1] & 255) << 16);
                break;
            default:
                value = (bytes[0] & 255) | ((bytes[1] & 255) << 8);
                break;
        }
        return value;
    }

    void setTimeSlotSize(int timeSlotSize) {
        this.timeSlotSize = timeSlotSize;
    }

    int getTimeSlotSize() {
        return this.timeSlotSize;
    }

    /**
     * @param length specifies the byte array length in bytes
     * @return returns a byte array which bytes are all set to be ascii space
     */
    private byte[] CreateSpaceByteArray(int length) {
        byte[] bytes = new byte[length];
        Arrays.fill(bytes, (byte)32);
        return bytes;
    }

    /**
     * @param number the integer to be converted to byte array
     * @param size the size of the required byte array
     * @return returns a byte array with the required length and value
     */
    private byte[] CreateIntByteArray(int number, int size) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.putInt(number);
        return byteBuffer.array();
    }

    /**
     * @param bytes byte array with ascii content inside
     * @return returns the decoded text as String
     */
    private String asciiBytesToString( byte[] bytes )
    {
        if ( (bytes == null) || (bytes.length == 0 ) )
            return "";

        char[] ascii = new char[bytes.length];
        IntStream.range(0, bytes.length).forEach(i -> ascii[i] = (char) bytes[i]);

        return new String(ascii);
    }

    BinFile getOutputFile() {
        return outputFile;
    }

    void setOutputFile(BinFile outputFile) {
        this.outputFile = outputFile;
    }

    private Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    String getFileType() {
        return fileType;
    }

    private void setFileType() {
        fileType = getFileExtension(inputFile.getPath()).toString().toUpperCase();
    }

    int getSignalDataLength() {
        return signalByteSize;
    }

    private void setSignalByteSize() {
        switch (fileType) {
            case "EDF" : signalByteSize = 2;
                break;
            case "BDF" : signalByteSize = 3;
                break;
            default : signalByteSize = 2;
        }
    }
}
