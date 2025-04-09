package comp3111.examsystem.controller;

import java.io.IOException;

import comp3111.examsystem.Main;
import comp3111.examsystem.tools.UIhelper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * The controller for the select login page.
 * This controller is responsible for redirecting the user to the appropriate login page
 * based on their selection of student, teacher, or manager.
 */
public class SelectLoginController {

    /**
     * Redirects to the student login page.
     * @param e The event that triggers the method.
     */
    @FXML
    public void studentLogin(ActionEvent e) {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("StudentLoginUI.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Student Login");
            stage.setScene(scene);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Redirects to the teacher login page.
     * @param e The event that triggers the method.
     */
    @FXML
    public void teacherLogin(ActionEvent e) {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherLoginUI.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Teacher Login");
            stage.setScene(scene);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Redirects to the manager login page.
     * @param e The event that triggers the method.
     */
    @FXML
    public void managerLogin(ActionEvent e) {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("ManagerLoginUI.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Manager Login");
            stage.setScene(scene);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Exits the application.
     */
    @FXML
    public void exit() {
        System.exit(0);
    }
}
