package server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLogger {
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String message) {
        System.out.println("[" + LocalDateTime.now().format(fmt) + "] " + message);
    }

    public static void error(String message, Exception e) {
        System.err.println("[" + LocalDateTime.now().format(fmt) + "] ERROR: " + message);
        if (e != null) e.printStackTrace();
    }
}
