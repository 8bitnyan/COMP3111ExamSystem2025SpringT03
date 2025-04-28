package comp3111.examsystem.controller;
import comp3111.examsystem.Main;
import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.data.Position;
import comp3111.examsystem.entity.Manager;
import comp3111.examsystem.entity.Student;
import comp3111.examsystem.entity.Teacher;
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
import java.util.*;

/**
 * Controller class for managing Teachers in the Exam System.
 * This class handles displaying, filtering, adding, updating, and deleting Teachers.
 */
public class TeacherManagementController {
    private Manager manager;
    /**
     * Sets the manager context for this controller.
     * @param manager The currently logged-in manager.
     */
    public void presetController(Manager manager) {
        this.manager = manager;
    }
    // Database instance for handling Course objects
    private final Database<Teacher> teacherDatabase = new Database<>(Teacher.class);
    private ObservableList<Teacher> allTeachers;
    //Table columns for displaying course data
    @FXML private TableView<Teacher> teacherTable;
    @FXML private TableColumn<Teacher, String> colName;
    @FXML private TableColumn<Teacher, String> colGender;
    @FXML private TableColumn<Teacher, Integer> colAge;
    @FXML private TableColumn<Teacher, String> colDepartment;
    @FXML private TableColumn<Teacher, String> colUsername;
    @FXML private TableColumn<Teacher, String> colPosition;
    @FXML private TableColumn<Teacher, String> colPassword;
    //Filter UI fields
    @FXML private TextField filterUsername;
    @FXML private TextField filterName;
    @FXML private ComboBox<String> filterDepartment;
    //Teacher input form fields
    @FXML private TextField tfUsername;
    @FXML private TextField tfName;
    @FXML private TextField tfPassword;
    @FXML private ComboBox<Department> cbDepartment;
    @FXML private TextField tfAge;
    @FXML private ComboBox<Gender> cbGender;
    @FXML private ComboBox<Position> cbPosition;

