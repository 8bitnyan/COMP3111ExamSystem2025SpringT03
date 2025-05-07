package comp3111.examsystem.controller;

import comp3111.examsystem.entity.Student;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;

public class StudentGradeStatisticsControllerTest {
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

    StudentGradeStatisticsController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new StudentGradeStatisticsController();
        setField(controller, "allQuizGrades", new ArrayList<>());
        setField(controller, "filteredQuizGrades", new ArrayList<>());
        setField(controller, "courseComboBox", new ComboBox<>());
        setField(controller, "minScoreField", new TextField());
        setField(controller, "maxScoreField", new TextField());
        setField(controller, "startDatePicker", new DatePicker());
        setField(controller, "endDatePicker", new DatePicker());
        setField(controller, "filterButton", new Button());
        setField(controller, "resetButton", new Button());
        setField(controller, "refreshButton", new Button());
        setField(controller, "quizListView", new ListView<>());
        setField(controller, "gradeChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));
        setField(controller, "xAxis", new CategoryAxis());
        setField(controller, "yAxis", new NumberAxis());
        setField(controller, "averageScoreLabel", new Label());
        setField(controller, "highestScoreLabel", new Label());
        setField(controller, "lowestScoreLabel", new Label());
        setField(controller, "backButton", new Button());
        setField(controller, "closeButton", new Button());
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    void testInitializeSetsUpFieldsAndListeners() {
        assertDoesNotThrow(() -> controller.initialize(null, null));
        // Should set prompt texts and initialize lists
        assertEquals("0", ((TextField) getField(controller, "minScoreField")).getPromptText());
        assertEquals("100", ((TextField) getField(controller, "maxScoreField")).getPromptText());
    }

    @Test
    void testPreSetControllerHandlesEmptyState() throws Exception {
        Student student = new Student();
        // Ensure allQuizGrades is initialized
        setField(controller, "allQuizGrades", new ArrayList<>());
        setField(controller, "filteredQuizGrades", new ArrayList<>());
        assertDoesNotThrow(() -> controller.preSetController(student));
        // Should not throw and should set up empty lists
    }

    @Test
    void testLoadCoursesAndApplyFilterBranches() throws Exception {
        // Inject some grades
        List<StudentGradeStatisticsController.QuizGrade> grades = Arrays.asList(
                new StudentGradeStatisticsController.QuizGrade("Quiz1", "CSE", 95, LocalDate.now()),
                new StudentGradeStatisticsController.QuizGrade("Quiz2", "EEE", 60, LocalDate.now().minusDays(1)),
                new StudentGradeStatisticsController.QuizGrade("Quiz3", "CSE", 50, LocalDate.now().minusDays(2))
        );
        setField(controller, "allQuizGrades", new ArrayList<>(grades));
        setField(controller, "filteredQuizGrades", new ArrayList<>(grades));
        ComboBox<String> courseComboBox = (ComboBox<String>) getField(controller, "courseComboBox");
        courseComboBox.getItems().clear();
        // Should add "All Courses" and unique courses
        var loadCourses = controller.getClass().getDeclaredMethod("loadCourses");
        loadCourses.setAccessible(true);
        loadCourses.invoke(controller);
        assertTrue(courseComboBox.getItems().contains("All Courses"));
        assertTrue(courseComboBox.getItems().contains("CSE"));
        assertTrue(courseComboBox.getItems().contains("EEE"));
        // Test applyFilter with all branches
        courseComboBox.getSelectionModel().select("CSE");
        setField(controller, "minScoreField", new TextField("60"));
        setField(controller, "maxScoreField", new TextField("100"));
        setField(controller, "startDatePicker", new DatePicker(LocalDate.now().minusDays(1)));
        setField(controller, "endDatePicker", new DatePicker(LocalDate.now()));
        var applyFilter = controller.getClass().getDeclaredMethod("applyFilter");
        applyFilter.setAccessible(true);
        assertDoesNotThrow(() -> applyFilter.invoke(controller));
        // Invalid score input
        setField(controller, "minScoreField", new TextField("notanumber"));
        setField(controller, "maxScoreField", new TextField("100"));
        final boolean[] alertShown = {false};
        Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        showAlertField.set(controller, (StudentGradeStatisticsController.AlertShower) (type, title, content) -> alertShown[0] = true);
        assertDoesNotThrow(() -> applyFilter.invoke(controller));
        assertTrue(alertShown[0]);
    }

    @Test
    void testUpdateQuizListAndChartAndStatistics() throws Exception {
        List<StudentGradeStatisticsController.QuizGrade> grades = Arrays.asList(
                new StudentGradeStatisticsController.QuizGrade("Quiz1", "CSE", 95, LocalDate.now()),
                new StudentGradeStatisticsController.QuizGrade("Quiz2", "EEE", 60, LocalDate.now().minusDays(1)),
                new StudentGradeStatisticsController.QuizGrade("Quiz3", "CSE", 50, LocalDate.now().minusDays(2))
        );
        setField(controller, "filteredQuizGrades", new ArrayList<>(grades));
        // updateQuizList
        var updateQuizList = controller.getClass().getDeclaredMethod("updateQuizList");
        updateQuizList.setAccessible(true);
        assertDoesNotThrow(() -> updateQuizList.invoke(controller));
        // updateChart
        setField(controller, "gradeChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));
        var updateChart = controller.getClass().getDeclaredMethod("updateChart");
        updateChart.setAccessible(true);
        assertDoesNotThrow(() -> updateChart.invoke(controller));
        // updateStatistics
        setField(controller, "averageScoreLabel", new Label());
        setField(controller, "highestScoreLabel", new Label());
        setField(controller, "lowestScoreLabel", new Label());
        var updateStatistics = controller.getClass().getDeclaredMethod("updateStatistics");
        updateStatistics.setAccessible(true);
        assertDoesNotThrow(() -> updateStatistics.invoke(controller));
        // Empty grades
        setField(controller, "filteredQuizGrades", new ArrayList<>());
        assertDoesNotThrow(() -> updateStatistics.invoke(controller));
    }

    @Test
    void testGetColorForScoreBranches() throws Exception {
        var getColorForScore = controller.getClass().getDeclaredMethod("getColorForScore", double.class);
        getColorForScore.setAccessible(true);
        assertEquals("#28a745", getColorForScore.invoke(controller, 95.0)); // Green
        assertEquals("#17a2b8", getColorForScore.invoke(controller, 80.0)); // Blue
        assertEquals("#ffc107", getColorForScore.invoke(controller, 65.0)); // Yellow
        assertEquals("#dc3545", getColorForScore.invoke(controller, 50.0)); // Red
    }

    @Test
    void testHandleFilterAndResetAndRefresh() throws Exception {
        // Ensure allQuizGrades and filteredQuizGrades are initialized
        setField(controller, "allQuizGrades", new ArrayList<>());
        setField(controller, "filteredQuizGrades", new ArrayList<>());
        // Just check no exception and UI is updated
        assertDoesNotThrow(() -> controller.handleFilter(null));
        assertDoesNotThrow(() -> controller.handleReset(null));
        assertDoesNotThrow(() -> controller.handleRefresh(null));
    }

    @Test
    void testHandleQuizSelectionBranches() throws Exception {
        List<StudentGradeStatisticsController.QuizGrade> grades = Arrays.asList(
                new StudentGradeStatisticsController.QuizGrade("Quiz1", "CSE", 95, LocalDate.now()),
                new StudentGradeStatisticsController.QuizGrade("Quiz2", "EEE", 60, LocalDate.now().minusDays(1))
        );
        setField(controller, "filteredQuizGrades", new ArrayList<>(grades));
        ListView<String> quizListView = new ListView<>();
        quizListView.setItems(FXCollections.observableArrayList("Quiz1 (CSE)", "Quiz2 (EEE)"));
        setField(controller, "quizListView", quizListView);
        // Patch showAlert
        Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] alertShown = {false};
        showAlertField.set(controller, (StudentGradeStatisticsController.AlertShower) (type, title, content) -> alertShown[0] = true);
        // Valid selection
        quizListView.getSelectionModel().select(0);
        controller.handleQuizSelection(new MouseEvent(
            MouseEvent.MOUSE_CLICKED, 0.0, 0.0, 0.0, 0.0, javafx.scene.input.MouseButton.PRIMARY, 1,
            false, false, false, false, false, false, false, false, false, false, false, false, new javafx.scene.input.PickResult(null, 0, 0)));
        assertTrue(alertShown[0]);
        // Null selection
        quizListView.getSelectionModel().clearSelection();
        assertDoesNotThrow(() -> controller.handleQuizSelection(new MouseEvent(
            MouseEvent.MOUSE_CLICKED, 0.0, 0.0, 0.0, 0.0, javafx.scene.input.MouseButton.PRIMARY, 1,
            false, false, false, false, false, false, false, false, false, false, false, false, new javafx.scene.input.PickResult(null, 0, 0))));
    }

    // Helper to get private field
    private Object getField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 