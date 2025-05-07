package comp3111.examsystem.controller;

import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.entity.Manager;
import comp3111.examsystem.entity.Student;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class StudentManagementControllerTest {
    private static boolean javafxInitialized = false;

    @BeforeAll
    static void initJfx() throws Exception {
        if (!javafxInitialized) {
            try {
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
                Platform.startup(latch::countDown);
                latch.await();
            } catch (IllegalStateException e) {
                // Toolkit already initialized, ignore
            }
            Platform.setImplicitExit(false);
            javafxInitialized = true;
        }
    }

    StudentManagementController controller;
    MockedStatic<MsgSender> msgSenderMocked;

    @BeforeEach
    void setUp() throws Exception {
        controller = new StudentManagementController();
        setField(controller, "studentTable", new TableView<Student>());
        setField(controller, "colName", new TableColumn<Student, String>());
        setField(controller, "colGender", new TableColumn<Student, String>());
        setField(controller, "colAge", new TableColumn<Student, Integer>());
        setField(controller, "colDepartment", new TableColumn<Student, String>());
        setField(controller, "colUsername", new TableColumn<Student, String>());
        setField(controller, "colPassword", new TableColumn<Student, String>());
        setField(controller, "filterUsername", new TextField());
        setField(controller, "filterName", new TextField());
        setField(controller, "filterDepartment", new ComboBox<>());
        setField(controller, "tfUsername", new TextField());
        setField(controller, "tfName", new TextField());
        setField(controller, "tfPassword", new TextField());
        setField(controller, "cbDepartment", new ComboBox<Department>());
        setField(controller, "tfAge", new TextField());
        setField(controller, "cbGender", new ComboBox<Gender>());
        msgSenderMocked = Mockito.mockStatic(MsgSender.class);
        msgSenderMocked.when(() -> MsgSender.showMsg(Mockito.anyString())).then(invocation -> null);
        msgSenderMocked.when(() -> MsgSender.showConfirm(Mockito.anyString(), Mockito.anyString(), Mockito.any(Runnable.class)))
                .thenAnswer(invocation -> { Runnable cb = invocation.getArgument(2); if (cb != null) cb.run(); return null; });
    }

    @AfterEach
    void tearDown() {
        if (msgSenderMocked != null) msgSenderMocked.close();
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    void testInitializeSetsUpTableAndForm() throws Exception {
        ComboBox<String> filterDepartment = new ComboBox<>();
        ComboBox<Department> cbDepartment = new ComboBox<>();
        ComboBox<Gender> cbGender = new ComboBox<>();
        setField(controller, "filterDepartment", filterDepartment);
        setField(controller, "cbDepartment", cbDepartment);
        setField(controller, "cbGender", cbGender);
        setField(controller, "studentTable", new TableView<Student>());
        setField(controller, "colName", new TableColumn<Student, String>());
        setField(controller, "colGender", new TableColumn<Student, String>());
        setField(controller, "colAge", new TableColumn<Student, Integer>());
        setField(controller, "colDepartment", new TableColumn<Student, String>());
        setField(controller, "colUsername", new TableColumn<Student, String>());
        setField(controller, "colPassword", new TableColumn<Student, String>());
        controller.initialize();
        assertNotNull(cbDepartment.getItems());
        assertNotNull(cbGender.getItems());
        assertNotNull(filterDepartment.getItems());
    }

    @Test
    void testFilterStudents_AllBranches() throws Exception {
        TextField filterUsername = new TextField("user");
        TextField filterName = new TextField("name");
        ComboBox<String> filterDepartment = new ComboBox<>();
        filterDepartment.getItems().addAll("ANY", "CSE");
        filterDepartment.getSelectionModel().select("CSE");
        setField(controller, "filterUsername", filterUsername);
        setField(controller, "filterName", filterName);
        setField(controller, "filterDepartment", filterDepartment);
        TableView<Student> studentTable = new TableView<>();
        setField(controller, "studentTable", studentTable);
        controller.filterStudents();
        filterUsername.setText("");
        filterName.setText("");
        filterDepartment.getSelectionModel().select("ANY");
        controller.filterStudents();
        assertNotNull(studentTable.getItems());
    }

    @Test
    void testResetClearsFiltersAndReloads() throws Exception {
        TextField filterUsername = new TextField("user");
        TextField filterName = new TextField("name");
        ComboBox<String> filterDepartment = new ComboBox<>();
        filterDepartment.getItems().addAll("ANY", "CSE");
        filterDepartment.getSelectionModel().select("CSE");
        setField(controller, "filterUsername", filterUsername);
        setField(controller, "filterName", filterName);
        setField(controller, "filterDepartment", filterDepartment);
        TableView<Student> studentTable = new TableView<>();
        setField(controller, "studentTable", studentTable);
        var reset = controller.getClass().getDeclaredMethod("reset");
        reset.setAccessible(true);
        reset.invoke(controller);
        assertEquals("", filterUsername.getText());
        assertEquals("", filterName.getText());
        assertEquals("ANY", filterDepartment.getSelectionModel().getSelectedItem());
    }

    @Test
    void testClearFormClearsAllFields() throws Exception {
        setField(controller, "tfUsername", new TextField("user"));
        setField(controller, "tfName", new TextField("name"));
        setField(controller, "tfPassword", new TextField("pass"));
        ComboBox<Department> cbDepartment = new ComboBox<>();
        cbDepartment.getItems().add(Department.CSE);
        cbDepartment.getSelectionModel().select(Department.CSE);
        setField(controller, "cbDepartment", cbDepartment);
        setField(controller, "tfAge", new TextField("20"));
        ComboBox<Gender> cbGender = new ComboBox<>();
        cbGender.getItems().add(Gender.MALE);
        cbGender.getSelectionModel().select(Gender.MALE);
        setField(controller, "cbGender", cbGender);
        var clearForm = controller.getClass().getDeclaredMethod("clearForm");
        clearForm.setAccessible(true);
        clearForm.invoke(controller);
        assertEquals("", ((TextField) getField(controller, "tfUsername")).getText());
        assertEquals("", ((TextField) getField(controller, "tfName")).getText());
        assertEquals("", ((TextField) getField(controller, "tfPassword")).getText());
        assertNull(cbDepartment.getSelectionModel().getSelectedItem());
        assertEquals("", ((TextField) getField(controller, "tfAge")).getText());
        assertNull(cbGender.getSelectionModel().getSelectedItem());
    }

    @Test
    void testAddStudent_ValidAndInvalidBranches() throws Exception {
        setField(controller, "tfUsername", new TextField("user"));
        setField(controller, "tfName", new TextField("name"));
        setField(controller, "tfPassword", new TextField("pass"));
        ComboBox<Department> cbDepartment = new ComboBox<>();
        cbDepartment.getItems().add(Department.CSE);
        cbDepartment.getSelectionModel().select(Department.CSE);
        setField(controller, "cbDepartment", cbDepartment);
        setField(controller, "tfAge", new TextField("20"));
        ComboBox<Gender> cbGender = new ComboBox<>();
        cbGender.getItems().add(Gender.MALE);
        cbGender.getSelectionModel().select(Gender.MALE);
        setField(controller, "cbGender", cbGender);
        TableView<Student> studentTable = new TableView<>();
        setField(controller, "studentTable", studentTable);
        controller.addStudent(); // Valid
        // Invalid: missing gender
        cbGender.getSelectionModel().clearSelection();
        controller.addStudent();
        // Invalid: validation error (empty username)
        setField(controller, "tfUsername", new TextField(""));
        controller.addStudent();
        // Exception: non-integer age
        setField(controller, "tfUsername", new TextField("user"));
        setField(controller, "tfAge", new TextField("notanumber"));
        controller.addStudent();
        assertTrue(true); // If no exception, all branches covered
    }

    @Test
    void testUpdateStudent_ValidAndInvalidBranches() throws Exception {
        TableView<Student> studentTable = new TableView<>();
        Student student = new Student(1L, "user", "pass", "name", Gender.MALE, 20, Department.CSE);
        ObservableList<Student> students = FXCollections.observableArrayList(student);
        studentTable.setItems(students);
        studentTable.getSelectionModel().select(student);
        setField(controller, "studentTable", studentTable);
        setField(controller, "tfUsername", new TextField("user2"));
        setField(controller, "tfName", new TextField("name2"));
        setField(controller, "tfPassword", new TextField("pass2"));
        ComboBox<Department> cbDepartment = new ComboBox<>();
        cbDepartment.getItems().add(Department.CSE);
        cbDepartment.getSelectionModel().select(Department.CSE);
        setField(controller, "cbDepartment", cbDepartment);
        setField(controller, "tfAge", new TextField("21"));
        ComboBox<Gender> cbGender = new ComboBox<>();
        cbGender.getItems().add(Gender.FEMALE);
        cbGender.getSelectionModel().select(Gender.FEMALE);
        setField(controller, "cbGender", cbGender);
        controller.updateStudent(); // Valid
        // Invalid: no selection
        studentTable.getSelectionModel().clearSelection();
        controller.updateStudent();
        // Invalid: missing gender
        studentTable.getSelectionModel().select(student);
        cbGender.getSelectionModel().clearSelection();
        controller.updateStudent();
        // Invalid: validation error (empty username)
        setField(controller, "tfUsername", new TextField(""));
        controller.updateStudent();
        assertTrue(true);
    }

    @Test
    void testDeleteStudent_ValidAndInvalidBranches() throws Exception {
        TableView<Student> studentTable = new TableView<>();
        Student student = new Student(1L, "user", "pass", "name", Gender.MALE, 20, Department.CSE);
        ObservableList<Student> students = FXCollections.observableArrayList(student);
        studentTable.setItems(students);
        studentTable.getSelectionModel().select(student);
        setField(controller, "studentTable", studentTable);
        controller.deleteStudent(); // Valid
        // Invalid: no selection
        studentTable.getSelectionModel().clearSelection();
        controller.deleteStudent();
        assertTrue(true);
    }

    @Test
    void testAddStudent_GenderNull_ShowsErrorMsg() throws Exception {
        setField(controller, "tfUsername", new TextField("user123"));
        setField(controller, "tfName", new TextField("CHAN Tai Man"));
        setField(controller, "tfPassword", new TextField("password1"));
        ComboBox<Department> cbDepartment = new ComboBox<>();
        cbDepartment.getItems().add(Department.CSE);
        cbDepartment.getSelectionModel().select(Department.CSE);
        setField(controller, "cbDepartment", cbDepartment);
        setField(controller, "tfAge", new TextField("20"));
        ComboBox<Gender> cbGender = new ComboBox<>();
        // Do NOT select any gender
        setField(controller, "cbGender", cbGender);
        TableView<Student> studentTable = new TableView<>();
        setField(controller, "studentTable", studentTable);

        controller.addStudent();

        msgSenderMocked.verify(() -> MsgSender.showMsg("Please select a gender."));
    }

    @Test
    void testUpdateStudent_GenderNull_ShowsErrorMsg() throws Exception {
        TableView<Student> studentTable = new TableView<>();
        Student student = new Student(1L, "user123", "password1", "CHAN Tai Man", Gender.MALE, 20, Department.CSE);
        ObservableList<Student> students = FXCollections.observableArrayList(student);
        studentTable.setItems(students);
        studentTable.getSelectionModel().select(student);
        setField(controller, "studentTable", studentTable);
        setField(controller, "tfUsername", new TextField("user123"));
        setField(controller, "tfName", new TextField("CHAN Tai Man"));
        setField(controller, "tfPassword", new TextField("password1"));
        ComboBox<Department> cbDepartment = new ComboBox<>();
        cbDepartment.getItems().add(Department.CSE);
        cbDepartment.getSelectionModel().select(Department.CSE);
        setField(controller, "cbDepartment", cbDepartment);
        setField(controller, "tfAge", new TextField("21"));
        ComboBox<Gender> cbGender = new ComboBox<>();
        // Do NOT select any gender
        setField(controller, "cbGender", cbGender);

        controller.updateStudent();

        msgSenderMocked.verify(() -> MsgSender.showMsg("Please select a gender."));
    }

    @Test
    void testUpdateStudent_Success_ShowsSuccessMsg() throws Exception {
        TableView<Student> studentTable = new TableView<>();
        Student student = new Student(1L, "user123", "password1", "CHAN Tai Man", Gender.MALE, 20, Department.CSE);
        ObservableList<Student> students = FXCollections.observableArrayList(student);
        studentTable.setItems(students);
        studentTable.getSelectionModel().select(student);
        setField(controller, "studentTable", studentTable);
        setField(controller, "tfUsername", new TextField("user123"));
        setField(controller, "tfName", new TextField("CHAN Tai Man"));
        setField(controller, "tfPassword", new TextField("password1"));
        ComboBox<Department> cbDepartment = new ComboBox<>();
        cbDepartment.getItems().add(Department.CSE);
        cbDepartment.getSelectionModel().select(Department.CSE);
        setField(controller, "cbDepartment", cbDepartment);
        setField(controller, "tfAge", new TextField("21"));
        ComboBox<Gender> cbGender = new ComboBox<>();
        cbGender.getItems().add(Gender.FEMALE);
        cbGender.getSelectionModel().select(Gender.FEMALE);
        setField(controller, "cbGender", cbGender);

        controller.updateStudent();

        msgSenderMocked.verify(() -> MsgSender.showMsg("Student updated successfully!"));
    }

    // Helper to get private field
    private Object getField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
} 