    @FXML
    public void initialize() {
        //Table Initialization
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));

        //Display courses in the table and filter
        ObservableList<Teacher> teachers = FXCollections.observableArrayList(teacherDatabase.getAllEnabled());
        teacherTable.setItems(teachers);
        allTeachers = teachers;
        for (Department dept : Department.values()) {
            filterDepartment.getItems().add(dept.toString());
            cbDepartment.getItems().add(Department.valueOf(dept.toString()));
        }
        filterDepartment.getSelectionModel().select("ANY");
        cbDepartment.getSelectionModel().select(Department.valueOf("ANY"));
        cbGender.getItems().addAll(Gender.values());
        cbGender.getSelectionModel().clearSelection();
        cbGender.setPromptText("Select Gender");
        cbPosition.getItems().addAll(Position.values());
        cbPosition.setPromptText(("Select Position"));

        //Select Teacher for Form editing
        teacherTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedTeacher) -> {
            if (selectedTeacher != null) {
                tfUsername.setText(selectedTeacher.getUsername());
                tfName.setText(selectedTeacher.getName());
                tfPassword.setText((selectedTeacher.getPassword()));
                cbDepartment.getSelectionModel().select(selectedTeacher.getDepartment());
                tfAge.setText(String.valueOf(selectedTeacher.getAge()));
                cbGender.getSelectionModel().select(selectedTeacher.getGender());
                cbPosition.getSelectionModel().select(selectedTeacher.getPosition());
            }
        });
    }

    /**
     * Applies filtering logic on teacher list based on filter inputs.
     */
    private List<Teacher> applyTeachersFilter(String username, String name, String department) {
        List<Teacher> filtered = teacherDatabase.getAllEnabled();
        if (username != null) {
            filtered = teacherDatabase.queryFuzzyByField("username", username);
        }
        if (name != null) {
            List<Teacher> nameFiltered = teacherDatabase.queryFuzzyByField("name", name);
            filtered = teacherDatabase.join(filtered, nameFiltered);
        }
        if (department != null && !department.equalsIgnoreCase("ANY")) {
            List<Teacher> deptFiltered = teacherDatabase.queryByField("department", department);
            filtered = teacherDatabase.join(filtered, deptFiltered);
        }
        return filtered;
    }

    /**
     * Handles filtering when user clicks the "Filter" button.
     */
    @FXML
    private void filterTeachers() {
        String username = filterUsername.getText().trim();
        String name = filterName.getText().trim();
        String department = filterDepartment.getValue();
        if (username.isEmpty()) {username = null;}
        if (name.isEmpty()) {name = null;}
        if (department == null || department.trim().isEmpty() || department.equalsIgnoreCase("any")) {department = null;}
        List<Teacher> filtered = applyTeachersFilter(username, name, department);
        teacherTable.setItems(FXCollections.observableArrayList(filtered));
    }

    /**
     * Resets all filters and reloads the full course list.
     */
    @FXML
    private void reset() {
        filterUsername.clear();
        filterName.clear();
        filterDepartment.getSelectionModel().select("ANY");
        teacherTable.setItems(FXCollections.observableArrayList(teacherDatabase.getAllEnabled()));
    }

    /**
     * Clears the input form on the right side.
     */
    private void clearForm() {
        tfUsername.clear();
        tfName.clear();
        tfPassword.clear();
        cbDepartment.getSelectionModel().clearSelection();
        cbDepartment.getSelectionModel().select(Department.valueOf("ANY"));
        tfAge.clear();
        cbPosition.getSelectionModel().clearSelection();
        cbGender.getSelectionModel().clearSelection();
        cbGender.setPromptText("Select Gender");
        cbPosition.setPromptText("Select Position");
    }

    /**
     * Adds a new Teacher using input form values.
     * Using functions provided in the database class
     */
    @FXML
    private void addTeacher() {
        try {
            String username = tfUsername.getText().trim();
            String name = tfName.getText().trim();
            String password = tfPassword.getText().trim();
            Department department = cbDepartment.getValue();
            Gender gender = cbGender.getValue();
            String ageStr = tfAge.getText().trim();
            Position position = cbPosition.getValue();
            // Validation
            StringBuilder validationMsg = Teacher.validateWithMessage(null, username, password, name, ageStr, department);
            if (validationMsg.length() > 0) {
                MsgSender.showMsg(validationMsg.toString());
                return; }
            if (gender == null) {
                MsgSender.showMsg("Please select a gender.");
                return; }
            if (position == null) {
                MsgSender.showMsg("Please select a position.");
                return; }
            int age = Integer.parseInt(ageStr);
            long id = System.currentTimeMillis();
            Teacher teacher = new Teacher(id, username, password, name, gender, age, department, position);
            teacherDatabase.add(teacher);
            allTeachers.add(teacher);
            teacherTable.setItems(FXCollections.observableArrayList(allTeachers));
            clearForm();
            MsgSender.showMsg("Teacher added successfully!");
        } catch (Exception e) {
            MsgSender.showMsg("Failed to add teacher.");
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing teacher selected in the table using form values.
     */
    @FXML
    private void updateTeacher() {
        Teacher selectedTeacher = teacherTable.getSelectionModel().getSelectedItem();
        if (selectedTeacher == null) {
            MsgSender.showMsg("Please select a Teacher to update.");
            return;
        }
        // Get updated form values
        String username = tfUsername.getText().trim();
        String name = tfName.getText().trim();
        String password = tfPassword.getText().trim();
        Department department = cbDepartment.getValue();
        Gender gender = cbGender.getValue();
        String ageStr = tfAge.getText().trim();
        Position position = cbPosition.getValue();

        // Validation
        StringBuilder validationMsg = Student.validateWithMessage(null, username, password, name, ageStr, department);
        if(validationMsg.length() > 0) {
            MsgSender.showMsg(validationMsg.toString());
            return; }
        if (gender == null) {
            MsgSender.showMsg("Please select a gender.");
            return; }
        if (position == null) {
            MsgSender.showMsg("Please select a position.");
            return; }

        // Update selected Teacher
        int age = Integer.parseInt(tfAge.getText().trim());
        selectedTeacher.setUsername(username);
        selectedTeacher.setName(name);
        selectedTeacher.setPassword(password);
        selectedTeacher.setDepartment(department);
        selectedTeacher.setGender(gender);
        selectedTeacher.setAge(age);
        selectedTeacher.setPosition(position);
        // Rewrite file with all Teachers
        saveAllTeachersToFile();
        teacherTable.refresh();
        MsgSender.showMsg("Teacher updated successfully!");
    }

    /**
     * Updates the changed values to the database
     */
    private void saveAllTeachersToFile() {
        try {
            for (Teacher t : allTeachers) {
                teacherDatabase.update(t);
            }
        } catch (Exception e) {
            MsgSender.showMsg("Failed to save teacher data.");
            e.printStackTrace();
        }
    }

    /**
     * Deletes the selected teacher from the database.
     */
    @FXML
    void deleteTeacher() {
        Teacher selectedTeacher = teacherTable.getSelectionModel().getSelectedItem();
        if (selectedTeacher == null) {
            MsgSender.showMsg("Please select a teacher to delete.");
            return;
        }
        MsgSender.showConfirm(
                "Delete Confirmation",
                "Are you sure you want to delete this teacher?",
                () -> {
                    teacherDatabase.delByKey(String.valueOf(selectedTeacher.getId()));
                    allTeachers = FXCollections.observableArrayList(teacherDatabase.getAllEnabled());
                    teacherTable.setItems(allTeachers);
                    MsgSender.showMsg("Teacher deleted successfully.");
                    clearForm();
                }
        );
    }

    /**
     * Navigates back to the Manager main screen.
     * @param e The triggered action event.
     */
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

    /**
     * Closes the application with a confirmation.
     */
    @FXML
    void closeApplication() {
        MsgSender.showConfirm(
                "Exit Confirmation",
                "Are you sure you want to exit?\nClick OK to exit the application.",
                Platform::exit
        );
    }
}
