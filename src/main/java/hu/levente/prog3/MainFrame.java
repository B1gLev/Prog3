package hu.levente.prog3;

import hu.levente.prog3.concurrent.AsyncSchemaProcessor;
import hu.levente.prog3.exporter.ProjectJsonCodec;
import hu.levente.prog3.exporter.ProjectSnapshot;
import hu.levente.prog3.exporter.SqlDialect;
import hu.levente.prog3.exporter.SqlExporter;
import hu.levente.prog3.factory.RelationalEntityFactory;
import hu.levente.prog3.model.Column;
import hu.levente.prog3.model.Schema;
import hu.levente.prog3.parser.SchemaParser;
import hu.levente.prog3.util.Result;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;

public class MainFrame extends JFrame {
    private final JTextArea codeEditor = new JTextArea();
    private final JLabel statusLabel = new JLabel("Kész");
    private final DiagramCanvas canvas = new DiagramCanvas();
    private final AsyncSchemaProcessor asyncProcessor;
    private final ProjectJsonCodec projectJsonCodec = new ProjectJsonCodec();
    private String currentProjectName = "Projekt";
    private final SchemaParser parser;

    public MainFrame() {
        var factory = new RelationalEntityFactory();
        parser = new SchemaParser(factory);
        this.asyncProcessor = new AsyncSchemaProcessor(parser);

        setTitle("Teszt");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);

