package javaedflib;

import java.util.stream.IntStream;

class DataReader {
    private BinFile binFile = new BinFile();

    String getFileType(String path) {
        binFile.setPath(path);
        byte[] version = binFile.ReadBytes(0,8);
        String fileType = asciiBytesToString(version);
        if (fileType.equals("0       ")) {
            return "EDF";
        }
        return fileType;
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
