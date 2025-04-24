package comp3111.examsystem.controller;

import comp3111.examsystem.Main;
import comp3111.examsystem.data.Department;
import comp3111.examsystem.entity.Course;
import comp3111.examsystem.entity.Manager;
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

public class CourseManagementController {
    private Manager manager;

    public void presetController(Manager manager) {
        this.manager = manager;
    }

    //Database
    private final Database<Course> courseDatabase = new Database<>(Course.class);
    private ObservableList<Course> allCourses;
    //Table
    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> colCourseCode;
    @FXML private TableColumn<Course, String> colCourseName;
    @FXML private TableColumn<Course, String> colDepartment;
    //Filter
    @FXML private TextField filterCourseCode;
    @FXML private TextField filterCourseName;
    @FXML private ComboBox<String> filterDepartment;
    //Form
    @FXML private TextField tfCourseCode;
    @FXML private TextField tfCourseName;
    @FXML private ComboBox<Department> cbDepartment;

    @FXML
    public void initialize() {
        //Table Initialization
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        ObservableList<Course> courses = FXCollections.observableArrayList(courseDatabase.getAllEnabled());
        courseTable.setItems(courses);
        allCourses = courses;

        //Filter + Form initialization
        for (Department dept : Department.values()) {
            filterDepartment.getItems().add(dept.toString());
            cbDepartment.getItems().add(Department.valueOf(dept.toString()));
        }
        filterDepartment.getSelectionModel().select("ANY");
        cbDepartment.getSelectionModel().select(Department.valueOf("ANY"));

        //Select Course for Form editing
        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedCourse) -> {
            if (selectedCourse != null) {
                tfCourseCode.setText(selectedCourse.getCourseCode());
                tfCourseName.setText(selectedCourse.getCourseName());
                cbDepartment.getSelectionModel().select(selectedCourse.getDepartment());
            }
        });
    }

//Filter Courses
    private List<Course> applyCoursesFilter(String courseCode, String courseName, String department) {
        return allCourses.stream()
                .filter(s -> courseCode == null || s.getCourseCode().toLowerCase().contains(courseCode.toLowerCase()))
                .filter(s -> courseName == null || s.getCourseName().toLowerCase().contains(courseName.toLowerCase()))
                .filter(s -> department == null || department.equalsIgnoreCase("ANY") ||
                        s.getDepartment().toString().equalsIgnoreCase(department))
                .toList();
    }

    @FXML
    private void filterCourses() {
        String courseCode = filterCourseCode.getText().trim();
        String courseName = filterCourseName.getText().trim();
        String department = filterDepartment.getValue();
        if (courseCode.isEmpty()) courseCode = null;
        if (courseName.isEmpty()) courseName = null;
        if (department == null || department.trim().isEmpty() || department.equalsIgnoreCase("any")) department = null;
        allCourses = FXCollections.observableArrayList(courseDatabase.getAllEnabled());
        List<Course> filtered = applyCoursesFilter(courseCode, courseName, department);
        courseTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void reset() {
        filterCourseCode.clear();
        filterCourseName.clear();
        filterDepartment.getSelectionModel().select("ANY");
        List<Course> resetFiltered = applyCoursesFilter(null, null, "ANY");
        courseTable.setItems(FXCollections.observableArrayList(resetFiltered));
    }

    //Right Form Action
    private void clearForm() {
        tfCourseCode.clear();
        tfCourseName.clear();
        cbDepartment.getSelectionModel().clearSelection();
    }

    @FXML
    private void addCourse() {
        String courseCode = tfCourseCode.getText().trim();
        String courseName = tfCourseName.getText().trim();
        Department department = cbDepartment.getValue();
        // Validation
        if (courseCode.isEmpty() || courseName.isEmpty() || department == null) {
            MsgSender.showMsg("All fields must be filled correctly.");
            return;
        }
        long id = System.currentTimeMillis();
        Course course = new Course(id, courseCode, courseName, department);
        try {
            courseDatabase.add(course);
            allCourses.add(course);
            courseTable.setItems(FXCollections.observableArrayList(allCourses));
            MsgSender.showMsg("Course added successfully!");
        } catch (Exception e) {
            MsgSender.showMsg("Failed to add course.");
            e.printStackTrace();
        }
    }

    @FXML
    private void updateCourse() {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            MsgSender.showMsg("Please select a Course to update.");
            return;
        }
        String courseCode = tfCourseCode.getText().trim();
        String courseName = tfCourseName.getText().trim();
        Department department = cbDepartment.getValue();
        if (courseCode.isEmpty() || courseName.isEmpty() || department == null ) {
            MsgSender.showMsg("All fields must be filled correctly.");
            return;
        }
        selectedCourse.setCourseCode(courseCode);
        selectedCourse.setCourseName(courseName);
        selectedCourse.setDepartment(department);
        try {
            courseDatabase.update(selectedCourse);
            courseTable.refresh();
            MsgSender.showMsg("Course updated successfully!");
        } catch (Exception e) {
            MsgSender.showMsg("Failed to update course.");
            e.printStackTrace();
        }
    }

    private void saveAllCoursesToFile() {
        for (Course c : allCourses) {
            courseDatabase.update(c);
        }
    }

    @FXML
    void deleteCourse() {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            MsgSender.showMsg("Please select a course to delete.");
            return;
        }

        MsgSender.showConfirm(
                "Delete Confirmation",
                "Are you sure you want to delete this course?\nThis will also delete all associated exams.",
                () -> {
                    try {
                        // Delete course by ID (set isAble = false)
                        courseDatabase.delByKey(String.valueOf(selectedCourse.getId()));

                        // Delete all exams linked to this courseCode
                        Database<comp3111.examsystem.entity.Exam> examDatabase = new Database<>(comp3111.examsystem.entity.Exam.class);
                        List<comp3111.examsystem.entity.Exam> allExams = examDatabase.getAllEnabled();

                        for (comp3111.examsystem.entity.Exam exam : allExams) {
                            if (selectedCourse.getCourseCode().equals(exam.getCourseCode()) && exam.getId() != null) {
                                examDatabase.delByKey(String.valueOf(exam.getId()));
                            }
                        }

                        allCourses = FXCollections.observableArrayList(courseDatabase.getAllEnabled());
                        courseTable.setItems(allCourses);
                        clearForm();

                        MsgSender.showMsg("Course and associated exams deleted successfully!");
                    } catch (Exception e) {
                        MsgSender.showMsg("Failed to delete course.");
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