        initMenuBar();
        initUI();
        initLiveAsyncEditor();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                asyncProcessor.close();
            }
        });
    }

    /**
     * Felépíti a mentés, betöltés, és SQL exportálás menüpontokat, valamint hozzárendeli a megfelelő eseménykezelőt.
     * */
    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Fájl");
        JMenu exportMenu = new JMenu("Exportálás");

        JMenuItem saveJson = new JMenuItem("Projekt mentése JSON-be");
        saveJson.addActionListener(e -> handleSaveProject());

        JMenuItem loadJson = new JMenuItem("Projekt betöltése JSON-ből");
        loadJson.addActionListener(e -> handleLoadProject());

        JMenuItem exportPg = new JMenuItem("PostgreSQL DDL");
        exportPg.addActionListener(e -> handleSqlExport(SqlDialect.POSTGRESQL));

        JMenuItem exportMySql = new JMenuItem("MySQL DDL");
        exportMySql.addActionListener(e -> handleSqlExport(SqlDialect.MYSQL));

        exportMenu.add(exportPg);
        exportMenu.add(exportMySql);

        fileMenu.add(saveJson);
        fileMenu.add(loadJson);
        fileMenu.addSeparator();
        fileMenu.add(exportMenu);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Feltölti a kódszerkesztőt egy példa adatbázis schemaval.
     * */
    private void initUI() {
        codeEditor.setFont(new Font("Monospaced", Font.PLAIN, 13));
        codeEditor.setTabSize(2);
        codeEditor.setText("""
        Table users {
          id int [pk]
          username varchar
          email varchar
        }

        Table orders {
          id int [pk]
          user_id int
          total decimal
        }

        Ref: orders.user_id > users.id
        """);

        JScrollPane canvasScrollPane = new JScrollPane(canvas);
        canvasScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        canvasScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                canvas,
                new JScrollPane(codeEditor)
        );
        splitPane.setResizeWeight(0.65);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(statusLabel, BorderLayout.SOUTH);
    }

    /**
     * Regisztrálja a kódszerkesztő figyelőjét, ameléy minden szövegváltozásnál frissíti az elemzést.
     * */
    private void initLiveAsyncEditor() {
        codeEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                triggerParse();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                triggerParse();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                triggerParse();
            }
        });
        triggerParse();
    }

    /**
     * Elindítja a szerkesztő aktuálisc tartalmának elemzését, siker esetén frissíti a diargammokat, hiba esetén pedig
     * megjeleníti a szintaktikai vagy szemantikai hibaüzenetet.
     * */
    private void triggerParse() {
        statusLabel.setForeground(new Color(156, 163, 175));
        statusLabel.setText(" Elemzés folyamatban...");

        asyncProcessor.scheduleParsing(
                codeEditor.getText(),
                250,
                schema -> {
                    canvas.updateSchema(schema);
                    statusLabel.setForeground(new Color(34, 197, 94));
                    statusLabel.setText(" Szintaxis helyes. ");
                },
                error -> {
                    statusLabel.setForeground(new Color(239, 68, 68));
                    statusLabel.setText(" " + error.getMessage());
                }
        );
    }

    /**
     * Megjeleníti a fájlmentési ablakot
     * */
    private void handleSaveProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("project.json"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        String currentCode = codeEditor.getText();
        String projectName = file.getName().replace(".json", "");

        asyncProcessor.runIoTask(
                () -> {
                    try {
                        var snapshot = new ProjectSnapshot(projectName, currentCode, Instant.now());
                        String json = projectJsonCodec.export(snapshot);
                        Files.writeString(file.toPath(), json);
                    } catch (Exception e) {
                        throw new RuntimeException("Mentési hiba: " + e.getMessage(), e);
                    }
                },
                () -> {
                    this.currentProjectName = projectName;
                    statusLabel.setText(" Projekt elmentve: " + file.getName());
                    JOptionPane.showMessageDialog(this, "Projekt sikeresen mentve!");
                },
                err -> JOptionPane.showMessageDialog(this, err.getMessage(), "Mentési hiba", JOptionPane.ERROR_MESSAGE)
        );
    }

    /**
     * Megnyitja a fájlválasztó menüt
     * */
    private void handleLoadProject() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File sourceFile = chooser.getSelectedFile();

            asyncProcessor.runIoTask(
                    () -> {
                        try {
                            String jsonContent = Files.readString(sourceFile.toPath());
                            var snapshot = projectJsonCodec.importFrom(jsonContent)
                                    .orElseThrow(() -> new IllegalArgumentException("Érvénytelen vagy sérült JSON fájl."));

                            SwingUtilities.invokeLater(() -> {
                                this.currentProjectName = snapshot.projectName();
                                setTitle(currentProjectName);
                                codeEditor.setText(snapshot.rawCode());
                            });
                        } catch (Exception e) {
                            throw new RuntimeException("Betöltési hiba: " + e.getMessage(), e);
                        }
                    },
                    () -> statusLabel.setText("Projekt betöltve: " + sourceFile.getName()),
                    err -> JOptionPane.showMessageDialog(this, err.getMessage(), "Betöltési hiba", JOptionPane.ERROR_MESSAGE)
            );
        }
    }

    /**
     * A megadott {@link SqlDialect} alapján legenerálja és a fájlba menti az aktuális SQL scriptet.
     * */
    private void handleSqlExport(SqlDialect sqlDialect) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("schema_" + sqlDialect.name().toLowerCase() + ".sql"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File targetFile = chooser.getSelectedFile();
        String currentCode = codeEditor.getText();
        statusLabel.setText(" SQL fájl generálása (" + sqlDialect.getDisplayName() + ")...");

        asyncProcessor.runIoTask(
                () -> {
                    Result<Schema, ?> parseResult = parser.parse(currentCode);
                    if (parseResult instanceof Result.Success<Schema, ?>(Schema value)) {
                        try {
                            String sql = new SqlExporter(sqlDialect).export(value);
                            Files.writeString(targetFile.toPath(), sql);
                        } catch (Exception e) {
                            throw new RuntimeException("Fájlírási hiba: " + e.getMessage(), e);
                        }
                    } else {
                        throw new IllegalStateException("A schmea hibás, javítsd a szintaxist exportálás előtt!");
                    }
                },
                () -> {
                    statusLabel.setText(" " + sqlDialect.getDisplayName() + " SQL sikeresen exportálva: " + targetFile.getName());
                    JOptionPane.showMessageDialog(this, "SQL exportálás sikeres!");
                },
                err -> JOptionPane.showMessageDialog(this, err.getMessage(), "Exportálási hiba", JOptionPane.ERROR_MESSAGE)
        );
    }
}
