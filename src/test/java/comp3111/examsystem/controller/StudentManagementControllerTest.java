package comp3111.examsystem.controller;

import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.entity.Student;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentManagementControllerTest {
    @InjectMocks
    StudentManagementController controller;

    TableView<Student> studentTable;
    TableColumn<Student, String> colName;
    TableColumn<Student, String> colGender;
    TableColumn<Student, Integer> colAge;
    TableColumn<Student, String> colDepartment;
    TableColumn<Student, String> colUsername;
    TableColumn<Student, String> colPassword;
    TextField filterUsername;
    TextField filterName;
    ComboBox<String> filterDepartment;
    TextField tfUsername;
    TextField tfName;
    TextField tfPassword;
    ComboBox<Department> cbDepartment;
    TextField tfAge;
    ComboBox<Gender> cbGender;

    private MockedStatic<MsgSender> msgSenderMockedStatic;

    FakeStudentDatabase studentDatabase;

    static class FakeStudentDatabase extends Database<Student> {
        List<Student> students = new ArrayList<>();
        public FakeStudentDatabase() { super(Student.class); }
        @Override public List<Student> getAllEnabled() { return new ArrayList<>(students); }
        @Override public void add(Student s) { students.add(s); }
        @Override public void delByKey(String key) { students.removeIf(s -> String.valueOf(s.getId()).equals(key)); }
        @Override public List<Student> queryFuzzyByField(String fieldName, String fieldValue) {
            if ("username".equals(fieldName)) {
                List<Student> result = new ArrayList<>();
                for (Student s : students) {
                    if (s.getUsername().contains(fieldValue)) result.add(s);
                }
                return result;
            }
            return new ArrayList<>();
        }
    }

    @BeforeAll
    static void initJfx() {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    @BeforeEach
    void setup() {
        // Patch MsgSender static methods
        msgSenderMockedStatic = mockStatic(MsgSender.class);
        msgSenderMockedStatic.when(() -> MsgSender.showMsg(anyString())).then(invocation -> null);
        msgSenderMockedStatic.when(() -> MsgSender.showConfirm(anyString(), anyString(), any())).then(invocation -> {
            Runnable callback = invocation.getArgument(2);
            callback.run();
            return null;
        });
        // Inject test double for Database<Student>
        studentDatabase = new FakeStudentDatabase();
        try {
            java.lang.reflect.Field dbField = StudentManagementController.class.getDeclaredField("studentDatabase");
            dbField.setAccessible(true);
            dbField.set(controller, studentDatabase);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Create and inject real JavaFX controls
        studentTable = new TableView<>();
        colName = new TableColumn<>();
        colGender = new TableColumn<>();
        colAge = new TableColumn<>();
        colDepartment = new TableColumn<>();
        colUsername = new TableColumn<>();
        colPassword = new TableColumn<>();
        filterUsername = new TextField();
        filterName = new TextField();
        filterDepartment = new ComboBox<>();
        tfUsername = new TextField();
        tfName = new TextField();
        tfPassword = new TextField();
        cbDepartment = new ComboBox<>();
        tfAge = new TextField();
        cbGender = new ComboBox<>();
        setField(controller, "studentTable", studentTable);
        setField(controller, "colName", colName);
        setField(controller, "colGender", colGender);
        setField(controller, "colAge", colAge);
        setField(controller, "colDepartment", colDepartment);
        setField(controller, "colUsername", colUsername);
        setField(controller, "colPassword", colPassword);
        setField(controller, "filterUsername", filterUsername);
        setField(controller, "filterName", filterName);
        setField(controller, "filterDepartment", filterDepartment);
        setField(controller, "tfUsername", tfUsername);
        setField(controller, "tfName", tfName);
        setField(controller, "tfPassword", tfPassword);
        setField(controller, "cbDepartment", cbDepartment);
        setField(controller, "tfAge", tfAge);
        setField(controller, "cbGender", cbGender);
        cbDepartment.getItems().addAll(Department.values());
        cbGender.getItems().addAll(Gender.values());
        // Call controller.initialize() to set up listeners and bindings
        controller.initialize();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        if (msgSenderMockedStatic != null) {
            msgSenderMockedStatic.close();
        }
    }

    @Test
    void testAddStudent_Success() {
        tfUsername.setText("testuser");
        tfName.setText("Test User");
        tfPassword.setText("password123");
        cbDepartment.setValue(Department.CSE);
        cbGender.setValue(Gender.MALE);
        tfAge.setText("20");
        controller.addStudent();
        assertEquals(1, studentDatabase.students.size());
        verifyStaticMsg("Student added successfully!");
    }

    @Test
    void testAddStudent_InvalidInput() {
        tfUsername.setText("bad"); // too short
        tfName.setText("Test User");
        tfPassword.setText("pass"); // too short
        cbDepartment.setValue(Department.CSE);
        cbGender.setValue(Gender.MALE);
        tfAge.setText("200"); // invalid age
        controller.addStudent();
        assertEquals(0, studentDatabase.students.size());
        verifyStaticMsgContains("Invalid");
    }

    @Test
    void testAddStudent_MissingGender() {
        tfUsername.setText("testuser");
        tfName.setText("Test User");
        tfPassword.setText("password123");
        cbDepartment.setValue(Department.CSE);
        cbGender.setValue(null);
        tfAge.setText("20");
        controller.addStudent();
        assertEquals(0, studentDatabase.students.size());
        verifyStaticMsg("Please select a gender.");
    }

    @Test
    void testUpdateStudent_Success() {
        Student student = new Student(1L, "testuser", "password123", "Test User", Gender.MALE, 20, Department.CSE);
        TableView.TableViewSelectionModel<Student> selectionModel = studentTable.getSelectionModel();
        studentDatabase.students.add(student);
        studentTable.getItems().add(student);
        selectionModel.select(student);
        studentTable.requestFocus();
        studentTable.getSelectionModel().select(0);
        tfUsername.setText("testuser2");
        tfName.setText("Test UserX");
        tfPassword.setText("password456");
        cbDepartment.setValue(Department.MATH);
        cbGender.setValue(Gender.FEMALE);
        tfAge.setText("22");
        // Debug print before update
        System.out.println("Before update: " + student);
        controller.updateStudent();
        // Debug print after update
        System.out.println("After update: " + student);
        Student updated = studentTable.getItems().get(0);
        System.out.println("From table: " + updated);
        System.out.println("student == updated: " + (student == updated));
        System.out.println("student fields: username=" + student.getUsername() + ", name=" + student.getName());
        System.out.println("updated fields: username=" + updated.getUsername() + ", name=" + updated.getName());
        System.out.println("All students in table after update:");
        for (Student st : studentTable.getItems()) {
            System.out.println("Student: username=" + st.getUsername() + ", name=" + st.getName());
        }
        assertEquals("testuser2", updated.getUsername());
        assertEquals("Test UserX", updated.getName());
        assertEquals("password456", updated.getPassword());
        assertEquals(Department.MATH, updated.getDepartment());
        assertEquals(Gender.FEMALE, updated.getGender());
        assertEquals(22, updated.getAge());
        verifyStaticMsg("Student updated successfully!");
    }

    @Test
    void testUpdateStudent_NoSelection() {
        studentTable.getSelectionModel().clearSelection();
        controller.updateStudent();
        verifyStaticMsg("Please select a student to update.");
    }

    @Test
    void testDeleteStudent_Success() {
        Student student = new Student(1L, "testuser", "password123", "Test User", Gender.MALE, 20, Department.CSE);
        studentDatabase.students.add(student);
        studentTable.getItems().add(student);
        studentTable.getSelectionModel().select(student);
        controller.deleteStudent();
        assertEquals(0, studentDatabase.students.size());
        verifyStaticMsg("Student deleted successfully.");
    }

    @Test
    void testDeleteStudent_NoSelection() {
        studentTable.getSelectionModel().clearSelection();
        controller.deleteStudent();
        verifyStaticMsg("Please select a student to delete.");
    }

    @Test
    void testFilterStudents_ByUsername() {
        filterUsername.setText("testuser");
        filterName.setText("");
        filterDepartment.setValue("ANY");
        Student s = new Student(1L, "testuser", "password123", "Test User", Gender.MALE, 20, Department.CSE);
        studentDatabase.students.add(s);
        List<Student> filtered = List.of(s);
        controller.filterStudents();
        assertEquals(FXCollections.observableArrayList(filtered), studentTable.getItems());
    }

    // --- Helper methods for static MsgSender verification ---
    private void verifyStaticMsg(String expected) {
        msgSenderMockedStatic.verify(() -> MsgSender.showMsg(eq(expected)));
    }
    private void verifyStaticMsgContains(String expectedSubstring) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        msgSenderMockedStatic.verify(() -> MsgSender.showMsg(captor.capture()));
        String actual = captor.getValue();
        assertTrue(actual.contains(expectedSubstring), "Expected message to contain: " + expectedSubstring);
    }
} 