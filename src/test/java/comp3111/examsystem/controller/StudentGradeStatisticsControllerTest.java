package comp3111.examsystem.controller;

import comp3111.examsystem.entity.Student;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import static org.junit.jupiter.api.Assertions.*;

public class StudentGradeStatisticsControllerTest {
    private StudentGradeStatisticsController controller;
    private ComboBox<String> courseComboBox;
    private TextField minScoreField;
    private TextField maxScoreField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Button filterButton;
    private Button resetButton;
    private Button refreshButton;
    private ListView<String> quizListView;
    private BarChart<String, Number> gradeChart;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;
    private Label averageScoreLabel;
    private Label highestScoreLabel;
    private Label lowestScoreLabel;
    private Button backButton;
    private Button closeButton;

    @BeforeAll
    static void initJfx() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new StudentGradeStatisticsController();
        courseComboBox = new ComboBox<>();
        minScoreField = new TextField();
        maxScoreField = new TextField();
        startDatePicker = new DatePicker();
        endDatePicker = new DatePicker();
        filterButton = new Button();
        resetButton = new Button();
        refreshButton = new Button();
        quizListView = new ListView<>();
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();
        gradeChart = new BarChart<>(xAxis, yAxis);
        averageScoreLabel = new Label();
        highestScoreLabel = new Label();
        lowestScoreLabel = new Label();
        backButton = new Button();
        closeButton = new Button();
        setField(controller, "courseComboBox", courseComboBox);
        setField(controller, "minScoreField", minScoreField);
        setField(controller, "maxScoreField", maxScoreField);
        setField(controller, "startDatePicker", startDatePicker);
        setField(controller, "endDatePicker", endDatePicker);
        setField(controller, "filterButton", filterButton);
        setField(controller, "resetButton", resetButton);
        setField(controller, "refreshButton", refreshButton);
        setField(controller, "quizListView", quizListView);
        setField(controller, "gradeChart", gradeChart);
        setField(controller, "xAxis", xAxis);
        setField(controller, "yAxis", yAxis);
        setField(controller, "averageScoreLabel", averageScoreLabel);
        setField(controller, "highestScoreLabel", highestScoreLabel);
        setField(controller, "lowestScoreLabel", lowestScoreLabel);
        setField(controller, "backButton", backButton);
        setField(controller, "closeButton", closeButton);
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    void testInitializeSetsPromptsAndListeners() {
        controller.initialize(null, null);
        assertEquals("0", minScoreField.getPromptText());
        assertEquals("100", maxScoreField.getPromptText());
        minScoreField.setText("abc123");
        maxScoreField.setText("xyz456");
        assertEquals("123", minScoreField.getText());
        assertEquals("456", maxScoreField.getText());
    }

    @Test
    void testPreSetControllerLoadsDataAndAppliesFilter() {
        Student student = new Student();
        controller.preSetController(student);
        assertNotNull(courseComboBox.getItems());
        assertTrue(courseComboBox.getItems().contains("All Courses"));
        assertFalse(quizListView.getItems().isEmpty());
        assertNotNull(gradeChart.getData());
        assertNotNull(averageScoreLabel.getText());
    }

    @Test
    void testApplyFilter_AllBranches() throws Exception {
        controller.preSetController(new Student());
        // Set course filter
        courseComboBox.getSelectionModel().select("COMP3111");
        minScoreField.setText("80");
        maxScoreField.setText("90");
        startDatePicker.setValue(LocalDate.now().minusDays(31));
        endDatePicker.setValue(LocalDate.now().minusDays(14));
        // Call applyFilter via reflection
        Field f = controller.getClass().getDeclaredField("filteredQuizGrades");
        f.setAccessible(true);
        var method = controller.getClass().getDeclaredMethod("applyFilter");
        method.setAccessible(true);
        method.invoke(controller);
        var filtered = (java.util.List<?>) f.get(controller);
        assertTrue(filtered.stream().allMatch(qg -> {
            try {
                Field course = qg.getClass().getDeclaredField("course");
                course.setAccessible(true);
                String c = (String) course.get(qg);
                Field score = qg.getClass().getDeclaredField("score");
                score.setAccessible(true);
                double s = (double) score.get(qg);
                Field date = qg.getClass().getDeclaredField("date");
                date.setAccessible(true);
                LocalDate d = (LocalDate) date.get(qg);
                return c.equals("COMP3111") && s >= 80 && s <= 90 &&
                        (d.isEqual(LocalDate.now().minusDays(31)) || d.isAfter(LocalDate.now().minusDays(31))) &&
                        (d.isEqual(LocalDate.now().minusDays(14)) || d.isBefore(LocalDate.now().minusDays(14)));
            } catch (Exception e) { return false; }
        }));
    }

    @Test
    void testApplyFilter_InvalidScoreInputShowsAlert() throws Exception {
        controller.preSetController(new Student());
        minScoreField.setText("abc");
        maxScoreField.setText("");
        // Patch showAlert to set a flag
        Field alertFlag = StudentGradeStatisticsControllerTest.class.getDeclaredField("alertShown");
        alertFlag.setAccessible(true);
        alertShown = false;
        controller.showAlert = (type, title, content) -> alertShown = true;
        var method = controller.getClass().getDeclaredMethod("applyFilter");
        method.setAccessible(true);
        method.invoke(controller);
        assertTrue(alertShown);
    }

    private static boolean alertShown = false;

    @Test
    void testHandleResetClearsFilters() throws Exception {
        controller.preSetController(new Student());
        courseComboBox.getSelectionModel().select("COMP3111");
        minScoreField.setText("80");
        maxScoreField.setText("90");
        startDatePicker.setValue(LocalDate.now().minusDays(31));
        endDatePicker.setValue(LocalDate.now().minusDays(14));
        controller.handleReset(null);
        assertEquals(0, courseComboBox.getSelectionModel().getSelectedIndex());
        assertEquals("", minScoreField.getText());
        assertEquals("", maxScoreField.getText());
        assertNull(startDatePicker.getValue());
        assertNull(endDatePicker.getValue());
    }

    @Test
    void testHandleRefreshReloadsData() {
        controller.preSetController(new Student());
        quizListView.getItems().clear();
        controller.handleRefresh(null);
        assertFalse(quizListView.getItems().isEmpty());
    }

    @Test
    void testHandleQuizSelectionShowsAlert() throws Exception {
        controller.preSetController(new Student());
        quizListView.getItems().add("Week 1 Quiz (COMP3111)");
        quizListView.getSelectionModel().select(0);
        // Patch showAlert to set a flag
        Field alertFlag = StudentGradeStatisticsControllerTest.class.getDeclaredField("alertShown");
        alertFlag.setAccessible(true);
        alertShown = false;
        controller.showAlert = (type, title, content) -> alertShown = true;
        controller.handleQuizSelection(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 1, false, false, false, false, false, false, false, false, false, false, null));
        assertTrue(alertShown);
    }
} 