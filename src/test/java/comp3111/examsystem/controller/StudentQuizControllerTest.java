package comp3111.examsystem.controller;

import comp3111.examsystem.entity.Student;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;
import comp3111.examsystem.entity.Exam;
import comp3111.examsystem.tools.Database;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class StudentQuizControllerTest {
    StudentQuizController controller;
    MockedStatic<comp3111.examsystem.tools.MsgSender> msgSenderMocked;

    @BeforeEach
    void setUp() throws Exception {
        controller = new StudentQuizController();
        // Attach questionText to a Scene and Stage
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                VBox root = new VBox();
                Text questionText = new Text();
                root.getChildren().add(questionText);
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                try {
                    setField(controller, "questionText", questionText);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        setField(controller, "quizNameText", new Text());
        setField(controller, "totalQuestionsText", new Text());
        setField(controller, "timerText", new Text());
        setField(controller, "questionListContainer", new VBox());
        setField(controller, "multipleChoiceContainer", new VBox());
        setField(controller, "shortAnswerContainer", new VBox());
        setField(controller, "option1", new RadioButton());
        setField(controller, "option2", new RadioButton());
        setField(controller, "option3", new RadioButton());
        setField(controller, "option4", new RadioButton());
        setField(controller, "shortAnswerField", new TextArea());
        setField(controller, "previousButton", new Button());
        setField(controller, "nextButton", new Button());
        setField(controller, "submitButton", new Button());
        setField(controller, "answerGroup", new ToggleGroup());

        // Add a mock Exam with name 'Sample Quiz' to the test database
        Exam exam = new Exam();
        exam.setId(1L);
        exam.setName("Sample Quiz");
        exam.setQuestions("1"); // minimal valid questions string
        exam.setDuration(5); // 5 minutes
        Database<Exam> examDB = new Database<>(Exam.class);
        examDB.add(exam);

        msgSenderMocked = Mockito.mockStatic(comp3111.examsystem.tools.MsgSender.class);
        msgSenderMocked.when(() -> comp3111.examsystem.tools.MsgSender.showMsg(Mockito.anyString())).then(invocation -> null);
        msgSenderMocked.when(() -> comp3111.examsystem.tools.MsgSender.showConfirm(
            Mockito.anyString(), Mockito.anyString(), Mockito.any(Runnable.class)
        )).thenAnswer(invocation -> {
            Runnable callback = invocation.getArgument(2);
            if (callback != null) callback.run();
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        if (msgSenderMocked != null) msgSenderMocked.close();
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private <T> T getFieldValue(Object obj, String fieldName, Class<T> type) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return type.cast(field.get(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testPreSetControllerAndNavigation() {
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
            new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D")),
            new StudentQuizController.QuizQuestion("Q2")
        );
        controller.preSetController(student, "Sample Quiz", questions, 5);
        Text quizNameText = getFieldValue(controller, "quizNameText", Text.class);
        Text totalQuestionsText = getFieldValue(controller, "totalQuestionsText", Text.class);
        assertEquals("Sample Quiz", quizNameText.getText());
        assertEquals("2", totalQuestionsText.getText());
        // Test navigation
        controller.handleNext(null);
        assertEquals(1, getPrivateInt(controller, "currentQuestionIndex"));
        controller.handlePrevious(null);
        assertEquals(0, getPrivateInt(controller, "currentQuestionIndex"));
    }

    @Test
    void testAnswerPersistence() {
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
            new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D")),
            new StudentQuizController.QuizQuestion("Q2")
        );
        controller.preSetController(student, "Sample Quiz", questions, 5);
        // Simulate answering Q1
        RadioButton option2 = getFieldValue(controller, "option2", RadioButton.class);
        option2.setSelected(true);
        controller.handleNext(null);
        // Simulate answering Q2
        TextArea shortAnswerField = getFieldValue(controller, "shortAnswerField", TextArea.class);
        shortAnswerField.setText("Short answer");
        controller.handlePrevious(null);
        // Q1 should still have answer
        assertTrue(option2.isSelected());
        controller.handleNext(null);
        assertEquals("Short answer", shortAnswerField.getText());
    }

    @Test
    void testTimerAndAutoSubmit() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Student student = new Student();
                List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
                    new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"))
                );
                controller.preSetController(student, "Sample Quiz", questions, 0); // 0 min for instant expiry
                // Simulate timer expiry
                try {
                    setField(controller, "seconds", 0);
                    setField(controller, "minutes", 0);
                    setField(controller, "hours", 0);
                    var submitQuiz = controller.getClass().getDeclaredMethod("submitQuiz");
                    submitQuiz.setAccessible(true);
                    submitQuiz.invoke(controller);
                    msgSenderMocked.verify(() -> comp3111.examsystem.tools.MsgSender.showMsg(Mockito.anyString()), Mockito.atLeastOnce());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    void testHandleSubmit() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Student student = new Student();
                List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
                    new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"))
                );
                controller.preSetController(student, "Sample Quiz", questions, 5);
                controller.handleSubmit(null);
                msgSenderMocked.verify(() -> comp3111.examsystem.tools.MsgSender.showMsg(Mockito.anyString()), Mockito.atLeastOnce());
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    void testWindowCloseHandler() throws Exception {
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
            new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"))
        );
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.preSetController(student, "Sample Quiz", questions, 5);
                Stage stage = (Stage) getFieldValue(controller, "questionText", Text.class).getScene().getWindow();
                WindowEvent closeEvent = new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST);
                stage.fireEvent(closeEvent);
                // You can add assertions or verifications here if needed
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    private int getPrivateInt(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 