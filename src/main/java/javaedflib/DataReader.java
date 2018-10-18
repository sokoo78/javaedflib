package javaedflib;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

class DataReader {
    private BinFile binFile;

    DataReader(String path) {
        binFile = new BinFile(path);
    }

    FileHeader GetHeader() {
        ByteBuffer binHeader = ByteBuffer.wrap(binFile.ReadBytes(0, 256));

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

    private String asciiBytesToString( byte[] bytes )
    {
        if ( (bytes == null) || (bytes.length == 0 ) )
            return "";

        char[] ascii = new char[bytes.length];
        IntStream.range(0, bytes.length).forEach(i -> ascii[i] = (char) bytes[i]);

        return new String(ascii);
    }
}
