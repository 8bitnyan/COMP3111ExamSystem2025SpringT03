package comp3111.examsystem.tools;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class UIhelper {
    public static void expandToFullScreen(Stage stage) {
        // Get the primary screen bounds
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        // Set the stage size to fit the screen resolution
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
    }
}
