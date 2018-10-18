package javaedflib;

public class Signals {
    private static DataReader dataReader = new DataReader();

    public static void main(String[] args) {
        String path = System.getProperty("user.dir") + "\\src\\main\\test_generator.edf";
        String format = dataReader.getFileType(path);
        System.out.println("File format: " + format);
    }


}
