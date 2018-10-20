package javaedflib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

class BinFileIO {
    private BinFile inputFile;
    private BinFile outputFile;

    BinFileIO(String path) {
        inputFile = new BinFile(path);
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

    Map<Integer, ChannelHeader> ReadChannelHeaders(int numberOfChannels) throws IOException {
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
            channelHeader.setNumberOfSamples(Long.valueOf(new String(numberOfSamples).trim()));
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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(version);
        outputStream.write(SpaceByteArray(8 - version.length));
        outputStream.write(patientInfo);
        outputStream.write(SpaceByteArray(80 - patientInfo.length));
        outputStream.write(recordingInfo);
        outputStream.write(SpaceByteArray(80 - recordingInfo.length));
        outputStream.write(startDate);
        outputStream.write(SpaceByteArray(8 - startDate.length));
        outputStream.write(startTime);
        outputStream.write(SpaceByteArray(8 - startTime.length));
        outputStream.write(headerSize);
        outputStream.write(SpaceByteArray(8 - headerSize.length));
        outputStream.write(reserved);
        outputStream.write(SpaceByteArray(44 - reserved.length));
        outputStream.write(numberOfDataRecords);
        outputStream.write(SpaceByteArray(8 - numberOfDataRecords.length));
        outputStream.write(durationOfDataRecords);
        outputStream.write(SpaceByteArray(8 - durationOfDataRecords.length));
        outputStream.write(numberOfSignals);
        outputStream.write(SpaceByteArray(4 - numberOfSignals.length));

        byte[] binaryHeader = outputStream.toByteArray( );
        outputFile = new BinFile(path);
        outputFile.WriteBytes(binaryHeader, StandardOpenOption.WRITE);
    }

    /**
     * @param length specifies the byte array length in bytes
     * @return returns a byte array which bytes are all set to be ascii space
     */
    private byte[] SpaceByteArray(int length) {
        byte[] bytes = new byte[length];
        Arrays.fill(bytes, (byte)32);
        return bytes;
    }

    /**
     * @param number the integer to be converted to byte array
     * @param size the size of the required byte array
     * @return returns a byte array with the required length and value
     */
    private byte[] IntByteArray(int number, int size) {
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

    public BinFile getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(BinFile outputFile) {
        this.outputFile = outputFile;
    }
}
