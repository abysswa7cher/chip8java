package abysswatcher;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        CmdLineParser cmdParser = CmdLineParser.getInstance();
        cmdParser.parseCmdLine(args);

        if (!cmdParser.isRomPathSet()) {
            logger.log("No ROM path provided", ELogLevel.ERROR);
            System.exit(1);
        }

        logger.log("ROM path: " + cmdParser.getRomPath(), ELogLevel.INFO);
        logger.log(cmdParser.toString(), ELogLevel.INFO);
    }
}