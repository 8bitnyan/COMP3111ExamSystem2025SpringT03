package comp3111.examsystem.controller;

import comp3111.examsystem.Main;
import comp3111.examsystem.entity.Manager;
import comp3111.examsystem.entity.Teacher;
import comp3111.examsystem.tools.UIhelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for the main page of the manager.
 */
public class ManagerMainController implements Initializable {
    @FXML
    private VBox mainbox;

    private Manager manager;
    /**
     * Initializes the Manager Main UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void preSetController(Manager manager) {
        this.manager = manager;
    }

    @FXML
    public void openStudentManagementUI() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("StudentManagementUI.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Student Management");
            stage.setScene(new Scene(fxmlLoader.load()));
            StudentManagementController controller = fxmlLoader.getController();
            controller.presetController(manager);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) mainbox.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openTeacherManagementUI() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherManagementUI.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Teacher Management");
            stage.setScene(new Scene(fxmlLoader.load()));
            TeacherManagementController controller = fxmlLoader.getController();
            controller.presetController(manager);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) mainbox.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openCourseManagementUI() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("CourseManagementUI.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Course Management");
            stage.setScene(new Scene(fxmlLoader.load()));
            CourseManagementController controller = fxmlLoader.getController();
            controller.presetController(manager);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) mainbox.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout(ActionEvent e) {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("LoginUI.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 640, 480);
            stage.setTitle("Please Log In");
            stage.setScene(scene);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @FXML
    public void exit() {
        System.exit(0);
    }

}
