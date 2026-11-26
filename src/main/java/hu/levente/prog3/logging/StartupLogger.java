package hu.levente.prog3.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 * Az alkalmazás indítási állapotát és környezeti metaadatait JSON fomrátumban menti.
 * A naplóbejegyezéseket szálbiztos módon hozzáfúzi a fájlhoz, rögzíti az indítási időt, az operációsrendszer és a
 * Java futtatókörnyezetet, a grafikus felület inicializálásnak sikerességét, valamint az esetleges hibaüzeneteket.
 * */
public class StartupLogger {
    private static final Path LOG_PATH = Paths.get("app_startup.log.json");

    public static synchronized void log(boolean guiSuccess, String errorMessage) {
        String logEntry = """
        {
          "timestamp": "%s",
          "os": "%s",
          "javaVersion": "%s",
          "guiInitialized": %b,
          "error": %s
        }
        """.formatted(
                Instant.now(),
                System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")",
                System.getProperty("java.version"),
                guiSuccess,
                errorMessage == null ? "null" : "\"" + errorMessage.replace("\"", "\\\"") + "\""
        );

        try {
            Files.writeString(
                    LOG_PATH,
                    logEntry + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Hiba: " + e.getMessage());
        }
    }
}
