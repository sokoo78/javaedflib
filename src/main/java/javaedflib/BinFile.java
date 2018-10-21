package javaedflib;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BinFile {
    private String path;

    BinFile(String path) {
        this.path = path;
    }

    ByteBuffer ReadBytes(final long offset, int length) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        try (FileChannel fileChannel = new FileInputStream(path).getChannel().position(offset) ) {
            fileChannel.read(byteBuffer);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        byteBuffer.flip(); // Switch buffer to read only mode
        return byteBuffer;
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
