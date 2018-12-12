package javaedflib;

import java.io.IOException;

public class TestApp {

    public static void main(String[] args) throws IOException {

        // Create object to hold data
        //String inputPath = System.getProperty("user.dir") + "\\src\\main\\test_generator.edf";
       String inputPath = System.getProperty("user.dir") + "\\src\\main\\Newtest17-256.bdf";
        var dataBuffer = new DataBuffer(inputPath);

        // Test header reading
        dataBuffer.PrintFileHeader();
        dataBuffer.PrintChannelLabels();



        // Test signal reading with channel selection
       // dataBuffer.readChannelData(7,0,1);
       // dataBuffer.printChannelData(7);

        // Test signal reading for all channels
        dataBuffer.readAllChannelData(0, 0);
        //dataBuffer.writeAllChannelData();
       dataBuffer.printAllChannelData();

        // Test header writing
        //String outputPath = System.getProperty("user.dir") + "\\src\\main\\newtest_output.bdf";
        //dataBuffer.WriteFileHeader(outputPath);
        //dataBuffer.writeAllChannelData(outputPath);

    }

}
