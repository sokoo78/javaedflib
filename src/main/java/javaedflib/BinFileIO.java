package javaedflib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.IntStream;

class BinFileIO {
    private BinFile inputFile;
    private BinFile outputFile;

    BinFileIO(String path) {
        inputFile = new BinFile(path);
    }

    FileHeader ReadHeader() {
        ByteBuffer binHeader = ByteBuffer.wrap(inputFile.ReadBytes(0, 256));

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
        fileHeader.setNumberOfSignals(Integer.valueOf(new String(numberOfSignals).trim()));

        return fileHeader;
    }

    void WriteHeader(FileHeader fileHeader, String path) throws IOException {

        byte[] version = fileHeader.getVersion().getBytes();
        byte[] patientInfo = fileHeader.getPatientInfo().getBytes();
        byte[] recordingInfo = fileHeader.getRecordingInfo().getBytes();
        byte[] startDate = fileHeader.getStartDate().getBytes();
        byte[] startTime = fileHeader.getStartTime().getBytes();
        byte[] headerSize = Integer.toString(fileHeader.getHeaderSize()).getBytes();
        byte[] reserved = fileHeader.getReserved().getBytes();
        byte[] numberOfDataRecords = Integer.toString(fileHeader.getNumberOfDataRecords()).getBytes();
        byte[] durationOfDataRecords = Long.toString(fileHeader.getDurationOfDataRecords()).getBytes();
        byte[] numberOfSignals = Integer.toString(fileHeader.getNumberOfSignals()).getBytes();

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

    private byte[] SpaceByteArray(int length) {
        byte[] bytes = new byte[length];
        Arrays.fill(bytes, (byte)32);
        return bytes;
    }

    private byte[] IntByteArray(int number, int size) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.putInt(number);
        return byteBuffer.array();
    }

    String getFileType(String path) {
        inputFile.setPath(path);
        byte[] version = inputFile.ReadBytes(0,8);
        return asciiBytesToString(version);
    }

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
