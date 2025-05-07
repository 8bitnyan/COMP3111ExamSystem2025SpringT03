package comp3111.examsystem.controller;

import comp3111.examsystem.entity.Student;
import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import org.junit.jupiter.api.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

import comp3111.examsystem.entity.Exam;
import comp3111.examsystem.entity.Course;
import comp3111.examsystem.tools.Database;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

class StudentMainControllerTest {
    StudentMainController controller;
    ComboBox<String> takeExamComboBox;
    ComboBox<String> checkResultComboBox;
    Button startExamButton;
    Button checkResultButton;
    Button viewStatisticsButton;
    Button logoutButton;
    Button exitButton;
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

    @BeforeEach
    void setup() {
        // Clear all relevant databases for test isolation
        new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class).getAll().forEach(e -> new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class).delByKey(e.getId().toString()));
        new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Course.class).getAll().forEach(e -> new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Course.class).delByKey(e.getId().toString()));
        new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Record.class).getAll().forEach(e -> new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Record.class).delByKey(e.getId().toString()));
        controller = new StudentMainController();
        takeExamComboBox = new ComboBox<>();
        checkResultComboBox = new ComboBox<>();
        startExamButton = new Button();
        checkResultButton = new Button();
        viewStatisticsButton = new Button();
        logoutButton = new Button();
        exitButton = new Button();

        inject(controller, "TakeExamComboBox", takeExamComboBox);
        inject(controller, "CheckResultComboBox", checkResultComboBox);
        inject(controller, "startExamButton", startExamButton);
        inject(controller, "checkResultButton", checkResultButton);
        inject(controller, "viewStatisticsButton", viewStatisticsButton);
        inject(controller, "logoutButton", logoutButton);
        inject(controller, "exitButton", exitButton);
    }

    private void inject(Object target, String field, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                var f = clazz.getDeclaredField(field);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Field not found: " + field);
    }

    @Test
    void testInitializeDisablesButtons() {
        controller.initialize(null, null);
        assertTrue(startExamButton.isDisabled());
        assertTrue(checkResultButton.isDisabled());
    }

    @Test
    void testPreSetControllerPopulatesComboBoxes() {
        Student student = new Student(1L, "user", "pass", "Name", Gender.MALE, 20, Department.CSE);
        controller.preSetController(student);
        assertNotNull(takeExamComboBox.getItems());
        assertNotNull(checkResultComboBox.getItems());
    }

    @Test
    void testPreSetControllerWithNullStudent() {
        assertDoesNotThrow(() -> controller.preSetController(null));
        // Should not throw, should not populate combo boxes
        assertNotNull(takeExamComboBox.getItems());
        assertNotNull(checkResultComboBox.getItems());
        assertEquals(0, takeExamComboBox.getItems().size());
        assertEquals(0, checkResultComboBox.getItems().size());
    }

    @Test
    void testHandleStartExam_NoQuizSelected_ShowsAlert() throws Exception {
        controller.initialize(null, null);
        takeExamComboBox.getItems().add("Quiz1");
        takeExamComboBox.getSelectionModel().clearSelection();
        // Patch showAlert via reflection
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] alertShown = {false};
        showAlertField.set(controller, (StudentMainController.AlertShower) (type, title, content) -> {
            alertShown[0] = true;
            assertEquals(Alert.AlertType.WARNING, type);
            assertTrue(content.contains("select a quiz"));
        });
        // Call handleStartExam via reflection
        var method = controller.getClass().getDeclaredMethod("handleStartExam", ActionEvent.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller, new ActionEvent()));
        assertTrue(alertShown[0], "Alert should be shown for no quiz selected");
    }

    @Test
    void testHandleCheckResult_NoResultSelected_ShowsAlert() throws Exception {
        controller.initialize(null, null);
        checkResultComboBox.getItems().add("Quiz1");
        checkResultComboBox.getSelectionModel().clearSelection();
        // Patch showAlert via reflection
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] alertShown = {false};
        showAlertField.set(controller, (StudentMainController.AlertShower) (type, title, content) -> {
            alertShown[0] = true;
            assertEquals(Alert.AlertType.WARNING, type);
            assertTrue(content.contains("select a quiz result"));
        });
        // Call handleCheckResult via reflection
        var method = controller.getClass().getDeclaredMethod("handleCheckResult", ActionEvent.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller, new ActionEvent()));
        assertTrue(alertShown[0], "Alert should be shown for no result selected");
    }

    @Test
    void testHandleStartExam_ExamNotFound_ShowsAlert() throws Exception {
        controller.initialize(null, null);
        takeExamComboBox.getItems().add("Nonexistent Quiz");
        takeExamComboBox.getSelectionModel().select("Nonexistent Quiz");
        // Patch showAlert via reflection
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] alertShown = {false};
        showAlertField.set(controller, (StudentMainController.AlertShower) (type, title, content) -> {
            alertShown[0] = true;
            assertEquals(Alert.AlertType.ERROR, type);
            assertTrue(content.contains("not find the selected exam"));
        });
        // Call handleStartExam via reflection
        var method = controller.getClass().getDeclaredMethod("handleStartExam", ActionEvent.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller, new ActionEvent()));
        assertTrue(alertShown[0], "Alert should be shown for exam not found");
    }

    @Test
    void testHandleCheckResult_ExamNotFound_ShowsAlert() throws Exception {
        controller.initialize(null, null);
        checkResultComboBox.getItems().add("Nonexistent Quiz");
        checkResultComboBox.getSelectionModel().select("Nonexistent Quiz");
        // Patch showAlert via reflection
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] alertShown = {false};
        showAlertField.set(controller, (StudentMainController.AlertShower) (type, title, content) -> {
            alertShown[0] = true;
            assertEquals(Alert.AlertType.ERROR, type);
            assertTrue(content.contains("not find the selected exam"));
        });
        // Call handleCheckResult via reflection
        var method = controller.getClass().getDeclaredMethod("handleCheckResult", ActionEvent.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller, new ActionEvent()));
        assertTrue(alertShown[0], "Alert should be shown for exam not found");
    }

    @Test
    void testPreSetController_StudentNoDepartment() {
        Student student = new Student();
        student.setDepartment((Department) null);
        assertDoesNotThrow(() -> controller.preSetController(student));
        assertEquals(0, takeExamComboBox.getItems().size());
        assertEquals(0, checkResultComboBox.getItems().size());
    }

    @Test
    void testPreSetController_NoExamsInDatabase() {
        // Clear exams DB
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Exam> examDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class);
        examDB.getAll().forEach(e -> examDB.delByKey(e.getId().toString()));
        Student student = new Student(1L, "user", "pass", "Name", Gender.MALE, 20, Department.CSE);
        assertDoesNotThrow(() -> controller.preSetController(student));
        assertEquals(0, takeExamComboBox.getItems().size());
        assertEquals(0, checkResultComboBox.getItems().size());
    }

    @Test
    void testPreSetController_ExamWithNullOrEmptyCourseCode() {
        comp3111.examsystem.entity.Exam exam = new comp3111.examsystem.entity.Exam();
        exam.setId(100L);
        exam.setName("NullCourseExam");
        exam.setCourseCode(null);
        exam.setIsPublishedInt(1);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Exam> examDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class);
        examDB.add(exam);
        Student student = new Student(2L, "user2", "pass2", "Name2", Gender.FEMALE, 21, Department.CSE);
        assertDoesNotThrow(() -> controller.preSetController(student));
        assertEquals(0, takeExamComboBox.getItems().size());
    }

    @Test
    void testPreSetController_CourseNotFound() {
        comp3111.examsystem.entity.Exam exam = new comp3111.examsystem.entity.Exam();
        exam.setId(101L);
        exam.setName("NoCourseExam");
        exam.setCourseCode("FAKECODE");
        exam.setIsPublishedInt(1);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Exam> examDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class);
        examDB.add(exam);
        Student student = new Student(3L, "user3", "pass3", "Name3", Gender.FEMALE, 22, Department.CSE);
        assertDoesNotThrow(() -> controller.preSetController(student));
        assertEquals(0, takeExamComboBox.getItems().size());
    }

    @Test
    void testPreSetController_CourseWithNullDepartment() {
        comp3111.examsystem.entity.Course course = new comp3111.examsystem.entity.Course();
        course.setCourseCode("NULLDEPT");
        course.setDepartment((Department) null);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Course> courseDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Course.class);
        courseDB.add(course);
        comp3111.examsystem.entity.Exam exam = new comp3111.examsystem.entity.Exam();
        exam.setId(102L);
        exam.setName("NullDeptExam");
        exam.setCourseCode("NULLDEPT");
        exam.setIsPublishedInt(1);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Exam> examDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class);
        examDB.add(exam);
        Student student = new Student(4L, "user4", "pass4", "Name4", Gender.FEMALE, 23, Department.CSE);
        assertDoesNotThrow(() -> controller.preSetController(student));
        assertEquals(0, takeExamComboBox.getItems().size());
    }

    @Test
    void testPreSetController_AllExamsAttempted() {
        // Add an exam and a record for the student
        comp3111.examsystem.entity.Exam exam = new comp3111.examsystem.entity.Exam();
        exam.setId(103L);
        exam.setName("AttemptedExam");
        exam.setCourseCode("CSE101");
        exam.setIsPublishedInt(1);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Exam> examDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class);
        examDB.add(exam);
        comp3111.examsystem.entity.Course course = new comp3111.examsystem.entity.Course();
        course.setCourseCode("CSE101");
        course.setDepartment(Department.CSE);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Course> courseDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Course.class);
        courseDB.add(course);
        comp3111.examsystem.entity.Record record = new comp3111.examsystem.entity.Record();
        record.setStudent(5L);
        record.setExamID(103L);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Record> recordDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Record.class);
        recordDB.add(record);
        Student student = new Student(5L, "user5", "pass5", "Name5", Gender.MALE, 24, Department.CSE);
        assertDoesNotThrow(() -> controller.preSetController(student));
        assertEquals(0, takeExamComboBox.getItems().size());
    }

    @Test
    void testPreSetController_NoCompletedExams() {
        // Add an exam but no record for the student
        comp3111.examsystem.entity.Exam exam = new comp3111.examsystem.entity.Exam();
        exam.setId(104L);
        exam.setName("UnattemptedExam");
        exam.setCourseCode("CSE102");
        exam.setIsPublishedInt(1);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Exam> examDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class);
        examDB.add(exam);
        comp3111.examsystem.entity.Course course = new comp3111.examsystem.entity.Course();
        course.setCourseCode("CSE102");
        course.setDepartment(Department.CSE);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Course> courseDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Course.class);
        courseDB.add(course);
        Student student = new Student(6L, "user6", "pass6", "Name6", Gender.FEMALE, 25, Department.CSE);
        assertDoesNotThrow(() -> controller.preSetController(student));
        assertEquals(0, checkResultComboBox.getItems().size());
    }

    @Test
    void testPreSetController_ExamNotPublished() {
        comp3111.examsystem.entity.Exam exam = new comp3111.examsystem.entity.Exam();
        exam.setId(105L);
        exam.setName("UnpublishedExam");
        exam.setCourseCode("CSE103");
        exam.setIsPublishedInt(0);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Exam> examDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class);
        examDB.add(exam);
        comp3111.examsystem.entity.Course course = new comp3111.examsystem.entity.Course();
        course.setCourseCode("CSE103");
        course.setDepartment(Department.CSE);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Course> courseDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Course.class);
        courseDB.add(course);
        Student student = new Student(7L, "user7", "pass7", "Name7", Gender.MALE, 26, Department.CSE);
        assertDoesNotThrow(() -> controller.preSetController(student));
        assertEquals(0, takeExamComboBox.getItems().size());
    }

    @Test
    void testHandleStartExam_AlreadyAttempted_ShowsAlert() throws Exception {
        // Add exam, course, and record for already attempted
        comp3111.examsystem.entity.Exam exam = new comp3111.examsystem.entity.Exam();
        exam.setId(106L);
        exam.setName("AlreadyAttemptedExam");
        exam.setCourseCode("CSE104");
        exam.setIsPublishedInt(1);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Exam> examDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class);
        examDB.add(exam);
        comp3111.examsystem.entity.Course course = new comp3111.examsystem.entity.Course();
        course.setCourseCode("CSE104");
        course.setDepartment(Department.CSE);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Course> courseDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Course.class);
        courseDB.add(course);
        comp3111.examsystem.entity.Record record = new comp3111.examsystem.entity.Record();
        record.setStudent(8L);
        record.setExamID(106L);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Record> recordDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Record.class);
        recordDB.add(record);
        Student student = new Student(8L, "user8", "pass8", "Name8", Gender.FEMALE, 27, Department.CSE);
        controller.preSetController(student);
        takeExamComboBox.getItems().add("AlreadyAttemptedExam");
        takeExamComboBox.getSelectionModel().select("AlreadyAttemptedExam");
        // Patch showAlert
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] alertShown = {false};
        showAlertField.set(controller, (StudentMainController.AlertShower) (type, title, content) -> {
            alertShown[0] = true;
            assertEquals(Alert.AlertType.WARNING, type);
            assertTrue(content.contains("already attempted"));
        });
        var method = controller.getClass().getDeclaredMethod("handleStartExam", ActionEvent.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller, new ActionEvent()));
        assertTrue(alertShown[0], "Alert should be shown for already attempted");
    }

    @Test
    void testHandleStartExam_NoQuestions_ShowsAlert() throws Exception {
        // Add exam, course, but no questions
        comp3111.examsystem.entity.Exam exam = new comp3111.examsystem.entity.Exam();
        exam.setId(107L);
        exam.setName("NoQuestionsExam");
        exam.setCourseCode("CSE105");
        exam.setIsPublishedInt(1);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Exam> examDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class);
        examDB.add(exam);
        comp3111.examsystem.entity.Course course = new comp3111.examsystem.entity.Course();
        course.setCourseCode("CSE105");
        course.setDepartment(Department.CSE);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Course> courseDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Course.class);
        courseDB.add(course);
        Student student = new Student(9L, "user9", "pass9", "Name9", Gender.MALE, 28, Department.CSE);
        controller.preSetController(student);
        takeExamComboBox.getItems().add("NoQuestionsExam");
        takeExamComboBox.getSelectionModel().select("NoQuestionsExam");
        // Patch showAlert
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] alertShown = {false};
        showAlertField.set(controller, (StudentMainController.AlertShower) (type, title, content) -> {
            alertShown[0] = true;
            assertEquals(Alert.AlertType.WARNING, type);
            assertTrue(content.contains("does not have any questions"));
        });
        var method = controller.getClass().getDeclaredMethod("handleStartExam", ActionEvent.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller, new ActionEvent()));
        assertTrue(alertShown[0], "Alert should be shown for no questions");
    }

    @Test
    void testHandleCheckResult_NoResults_ShowsAlert() throws Exception {
        // Add exam, course, but no results for student
        comp3111.examsystem.entity.Exam exam = new comp3111.examsystem.entity.Exam();
        exam.setId(108L);
        exam.setName("NoResultsExam");
        exam.setCourseCode("CSE106");
        exam.setIsPublishedInt(1);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Exam> examDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class);
        examDB.add(exam);
        comp3111.examsystem.entity.Course course = new comp3111.examsystem.entity.Course();
        course.setCourseCode("CSE106");
        course.setDepartment(Department.CSE);
        comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Course> courseDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Course.class);
        courseDB.add(course);
        Student student = new Student(10L, "user10", "pass10", "Name10", Gender.FEMALE, 29, Department.CSE);
        controller.preSetController(student);
        checkResultComboBox.getItems().add("NoResultsExam");
        checkResultComboBox.getSelectionModel().select("NoResultsExam");
        // Patch showAlert
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] alertShown = {false};
        showAlertField.set(controller, (StudentMainController.AlertShower) (type, title, content) -> {
            alertShown[0] = true;
            assertEquals(Alert.AlertType.WARNING, type);
            assertTrue(content.contains("No results found"));
        });
        var method = controller.getClass().getDeclaredMethod("handleCheckResult", ActionEvent.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller, new ActionEvent()));
        assertTrue(alertShown[0], "Alert should be shown for no results");
    }

    // Testable subclass for simulating FXML loading errors
    static class TestableStudentMainController extends StudentMainController {
        @Override
        protected javafx.fxml.FXMLLoader createFXMLLoader(String resource) throws IOException {
            if ("/comp3111/examsystem/StudentQuizResultUI.fxml".equals(resource)) {
                throw new RuntimeException(new java.io.IOException("Simulated IO error"));
            }
            return super.createFXMLLoader(resource);
        }
    }

    @Test
    void testInitializeCalledTwice_Idempotent() {
        controller.initialize(null, null);
        assertDoesNotThrow(() -> controller.initialize(null, null));
        assertTrue(startExamButton.isDisabled());
        assertTrue(checkResultButton.isDisabled());
    }

    @Test
    void testComboBoxSelectionChangeEnablesButtons() {
        controller.initialize(null, null);
        takeExamComboBox.getItems().add("Quiz1");
        takeExamComboBox.getSelectionModel().select("Quiz1");
        checkResultComboBox.getItems().add("Quiz2");
        checkResultComboBox.getSelectionModel().select("Quiz2");
        // Simulate selection change
        takeExamComboBox.getSelectionModel().select("Quiz1");
        checkResultComboBox.getSelectionModel().select("Quiz2");
        // Buttons should be enabled if selection is not null
        assertFalse(startExamButton.isDisabled());
        assertFalse(checkResultButton.isDisabled());
    }


    @Test
    void testTakeExamComboBox_NullSelection() {
        controller.initialize(null, null);
        takeExamComboBox.getItems().add("Quiz1");
        takeExamComboBox.getSelectionModel().select(null);
        assertTrue(startExamButton.isDisabled());
    }

    @Test
    void testCheckResultComboBox_NullSelection() {
        controller.initialize(null, null);
        checkResultComboBox.getItems().add("Quiz1");
        checkResultComboBox.getSelectionModel().select(null);
        assertTrue(checkResultButton.isDisabled());
    }


    @Test
    void testHandleViewStatistics() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                Scene scene = new Scene(viewStatisticsButton);
                stage.setScene(scene);
                stage.show();
                assertDoesNotThrow(() -> controller.handleViewStatistics(new ActionEvent()));
                stage.close();
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    void testHandleLogout() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                Scene scene = new Scene(logoutButton);
                stage.setScene(scene);
                stage.show();
                assertDoesNotThrow(() -> controller.handleLogout(new ActionEvent()));
                stage.close();
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    void testHandleExit() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                Scene scene = new Scene(exitButton);
                stage.setScene(scene);
                stage.show();
                assertDoesNotThrow(() -> controller.handleExit(new ActionEvent()));
                stage.close();
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    void testHandleStartExam_NoSelection() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.handleStartExam(new ActionEvent());
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }
    @Test
    void testHandleStartExam_ExamNotPublished_ShowsAlert() throws Exception {
        // Setup: Add an exam that is not published
        Exam exam = new Exam();
        exam.setId(200L);
        exam.setName("Unpublished");
        exam.setCourseCode("CSE200");
        exam.setIsPublishedInt(0); // Not published
        new Database<>(Exam.class).add(exam);
        Course course = new Course();
        course.setCourseCode("CSE200");
        course.setDepartment(Department.CSE);
        new Database<>(Course.class).add(course);
        Student student = new Student(20L, "user", "pass", "Name", Gender.MALE, 20, Department.CSE);
        controller.preSetController(student);
        takeExamComboBox.getItems().add("Unpublished");
        takeExamComboBox.getSelectionModel().select("Unpublished");
        // Patch showAlert
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] alertShown = {false};
        showAlertField.set(controller, (StudentMainController.AlertShower) (type, title, content) -> {
            alertShown[0] = true;
            assertEquals(Alert.AlertType.WARNING, type);
            assertTrue(content.contains("not published"));
        });
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.handleStartExam(new ActionEvent());
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        assertTrue(alertShown[0], "Alert should be shown for unpublished exam");
    }

    @Test
    void testHandleStartExam_HappyPath() throws Exception {
        // Setup: Add exam, course, and question
        Exam exam = new Exam();
        exam.setId(300L);
        exam.setName("HappyExam");
        exam.setCourseCode("CSE300");
        exam.setIsPublishedInt(1);
        exam.setQuestions("1");
        new Database<>(Exam.class).add(exam);

        Course course = new Course();
        course.setCourseCode("CSE300");
        course.setDepartment(Department.CSE);
        new Database<>(Course.class).add(course);

        comp3111.examsystem.entity.Question question = new comp3111.examsystem.entity.Question();
        question.setId(1L);
        question.setQuestionText("What is 2+2?");
        question.setType("MCQ");
        question.setOptions(Collections.singletonList("4,3,2,1"));
        question.setAnswer("4");
        question.setScore(1);
        new Database<>(comp3111.examsystem.entity.Question.class).add(question);

        Student student = new Student(30L, "user", "pass", "Name", Gender.MALE, 20, Department.CSE);
        controller.preSetController(student);
        takeExamComboBox.getItems().add("HappyExam");
        takeExamComboBox.getSelectionModel().select("HappyExam");

        // Patch showAlert to fail if called (should not be called in happy path)
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        showAlertField.set(controller, (StudentMainController.AlertShower) (type, title, content) -> {
            fail("showAlert should not be called in happy path");
        });

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.handleStartExam(new ActionEvent());
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    void testHandleStartExam_HappyPath_StageShow() throws Exception {
        Exam exam = new Exam();
        exam.setId(400L);
        exam.setName("HappyExamStage");
        exam.setCourseCode("CSE400");
        exam.setIsPublishedInt(1);
        exam.setQuestions("1");
        new Database<>(Exam.class).add(exam);

        Course course = new Course();
        course.setCourseCode("CSE400");
        course.setDepartment(Department.CSE);
        new Database<>(Course.class).add(course);

        comp3111.examsystem.entity.Question question = new comp3111.examsystem.entity.Question();
        question.setId(1L);
        question.setQuestionText("What is 3+3?");
        question.setType("MCQ");
        question.setOptions(Collections.singletonList("6,5,4,3"));
        question.setAnswer("6");
        question.setScore(1);
        new Database<>(comp3111.examsystem.entity.Question.class).add(question);

        Student student = new Student(40L, "user", "pass", "Name", Gender.MALE, 20, Department.CSE);
        controller.preSetController(student);
        takeExamComboBox.getItems().add("HappyExamStage");
        takeExamComboBox.getSelectionModel().select("HappyExamStage");

        // Attach startExamButton to a scene and stage
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                Scene scene = new Scene(startExamButton);
                stage.setScene(scene);
                stage.show();
                assertDoesNotThrow(() -> controller.handleStartExam(new ActionEvent()));
                stage.close();
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    void testHandleCheckResult_HappyPath_StageShow() throws Exception {
        // Setup: Add exam, course, question, and record
        Exam exam = new Exam();
        exam.setId(401L);
        exam.setName("ResultExamStage");
        exam.setCourseCode("CSE401");
        exam.setIsPublishedInt(1);
        exam.setQuestions("2");
        new Database<>(Exam.class).add(exam);

        Course course = new Course();
        course.setCourseCode("CSE401");
        course.setDepartment(Department.CSE);
        new Database<>(Course.class).add(course);

        comp3111.examsystem.entity.Question question = new comp3111.examsystem.entity.Question();
        question.setId(2L);
        question.setQuestionText("What is 5+5?");
        question.setType("MCQ");
        question.setOptions(Collections.singletonList("10,9,8,7"));
        question.setAnswer("10");
        question.setScore(1);
        new Database<>(comp3111.examsystem.entity.Question.class).add(question);

        comp3111.examsystem.entity.Record record = new comp3111.examsystem.entity.Record();
        record.setStudent(41L);
        record.setExamID(401L);
        record.setQuestionID(2L);
        record.setScore(1);
        record.setResponse("10");
        new Database<>(comp3111.examsystem.entity.Record.class).add(record);

        Student student = new Student(41L, "user", "pass", "Name", Gender.MALE, 20, Department.CSE);
        controller.preSetController(student);
        checkResultComboBox.getItems().add("ResultExamStage");
        checkResultComboBox.getSelectionModel().select("ResultExamStage");

        // Attach checkResultButton to a scene and stage
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                Scene scene = new Scene(checkResultButton);
                stage.setScene(scene);
                stage.show();
                assertDoesNotThrow(() -> controller.handleCheckResult(new ActionEvent()));
                stage.close();
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    void testGenerateFeedbackBranches() throws Exception {
        var method = controller.getClass().getDeclaredMethod("generateFeedback", double.class, double.class);
        method.setAccessible(true);
        // score == 0
        String feedback0 = (String) method.invoke(controller, 0.0, 2.0);
        assertTrue(feedback0.contains("Incorrect answer"));
        // score < maxScore
        String feedbackPartial = (String) method.invoke(controller, 1.0, 2.0);
        assertTrue(feedbackPartial.contains("Partially correct"));
        // score == maxScore
        String feedbackFull = (String) method.invoke(controller, 2.0, 2.0);
        assertTrue(feedbackFull.contains("Excellent"));
    }
}