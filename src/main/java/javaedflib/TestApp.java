package javaedflib;

import java.io.IOException;

public class TestApp {

    public static void main(String[] args) throws IOException {

        // Create class to hold data
         String inputPath = System.getProperty("user.dir") + "\\src\\main\\test_generator.edf";
        var dataBuffer = new DataBuffer(inputPath);

        // Test header reading
        dataBuffer.PrintHeader();

        // Test header writing
        String outputPath = System.getProperty("user.dir") + "\\src\\main\\test_output.edf";
        dataBuffer.WriteHeader(outputPath);
        dataBuffer.PrintChannelLabels();
    }
}
