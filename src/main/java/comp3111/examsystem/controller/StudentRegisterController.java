package comp3111.examsystem.controller;

import comp3111.examsystem.Main;
import comp3111.examsystem.entity.Student;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.data.*;
import comp3111.examsystem.tools.MsgSender;
import comp3111.examsystem.tools.UIhelper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The controller for the student register page. Handles student registration logic and navigation for the registration UI.
 */
public class StudentRegisterController implements Initializable {
    @FXML
    Button registerButton, cancelButton;
    @FXML
    TextField usernameTxt;
    @FXML
    TextField nameTxt;
    @FXML
    TextField ageTxt;
    @FXML
    ComboBox<Gender> genderCmb;
    @FXML
    ComboBox<Department> departmentCmb;
    @FXML
    PasswordField passwordTxt;
    @FXML
    PasswordField passwordConfirmTxt;

    Database<Student> studentDatabase = new Database<>(Student.class);

    /**
     * Validates the registration information entered by the user.
     *
     * @return An error message if the registration information is invalid, empty string otherwise.
     */
    private StringBuilder ValidateRegister() {
        StringBuilder stringBuilder = new StringBuilder();
        if (usernameTxt.getText().isEmpty() || nameTxt.getText().isEmpty() ||
                ageTxt.getText().isEmpty() || passwordTxt.getText().isEmpty() ||
                passwordConfirmTxt.getText().isEmpty()) {
            stringBuilder.append("Please fill in all fields!\n");
        } else {
            stringBuilder.append(Student.validateWithMessage(usernameTxt.getText(), passwordTxt.getText(),
                    nameTxt.getText(), ageTxt.getText(), departmentCmb.getValue()));
            List<Student> students = studentDatabase.getAllEnabled();
            for (Student student : students) if (student.getUsername().equals(usernameTxt.getText())) stringBuilder.append("Username has been used!\n");
            if (!passwordTxt.getText().equals(passwordConfirmTxt.getText())) stringBuilder.append("Password and Confirm Password do not match!\n");
        }
        return stringBuilder;
    }

    /**
     * Saves the registration information to the database and returns the registered student object.
     *
     * @return A student object with the registered values.
     */
    Student saveRegistration() {
        String username = usernameTxt.getText();
        String password = passwordTxt.getText();
        String name = nameTxt.getText();
        Gender gender = genderCmb.getValue();
        int age = Integer.parseInt(ageTxt.getText());
        Department department = departmentCmb.getValue();

        Student student = new Student(null, username, password, name, gender, age, department);

        studentDatabase.add(student);

        return student;
    }

    /**
     * Initializes the Student Registration UI. Sets up combo boxes for gender and department.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    public void initialize(URL location, ResourceBundle resources) {
        genderCmb.getItems().setAll(Gender.values());
        genderCmb.setValue(Gender.MALE);
        departmentCmb.getItems().setAll(Department.values());
        departmentCmb.setValue(Department.ANY);
    }

    /**
     * Handles the register button click event. Validates input, saves registration, and navigates to the main UI if successful.
     *
     * @param e The action event.
     */
    @FXML
    public void register(ActionEvent e) {
        StringBuilder msg = ValidateRegister();
        if (!msg.isEmpty()) {
            MsgSender.showMsg(msg.toString());
            return;
        }
        Student student = saveRegistration();
        comp3111.examsystem.tools.MsgSender.showMsg("Successful Register");
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("StudentMainUI.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hi " + usernameTxt.getText() + ", Welcome to HKUST Examination System");
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
     * Handles the cancel button click event. Navigates back to the student login UI.
     *
     * @param e The action event.
     */
    @FXML
    public void cancel(ActionEvent e) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("StudentLoginUI.fxml"));
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

}
