package javaedflib;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class BinFile {
    private String path;

    public byte[] ReadBytes (int offset, int length) {
        byte[] bytes = new byte[length];
        try (FileInputStream fis = new FileInputStream(path);
             BufferedInputStream bis = new BufferedInputStream(fis)
        ) {
            bis.read(bytes, offset, length);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
