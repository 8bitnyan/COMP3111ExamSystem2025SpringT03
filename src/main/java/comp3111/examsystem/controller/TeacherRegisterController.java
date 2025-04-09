package comp3111.examsystem.controller;

import comp3111.examsystem.data.*;
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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The controller for teacher registration page.
 */
public class TeacherRegisterController implements Initializable {

    @FXML
    private TextField ageTxt, usernameTxt, nameTxt;
    @FXML
    private ComboBox<Gender> genderCmb;
    @FXML
    private ComboBox<Department> departmentCmb;
    @FXML
    private ComboBox<Position> positionCmb;
    @FXML
    private PasswordField passwordConfirmTxt, passwordTxt;

    private final Database<Teacher> teacherDatabase = new Database<>(Teacher.class);

    /**
     * Validates the registration information.
     * @return An error message if the registration information is invalid, empty string otherwise.
     */
    private StringBuilder validateTeacherRegistration() {
        StringBuilder stringBuilder = new StringBuilder();
        if (usernameTxt.getText().isEmpty() || nameTxt.getText().isEmpty() ||
                ageTxt.getText().isEmpty() || passwordTxt.getText().isEmpty() || passwordConfirmTxt.getText().isEmpty()) {
            stringBuilder.append("Please fill in all fields!\n");
            return stringBuilder;
        } else {
            stringBuilder.append(Teacher.validateWithMessage(usernameTxt.getText(), passwordTxt.getText(),
                    nameTxt.getText(), ageTxt.getText(), departmentCmb.getValue()));
            List<Teacher> teachers = teacherDatabase.getAllEnabled();
            for (Teacher teacher : teachers) if (teacher.getUsername().equals(usernameTxt.getText())) stringBuilder.append("Username has been used!\n");
            if (!passwordTxt.getText().equals(passwordConfirmTxt.getText())) stringBuilder.append("Password and Confirm Password do not match!\n");
        }
        return stringBuilder;
    }

    /**
     * Saves the registration information to the database.
     * @return A teacher object with the registered values.
     */
    private Teacher saveRegistration() {
        String username = usernameTxt.getText();
        String password = passwordTxt.getText();
        String name = nameTxt.getText();
        Gender gender = genderCmb.getValue();
        int age = Integer.parseInt(ageTxt.getText());
        Department department = departmentCmb.getValue();
        Position position = positionCmb.getValue();

        Teacher teacher = new Teacher(null, username, password, name, gender, age, department, position);

        teacherDatabase.add(teacher);

        return teacher;
    }

    /**
     * Initializes the Teacher Registration UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    public void initialize(URL location, ResourceBundle resources) {
        genderCmb.getItems().setAll(Gender.values());
        genderCmb.setValue(Gender.MALE);
        positionCmb.getItems().setAll(Position.values());
        positionCmb.setValue(Position.AP);
        departmentCmb.getItems().setAll(Department.values());
        departmentCmb.setValue(Department.ANY);
    }

    /**
     * Handles the cancel button click event.
     * @param e The action event.
     */
    @FXML
    void cancel(ActionEvent e) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherLoginUI.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Student Login");
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
     * Handles the register button click event.
     * @param e The action event.
     */
    @FXML
    void register(ActionEvent e) {
        StringBuilder msg = validateTeacherRegistration();
        if (!msg.isEmpty()) {
            MsgSender.showMsg(msg.toString());
            return;
        }
        Teacher teacher = saveRegistration();
        MsgSender.showMsg("Successful Register");
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherMainUI.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hi " + usernameTxt.getText() + ", Welcome to HKUST Examination System");
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
            TeacherMainController controller = fxmlLoader.getController();
            controller.preSetController(teacher);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        UIhelper.expandToFullScreen(stage);
        stage.show();
        ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
    }

}
