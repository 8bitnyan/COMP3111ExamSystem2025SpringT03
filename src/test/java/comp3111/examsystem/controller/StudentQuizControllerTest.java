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
import javafx.scene.control.Alert;

public class StudentQuizControllerTest {
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
        setField(controller, "option5", new RadioButton());
        setField(controller, "shortAnswerField", new TextArea());
        setField(controller, "previousButton", new Button());
        setField(controller, "nextButton", new Button());
        setField(controller, "submitButton", new Button());
        setField(controller, "answerGroup", new ToggleGroup());
        setField(controller, "maxScoreText", new Text());

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
            new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"), 1, "A"),
            new StudentQuizController.QuizQuestion("Q2", 1)
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
            new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"), 1, "A"),
            new StudentQuizController.QuizQuestion("Q2", 1)
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
                    new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"), 1, "A")
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
                    new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"), 1, "A")
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
                new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"), 1, "A")
        );
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Patch confirmDialogShower to simulate user clicking OK
                try {
                    setField(controller, "confirmDialogShower", (StudentQuizController.ConfirmDialogShower) (title, header, content) -> true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                controller.preSetController(student, "Sample Quiz", questions, 5);
                Stage stage = (Stage) getFieldValue(controller, "questionText", Text.class).getScene().getWindow();
                WindowEvent closeEvent = new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST);
                stage.fireEvent(closeEvent);
                stage.close();
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    void testMCQOptionVisibilityAndSelection() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Stage stage = null;
            try {
                // Attach all option buttons to a scene and stage
                VBox root = new VBox(
                    getFieldValue(controller, "option1", RadioButton.class),
                    getFieldValue(controller, "option2", RadioButton.class),
                    getFieldValue(controller, "option3", RadioButton.class),
                    getFieldValue(controller, "option4", RadioButton.class),
                    getFieldValue(controller, "option5", RadioButton.class)
                );
                stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

                Student student = new Student();
                // 1 option
                List<StudentQuizController.QuizQuestion> q1 = Arrays.asList(new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A"), 1, "A"));
                controller.preSetController(student, "Quiz1", q1, 5);
                assertTrue(getFieldValue(controller, "option1", RadioButton.class).isVisible());
                assertFalse(getFieldValue(controller, "option2", RadioButton.class).isVisible());
                // 2 options
                List<StudentQuizController.QuizQuestion> q2 = Arrays.asList(new StudentQuizController.QuizQuestion("Q2", Arrays.asList("A", "B"), 1, "A"));
                controller.preSetController(student, "Quiz2", q2, 5);
                assertTrue(getFieldValue(controller, "option2", RadioButton.class).isVisible());
                // 3 options
                List<StudentQuizController.QuizQuestion> q3 = Arrays.asList(new StudentQuizController.QuizQuestion("Q3", Arrays.asList("A", "B", "C"), 1, "A"));
                controller.preSetController(student, "Quiz3", q3, 5);
                assertTrue(getFieldValue(controller, "option3", RadioButton.class).isVisible());
                // 4 options
                List<StudentQuizController.QuizQuestion> q4 = Arrays.asList(new StudentQuizController.QuizQuestion("Q4", Arrays.asList("A", "B", "C", "D"), 1, "A"));
                controller.preSetController(student, "Quiz4", q4, 5);
                assertTrue(getFieldValue(controller, "option4", RadioButton.class).isVisible());
                // 0 options
                List<StudentQuizController.QuizQuestion> q0 = Arrays.asList(new StudentQuizController.QuizQuestion("Q0", new ArrayList<>(), 1, "A"));
                controller.preSetController(student, "Quiz0", q0, 5);
                assertFalse(getFieldValue(controller, "option1", RadioButton.class).isVisible());
            } finally {
                if (stage != null) stage.close();
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void testShortAnswerEdgeCases() {
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(new StudentQuizController.QuizQuestion("Q1", 1));
        controller.preSetController(student, "QuizSA", questions, 5);
        TextArea sa = getFieldValue(controller, "shortAnswerField", TextArea.class);
        sa.setText("");
        controller.handleNext(null);
        sa.setText("   ");
        controller.handlePrevious(null);
        sa.setText("A very long answer that should be accepted as valid input for the short answer field.");
        controller.handleNext(null);
        assertNotNull(sa.getText());
    }

    @Test
    void testNavigationEdgeCases() {
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
            new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B"), 1, "A"),
            new StudentQuizController.QuizQuestion("Q2", 1)
        );
        controller.preSetController(student, "QuizNav", questions, 5);
        // Try previous on first question
        controller.handlePrevious(null);
        assertEquals(0, getPrivateInt(controller, "currentQuestionIndex"));
        // Go to last question
        controller.handleNext(null);
        // Try next on last question
        controller.handleNext(null);
        assertEquals(1, getPrivateInt(controller, "currentQuestionIndex"));
        // Try loading invalid index
        var loadQuestion = assertDoesNotThrow(() -> controller.getClass().getDeclaredMethod("loadQuestion", int.class));
        loadQuestion.setAccessible(true);
        assertDoesNotThrow(() -> loadQuestion.invoke(controller, -1));
        assertDoesNotThrow(() -> loadQuestion.invoke(controller, 99));
    }

    @Test
    void testTimerExpiryBranches() throws Exception {
        // hours > 0
        setField(controller, "hours", 1);
        setField(controller, "minutes", 0);
        setField(controller, "seconds", 0);
        var updateTimerDisplay = controller.getClass().getDeclaredMethod("updateTimerDisplay");
        updateTimerDisplay.setAccessible(true);
        assertDoesNotThrow(() -> updateTimerDisplay.invoke(controller));
        // minutes > 0
        setField(controller, "hours", 0);
        setField(controller, "minutes", 1);
        setField(controller, "seconds", 0);
        assertDoesNotThrow(() -> updateTimerDisplay.invoke(controller));
        // seconds > 0
        setField(controller, "hours", 0);
        setField(controller, "minutes", 0);
        setField(controller, "seconds", 1);
        assertDoesNotThrow(() -> updateTimerDisplay.invoke(controller));
    }

    @Test
    void testSubmitQuizBranches() throws Exception {
        // exam not found
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A"), 1, "A"));
        controller.preSetController(student, "Nonexistent Quiz", questions, 5);
        var submitQuiz = controller.getClass().getDeclaredMethod("submitQuiz");
        submitQuiz.setAccessible(true);
        CountDownLatch latch1 = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                submitQuiz.invoke(controller);
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.getCause().printStackTrace();
                fail("submitQuiz threw: " + e.getCause());
            } catch (Exception e) {
                e.printStackTrace();
                fail("submitQuiz threw: " + e);
            } finally {
                latch1.countDown();
            }
        });
        latch1.await();
        // questions string empty
        Exam exam = new Exam();
        exam.setId(2L);
        exam.setName("EmptyQ");
        exam.setQuestions("");
        Database<Exam> examDB = new Database<>(Exam.class);
        examDB.add(exam);
        controller.preSetController(student, "EmptyQ", questions, 5);
        CountDownLatch latch2 = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                submitQuiz.invoke(controller);
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.getCause().printStackTrace();
                fail("submitQuiz threw: " + e.getCause());
            } catch (Exception e) {
                e.printStackTrace();
                fail("submitQuiz threw: " + e);
            } finally {
                latch2.countDown();
            }
        });
        latch2.await();
        // questions string null
        Exam exam2 = new Exam();
        exam2.setId(3L);
        exam2.setName("NullQ");
        exam2.setQuestions(null);
        examDB.add(exam2);
        controller.preSetController(student, "NullQ", questions, 5);
        CountDownLatch latch3 = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                submitQuiz.invoke(controller);
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.getCause().printStackTrace();
                fail("submitQuiz threw: " + e.getCause());
            } catch (Exception e) {
                e.printStackTrace();
                fail("submitQuiz threw: " + e);
            } finally {
                latch3.countDown();
            }
        });
        latch3.await();
        // question count mismatch
        Exam exam3 = new Exam();
        exam3.setId(4L);
        exam3.setName("MismatchQ");
        exam3.setQuestions("1,2");
        examDB.add(exam3);
        controller.preSetController(student, "MismatchQ", questions, 5);
        CountDownLatch latch4 = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                submitQuiz.invoke(controller);
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.getCause().printStackTrace();
                fail("submitQuiz threw: " + e.getCause());
            } catch (Exception e) {
                e.printStackTrace();
                fail("submitQuiz threw: " + e);
            } finally {
                latch4.countDown();
            }
        });
        latch4.await();
        // bad questionId
        Exam exam4 = new Exam();
        exam4.setId(5L);
        exam4.setName("BadIdQ");
        exam4.setQuestions("notanumber");
        examDB.add(exam4);
        controller.preSetController(student, "BadIdQ", questions, 5);
        CountDownLatch latch5 = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                submitQuiz.invoke(controller);
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.getCause().printStackTrace();
                fail("submitQuiz threw: " + e.getCause());
            } catch (Exception e) {
                e.printStackTrace();
                fail("submitQuiz threw: " + e);
            } finally {
                latch5.countDown();
            }
        });
        latch5.await();
        // time spent negative
        Exam exam5 = new Exam();
        exam5.setId(6L);
        exam5.setName("TimeNegQ");
        exam5.setQuestions("1");
        exam5.setDuration(5);
        examDB.add(exam5);
        setField(controller, "hours", 10);
        setField(controller, "minutes", 0);
        controller.preSetController(student, "TimeNegQ", questions, 5);
        CountDownLatch latch6 = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                submitQuiz.invoke(controller);
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.getCause().printStackTrace();
                fail("submitQuiz threw: " + e.getCause());
            } catch (Exception e) {
                e.printStackTrace();
                fail("submitQuiz threw: " + e);
            } finally {
                latch6.countDown();
            }
        });
        latch6.await();
    }

    @Test
    void testSubmitQuizDialogFlow() throws Exception {
        // This test simulates the dialog flow for submitQuiz
        // (simulate user clicking OK and not clicking OK)
        // Not easily testable in headless mode, but we can invoke and check no exceptions
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A"), 1, "A"));
        controller.preSetController(student, "Sample Quiz", questions, 5);
        var submitQuiz = controller.getClass().getDeclaredMethod("submitQuiz");
        submitQuiz.setAccessible(true);
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                submitQuiz.invoke(controller);
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.getCause().printStackTrace();
                fail("submitQuiz threw: " + e.getCause());
            } catch (Exception e) {
                e.printStackTrace();
                fail("submitQuiz threw: " + e);
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    void testReturnToMainPageBranches() throws Exception {
        // With valid scene/window
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A"), 1, "A"));
        controller.preSetController(student, "Sample Quiz", questions, 5);
        var returnToMainPage = controller.getClass().getDeclaredMethod("returnToMainPage");
        returnToMainPage.setAccessible(true);
        assertDoesNotThrow(() -> returnToMainPage.invoke(controller));
        // Without valid scene/window
        setField(controller, "quizNameText", new Text()); // no scene
        assertDoesNotThrow(() -> returnToMainPage.invoke(controller));
    }

    @Test
    void testHandleSubmit_ConfirmationDialogBranch() throws Exception {
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
            new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"), 1, "A")
        );
        controller.preSetController(student, "Sample Quiz", questions, 5);
        // Patch showAlert to simulate user confirming submission
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] submitCalled = {false};
        showAlertField.set(controller, (StudentQuizController.AlertShower) (type, title, content) -> {
            if (type == Alert.AlertType.CONFIRMATION && content.contains("submit this quiz")) {
                try {
                    // Set showAlert to a no-op lambda to avoid recursion and dialog errors
                    showAlertField.set(controller, (StudentQuizController.AlertShower) (t, ti, c) -> {});
                    var submitQuiz = controller.getClass().getDeclaredMethod("submitQuiz");
                    submitQuiz.setAccessible(true);
                    submitQuiz.invoke(controller);
                    submitCalled[0] = true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        controller.handleSubmit(null);
        assertTrue(submitCalled[0], "submitQuiz should be called after confirmation");
    }

    @Test
    void testSubmitQuiz_CompletionDialogBranch() throws Exception {
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
            new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"), 1, "A")
        );
        controller.preSetController(student, "Sample Quiz", questions, 5);
        // Patch showAlert to simulate completion dialog
        java.lang.reflect.Field showAlertField = controller.getClass().getDeclaredField("showAlert");
        showAlertField.setAccessible(true);
        final boolean[] completionShown = {false};
        showAlertField.set(controller, (StudentQuizController.AlertShower) (type, title, content) -> {
            if (type == Alert.AlertType.INFORMATION && content.contains("completed the quiz")) {
                completionShown[0] = true;
            }
        });
        // Call submitQuiz via reflection
        var submitQuiz = controller.getClass().getDeclaredMethod("submitQuiz");
        submitQuiz.setAccessible(true);
        submitQuiz.invoke(controller);
        assertTrue(completionShown[0], "Completion dialog should be shown");
    }

    @Test
    void testMCQToggleSelectionUpdatesAnswerAndSidebar() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Setup MCQ question
                Student student = new Student();
                List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
                    new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"), 1, "A")
                );
                controller.preSetController(student, "QuizMCQ", questions, 5);

                // Simulate selecting option 2 (B)
                RadioButton option2 = getFieldValue(controller, "option2", RadioButton.class);
                option2.setSelected(true);

                // Simulate toggle event
                ToggleGroup group = getFieldValue(controller, "answerGroup", ToggleGroup.class);
                group.selectToggle(option2);

                // Check that answer is updated
                List<String> studentAnswers = (List<String>) getFieldValue(controller, "studentAnswers", List.class);
                assertEquals("B", studentAnswers.get(0));
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void testShortAnswerTextChangeUpdatesAnswerAndSidebar() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Student student = new Student();
                List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
                    new StudentQuizController.QuizQuestion("Q1", 1)
                );
                controller.preSetController(student, "QuizSA", questions, 5);

                TextArea sa = getFieldValue(controller, "shortAnswerField", TextArea.class);
                sa.setText("Test answer");
                // Simulate text change event if needed (depends on controller code)
                // For most JavaFX, setText triggers listeners

                List<String> studentAnswers = (List<String>) getFieldValue(controller, "studentAnswers", List.class);
                assertEquals("Test answer", studentAnswers.get(0));
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void testRestoreMCQSelectionFromSavedAnswer() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Student student = new Student();
                List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
                    new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"), 1, "A")
                );
                controller.preSetController(student, "QuizMCQ", questions, 5);

                // Simulate saving answer "C"
                List<String> studentAnswers = (List<String>) getFieldValue(controller, "studentAnswers", List.class);
                studentAnswers.set(0, "C");

                // Reload question (simulate navigation)
                var loadQuestion = controller.getClass().getDeclaredMethod("loadQuestion", int.class);
                loadQuestion.setAccessible(true);
                loadQuestion.invoke(controller, 0);

                RadioButton option3 = getFieldValue(controller, "option3", RadioButton.class);
                assertTrue(option3.isSelected());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void testGetCurrentQuestionMaxScoreAndCorrectAnswer() throws Exception {
        Student student = new Student();
        List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
            new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D"), 5, "B")
        );
        controller.preSetController(student, "QuizMaxScore", questions, 5);

        assertEquals(5, controller.getCurrentQuestionMaxScore());
        assertEquals("B", controller.getCurrentQuestionCorrectAnswer());

        // Set index out of bounds
        setField(controller, "currentQuestionIndex", -1);
        assertEquals(0, controller.getCurrentQuestionMaxScore());
        assertNull(controller.getCurrentQuestionCorrectAnswer());
    }

    @Test
    void testRecordDeletionOnSubmitQuiz() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Student student = new Student();
                student.setId(123L);
                List<StudentQuizController.QuizQuestion> questions = Arrays.asList(
                    new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B"), 1, "A")
                );
                controller.preSetController(student, "QuizRecord", questions, 5);

                // Add a matching record to the DB
                comp3111.examsystem.entity.Exam exam = new comp3111.examsystem.entity.Exam();
                exam.setId(999L);
                exam.setName("QuizRecord");
                exam.setQuestions("1");
                new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Exam.class).add(exam);

                comp3111.examsystem.entity.Record record = new comp3111.examsystem.entity.Record();
                record.setId(888L);
                record.setStudent(123L);
                record.setExamID(999L);
                new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Record.class).add(record);

                // Submit the quiz
                java.lang.reflect.Method submitQuiz = controller.getClass().getDeclaredMethod("submitQuiz");
                submitQuiz.setAccessible(true);
                submitQuiz.invoke(controller);

                // Check that the record is deleted
                comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Record> recordDB =
                    new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Record.class);
                boolean exists = recordDB.getAll().stream().anyMatch(r -> r.getId().equals(888L));
                assertFalse(exists, "Record should be deleted after quiz submission");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
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