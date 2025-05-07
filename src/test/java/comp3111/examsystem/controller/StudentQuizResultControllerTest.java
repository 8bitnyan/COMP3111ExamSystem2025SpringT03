package comp3111.examsystem.controller;

import comp3111.examsystem.entity.Student;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StudentQuizResultControllerTest {
    StudentQuizResultController controller;
    Text quizNameText;
    Text totalQuestionsText;
    Text scoreText;
    VBox questionListContainer;
    Label questionNumberLabel;
    Label questionLabel;
    Label studentAnswerLabel;
    Label correctAnswerLabel;
    Label questionScoreLabel;
    TextArea feedbackTextArea;
    Button previousButton;
    Button nextButton;
    Button backButton;

    @BeforeAll
    public static void initJfx() {
        new javafx.embed.swing.JFXPanel(); // Initializes JavaFX environment
    }

    @BeforeEach
    void setup() {
        controller = new StudentQuizResultController();
        quizNameText = new Text();
        totalQuestionsText = new Text();
        scoreText = new Text();
        questionListContainer = new VBox();
        questionNumberLabel = new Label();
        questionLabel = new Label();
        studentAnswerLabel = new Label();
        correctAnswerLabel = new Label();
        questionScoreLabel = new Label();
        feedbackTextArea = new TextArea();
        previousButton = new Button();
        nextButton = new Button();
        backButton = new Button();
        inject(controller, "quizNameText", quizNameText);
        inject(controller, "totalQuestionsText", totalQuestionsText);
        inject(controller, "scoreText", scoreText);
        inject(controller, "questionListContainer", questionListContainer);
        inject(controller, "questionNumberLabel", questionNumberLabel);
        inject(controller, "questionLabel", questionLabel);
        inject(controller, "studentAnswerLabel", studentAnswerLabel);
        inject(controller, "correctAnswerLabel", correctAnswerLabel);
        inject(controller, "questionScoreLabel", questionScoreLabel);
        inject(controller, "feedbackTextArea", feedbackTextArea);
        inject(controller, "previousButton", previousButton);
        inject(controller, "nextButton", nextButton);
        inject(controller, "backButton", backButton);
    }

    private void inject(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<StudentQuizResultController.QuizResult> sampleResults() {
        List<StudentQuizResultController.QuizResult> results = new ArrayList<>();
        results.add(new StudentQuizResultController.QuizResult("Q1", "A", "A", 1, 1, "Good"));
        results.add(new StudentQuizResultController.QuizResult("Q2", "B", "C", 0, 1, "Bad"));
        return results;
    }

    @Test
    void testPreSetControllerAndInitialState() {
        Student student = new Student();
        List<StudentQuizResultController.QuizResult> results = sampleResults();
        controller.preSetController(student, "QuizName", results);
        assertEquals("QuizName", quizNameText.getText());
        assertEquals("2", totalQuestionsText.getText());
        assertTrue(scoreText.getText().contains("%"));
        assertEquals(2, questionListContainer.getChildren().size());
        // First question loaded
        assertEquals("1", questionNumberLabel.getText());
        assertEquals("Q1", questionLabel.getText());
        assertEquals("A", studentAnswerLabel.getText());
        assertEquals("A", correctAnswerLabel.getText());
        assertEquals("1.0/1.0", questionScoreLabel.getText());
        assertEquals("Good", feedbackTextArea.getText());
        // Navigation buttons
        assertTrue(previousButton.isDisabled());
        assertFalse(nextButton.isDisabled());
    }

    @Test
    void testNavigationNextAndPrevious() {
        Student student = new Student();
        List<StudentQuizResultController.QuizResult> results = sampleResults();
        controller.preSetController(student, "QuizName", results);
        // Go to next
        controller.handleNext(null);
        assertEquals("2", questionNumberLabel.getText());
        assertEquals("Q2", questionLabel.getText());
        assertEquals("B", studentAnswerLabel.getText());
        assertEquals("C", correctAnswerLabel.getText());
        assertEquals("0.0/1.0", questionScoreLabel.getText());
        assertEquals("Bad", feedbackTextArea.getText());
        assertFalse(previousButton.isDisabled());
        assertTrue(nextButton.isDisabled());
        // Go back to previous
        controller.handlePrevious(null);
        assertEquals("1", questionNumberLabel.getText());
        assertTrue(previousButton.isDisabled());
        assertFalse(nextButton.isDisabled());
    }

    @Test
    void testLoadQuestionResultOutOfBounds() {
        Student student = new Student();
        List<StudentQuizResultController.QuizResult> results = sampleResults();
        controller.preSetController(student, "QuizName", results);
        // Try to load invalid index
        var method = getPrivateMethod(controller, "loadQuestionResult", int.class);
        assertDoesNotThrow(() -> method.invoke(controller, -1));
        assertDoesNotThrow(() -> method.invoke(controller, 99));
        // Should not change currentQuestionIndex
        assertEquals("1", questionNumberLabel.getText());
    }

    @Test
    void testUpdateNavigationButtons() throws Exception {
        Student student = new Student();
        List<StudentQuizResultController.QuizResult> results = sampleResults();
        controller.preSetController(student, "QuizName", results);
        // Use reflection to call updateNavigationButtons directly
        var method = getPrivateMethod(controller, "updateNavigationButtons");
        // On first question
        method.invoke(controller);
        assertTrue(previousButton.isDisabled());
        // Go to last question
        controller.handleNext(null);
        method.invoke(controller);
        assertTrue(nextButton.isDisabled());
    }

    @Test
    void testHandleBackCatchesIOException() throws Exception {
        // Patch backButton to have a null scene/window to force null safety branch
        backButton = new Button();
        inject(controller, "backButton", backButton);
        // Patch student
        Student student = new Student();
        List<StudentQuizResultController.QuizResult> results = sampleResults();
        controller.preSetController(student, "QuizName", results);
        // Patch showAlert to record alert invocations using reflection
        final boolean[] alertShown = {false};
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        showAlertField.set(controller, (StudentQuizResultController.AlertShower) (type, title, content) -> {
            alertShown[0] = true;
            assertEquals(Alert.AlertType.ERROR, type);
            assertTrue(content.contains("No window attached"));
        });
        // Should not throw, should show alert
        assertDoesNotThrow(() -> controller.handleBack(null));
        assertTrue(alertShown[0], "Alert should be shown when no window attached");
    }

    // Helper to get private method via reflection
    private java.lang.reflect.Method getPrivateMethod(Object obj, String name, Class<?>... params) {
        try {
            var m = obj.getClass().getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 