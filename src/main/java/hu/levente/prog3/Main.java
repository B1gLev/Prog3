package hu.levente.prog3;

import hu.levente.prog3.logging.StartupLogger;

import javax.swing.*;

public final class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new MainFrame().setVisible(true);

                StartupLogger.log(true, null);
            } catch (Exception e) {
                StartupLogger.log(true, e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
