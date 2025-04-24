package comp3111.examsystem.controller;

import comp3111.examsystem.Main;
import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.entity.Manager;
import comp3111.examsystem.entity.Student;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.UIhelper;
import comp3111.examsystem.tools.MsgSender;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class StudentManagementController {
    private Manager manager;

    public void presetController(Manager manager) {
        this.manager = manager;
    }

    //Database
    private final Database<Student> studentDatabase = new Database<>(Student.class);
    private ObservableList<Student> allStudents;

    //Table
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colGender;
    @FXML private TableColumn<Student, Integer> colAge;
    @FXML private TableColumn<Student, String> colDepartment;
    @FXML private TableColumn<Student, String> colUsername;
    @FXML private TableColumn<Student, String> colPassword;
    //Filter
    @FXML private TextField filterUsername;
    @FXML private TextField filterName;
    @FXML private ComboBox<String> filterDepartment;
    //Form
    @FXML private TextField tfUsername;
    @FXML private TextField tfName;
    @FXML private TextField tfPassword;
    @FXML private ComboBox<Department> cbDepartment;
    @FXML private TextField tfAge;
    @FXML private ComboBox<Gender> cbGender;

    @FXML
    public void initialize() {
        //Table Initialization
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        ObservableList<Student> students = FXCollections.observableArrayList(studentDatabase.getAllEnabled());
        studentTable.setItems(students);
        allStudents = students;

        //Filter + Form initialization
        for (Department dept : Department.values()) {
            filterDepartment.getItems().add(dept.toString());
            cbDepartment.getItems().add(Department.valueOf(dept.toString()));
        }
        filterDepartment.getSelectionModel().select("ANY");
        cbDepartment.getSelectionModel().select(Department.valueOf("ANY"));
        cbGender.getItems().addAll(Gender.values());
        cbGender.getSelectionModel().clearSelection();
        cbGender.setPromptText("Select Gender");

        //Select Student for Form editing
        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedStudent) -> {
            if (selectedStudent != null) {
                tfUsername.setText(selectedStudent.getUsername());
                tfName.setText(selectedStudent.getName());
                tfPassword.setText((selectedStudent.getPassword()));
                cbDepartment.getSelectionModel().select(selectedStudent.getDepartment());
                tfAge.setText(String.valueOf(selectedStudent.getAge()));
                cbGender.getSelectionModel().select(selectedStudent.getGender());
            }
        });
    }

