package comp3111.examsystem.controller;

import comp3111.examsystem.Main;
import comp3111.examsystem.tools.MsgSender;
import comp3111.examsystem.entity.Manager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import comp3111.examsystem.tools.UIhelper;

/**
 * The controller for manager login page.
 */
public class ManagerLoginController implements Initializable {
    @FXML
    private TextField usernameTxt;
    @FXML
    private PasswordField passwordTxt;

    /**
     * Initializes the Manager Login UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * Handles the login button click event.
     * @param e The action event.
     */
    @FXML
    public void login(ActionEvent e) {
        if (Manager.validateLogin(usernameTxt.getText(), passwordTxt.getText())) {
            MsgSender.showMsg("Login successful!");
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("ManagerMainUI.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Hi " + usernameTxt.getText() + ", Welcome to HKUST Examination System");
            try {
                stage.setScene(new Scene(fxmlLoader.load()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
        }
        else MsgSender.showMsg("Invalid username or password!");
    }

    /**
     * Handles the cancel button click event.
     * @param e The action event.
     */
    @FXML
    public void cancel(ActionEvent e) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("LoginUI.fxml"));
        Stage stage = new Stage();
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        UIhelper.expandToFullScreen(stage);
        stage.show();
        ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
    }
}
