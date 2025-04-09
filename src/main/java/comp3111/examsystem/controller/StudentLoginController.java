package comp3111.examsystem.controller;

import comp3111.examsystem.Main;
import comp3111.examsystem.entity.Student;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import comp3111.examsystem.tools.UIhelper;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * The controller for student login page.
 */
public class StudentLoginController implements Initializable {
    @FXML
    public Button cancelButton, loginButton, registerButton;
    public Label titleLbl;
    @FXML
    TextField usernameTxt;
    @FXML
    PasswordField passwordTxt;

    Database<Student> studentDatabase = new Database<>(Student.class);

    /**
     * Validates the login information of the student.
     * @return A student object if the login information is valid, null otherwise.
     */
    private Student ValidateLogin() {
        List<Student> students = studentDatabase.getAllEnabled();
        for (Student student : students) {
            if (student.getUsername().equals(usernameTxt.getText()) && student.getPassword().equals(passwordTxt.getText())) {
                return student;
            }
        }
        comp3111.examsystem.tools.MsgSender.showMsg("Invalid username or password!\n");
        return null;
    }

    /**
     * Initializes the Student Login UI.
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
        Student student = ValidateLogin();
        if (student == null) {
            return;
        }
        MsgSender.showMsg("Login successful!");
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("StudentMainUI.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hi " + usernameTxt.getText() +", Welcome to HKUST Examination System");
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            StudentMainController controller = fxmlLoader.getController();
            controller.preSetController(student);
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
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("StudentRegisterUI.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Student Register");
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
