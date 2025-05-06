package comp3111.examsystem.controller;

import comp3111.examsystem.entity.Record;
import comp3111.examsystem.entity.Teacher;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class TeacherGradeExamControllerTest {
    @TempDir Path tempDir;
    Path examFile;
    Path questionFile;
    Path studentFile;
    TeacherGradeExamController controller;
    MockedStatic<MsgSender> msgSenderMocked;
    FakeRecordDatabase fakeRecordDb;

    @BeforeAll
    static void initJfx() {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    @BeforeEach
    void setUp() throws Exception {
        examFile = tempDir.resolve("exam.txt");
        questionFile = tempDir.resolve("question.txt");
        studentFile = tempDir.resolve("student.txt");

        // Write test data
        Files.write(examFile, List.of(
            "id%&:1!@#courseCode%&:COMP!@#examName%&:Midterm!@#questions%&:101,102!@#isAble%&:true"
        ));
        Files.write(questionFile, List.of(
            "id%&:101!@#questionText%&:What is 2+2?!@#isAble%&:true!@#answer%&:4!@#score%&:10"
        ));
        Files.write(studentFile, List.of(
            "id%&:1!@#name%&:Alice!@#isAble%&:true"
        ));

        fakeRecordDb = new FakeRecordDatabase();
        controller = new TeacherGradeExamController(examFile, questionFile, studentFile, fakeRecordDb);
        setField(controller, "courseFilter", new ComboBox<>());
        setField(controller, "examFilter", new ComboBox<>());
        setField(controller, "studentFilter", new ComboBox<>());
        setField(controller, "questionList", new ListView<>());
        setField(controller, "answerTable", new TableView<>());
        setField(controller, "manualScoreField", new TextField());
        setField(controller, "colStudentName", new TableColumn<>());
        setField(controller, "colStudentAnswer", new TableColumn<>());
        setField(controller, "colScore", new TableColumn<>());
        setField(controller, "correctAnswerLabel", new Label());
        setField(controller, "maxScoreLabel", new Label());

        msgSenderMocked = Mockito.mockStatic(MsgSender.class);
        msgSenderMocked.when(() -> MsgSender.showMsg(Mockito.anyString())).then(invocation -> null);
        msgSenderMocked.when(() -> MsgSender.showConfirm(Mockito.anyString(), Mockito.anyString(), Mockito.any())).then(invocation -> {
            Runnable callback = invocation.getArgument(2);
            callback.run();
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        if (msgSenderMocked != null) msgSenderMocked.close();
    }

    // --- Helper methods ---
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    private Object getField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    // --- TESTS ---

    @Test
    void testInitialState() {
        assertTrue(((TableView<?>) getField(controller, "answerTable")).getItems().isEmpty());
    }

    @Test
    void testFilterExam_HappyPath() throws Exception {
        ComboBox<String> courseFilter = (ComboBox<String>) getField(controller, "courseFilter");
        ComboBox<String> examFilter = (ComboBox<String>) getField(controller, "examFilter");
        // Simulate examLineMap and courseToExamsMap
        setField(controller, "currentExamID", 1L);
        Map<String, String> examLineMap = new HashMap<>();
        String examLine = "id%&:1!@#courseCode%&:COMP!@#examName%&:Midterm!@#questions%&:101,102!@#isAble%&:true";
        examLineMap.put("COMP|Midterm", examLine);
        setField(controller, "examLineMap", examLineMap);
        courseFilter.getItems().add("COMP");
        examFilter.getItems().add("Midterm");
        courseFilter.getSelectionModel().select("COMP");
        examFilter.getSelectionModel().select("Midterm");
        // Patch displayQuestionsFromExamLine to avoid file IO
        Field qMapField = TeacherGradeExamController.class.getDeclaredField("questionTextToId");
        qMapField.setAccessible(true);
        qMapField.set(controller, new HashMap<>());
        // Call filterExam
        var method = controller.getClass().getDeclaredMethod("filterExam");
        method.setAccessible(true);
        method.invoke(controller);
        // Should not call error message
        msgSenderMocked.verify(() -> MsgSender.showMsg(anyString()), Mockito.never());
    }

    @Test
    void testFilterExam_MissingCourseOrExam() throws Exception {
        ComboBox<String> courseFilter = (ComboBox<String>) getField(controller, "courseFilter");
        ComboBox<String> examFilter = (ComboBox<String>) getField(controller, "examFilter");
        // Only course selected
        courseFilter.getItems().add("COMP");
        courseFilter.getSelectionModel().select("COMP");
        var method = controller.getClass().getDeclaredMethod("filterExam");
        method.setAccessible(true);
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("Please select both a course and an exam.")));
        // Only exam selected
        courseFilter.getSelectionModel().clearSelection();
        examFilter.getItems().add("Midterm");
        examFilter.getSelectionModel().select("Midterm");
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("Please select both a course and an exam.")), Mockito.times(2));
    }

    @Test
    void testFilterExam_NonExistentExam() throws Exception {
        ComboBox<String> courseFilter = (ComboBox<String>) getField(controller, "courseFilter");
        ComboBox<String> examFilter = (ComboBox<String>) getField(controller, "examFilter");
        courseFilter.getItems().add("COMP");
        examFilter.getItems().add("Midterm");
        courseFilter.getSelectionModel().select("COMP");
        examFilter.getSelectionModel().select("Midterm");
        setField(controller, "examLineMap", new HashMap<>()); // empty
        var method = controller.getClass().getDeclaredMethod("filterExam");
        method.setAccessible(true);
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("No exam found")));
    }

    @Test
    void testResetFilter() throws Exception {
        ComboBox<String> courseFilter = (ComboBox<String>) getField(controller, "courseFilter");
        ComboBox<String> examFilter = (ComboBox<String>) getField(controller, "examFilter");
        ListView<String> questionList = (ListView<String>) getField(controller, "questionList");
        courseFilter.getItems().add("COMP");
        examFilter.getItems().add("Midterm");
        questionList.getItems().add("Q1");
        var method = controller.getClass().getDeclaredMethod("resetFilter");
        method.setAccessible(true);
        method.invoke(controller);
        assertTrue(courseFilter.getSelectionModel().isEmpty());
        assertTrue(examFilter.getSelectionModel().isEmpty());
        assertTrue(examFilter.getItems().isEmpty());
        assertTrue(questionList.getItems().isEmpty());
    }

    @Test
    void testUpdateScore_NoRecordSelected() throws Exception {
        setField(controller, "selectedRecord", null);
        var method = controller.getClass().getDeclaredMethod("updateScore");
        method.setAccessible(true);
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("Please select a student answer first.")));
    }

    @Test
    void testUpdateScore_EmptyScore() throws Exception {
        Record rec = new Record(101L, 1L, 1L, "A", 5);
        setField(controller, "selectedRecord", rec);
        ((TextField) getField(controller, "manualScoreField")).setText("");
        var method = controller.getClass().getDeclaredMethod("updateScore");
        method.setAccessible(true);
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("Score cannot be empty.")));
    }

    @Test
    void testUpdateScore_NonNumericScore() throws Exception {
        Record rec = new Record(101L, 1L, 1L, "A", 5);
        setField(controller, "selectedRecord", rec);
        ((TextField) getField(controller, "manualScoreField")).setText("abc");
        var method = controller.getClass().getDeclaredMethod("updateScore");
        method.setAccessible(true);
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("valid numeric score")));
    }

    @Test
    void testUpdateScore_ScoreOutOfRange() throws Exception {
        Record rec = new Record(101L, 1L, 1L, "A", 5);
        setField(controller, "selectedRecord", rec);
        ((TextField) getField(controller, "manualScoreField")).setText("100");
        ((Label) getField(controller, "maxScoreLabel")).setText("10");
        var method = controller.getClass().getDeclaredMethod("updateScore");
        method.setAccessible(true);
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("Score must be between 0 and 10")));
    }

    @Test
    void testUpdateScore_ValidScore() throws Exception {
        Record rec = new Record(101L, 1L, 1L, "A", 5);
        rec.setId(1L);
        fakeRecordDb.records.clear();
        fakeRecordDb.records.add(rec);
        setField(controller, "selectedRecord", rec);
        ((TextField) getField(controller, "manualScoreField")).setText("8");
        ((Label) getField(controller, "maxScoreLabel")).setText("10");
        var method = controller.getClass().getDeclaredMethod("updateScore");
        method.setAccessible(true);
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(Mockito.contains("Score updated successfully.")));
    }

    @Test
    void testDisplayQuestionDetails_Valid() throws Exception {
        var method = controller.getClass().getDeclaredMethod("displayQuestionDetails", String.class);
        method.setAccessible(true);
        method.invoke(controller, "101");
        Label correctAnswerLabel = (Label) getField(controller, "correctAnswerLabel");
        Label maxScoreLabel = (Label) getField(controller, "maxScoreLabel");
        assertEquals("4", correctAnswerLabel.getText());
        assertEquals("10", maxScoreLabel.getText());
    }

    @Test
    void testDisplayQuestionDetails_Invalid() throws Exception {
        var method = controller.getClass().getDeclaredMethod("displayQuestionDetails", String.class);
        method.setAccessible(true);
        method.invoke(controller, "999");
        Label correctAnswerLabel = (Label) getField(controller, "correctAnswerLabel");
        Label maxScoreLabel = (Label) getField(controller, "maxScoreLabel");
        assertEquals("N/A", correctAnswerLabel.getText());
        assertEquals("N/A", maxScoreLabel.getText());
    }

    @Test
    void testDisplayStudentResponsesForQuestion() throws Exception {
        fakeRecordDb.records.clear();
        Record rec = new Record(101L, 1L, 1L, "A", 5);
        rec.setId(1L);
        fakeRecordDb.records.add(rec);
        setField(controller, "currentExamID", 1L);
        ComboBox<String> studentFilter = (ComboBox<String>) getField(controller, "studentFilter");
        studentFilter.getItems().addAll("ALL", "Alice", "Bob");
        studentFilter.getSelectionModel().select("ALL");
        var method = controller.getClass().getDeclaredMethod("displayStudentResponsesForQuestion", String.class);
        method.setAccessible(true);
        method.invoke(controller, "101");
        TableView<Record> answerTable = (TableView<Record>) getField(controller, "answerTable");
        assertEquals(1, answerTable.getItems().size());
    }

    @Test
    void testCloseApplication() throws Exception {
        var method = controller.getClass().getDeclaredMethod("closeApplication");
        method.setAccessible(true);
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showConfirm(anyString(), anyString(), any()));
    }

    @Test
    void testLoadExamOptions_FileIoSuccess() throws Exception {
        var method = controller.getClass().getDeclaredMethod("loadExamOptions");
        method.setAccessible(true);
        method.invoke(controller);
        ComboBox<String> courseFilter = (ComboBox<String>) getField(controller, "courseFilter");
        assertTrue(courseFilter.getItems().contains("COMP"));
    }

    @Test
    void testGetStudentNameById_FileIo() throws Exception {
        var method = controller.getClass().getDeclaredMethod("getStudentNameById", String.class);
        method.setAccessible(true);
        String name1 = (String) method.invoke(controller, "1");
        String name2 = (String) method.invoke(controller, "999");
        assertEquals("Alice", name1);
        assertEquals("Unknown", name2);
    }

    @Test
    void testDisplayQuestionsFromExamLine_PopulatesQuestionsAndStudents() throws Exception {
        fakeRecordDb.records.clear();
        Record rec = new Record(101L, 1L, 1L, "A", 5);
        rec.setId(1L);
        fakeRecordDb.records.add(rec);
        setField(controller, "currentExamID", 1L);
        var method = controller.getClass().getDeclaredMethod("displayQuestionsFromExamLine", String.class);
        method.setAccessible(true);
        String examLine = "id%&:1!@#courseCode%&:COMP!@#examName%&:Midterm!@#questions%&:101!@#isAble%&:true";
        method.invoke(controller, examLine);
        ListView<String> questionList = (ListView<String>) getField(controller, "questionList");
        assertTrue(questionList.getItems().contains("What is 2+2?"));
        ComboBox<String> studentFilter = (ComboBox<String>) getField(controller, "studentFilter");
        assertTrue(studentFilter.getItems().contains("Alice"));
    }

    static class FakeRecordDatabase extends comp3111.examsystem.tools.Database<Record> {
        List<Record> records = new ArrayList<>();
        public FakeRecordDatabase() { super(Record.class); }
        @Override public List<Record> getAllEnabled() { return new ArrayList<>(records); }
        @Override public void add(Record r) { records.add(r); }
        @Override public void update(Record r) { /* no-op for test */ }
    }
} 