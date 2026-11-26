package hu.levente.prog3.exporter;

import java.time.Instant;
import java.util.Optional;

/**
 * A projekt JSON formátumába történő serializásáért és onnan visszaállításáért felelős konverter.
 *
 * Egyszerre valósítja meg a {@link Exporter} és a {@link Importer} interfaceket, így egységes felületet
 * biztosít a projektfájlok mentésére és visszatöltésére.
 * */
public class ProjectJsonCodec implements Exporter<ProjectSnapshot, String>, Importer<String, ProjectSnapshot> {

    /**
     * A megadott példányt formázott JSON szöveggé alakítja.
     *
     * @return az újonnan generált JSON
     * */
    @Override
    public String export(ProjectSnapshot projectSnapshot) {
        String escapedCode = escapeJson(projectSnapshot.rawCode());
        String escapedName = escapeJson(projectSnapshot.projectName());

        return """
        {
          "projectName": "%s",
          "savedAt": "%s",
          "rawCode": "%s"
        }
        """.formatted(escapedName, projectSnapshot.savedAt().toString(), escapedCode);
    }

    /**
     * Egy JSON karakterláncból visszaállítja {@link ProjectSnapshot} objektumot.
     *
     * Amennyiben a {@code projetName} vagy a {@code savedAt} mező hiányzik, alapértelmezett értéket használ.
     * Ha a {@code rawCode} mező hiányzik, vagy a JSON sérült, kivétel helyett egy {@link Optional#isEmpty()}
     * értékkel tér vissza.
     *
     * @param source a feldolgozandó karakterlánc
     * */
    @Override
    public Optional<ProjectSnapshot> importFrom(String source) {
        if (source == null || source.isBlank()) return Optional.empty();

        try {
            String name = extractField(source, "projectName").orElse("Névtelen projekt");
            String timeStr = extractField(source, "savedAt").orElse(Instant.now().toString());
            String rawCode = extractField(source, "rawCode").orElseThrow();

            return Optional.of(new ProjectSnapshot(unescapeJson(name), unescapeJson(rawCode), Instant.parse(timeStr)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Kinyeri a megadott kulcshoz tartozó szöveges értéket a JSON karakterláncból,
     * figyelmbe véve a karakterlánc belesjében található escapelt idézőjeleket.
     *
     * @param json a keresett JSON szöveg
     * @param key a JSON kulcs neve
     * */
    private Optional<String> extractField(String json, String key) {
        String searchKey = "\"" + key + "\": \"";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1) return Optional.empty();

        int valueStart = startIdx + searchKey.length();
        int valueEnd = valueStart;

        while (valueEnd < json.length()) {
            char c = json.charAt(valueEnd);
            if (c == '"' && json.charAt(valueEnd - 1) != '\\') {
                break;
            }
            valueEnd++;
        }

        if (valueEnd >= json.length()) return Optional.empty();
        return Optional.of(json.substring(valueStart, valueEnd));
    }

    /**
     * A bemeneti szöveget JSON kompatibilis formátummá alakítja escapeléssel
     *
     * @param input nyers szöveg
     * @return az átalakított szöveg
     * */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    /**
     * Visszaállítja a JSON-ben escapelt karaktereket az eredeti formájukra.
     *
     * @param input escapelt JSON mezőérték
     * @return visszaállított karakterlánc
     * */
    private String unescapeJson(String input) {
        return input
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
