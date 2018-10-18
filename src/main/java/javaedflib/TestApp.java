package javaedflib;

import java.io.IOException;

public class TestApp {
    private static DataInterface dataInterface;

    public static void main(String[] args) throws IOException {
        String path = System.getProperty("user.dir") + "\\src\\main\\test_generator.edf";
        dataInterface = new DataInterface(path);
        dataInterface.PrintHeader();
    }
}
