package abysswatcher;

import lombok.Getter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteLogger extends ILogger {

    private final File dumpFile = new File("./dump.txt");

    @Getter
    private static final WriteLogger instance = new WriteLogger();

    private WriteLogger() {}

    public void log(String message) {
        try (FileWriter out = new FileWriter(dumpFile, true)) {
            out.write(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
