package abysswatcher;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        PrintLogger printLogger = PrintLogger.getInstance();
        CmdLineParser cmdParser = CmdLineParser.getInstance();
        cmdParser.parseCmdLine(args);

        if (!cmdParser.isRomPathSet()) {
            printLogger.log("No ROM path provided", ELogLevel.ERROR);
            System.exit(1);
        }

        printLogger.log("ROM path: " + cmdParser.getRomPath(), ELogLevel.INFO);

        Chip8 emulator = Chip8.getInstance();
        emulator.init(cmdParser.getRomPath());
        emulator.run();
        emulator.deinit();
    }
}