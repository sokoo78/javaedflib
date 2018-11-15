package javaedflib;

import java.io.IOException;

public class TestApp {

    public static void main(String[] args) throws IOException {

        // Create class to hold data
        String inputPath = System.getProperty("user.dir") + "\\src\\main\\test_generator.edf";
       // String inputPath = System.getProperty("user.dir") + "\\src\\main\\Newtest17-256.bdf";
        var dataBuffer = new DataBuffer(inputPath);

        // Test header reading
        dataBuffer.PrintFileHeader();
        dataBuffer.PrintChannelLabels();

        // Test header writing
        //String outputPath = System.getProperty("user.dir") + "\\src\\main\\test_output.edf";
        //dataBuffer.WriteFileHeader(outputPath);

        // Test signal reading with channel selection
        dataBuffer.readChannelData(7,0,1);
        dataBuffer.printChannelData(7);

        // Test signal reading for all channels
     //  dataBuffer.readAllChannelData(0, 0);
      //  dataBuffer.printAllChannelData();
    }
}
