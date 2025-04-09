package comp3111.examsystem.controller;

import comp3111.examsystem.Main;
import comp3111.examsystem.entity.*;
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
 * The controller for teacher main page.
 */
public class TeacherMainController implements Initializable {
    @FXML
    private VBox mainbox;

    private Teacher teacher;

    /**
     * Initializes the UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * Sets the teacher object and initializes the UI.
     * @param teacher The teacher object that is operating this page.
     */
    public void preSetController(Teacher teacher) {
        this.teacher = teacher;
    }

    /**
     * Opens the question bank management UI.
     */
    @FXML
    public void openQuestionManageUI() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherQuestionBankMgmtUI.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Question Bank Management");
            stage.setScene(new Scene(fxmlLoader.load()));
            TeacherQuestionBankMgmtController controller = fxmlLoader.getController();
            controller.presetController(teacher); // Pass the Teacher object to the next controller
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) mainbox.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the exam management UI.
     */
    @FXML
    public void openExamManageUI() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherExamMgmtUI.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Exam Management");
            stage.setScene(new Scene(fxmlLoader.load()));
            TeacherExamMgmtController controller = fxmlLoader.getController();
            controller.presetController(teacher);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) mainbox.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the grade statistics UI.
     */
    @FXML
    public void openGradeStatisticUI() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherGradeStatistic.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Grade Statistics");
            stage.setScene(new Scene(fxmlLoader.load()));
            TeacherGradeStatisticController controller = fxmlLoader.getController();
            controller.presetController(teacher);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) mainbox.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the grade exam UI.
     */
    @FXML
    public void openGradeExamUI() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherGradeExam.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Grade Exam");
            stage.setScene(new Scene(fxmlLoader.load()));
            TeacherGradeExamController controller = fxmlLoader.getController();
            controller.presetController(teacher);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) mainbox.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the student management UI.
     */
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

    /**
     * Exits the application.
     */
    @FXML
    public void exit() {
        System.exit(0);
    }
}
