package abysswatcher;

public class Main {
    private long window;

    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        CmdLineParser cmdParser = CmdLineParser.getInstance();
        cmdParser.parseCmdLine(args);

        if (!cmdParser.isRomPathSet()) {
            logger.log("No ROM path provided", ELogLevel.ERROR);
            System.exit(1);
        }

        logger.log("ROM path: " + cmdParser.getRomPath(), ELogLevel.INFO);

        Chip8 emulator = Chip8.getInstance();
        emulator.init(cmdParser.getRomPath());
        emulator.run();
        emulator.deinit();
    }
}