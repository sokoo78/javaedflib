package javaedflib;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BinFile {
    private String path;

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

    void WriteBytes(byte[] bytes, StandardOpenOption mode) {
        try {
            if (!Files.exists(Paths.get(path)))
                Files.createFile(Paths.get(path));
            Files.write(Paths.get(path), bytes, mode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

}
