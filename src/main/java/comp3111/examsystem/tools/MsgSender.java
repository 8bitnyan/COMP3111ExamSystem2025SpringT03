package comp3111.examsystem.tools;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * The MsgSender class is a helper class for displaying javafx alert messages.
 * It supports displaying information popups and confirmation popups.
 */
public class MsgSender {
    /**
     * Displays a popup message in the UI.
     * @param msg The message string to be displayed.
     */
    static public void showMsg(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.titleProperty().set("Hint");
        alert.headerTextProperty().set(msg);
        alert.showAndWait();
    }

    /**
     * Displays a confirmation message in the UI. If the user confirms the action, callback is executed.
     * @param title The title string of the confirmation message.
     * @param msg The message string to be displayed.
     * @param callback The function to be executed if the user confirms.
     */
    static public void showConfirm(String title, String msg, Runnable callback) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(msg);
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);

        if (result == ButtonType.OK) {
            callback.run();
        }
    }
}
