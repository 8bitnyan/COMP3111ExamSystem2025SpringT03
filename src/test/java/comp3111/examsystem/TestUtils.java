package comp3111.examsystem;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class TestUtils {
    // Stub for test utility methods. Add real methods as needed.
    public static void setupJavaFX() {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    // JavaFX Initializer utility for tests
    public static class JavaFxInitializer {
        private static boolean started = false;
        public static synchronized void init() {
            if (!started) {
                try {
                    Platform.startup(() -> {});
                } catch (IllegalStateException e) {
                    // Already started, ignore
                }
                started = true;
            }
        }
    }
} 