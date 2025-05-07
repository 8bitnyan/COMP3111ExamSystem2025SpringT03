package comp3111.examsystem.controller;
import comp3111.examsystem.data.Department;
import comp3111.examsystem.entity.Course;
import comp3111.examsystem.entity.Exam;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class CourseManagementControllerTest {

    public CourseManagementController controller;
    public Database<Course> mockCourseDatabase;
    public TableView<Course> mockTableView;
    public TextField tfCourseCode, tfCourseName, filterCourseCode, filterCourseName;
    public ComboBox<Department> cbDepartment;
    public ComboBox<String> filterDepartment;

    static class FakeCourseDatabase extends Database<Course> {
        List<Course> courses = new ArrayList<>();
        public FakeCourseDatabase() { super(Course.class); }
        @Override public List<Course> getAllEnabled() { return new ArrayList<>(courses); }
        @Override public void add(Course c) { courses.add(c); }
        @Override public void update(Course c) { /* update logic if needed */ }
        @Override public void delByKey(String key) { courses.removeIf(c -> String.valueOf(c.getId()).equals(key)); }
        // Implement other methods as needed for your tests
    }

    static class FakeExamDatabase extends Database<Exam> {
        List<Exam> exams = new ArrayList<>();
        public FakeExamDatabase() { super(Exam.class); }
        @Override public List<Exam> getAllEnabled() { return new ArrayList<>(exams); }
        @Override public void delByKey(String key) { exams.removeIf(e -> String.valueOf(e.getId()).equals(key)); }
    }

    @BeforeAll
    public static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // already initialized
        }
    }

    @BeforeEach
    public void setup() throws Exception {
        controller = new CourseManagementController();
        mockCourseDatabase = new FakeCourseDatabase();
        mockTableView = new TableView<>();
        tfCourseCode = new TextField();
        tfCourseName = new TextField();
        filterCourseCode = new TextField();
        filterCourseName = new TextField();
        cbDepartment = new ComboBox<>();
        filterDepartment = new ComboBox<>();

        for (var field : CourseManagementController.class.getDeclaredFields()) {
            field.setAccessible(true);
            switch (field.getName()) {
                case "courseDatabase" -> field.set(controller, mockCourseDatabase);
                case "courseTable" -> field.set(controller, mockTableView);
                case "tfCourseCode" -> field.set(controller, tfCourseCode);
                case "tfCourseName" -> field.set(controller, tfCourseName);
                case "cbDepartment" -> field.set(controller, cbDepartment);
                case "filterCourseCode" -> field.set(controller, filterCourseCode);
                case "filterCourseName" -> field.set(controller, filterCourseName);
                case "filterDepartment" -> field.set(controller, filterDepartment);
            }
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private Object getPrivateField(Object target, String fieldName) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException("Failed to get field: " + fieldName, e);
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    //Filter Test
    @Test
    public void testFilterCourses_allEmpty_shouldShowAll() {
        filterCourseCode.setText("");
        filterCourseName.setText("");
        filterDepartment.getItems().add("ANY");
        filterDepartment.getSelectionModel().select("ANY");
        ((FakeCourseDatabase)mockCourseDatabase).courses.clear();
        controller.filterCourses();
        assert mockTableView.getItems().isEmpty();
    }

    @Test
    public void testFilterCourses_byCourseCode() {
        filterCourseCode.setText("COMP");
        filterCourseName.setText("");
        filterDepartment.getItems().add("ANY");
        filterDepartment.getSelectionModel().select("ANY");
        List<Course> mockList = List.of(
                new Course(11L, "COMP2011", "Intro to C++", Department.CSE),
                new Course(22L, "MATH2111", "Matrix Algebra", Department.MATH)
        );
        ((FakeCourseDatabase)mockCourseDatabase).courses.clear();
        ((FakeCourseDatabase)mockCourseDatabase).courses.addAll(mockList);
        controller.filterCourses();
        assert mockTableView.getItems().stream().allMatch(c -> c.getCourseCode().contains("COMP"));
    }
    @Test
    public void testFilterCourses_byCourseName() {
        filterCourseCode.setText("");
        filterCourseName.setText("intro");
        filterDepartment.getItems().add("ANY");
        filterDepartment.getSelectionModel().select("ANY");
        List<Course> mockList = List.of(
                new Course(11L, "COMP2011", "Intro to C++", Department.CSE),
                new Course(22L, "MATH2111", "Matrix Algebra", Department.MATH)
        );
        ((FakeCourseDatabase)mockCourseDatabase).courses.clear();
        ((FakeCourseDatabase)mockCourseDatabase).courses.addAll(mockList);
        controller.filterCourses();
        assert mockTableView.getItems().stream().allMatch(c -> c.getCourseName().toLowerCase().contains("intro"));
    }

    @Test
    public void testFilterCourses_byDepartment() {
        filterCourseCode.setText("");
        filterCourseName.setText("");
        filterDepartment.getItems().add("CSE");
        filterDepartment.getSelectionModel().select("CSE");
        List<Course> mockList = List.of(
                new Course(11L, "COMP2011", "Intro to C++", Department.CSE),
                new Course(22L, "MATH2023", "Linear Algebra", Department.MATH)
        );
        ((FakeCourseDatabase)mockCourseDatabase).courses.clear();
        ((FakeCourseDatabase)mockCourseDatabase).courses.addAll(mockList);
        controller.filterCourses();
        assert mockTableView.getItems().stream().allMatch(c -> c.getDepartment() == Department.CSE);
    }

    @Test
    public void testFilterCourses_combinedFilters() {
        filterCourseCode.setText("COMP");
        filterCourseName.setText("Java");
        filterDepartment.getItems().add("CSE");
        filterDepartment.getSelectionModel().select("CSE");
        List<Course> mockList = List.of(
                new Course(1L, "COMP2011", "Java Basics", Department.CSE),
                new Course(2L, "COMP3031", "Advanced Java", Department.CSE),
                new Course(3L, "COMP2611", "Computer Architecture", Department.CSE) // won't match
        );
        ((FakeCourseDatabase)mockCourseDatabase).courses.clear();
        ((FakeCourseDatabase)mockCourseDatabase).courses.addAll(mockList);
        controller.filterCourses();
        assert mockTableView.getItems().size() == 2;
        assert mockTableView.getItems().stream().allMatch(c ->
                c.getCourseCode().contains("COMP") &&
                        c.getCourseName().toLowerCase().contains("java") &&
                        c.getDepartment() == Department.CSE);
    }

    //Add Course Test
    @Test
    public void testAddCourse_missingFields_shouldShowError() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            tfCourseCode.setText("");
            tfCourseName.setText("");
            cbDepartment.getItems().add(Department.CSE);
            cbDepartment.getSelectionModel().clearSelection();
            controller.addCourse();
            assert ((FakeCourseDatabase)mockCourseDatabase).courses.isEmpty();
            mockMsgSender.verify(() -> MsgSender.showMsg(contains("All fields must be filled")), atLeastOnce());
        }
    }

    @Test
    public void testAddCourse_validInput_shouldSucceed() {
        try (MockedStatic<MsgSender> mockMsg = mockStatic(MsgSender.class)) {
            tfCourseCode.setText("COMP1234");
            tfCourseName.setText("Software Engineering");
            cbDepartment.getItems().add(Department.CSE);
            cbDepartment.getSelectionModel().select(Department.CSE);
            ((FakeCourseDatabase)mockCourseDatabase).courses.clear();
            controller.allCourses = FXCollections.observableArrayList();
            controller.addCourse();
            assert ((FakeCourseDatabase)mockCourseDatabase).courses.size() == 1;
            mockMsg.verify(() -> MsgSender.showMsg("Course added successfully!"), atLeastOnce());
        }
    }

    @Test
    public void testAddCourse_addThrowsException_shouldShowFailureMsg() {
        try (MockedStatic<MsgSender> mockMsg = mockStatic(MsgSender.class)) {
            tfCourseCode.setText("COMP9999");
            tfCourseName.setText("Systems Programming");
            cbDepartment.getItems().add(Department.CSE);
            cbDepartment.getSelectionModel().select(Department.CSE);
            ((FakeCourseDatabase)mockCourseDatabase).courses.clear();
            controller.allCourses = FXCollections.observableArrayList();
            // Simulate exception by overriding add
            Database<Course> throwingDb = new FakeCourseDatabase() {
                @Override public void add(Course c) { throw new RuntimeException("DB error"); }
            };
            for (var field : CourseManagementController.class.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getName().equals("courseDatabase")) field.set(controller, throwingDb);
            }
            controller.addCourse();
            mockMsg.verify(() -> MsgSender.showMsg("Failed to add course."), atLeastOnce());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    public void testAddCourse_missingFields_shouldShowValidationMsg() {
        try (MockedStatic<MsgSender> mockMsg = mockStatic(MsgSender.class)) {
            tfCourseCode.setText("COMP1000");
            tfCourseName.setText("");
            cbDepartment.getItems().add(Department.CSE);
            cbDepartment.getSelectionModel().clearSelection();
            controller.addCourse();
            assert ((FakeCourseDatabase)mockCourseDatabase).courses.isEmpty();
            mockMsg.verify(() -> MsgSender.showMsg("All fields must be filled correctly."), atLeastOnce());
        }
    }


    //Update Course Test
    @Test
    public void testUpdateCourse_nullSelected_shouldShowError() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            mockTableView.getItems().clear();
            mockTableView.getSelectionModel().clearSelection();
            controller.updateCourse();
            assert ((FakeCourseDatabase)mockCourseDatabase).courses.isEmpty();
            mockMsgSender.verify(() -> MsgSender.showMsg(contains("select a Course")), atLeastOnce());
        }
    }

    @Test
    public void testUpdateCourse_invalidFields_shouldShowMsg() {
        Course course = new Course(1L, "COMP1011", "Intro", Department.CSE);
        mockTableView.getItems().add(course);
        mockTableView.getSelectionModel().select(course);
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            tfCourseCode.setText("");
            tfCourseName.setText("New Name");
            cbDepartment.getItems().add(Department.CSE);
            cbDepartment.getSelectionModel().select(Department.CSE);
            controller.updateCourse();
            assert ((FakeCourseDatabase)mockCourseDatabase).courses.isEmpty();
            mockMsgSender.verify(() -> MsgSender.showMsg("All fields must be filled correctly."), atLeastOnce());
        }
    }

    @Test
    public void testUpdateCourse_validFields_shouldUpdateSuccessfully() {
        Course course = new Course(1L, "COMP1011", "Intro", Department.CSE);
        mockTableView.getItems().add(course);
        mockTableView.getSelectionModel().select(course);
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            tfCourseCode.setText("COMP2022");
            tfCourseName.setText("Advanced Java");
            cbDepartment.getItems().add(Department.CSE);
            cbDepartment.getSelectionModel().select(Department.CSE);
            ((FakeCourseDatabase)mockCourseDatabase).courses.add(course);
            controller.updateCourse();
            mockMsgSender.verify(() -> MsgSender.showMsg("Course updated successfully!"));
        }
    }

    @Test
    public void testUpdateCourse_databaseThrowsException_shouldShowFailureMsg() {
        Course course = new Course(1L, "COMP1011", "Intro", Department.CSE);
        mockTableView.getItems().add(course);
        mockTableView.getSelectionModel().select(course);
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            tfCourseCode.setText("COMP3030");
            tfCourseName.setText("Exception Handling");
            cbDepartment.getItems().add(Department.CSE);
            cbDepartment.getSelectionModel().select(Department.CSE);
            Database<Course> throwingDb = new FakeCourseDatabase() {
                @Override public void update(Course c) { throw new RuntimeException(); }
            };
            for (var field : CourseManagementController.class.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getName().equals("courseDatabase")) field.set(controller, throwingDb);
            }
            controller.updateCourse();
            mockMsgSender.verify(() -> MsgSender.showMsg("Failed to update course."), atLeastOnce());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    //Delete Course Test
    @Test
    public void testDeleteCourse_nullSelection_shouldShowError() {
        try (MockedStatic<MsgSender> mockMsgSender = mockStatic(MsgSender.class)) {
            mockTableView.getItems().clear();
            mockTableView.getSelectionModel().clearSelection();
            controller.deleteCourse();
            assert ((FakeCourseDatabase)mockCourseDatabase).courses.isEmpty();
            mockMsgSender.verify(() -> MsgSender.showMsg(contains("select a course")), atLeastOnce());
        }
    }

    class TestableCourseManagementController extends CourseManagementController {
        Database<Exam> fakeExamDb;
        @Override
        protected Database<Exam> createExamDatabase() {
            return fakeExamDb;
        }
    }

    @Test
    public void testDeleteCourse_valid_noAssociatedExams() {
        Course selectedCourse = new Course(1L, "COMP1011", "Intro", Department.CSE);
        mockTableView.getItems().add(selectedCourse);
        mockTableView.getSelectionModel().select(selectedCourse);
        ComboBox<Department> cbMock = new ComboBox<>();
        cbMock.getItems().add(Department.CSE);
        cbMock.getSelectionModel().select(Department.CSE);
        try {
            var field = CourseManagementController.class.getDeclaredField("cbDepartment");
            field.setAccessible(true);
            field.set(controller, cbMock);
        } catch (Exception e) {
            throw new RuntimeException("Injection failed", e);
        }
        try (MockedStatic<MsgSender> mockMsg = mockStatic(MsgSender.class)) {
            mockMsg.when(() -> MsgSender.showConfirm(anyString(), anyString(), any())).thenAnswer(inv -> {
                Runnable confirm = inv.getArgument(2);
                confirm.run();
                return null;
            });
            FakeExamDatabase fakeExamDb = new FakeExamDatabase();
            TestableCourseManagementController testController = new TestableCourseManagementController();
            testController.fakeExamDb = fakeExamDb;
            // Set private fields using reflection
            setPrivateField(testController, "courseTable", mockTableView);
            setPrivateField(testController, "allCourses", controller.allCourses);
            setPrivateField(testController, "filterCourseCode", filterCourseCode);
            setPrivateField(testController, "filterCourseName", filterCourseName);
            setPrivateField(testController, "filterDepartment", filterDepartment);
            setPrivateField(testController, "tfCourseCode", tfCourseCode);
            setPrivateField(testController, "tfCourseName", tfCourseName);
            setPrivateField(testController, "cbDepartment", cbMock);
            setPrivateField(testController, "colCourseCode", getPrivateField(controller, "colCourseCode"));
            setPrivateField(testController, "colCourseName", getPrivateField(controller, "colCourseName"));
            setPrivateField(testController, "colDepartment", getPrivateField(controller, "colDepartment"));
            setPrivateField(testController, "courseDatabase", mockCourseDatabase);
            ((FakeCourseDatabase)mockCourseDatabase).courses.add(selectedCourse);
            testController.deleteCourse();
            assert ((FakeCourseDatabase)mockCourseDatabase).courses.isEmpty();
            assert fakeExamDb.exams.isEmpty();
            mockMsg.verify(() -> MsgSender.showMsg("Course and associated exams deleted successfully!"));
        }
    }

    @Test
    public void testDeleteCourse_withMatchingExam_shouldDeleteExam() throws Exception {
        Course selectedCourse = new Course(1L, "COMP1011", "Intro", Department.CSE);
        mockTableView.getItems().add(selectedCourse);
        mockTableView.getSelectionModel().select(selectedCourse);
        ComboBox<Department> mockCbDepartment = new ComboBox<>();
        mockCbDepartment.getItems().add(Department.CSE);
        mockCbDepartment.getSelectionModel().select(Department.CSE);
        Field cbField = CourseManagementController.class.getDeclaredField("cbDepartment");
        cbField.setAccessible(true);
        cbField.set(controller, mockCbDepartment);
        Exam matchingExam = new Exam();
        matchingExam.setId(100L);
        matchingExam.setCourseCode("COMP1011");
        FakeExamDatabase fakeExamDb = new FakeExamDatabase();
        fakeExamDb.exams.add(matchingExam);
        TestableCourseManagementController testController = new TestableCourseManagementController();
        testController.fakeExamDb = fakeExamDb;
        // Set private fields using reflection
        setPrivateField(testController, "courseTable", mockTableView);
        setPrivateField(testController, "allCourses", controller.allCourses);
        setPrivateField(testController, "filterCourseCode", filterCourseCode);
        setPrivateField(testController, "filterCourseName", filterCourseName);
        setPrivateField(testController, "filterDepartment", filterDepartment);
        setPrivateField(testController, "tfCourseCode", tfCourseCode);
        setPrivateField(testController, "tfCourseName", tfCourseName);
        setPrivateField(testController, "cbDepartment", mockCbDepartment);
        setPrivateField(testController, "colCourseCode", getPrivateField(controller, "colCourseCode"));
        setPrivateField(testController, "colCourseName", getPrivateField(controller, "colCourseName"));
        setPrivateField(testController, "colDepartment", getPrivateField(controller, "colDepartment"));
        setPrivateField(testController, "courseDatabase", mockCourseDatabase);
        try (MockedStatic<MsgSender> mockMsg = mockStatic(MsgSender.class)) {
            mockMsg.when(() -> MsgSender.showConfirm(anyString(), anyString(), any()))
                    .thenAnswer(invocation -> {
                        Runnable confirm = invocation.getArgument(2);
                        confirm.run();
                        return null;
                    });
            ((FakeCourseDatabase)mockCourseDatabase).courses.add(selectedCourse);
            testController.deleteCourse();
            assert ((FakeCourseDatabase)mockCourseDatabase).courses.isEmpty();
            assert fakeExamDb.exams.isEmpty(); // Exam should be deleted
            mockMsg.verify(() -> MsgSender.showMsg("Course and associated exams deleted successfully!"));
        }
    }

    @Test
    public void testDeleteCourse_exceptionThrown_shouldShowFailureMsg() {
        Course course = new Course(3L, "COMP3030", "Advanced Topics", Department.CSE);
        mockTableView.getItems().add(course);
        mockTableView.getSelectionModel().select(course);
        try (MockedStatic<MsgSender> mockMsg = mockStatic(MsgSender.class)) {
            mockMsg.when(() -> MsgSender.showConfirm(any(), any(), any())).thenAnswer(inv -> {
                Runnable confirm = inv.getArgument(2);
                Database<Course> throwingDb = new FakeCourseDatabase() {
                    @Override public void delByKey(String key) { throw new RuntimeException(); }
                };
                for (var field : CourseManagementController.class.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.getName().equals("courseDatabase")) field.set(controller, throwingDb);
                }
                confirm.run();
                return null;
            });
            ((FakeCourseDatabase)mockCourseDatabase).courses.add(course);
            controller.deleteCourse();
            assert ((FakeCourseDatabase)mockCourseDatabase).courses.contains(course);
            mockMsg.verify(() -> MsgSender.showMsg("Failed to delete course."), atLeastOnce());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

}
