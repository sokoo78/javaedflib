package javaedflib;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class BinFile {
    private java.lang.String path;

    BinFile(String path) {
        this.path = path;
    }

    byte[] ReadBytes(int offset, int length) {
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

    public java.lang.String getPath() {
        return path;
    }

    void setPath(java.lang.String path) {
        this.path = path;
    }
}
