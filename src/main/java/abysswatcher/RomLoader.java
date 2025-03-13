package abysswatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class RomLoader {
    public RomLoader() {
    }

    private static final PrintLogger PRINT_LOGGER = PrintLogger.getInstance();

    public int loadRom(String path, int[] mem, int addr) throws IOException {
        PRINT_LOGGER.log("Loading ROM: " + path, ELogLevel.INFO);

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
            PRINT_LOGGER.log("Failed to load ROM: " + path, ELogLevel.ERROR);
            throw new RuntimeException();
        }


        PRINT_LOGGER.log(
                "ROM loaded successfully. Size: " + data.length + " bytes.", ELogLevel.INFO);
        PRINT_LOGGER.log("Data:", ELogLevel.DEBUG);
        PRINT_LOGGER.log(Arrays.toString(data), ELogLevel.DEBUG);

        System.arraycopy(data, 0, mem, addr, data.length);
        return data.length;
    }

}
