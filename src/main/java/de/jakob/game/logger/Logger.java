package de.jakob.game.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";

    private static String getTimestamp() {
        return LocalDateTime.now().format(FORMAT);
    }

    public static void info(String message) {
        System.out.println("[" + getTimestamp() + "] [INFO] " + message);
    }

    public static void warn(String message) {
        System.out.println(YELLOW + "[" + getTimestamp() + "] [WARN] " + message + RESET);
    }

    public static void error(String message) {
        System.err.println(RED + "[" + getTimestamp() + "] [ERROR] " + message + RESET);
    }
}