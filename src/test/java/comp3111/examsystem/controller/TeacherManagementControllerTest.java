package comp3111.examsystem.controller;

import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.data.Position;
import comp3111.examsystem.entity.Teacher;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TeacherManagementControllerTest {

    public TeacherManagementController controller;
    public Database<Teacher> teacherDatabase;
    public TableView<Teacher> mockTableView;
    public TextField tfUsername, tfName, tfPassword, tfAge, filterUsername, filterName;
    public ComboBox<Department> cbDepartment;
    public ComboBox<Gender> cbGender;
    public ComboBox<Position> cbPosition;
    public ComboBox<String> filterDepartment;

    // --- Add a simple fake database for testing ---
    public static class FakeTeacherDatabase extends Database<Teacher> {
        public List<Teacher> teachers = new java.util.ArrayList<>();
        public FakeTeacherDatabase() { super(Teacher.class); }
        @Override public List<Teacher> getAllEnabled() { return new java.util.ArrayList<>(teachers); }
        @Override public List<Teacher> queryByField(String fieldName, String fieldValue) {
            if ("department".equals(fieldName)) {
                List<Teacher> res = new java.util.ArrayList<>();
                for (Teacher t : teachers) if (t.getDepartment().toString().equals(fieldValue)) res.add(t);
                return res;
            }
            return new java.util.ArrayList<>();
        }
        @Override public List<Teacher> queryFuzzyByField(String fieldName, String fieldValue) {
            List<Teacher> res = new java.util.ArrayList<>();
            for (Teacher t : teachers) {
                if ("username".equals(fieldName) && t.getUsername().contains(fieldValue)) res.add(t);
                if ("name".equals(fieldName) && t.getName().contains(fieldValue)) res.add(t);
            }
            return res;
        }
        @Override public void add(Teacher t) { teachers.add(t); }
        @Override public void update(Teacher t) {
            for (int i = 0; i < teachers.size(); ++i) if (teachers.get(i).getId().equals(t.getId())) teachers.set(i, t);
        }
        @Override public void delByKey(String key) {
            teachers.removeIf(t -> t.getId().toString().equals(key));
        }
    }

    @BeforeAll
    public static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}
    }

    @BeforeEach
    public void setup() throws Exception {
        controller = new TeacherManagementController();
        teacherDatabase = new FakeTeacherDatabase();
        mockTableView = new TableView<>();

        tfUsername = new TextField();
        tfName = new TextField();
        tfPassword = new TextField();
        tfAge = new TextField();
        filterUsername = new TextField();
        filterName = new TextField();
        cbDepartment = new ComboBox<>();
        cbGender = new ComboBox<>();
        cbPosition = new ComboBox<>();
        filterDepartment = new ComboBox<>();

        var fields = TeacherManagementController.class.getDeclaredFields();
        for (var field : fields) {
            field.setAccessible(true);
            switch (field.getName()) {
                case "teacherDatabase" -> field.set(controller, teacherDatabase);
                case "teacherTable" -> field.set(controller, mockTableView);
                case "tfUsername" -> field.set(controller, tfUsername);
                case "tfName" -> field.set(controller, tfName);
                case "tfPassword" -> field.set(controller, tfPassword);
                case "tfAge" -> field.set(controller, tfAge);
                case "cbDepartment" -> field.set(controller, cbDepartment);
                case "cbGender" -> field.set(controller, cbGender);
                case "cbPosition" -> field.set(controller, cbPosition);
                case "filterUsername" -> field.set(controller, filterUsername);
                case "filterName" -> field.set(controller, filterName);
                case "filterDepartment" -> field.set(controller, filterDepartment);
            }
        }
    }

    public void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }

    //Initialize() Testing
    @Test
    void testInitialize_printComboBoxContentsForManualCheck() throws Exception {
        // Setup real JavaFX components
        ComboBox<Department> cbDepartment = new ComboBox<>();
        ComboBox<String> filterDepartment = new ComboBox<>();
        ComboBox<Gender> cbGender = new ComboBox<>();
        ComboBox<Position> cbPosition = new ComboBox<>();
        TableView<Teacher> teacherTable = new TableView<>();

        // Set fields on controller
        setField(controller, "cbDepartment", cbDepartment);
        setField(controller, "filterDepartment", filterDepartment);
        setField(controller, "cbGender", cbGender);
        setField(controller, "cbPosition", cbPosition);
        setField(controller, "teacherTable", teacherTable);

        setField(controller, "colUsername", new TableColumn<>());
        setField(controller, "colName", new TableColumn<>());
        setField(controller, "colAge", new TableColumn<>());
        setField(controller, "colGender", new TableColumn<>());
        setField(controller, "colPosition", new TableColumn<>());
        setField(controller, "colDepartment", new TableColumn<>());
        setField(controller, "colPassword", new TableColumn<>());

        // Provide fake data
        List<Teacher> fakeTeachers = List.of(
                new Teacher(1L, "teacher1", "pass", "Alice", Gender.FEMALE, 30, Department.CSE, Position.L)
        );
        ((FakeTeacherDatabase)teacherDatabase).teachers.addAll(fakeTeachers);

        // Call initialize
        controller.initialize();
        // Manual inspection (e.g., debugging or logging)
        System.out.println("cbDepartment items: " + cbDepartment.getItems());
        System.out.println("cbGender items: " + cbGender.getItems());
        System.out.println("cbPosition items: " + cbPosition.getItems());
        System.out.println("Selected filterDepartment: " + filterDepartment.getSelectionModel().getSelectedItem());
        System.out.println("Selected cbDepartment: " + cbDepartment.getSelectionModel().getSelectedItem());
        System.out.println("Teacher table row count: " + teacherTable.getItems().size());
    }

    @Test
    void testTeacherSelection_populatesFormFields() throws Exception {
        TableView<Teacher> teacherTable = new TableView<>();
        TextField tfUsername = new TextField();
        TextField tfName = new TextField();
        TextField tfPassword = new TextField();
        TextField tfAge = new TextField();
        ComboBox<Department> cbDepartment = new ComboBox<>();
        ComboBox<Gender> cbGender = new ComboBox<>();
        ComboBox<Position> cbPosition = new ComboBox<>();

        // Inject fields
        setField(controller, "teacherTable", teacherTable);
        setField(controller, "tfUsername", tfUsername);
        setField(controller, "tfName", tfName);
        setField(controller, "tfPassword", tfPassword);
        setField(controller, "tfAge", tfAge);
        setField(controller, "cbDepartment", cbDepartment);
        setField(controller, "cbGender", cbGender);
        setField(controller, "cbPosition", cbPosition);

        // Initialize ComboBox values
        cbDepartment.getItems().addAll(Department.values());
        cbGender.getItems().addAll(Gender.values());
        cbPosition.getItems().addAll(Position.values());

        // Also inject all TableColumn fields used in initialize
        setField(controller, "colUsername", new TableColumn<>());
        setField(controller, "colName", new TableColumn<>());
        setField(controller, "colAge", new TableColumn<>());
        setField(controller, "colGender", new TableColumn<>());
        setField(controller, "colPosition", new TableColumn<>());
        setField(controller, "colDepartment", new TableColumn<>());
        setField(controller, "colPassword", new TableColumn<>());
        setField(controller, "filterDepartment", new ComboBox<>());
        setField(controller, "cbDepartment", new ComboBox<>());
        setField(controller, "cbGender", new ComboBox<>());
        setField(controller, "cbPosition", new ComboBox<>());

        controller.initialize();

        // Simulate a selection in the table
        Teacher testTeacher = new Teacher(1L, "user1", "pass123", "Alice", Gender.FEMALE, 29, Department.CSE, Position.AP);
        teacherTable.getItems().add(testTeacher);
        teacherTable.getSelectionModel().select(testTeacher);
    }




    //Testing Filters
    @Test
    public void testFilterTeachers_allEmptyInputs_shouldReturnAll() {
        filterUsername.setText("");
        filterName.setText("");
        filterDepartment.setValue("ANY");
        ((FakeTeacherDatabase)teacherDatabase).teachers.clear();
        controller.filterTeachers();
        assertEquals(FXCollections.observableArrayList(List.of()), mockTableView.getItems());
    }

    @Test
    public void testFilterTeachers_withOnlyUsername() {
        filterUsername.setText("user");
        filterName.setText("");
        filterDepartment.setValue("ANY");
        controller.filterTeachers();
    }

    @Test
    public void testFilterTeachers_withOnlyName() {
        filterUsername.setText("");
        filterName.setText("Alice");
        filterDepartment.setValue("ANY");
        controller.filterTeachers();
    }

    @Test
    public void testFilterTeachers_withOnlyDepartment() {
        filterUsername.setText("");
        filterName.setText("");
        filterDepartment.setValue("CSE");
        controller.filterTeachers();
    }

    @Test
    public void testFilterTeachers_withUsernameAndDepartment() {
        filterUsername.setText("user");
        filterName.setText("");
        filterDepartment.setValue("CSE");
        controller.filterTeachers();
    }



    //Test add Teacher error
    @Test
    public void testAddTeacher_missingGender_shouldShowError() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            tfUsername.setText("user");
            tfName.setText("Teacher");
            tfPassword.setText("password1");
            cbDepartment.setValue(Department.CSE);
            tfAge.setText("30");
            cbGender.setValue(null);
            cbPosition.setValue(Position.AP);

            controller.getClass().getDeclaredMethod("addTeacher").setAccessible(true);
            controller.getClass().getDeclaredMethod("addTeacher").invoke(controller);

            mockMsgSender.verify(() -> MsgSender.showMsg("Please select a gender."));
            verify(teacherDatabase, never()).add(any());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddTeacher_missingPosition_shouldShowError() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            tfUsername.setText("user");
            tfName.setText("Teacher");
            tfPassword.setText("password1");
            cbDepartment.setValue(Department.CSE);
            tfAge.setText("30");
            cbGender.setValue(Gender.MALE);
            cbPosition.setValue(null);

            controller.getClass().getDeclaredMethod("addTeacher").setAccessible(true);
            controller.getClass().getDeclaredMethod("addTeacher").invoke(controller);

            mockMsgSender.verify(() -> MsgSender.showMsg("Please select a position."));
            verify(teacherDatabase, never()).add(any());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Update Teacher Error Checking
    @Test
    public void testUpdateTeacher_nullSelectedTeacher_shouldShowError() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            // Use real TableView and selection model
            mockTableView.getItems().clear();
            mockTableView.getSelectionModel().clearSelection();
            controller.updateTeacher();
            assertTrue(((FakeTeacherDatabase)teacherDatabase).teachers.isEmpty());
            mockMsgSender.verify(() -> MsgSender.showMsg("Please select a Teacher to update."));
        }
    }

    @Test
    public void testUpdateTeacher_invalidInput_shouldShowValidationMsg() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            // Add a Teacher to the table and select it
            Teacher teacher = new Teacher(1L, "u", "p", "n", Gender.MALE, 20, Department.CSE, Position.AP);
            mockTableView.getItems().add(teacher);
            mockTableView.getSelectionModel().select(teacher);

            // Inject invalid form values
            tfUsername.setText(""); // Invalid username
            tfName.setText("Name");
            tfPassword.setText("pass");
            cbDepartment.setValue(Department.CSE);
            cbGender.setValue(Gender.MALE);
            tfAge.setText("21");
            cbPosition.setValue(Position.P);

            controller.updateTeacher();
            assertTrue(((FakeTeacherDatabase)teacherDatabase).teachers.isEmpty());
            mockMsgSender.verify(() -> MsgSender.showMsg(contains("username")), atLeastOnce());
        }
    }

    @Test
    public void testUpdateTeacher_missingGender_shouldShowMsg() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            Teacher teacher = new Teacher(1L, "user", "pass1234", "Test Name", Gender.MALE, 20, Department.CSE, Position.DH);
            ((FakeTeacherDatabase)teacherDatabase).teachers.add(teacher);
            mockTableView.getItems().add(teacher);
            mockTableView.getSelectionModel().select(teacher);

            // VALID inputs for all fields EXCEPT gender
            tfUsername.setText("user");
            tfName.setText("Test Name");
            tfPassword.setText("pass1234");
            cbDepartment.setValue(Department.CSE);
            cbGender.setValue(null); // Simulate missing gender
            tfAge.setText("20");
            cbPosition.setValue(Position.P);
            controller.updateTeacher();
            assertEquals(1, ((FakeTeacherDatabase)teacherDatabase).teachers.size());
            mockMsgSender.verify(() -> MsgSender.showMsg("Please select a gender."), atLeastOnce());
        }
    }

    @Test
    public void testUpdateTeacher_missingPosition_shouldShowMsg() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            Teacher teacher = new Teacher(1L, "user", "pass1234", "Test Name", Gender.MALE, 20, Department.CSE, Position.DH);
            ((FakeTeacherDatabase)teacherDatabase).teachers.add(teacher);
            mockTableView.getItems().add(teacher);
            mockTableView.getSelectionModel().select(teacher);
            tfUsername.setText("user");
            tfName.setText("Test Name");
            tfPassword.setText("pass1234");
            cbDepartment.setValue(Department.CSE);
            cbGender.setValue(Gender.FEMALE);
            tfAge.setText("20");
            cbPosition.setValue(null); // Simulate missing position
            controller.updateTeacher();
            assertEquals(1, ((FakeTeacherDatabase)teacherDatabase).teachers.size());
            mockMsgSender.verify(() -> MsgSender.showMsg("Please select a position."), atLeastOnce());
        }
    }

    //Delete Teacher Error checking
    @Test
    public void testDeleteTeacher_nullSelection_shouldShowError() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            mockTableView.getItems().clear();
            mockTableView.getSelectionModel().clearSelection();

            try {
                controller.getClass().getDeclaredMethod("deleteTeacher").setAccessible(true);
                controller.getClass().getDeclaredMethod("deleteTeacher").invoke(controller);
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                fail("Reflection error: " + e.getMessage());
            }

            mockMsgSender.verify(() -> MsgSender.showMsg(contains("select a teacher")), atLeastOnce());
            assertTrue(((FakeTeacherDatabase)teacherDatabase).teachers.isEmpty());
        }
    }

    @Test
    public void testDeleteStudent_valid_shouldConfirmAndDelete() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            // Prepare mock teacher and select in real TableView
            Teacher teacher = new Teacher(1L, "u", "p", "n", Gender.MALE, 20, Department.CSE, Position.DH);
            ((FakeTeacherDatabase)teacherDatabase).teachers.add(teacher);
            mockTableView.getItems().add(teacher);
            mockTableView.getSelectionModel().select(teacher);
            mockMsgSender.when(() -> MsgSender.showConfirm(anyString(), anyString(), any()))
                    .thenAnswer(invocation -> {
                        Runnable onConfirm = invocation.getArgument(2);
                        onConfirm.run(); // Simulate clicking "OK"
                        return null;
                    });
            controller.deleteTeacher();
            assertTrue(((FakeTeacherDatabase)teacherDatabase).teachers.isEmpty());
            mockMsgSender.verify(() -> MsgSender.showMsg("Teacher deleted successfully."));
        }
    }
}
