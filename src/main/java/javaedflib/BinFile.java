package javaedflib;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

class BinFile {
    private String path;

    BinFile(String path) {
        this.path = path;
    }

    ByteBuffer ReadBytes(final long offset, int length) {
        /*
        * Reads bytes form file into a ByteBuffer.
        * @param offset start position from beginning of the file
        * @param length byte number to be read from start position
        * @return returns byte buffer with data bytes
        * */
        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        try (FileChannel fileChannel = new FileInputStream(path).getChannel().position(offset)) {
            fileChannel.read(byteBuffer);
            byteBuffer.flip(); // Switch buffer to read only mode
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return byteBuffer;
    }

    void WriteBytes(byte[] bytes, StandardOpenOption mode) {
        /*
        * Write bytes to a file.
        * @param bytes byte array to be write
        * @param mode open option (write, append, etc...)
        * */

        try {
            if (!Files.exists(Paths.get(path)))
                Files.createFile(Paths.get(path));
            Files.write(Paths.get(path), bytes, mode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getPath() {
        return path;
    }

}
