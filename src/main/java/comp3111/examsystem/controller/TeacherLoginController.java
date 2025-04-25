package comp3111.examsystem.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import comp3111.examsystem.Main;
import comp3111.examsystem.entity.Teacher;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import comp3111.examsystem.tools.UIhelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * The controller for teacher login page.
 */
public class TeacherLoginController implements Initializable {
    @FXML
    private TextField usernameTxt;
    @FXML
    private PasswordField passwordTxt;

    private final Database<Teacher> teacherDatabase = new Database<>(Teacher.class);

    /**
     * Initializes the Teacher Login UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * Validates the login information.
     * @return A teacher object if the login information is valid, null otherwise.
     */
    private Teacher validateLogin() {
        List<Teacher> teachers = teacherDatabase.getAllEnabled();
        for (Teacher teacher : teachers) {
            if (teacher.getUsername().equals(usernameTxt.getText()) && teacher.getPassword().equals(passwordTxt.getText())) {
                return teacher;
            }
        }
        comp3111.examsystem.tools.MsgSender.showMsg("Invalid username or password");
        return null;
    }

    /**
     * Handles the login button click event.
     * @param e The action event.
     */
    @FXML
    public void login(ActionEvent e) {
        Teacher teacher = validateLogin();
        if (teacher == null) {
            return;
        }
        MsgSender.showMsg("Login successful!");
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherMainUI.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hi " + usernameTxt.getText() + ", Welcome to HKUST Examination System");
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            TeacherMainController controller = fxmlLoader.getController();
            controller.presetController(teacher);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        UIhelper.expandToFullScreen(stage);
        stage.show();
        ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
    }

    /**
     * Handles the register button click event.
     * @param e The action event.
     */
    @FXML
    public void register(ActionEvent e) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherRegisterUI.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Teacher Register");
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        UIhelper.expandToFullScreen(stage);
        stage.show();
        ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
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