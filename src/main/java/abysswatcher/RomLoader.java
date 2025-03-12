package abysswatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class RomLoader {
    public RomLoader() {
    }

    private static final Logger logger = Logger.getInstance();

    public void loadRom(String path, int[] mem, int addr) throws IOException {
        logger.log("Loading ROM: " + path, ELogLevel.INFO);

        File rom = new File(path);
        int[] data = new int[(int) rom.length()];
        try (InputStream is = new FileInputStream(rom)) {
            int i = 0;
            while (is.available() > 0) {
                data[i] = is.readNBytes(1)[0] & 0xFF;
                i++;
            }
        }

        if (data.length == 0) {
            logger.log("Failed to load ROM: " + path, ELogLevel.ERROR);
            throw new RuntimeException();
        }


        logger.log("ROM loaded successfully. Size: " + data.length + " bytes.", ELogLevel.INFO);
        logger.log("Data:", ELogLevel.DEBUG);
        logger.log(Arrays.toString(data), ELogLevel.DEBUG);

        System.arraycopy(data, 0, mem, addr, data.length);
    }

}
