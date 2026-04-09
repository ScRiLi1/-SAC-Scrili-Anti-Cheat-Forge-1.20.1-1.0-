package com.scrili.sac.core.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogManager {

    private static final String LOG_DIR = "logs/sac";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void init() {
        new File(LOG_DIR).mkdirs();
    }

    public static void log(String playerName, String uuid, String action, String reason) {
        String date = LocalDate.now().format(DATE_FMT);
        String time = LocalDateTime.now().format(TIME_FMT);

        String entry = String.format("[%s] [%s] [%s / %s] %s | Причина: %s",
            date, time, playerName, uuid, action, reason);

        System.out.println("[SAC] " + entry);

        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_DIR + "/" + date + ".log", true))) {
            writer.println(entry);
        } catch (IOException e) {
            System.err.println("[SAC] Ошибка записи лога: " + e.getMessage());
        }
    }
}
