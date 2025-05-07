package comp3111.examsystem.controller;
import comp3111.examsystem.Main;
import comp3111.examsystem.data.Department;
import comp3111.examsystem.entity.Course;
import comp3111.examsystem.entity.Exam;
import comp3111.examsystem.entity.Manager;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import comp3111.examsystem.tools.UIhelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.List;

/**
 * Controller class for managing Courses in the Exam System.
 * This class handles displaying, filtering, adding, updating, and deleting courses.
 * It also links to associated entities such as Department and Exam.
 */
public class CourseManagementController {
    private Manager manager;
    /**
     * Sets the manager context for this controller.
     * @param manager The currently logged-in manager.
     */
    public void presetController(Manager manager) {
        this.manager = manager;
    }

    // Database instance for handling Course objects
    public final Database<Course> courseDatabase = new Database<>(Course.class);
    public ObservableList<Course> allCourses;

    // Table columns for displaying course data
    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> colCourseCode;
    @FXML private TableColumn<Course, String> colCourseName;
    @FXML private TableColumn<Course, String> colDepartment;
    // Filter UI fields
    @FXML private TextField filterCourseCode;
    @FXML private TextField filterCourseName;
    @FXML private ComboBox<String> filterDepartment;
    // Course input form fields
    @FXML private TextField tfCourseCode;
    @FXML private TextField tfCourseName;
    @FXML private ComboBox<Department> cbDepartment;

    /**
     * Initializes the UI components including table columns, combo boxes, and selection listeners.
     * This method is automatically called by JavaFX after the FXML has been loaded.
     */
    @FXML
    public void initialize() {
        // Set up table columns
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));

        // Display courses in table and filters
        ObservableList<Course> courses = FXCollections.observableArrayList(courseDatabase.getAllEnabled());
        courseTable.setItems(courses);
        allCourses = courses;
        for (Department dept : Department.values()) {
            filterDepartment.getItems().add(dept.toString());
            cbDepartment.getItems().add(dept);
        }
        filterDepartment.getSelectionModel().select("ANY");
        cbDepartment.getSelectionModel().select(Department.valueOf("ANY"));

        // Listen to table row selection to display in the form
        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedCourse) -> {
            if (selectedCourse != null) {
                tfCourseCode.setText(selectedCourse.getCourseCode());
                tfCourseName.setText(selectedCourse.getCourseName());
                cbDepartment.getSelectionModel().select(selectedCourse.getDepartment());
            }
        });
    }

    /**
     * Applies filtering logic on course list based on filter inputs.
     */
    public List<Course> applyCoursesFilter(String courseCode, String courseName, String department) {
        return allCourses.stream()
                .filter(s -> courseCode == null || s.getCourseCode().toLowerCase().contains(courseCode.toLowerCase()))
                .filter(s -> courseName == null || s.getCourseName().toLowerCase().contains(courseName.toLowerCase()))
                .filter(s -> department == null || department.equalsIgnoreCase("ANY") ||
                        s.getDepartment().toString().equalsIgnoreCase(department))
                .toList();
    }

    /**
     * Handles filtering when user clicks the "Filter" button.
     */
    @FXML
    public void filterCourses() {
        String courseCode = filterCourseCode.getText().trim();
        String courseName = filterCourseName.getText().trim();
        String department = filterDepartment.getValue();
        if (courseCode.isEmpty()) courseCode = null;
        if (courseName.isEmpty()) courseName = null;
        if (department == null || department.trim().isEmpty() || department.equalsIgnoreCase("ANY")) department = null;

        allCourses = FXCollections.observableArrayList(courseDatabase.getAllEnabled());
        List<Course> filtered = applyCoursesFilter(courseCode, courseName, department);
        courseTable.setItems(FXCollections.observableArrayList(filtered));
    }

    /**
     * Resets all filters and reloads the full course list.
     */
    @FXML
    public void reset() {
        filterCourseCode.clear();
        filterCourseName.clear();
        filterDepartment.getSelectionModel().select("ANY");
        List<Course> resetFiltered = applyCoursesFilter(null, null, "ANY");
        courseTable.setItems(FXCollections.observableArrayList(resetFiltered));
    }

    /**
     * Clears the input form on the right side.
     */
    public void clearForm() {
        tfCourseCode.clear();
        tfCourseName.clear();
        cbDepartment.getSelectionModel().clearSelection();
    }

    /**
     * Adds a new course using input form values.
     * Using functions provided in the database class
     */
    @FXML
    public void addCourse() {
        String courseCode = tfCourseCode.getText().trim();
        String courseName = tfCourseName.getText().trim();
        Department department = cbDepartment.getValue();
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

    /**
     * Updates an existing course selected in the table using form values.
     */
    @FXML
    public void updateCourse() {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            MsgSender.showMsg("Please select a Course to update.");
            return;
        }
        String courseCode = tfCourseCode.getText().trim();
        String courseName = tfCourseName.getText().trim();
        Department department = cbDepartment.getValue();
        if (courseCode.isEmpty() || courseName.isEmpty() || department == null) {
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

    // Add this factory method for testability
    protected Database<Exam> createExamDatabase() {
        return new Database<>(Exam.class);
    }

    /**
     * Deletes the selected course from the database and removes all associated exams as well.
     */
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
                        courseDatabase.delByKey(String.valueOf(selectedCourse.getId()));
                        Database<Exam> examDatabase = createExamDatabase();
                        List<Exam> allExams = examDatabase.getAllEnabled();
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
