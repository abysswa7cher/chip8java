package abysswatcher;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@ToString
public class CmdLineParser {
    private static final Logger logger = Logger.getInstance();
    @Getter
    private String romPath = "";

    @Getter
    private static final CmdLineParser instance = new CmdLineParser();

    private CmdLineParser() {}

    public static void printHelpMessage() {
        System.out.println("Usage: lcc [options] file...");
        System.out.println("Options:");
        System.out.println("   -h, --help              Display this help information");
        System.out.println("   -r, --romFileName       Set the rom file path to be used");
        System.out.println("   -l, --logLevel          Set the desired log level [NONE = 0, ERROR = 1, WARN = 2, INFO = 3, DEBUG = 4]");
    }

    public void parseCmdLine(String[] args) {
        int i = 0;
        logger.log("args: " + Arrays.toString(args), ELogLevel.INFO);
        while (i < args.length) {
            if (args[i].charAt(0) == '-') {
                if (args[i].equals("-h") || args[i].equals("--help")) {
                    printHelpMessage();
                    break;
                } else if (args[i].equals("-r") || args[i].equals("--romPath")) {
                    i++;
                    romPath = args[i];
                } else if (args[i].equals("-l") || args[i].equals("--logLevel")) {
                    i++;
                    int param = Integer.parseInt(args[i]);
                    if (param > 4 || param < 0) {
                        logger.log("Parameter must be a number [0, 4]", ELogLevel.ERROR);
                        break;
                    }
                    logger.setLogLevel(ELogLevel.values()[param]);
                }
            } else {
                logger.log("Unknown parameter: " + args[i], ELogLevel.ERROR);
            }
            i++;
        }
    }

    public Boolean isRomPathSet() {
        return !(romPath.isEmpty());
    }
}