//Filter Students
    private List<Student> applyStudentsFilter(String username, String name, String department) {
        List<Student> filtered = studentDatabase.getAllEnabled();
        if (username != null) {
            filtered = studentDatabase.queryFuzzyByField("username", username);
        }
        if (name != null) {
            List<Student> nameFiltered = studentDatabase.queryFuzzyByField("name", name);
            filtered = studentDatabase.join(filtered, nameFiltered);
        }
        if (department != null && !department.equalsIgnoreCase("ANY")) {
            List<Student> deptFiltered = studentDatabase.queryByField("department", department);
            filtered = studentDatabase.join(filtered, deptFiltered);
        }
        return filtered;
    }

    @FXML
    private void filterStudents() {
        String username = filterUsername.getText().trim();
        String name = filterName.getText().trim();
        String department = filterDepartment.getValue();
        if (username.isEmpty()) {username = null;}
        if (name.isEmpty()) {name = null;}
        if (department == null || department.trim().isEmpty() || department.equalsIgnoreCase("any")) {department = null;}
        List<Student> filtered = applyStudentsFilter(username, name, department);
        studentTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void reset() {
        filterUsername.clear();
        filterName.clear();
        filterDepartment.getSelectionModel().select("ANY");
        studentTable.setItems(FXCollections.observableArrayList(studentDatabase.getAllEnabled()));
    }

//Right Form Action
    private void clearForm() {
        tfUsername.clear();
        tfName.clear();
        tfPassword.clear();
        cbDepartment.getSelectionModel().clearSelection();
        tfAge.clear();
        cbGender.getSelectionModel().clearSelection();
    }

    @FXML
    private void addStudent() {
        try {
            String username = tfUsername.getText().trim();
            String name = tfName.getText().trim();
            String password = tfPassword.getText().trim();
            Department department = cbDepartment.getValue();
            Gender gender = cbGender.getValue();
            String ageStr = tfAge.getText().trim();

            // Validate input
            StringBuilder validationMsg = Student.validateWithMessage(null, username, password, name, ageStr, department);
            if (validationMsg.length() > 0) {
                MsgSender.showMsg(validationMsg.toString());
                return;
            }
            if (gender == null) {
                MsgSender.showMsg("Please select a gender.");
                return;
            }
            int age = Integer.parseInt(ageStr);
            Student student = new Student(null, username, password, name, gender, age, department);
            studentDatabase.add(student);
            allStudents = FXCollections.observableArrayList(studentDatabase.getAllEnabled());
            studentTable.setItems(allStudents);
            clearForm();
            MsgSender.showMsg("Student added successfully!");
        } catch (NumberFormatException e) {
            MsgSender.showMsg("Age must be a number.");
        } catch (Exception e) {
            MsgSender.showMsg("Failed to add student to system.");
            e.printStackTrace();
        }
    }

    @FXML
    private void updateStudent() {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            MsgSender.showMsg("Please select a student to update.");
            return;
        }
        // Get updated form values
        String username = tfUsername.getText().trim();
        String name = tfName.getText().trim();
        String password = tfPassword.getText().trim();
        Department department = cbDepartment.getValue();
        Gender gender = cbGender.getValue();
        String ageStr = tfAge.getText().trim();

        StringBuilder validationMsg = Student.validateWithMessage(null, username, password, name, ageStr, department);
        if(validationMsg.length() > 0) {
            MsgSender.showMsg(validationMsg.toString());
            return;
        }
        if (gender == null) {
            MsgSender.showMsg("Please select a gender.");
            return;
        }

        int age = Integer.parseInt(tfAge.getText().trim());
        // Update selected student
        selectedStudent.setUsername(username);
        selectedStudent.setName(name);
        selectedStudent.setPassword(password);
        selectedStudent.setDepartment(department);
        selectedStudent.setGender(gender);
        selectedStudent.setAge(age);
        // Rewrite file with all students
        saveAllStudentsToFile();
        studentTable.refresh();
        MsgSender.showMsg("Student updated successfully!");
    }

    private void saveAllStudentsToFile() {
        try {
            for (Student s : allStudents) {
                studentDatabase.update(s);  // Ensures only enabled students are updated correctly
            }
        } catch (Exception e) {
            MsgSender.showMsg("Failed to save student data.");
            e.printStackTrace();
        }
    }

    @FXML
    void deleteStudent() {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            MsgSender.showMsg("Please select a student to delete.");
            return;
        }
        MsgSender.showConfirm(
                "Delete Confirmation",
                "Are you sure you want to delete this student?",
                () -> {
                    try {
                        studentDatabase.delByKey(String.valueOf(selectedStudent.getId()));
                        allStudents = FXCollections.observableArrayList(studentDatabase.getAllEnabled());
                        studentTable.setItems(allStudents);
                        clearForm();
                        MsgSender.showMsg("Student deleted successfully.");
                    } catch (Exception e) {
                        MsgSender.showMsg("Failed to delete student.");
                        e.printStackTrace();
                    }
                }
        );
    }

    @FXML
    void back(ActionEvent e) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("ManagerMainUI.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Back");
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        UIhelper.expandToFullScreen(stage);
        stage.show();
        ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
    }

    @FXML
    void closeApplication(ActionEvent e) {
        MsgSender.showConfirm(
                "Exit Confirmation",
                "Are you sure you want to exit?\nClick OK to exit the application.",
                Platform::exit
        );
    }

}
