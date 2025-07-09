package abysswatcher;

import lombok.Getter;
import lombok.Setter;

@Setter
public class PrintLogger extends ILogger {
    private ELogLevel logLevel = ELogLevel.DEBUG;
    @Getter
    private static final PrintLogger instance = new PrintLogger();

    private PrintLogger() {}

    public void log(String message, ELogLevel logLevel) {
        if (logLevel.compareTo(this.logLevel) <= 0) {
            StringBuilder sb = new StringBuilder();
            switch (logLevel) {
                case ERROR -> sb.append("\u001B[31m[ERROR] ");
                case WARN -> sb.append("\u001B[33m[WARN] ");
                case INFO -> sb.append("\u001B[34m[INFO] ");
                case DEBUG -> sb.append("\u001B[0m[DEBUG] ");
            }
            sb.append(message).append("\u001B[0m");
            System.out.println(sb);
        }
    }
}
