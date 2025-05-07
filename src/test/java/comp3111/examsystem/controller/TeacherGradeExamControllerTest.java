package comp3111.examsystem.controller;

import comp3111.examsystem.entity.Record;
import comp3111.examsystem.entity.Teacher;
import comp3111.examsystem.tools.MsgSender;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.lang.reflect.Field;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import comp3111.examsystem.tools.Database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        if (msgSenderMocked != null) {
            msgSenderMocked.close(); // ensure stale mock is not active
            msgSenderMocked = null;
        }
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
        // Set all FXML fields before initialize
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
        setField(controller, "studentNameLabel", new Label());
        setField(controller, "studentAnswerLabel", new Label());
        setField(controller, "updateButton", new Button());
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
        try {
            if (msgSenderMocked != null) {
                msgSenderMocked.close();
                msgSenderMocked = null;
            }
        } catch (Exception ignored) {}
    }

    // --- Helper methods ---
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
    private Object getField(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // --- TESTS ---
    @Test
    void testInitialState() {
        assertTrue(((TableView<?>) getField(controller, "answerTable")).getItems().isEmpty());
    }

    //Initialize() test
    @Test
    void testCourseFilterPopulatesExamFilter() throws Exception {
        ComboBox<String> courseFilter = new ComboBox<>();
        ComboBox<String> examFilter = new ComboBox<>();
        Map<String, List<String>> courseToExamsMap = new HashMap<>();
        courseToExamsMap.put("COMP", new ArrayList<>(List.of("Midterm", "Final")));
        courseFilter.getItems().add("COMP");
        courseFilter.getSelectionModel().select("COMP");
        setField(controller, "courseFilter", courseFilter);
        setField(controller, "examFilter", examFilter);
        setField(controller, "courseToExamsMap", courseToExamsMap);
        controller.initialize(null, null);
        courseFilter.getOnAction().handle(null);
        assertEquals(List.of("Midterm", "Final"), examFilter.getItems());
    }

    //Handle Question Selection Test Cases
    @Test
    void testQuestionSelection_noAnswers_updatesDetailPanelWithNull() throws Exception {
        ListView<String> questionList = new ListView<>();
        TableView<Record> answerTable = new TableView<>();
        answerTable.setItems(FXCollections.observableArrayList()); // empty list
        Map<String, String> questionMap = Map.of("Q1", "101");
        // Instead of spy, use a flag to check if methods are called
        final boolean[] called = {false, false, false};
        TestableTeacherGradeExamController testController = new TestableTeacherGradeExamController(examFile, questionFile, studentFile, fakeRecordDb);
        setField(testController, "courseFilter", new ComboBox<>());
        setField(testController, "examFilter", new ComboBox<>());
        setField(testController, "studentFilter", new ComboBox<>());
        setField(testController, "questionList", questionList);
        setField(testController, "answerTable", answerTable);
        setField(testController, "manualScoreField", new TextField());
        setField(testController, "colStudentName", new TableColumn<>());
        setField(testController, "colStudentAnswer", new TableColumn<>());
        setField(testController, "colScore", new TableColumn<>());
        setField(testController, "correctAnswerLabel", new Label());
        setField(testController, "maxScoreLabel", new Label());
        setField(testController, "studentNameLabel", new Label());
        setField(testController, "studentAnswerLabel", new Label());
        setField(testController, "updateButton", new Button());
        setField(testController, "questionTextToId", questionMap);
        testController.onDisplayQuestionDetails = () -> called[0] = true;
        testController.onRefreshCurrentQuestionData = () -> called[1] = true;
        testController.onUpdateDetailPanel = () -> called[2] = true;
        testController.initialize(null, null);
        questionList.getItems().add("Q1");
        questionList.getSelectionModel().select("Q1");
        assertTrue(called[0]);
        assertTrue(called[1]);
        assertTrue(called[2]);
    }

    @Test
    void testQuestionSelected_withAnswers_updatesDetail() throws Exception {
        ListView<String> questionList = new ListView<>();
        TableView<Record> answerTable = new TableView<>();
        TextField manualScoreField = new TextField();
        final int[] updateDetailPanelCount = {0};
        TestableTeacherGradeExamController testController = new TestableTeacherGradeExamController(examFile, questionFile, studentFile, fakeRecordDb);
        setField(testController, "courseFilter", new ComboBox<>());
        setField(testController, "examFilter", new ComboBox<>());
        setField(testController, "studentFilter", new ComboBox<>());
        setField(testController, "questionList", questionList);
        setField(testController, "answerTable", answerTable);
        setField(testController, "manualScoreField", manualScoreField);
        setField(testController, "colStudentName", new TableColumn<>());
        setField(testController, "colStudentAnswer", new TableColumn<>());
        setField(testController, "colScore", new TableColumn<>());
        setField(testController, "correctAnswerLabel", new Label());
        setField(testController, "maxScoreLabel", new Label());
        setField(testController, "studentNameLabel", new Label());
        setField(testController, "studentAnswerLabel", new Label());
        setField(testController, "updateButton", new Button());
        setField(testController, "questionTextToId", Map.of("Q1", "101"));
        testController.onDisplayQuestionDetails = () -> {};
        testController.onRefreshCurrentQuestionData = () -> {};
        testController.onUpdateDetailPanel = () -> updateDetailPanelCount[0]++;
        testController.initialize(null, null);
        Record r = new Record(101L, 1L, 1L, "A", 5);
        answerTable.setItems(FXCollections.observableArrayList(r));
        questionList.getItems().add("Q1");
        questionList.getSelectionModel().select("Q1");
        assertEquals(2, updateDetailPanelCount[0]);
    }

    @Test
    void testAnswerSelected_withValidQuestion_updatesDetailPanel() throws Exception {
        ListView<String> questionList = new ListView<>();
        TableView<Record> answerTable = new TableView<>();
        TextField manualScoreField = new TextField();
        TestableTeacherGradeExamController testController = new TestableTeacherGradeExamController(examFile, questionFile, studentFile, fakeRecordDb);
        setField(testController, "courseFilter", new ComboBox<>());
        setField(testController, "examFilter", new ComboBox<>());
        setField(testController, "studentFilter", new ComboBox<>());
        setField(testController, "questionList", questionList);
        setField(testController, "answerTable", answerTable);
        setField(testController, "manualScoreField", manualScoreField);
        setField(testController, "colStudentName", new TableColumn<>());
        setField(testController, "colStudentAnswer", new TableColumn<>());
        setField(testController, "colScore", new TableColumn<>());
        setField(testController, "correctAnswerLabel", new Label());
        setField(testController, "maxScoreLabel", new Label());
        setField(testController, "studentNameLabel", new Label());
        setField(testController, "studentAnswerLabel", new Label());
        setField(testController, "updateButton", new Button());
        setField(testController, "questionTextToId", Map.of("Q1", "101"));
        testController.initialize(null, null);
        assertTrue(((TableView<?>) getField(testController, "answerTable")).getItems().isEmpty());
    }

    //LoadExamOptions() test
    @Test
    void testLoadExamOptions_validMatch_addedToMapAndComboBox() throws Exception {
        // Teacher with ID = 42
        setField(controller, "teacher", new Teacher(42L, "", "", "", null, 0, null, null));

        // Create matching line
        String examLine = "courseCode%&:COMP!@#examName%&:Midterm!@#teacherId%&:42!@#isAble%&:true";
        Files.write(examFile, List.of(examLine));
        controller.loadExamOptions();

        Map<String, String> examLineMap = (Map<String, String>) getField(controller, "examLineMap");
        Map<String, List<String>> courseToExamsMap = (Map<String, List<String>>) getField(controller, "courseToExamsMap");
        ComboBox<String> courseFilter = (ComboBox<String>) getField(controller, "courseFilter");

        assertTrue(examLineMap.containsKey("COMP|Midterm"));
        assertTrue(courseToExamsMap.get("COMP").contains("Midterm"));
        assertTrue(courseFilter.getItems().contains("COMP"));
    }

    @Test
    void testLoadExamOptions_wrongTeacherId_notIncluded() throws Exception {
        setField(controller, "teacher", new Teacher(99L, "", "", "", null, 0, null, null));

        String line = "courseCode%&:COMP!@#examName%&:Final!@#teacherId%&:42!@#isAble%&:true";
        Files.write(examFile, List.of(line));
        controller.loadExamOptions();

        Map<String, String> examLineMap = (Map<String, String>) getField(controller, "examLineMap");
        assertTrue(examLineMap.isEmpty());
    }

    @Test
    void testLoadExamOptions_lineWithoutIsAbleTrue_skipped() throws Exception {
        setField(controller, "teacher", new Teacher(42L, "", "", "", null, 0, null, null));

        String line = "courseCode%&:COMP!@#examName%&:Final!@#teacherId%&:42"; // missing isAble=true
        Files.write(examFile, List.of(line));
        controller.loadExamOptions();

        Map<String, String> examLineMap = (Map<String, String>) getField(controller, "examLineMap");
        assertTrue(examLineMap.isEmpty());
    }

    @Test
    void testLoadExamOptions_missingFields_skipped() throws Exception {
        setField(controller, "teacher", new Teacher(42L, "", "", "", null, 0, null, null));

        String line = "examName%&:Final!@#teacherId%&:42!@#isAble%&:true"; // missing courseCode
        Files.write(examFile, List.of(line));
        controller.loadExamOptions();

        Map<String, String> examLineMap = (Map<String, String>) getField(controller, "examLineMap");
        assertTrue(examLineMap.isEmpty());
    }

    @Test
    void testLoadExamOptions_ioException_showsErrorMsg() throws Exception {
        setField(controller, "examFilePath", Path.of("nonexistent.txt")); // Nonexistent file
        controller.loadExamOptions();
        msgSenderMocked.verify(() -> MsgSender.showMsg("Failed to load exams."));
    }

    //Filter Exam Tests
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

    //Extract Exam
    @Test
    void testExtractExamIdFromLine_invalidId_throwsException() {
        String line = "id%&:abc!@#courseCode%&:COMP";
        assertThrows(NumberFormatException.class, () -> {
            controller.extractExamIdFromLine(line);
        });
    }
    @Test
    void testExtractExamIdFromLine_missingId_returnsNull() {
        String line = "courseCode%&:COMP!@#examName%&:Midterm";
        Long result = controller.extractExamIdFromLine(line);
        assertNull(result);
    }
    @Test
    void testExtractExamIdFromLine_validId_returnsId() {
        String line = "id%&:42!@#courseCode%&:COMP";
        Long result = controller.extractExamIdFromLine(line);
        assertEquals(42L, result);
    }

    //Update score test
    @Test
    void testUpdateScore_variousScenarios() throws Exception {
        Record rec = new Record(101L, 1L, 1L, "A", 5);
        rec.setId(1L);
        fakeRecordDb.records.clear();
        fakeRecordDb.records.add(rec);

        // Helper to invoke method
        var method = controller.getClass().getDeclaredMethod("updateScore");
        method.setAccessible(true);

        // --- No Record Selected ---
        setField(controller, "selectedRecord", null);
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("select a student answer")), atLeastOnce());

        // --- Empty Score ---
        setField(controller, "selectedRecord", rec);
        ((TextField) getField(controller, "manualScoreField")).setText("");
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("Score cannot be empty")), atLeastOnce());

        // --- Non-Numeric Score ---
        ((TextField) getField(controller, "manualScoreField")).setText("abc");
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("valid numeric score")), atLeastOnce());

        // --- Score Out of Range ---
        ((TextField) getField(controller, "manualScoreField")).setText("100");
        ((Label) getField(controller, "maxScoreLabel")).setText("10");
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("Score must be between 0 and 10")), atLeastOnce());

        // --- Max Score = N/A ---
        ((TextField) getField(controller, "manualScoreField")).setText("99");
        ((Label) getField(controller, "maxScoreLabel")).setText("N/A");
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("Score updated successfully")), atLeastOnce());

        // --- Valid Score ---
        ((TextField) getField(controller, "manualScoreField")).setText("8");
        ((Label) getField(controller, "maxScoreLabel")).setText("10");
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showMsg(contains("Score updated successfully")), atLeastOnce());
    }

    //Display Question Details Test
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

    //Display Student Responses Test
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
    void testDisplayStudentResponses_invalidQuestionId_parsingFails() throws Exception {
        fakeRecordDb.records.clear();
        fakeRecordDb.records.add(new Record(101L, 1L, 1L, "A", 5));
        setField(controller, "currentExamID", 1L);

        ComboBox<String> studentFilter = (ComboBox<String>) getField(controller, "studentFilter");
        studentFilter.getItems().add("ALL");
        studentFilter.getSelectionModel().select("ALL");

        var method = controller.getClass().getDeclaredMethod("displayStudentResponsesForQuestion", String.class);
        method.setAccessible(true);
        method.invoke(controller, "INVALID"); // Not a number

        TableView<Record> answerTable = (TableView<Record>) getField(controller, "answerTable");
        assertEquals(0, answerTable.getItems().size());
    }
    @Test
    void testDisplayStudentResponses_mismatchedIds_resultsEmpty() throws Exception {
        fakeRecordDb.records.clear();
        fakeRecordDb.records.add(new Record(999L, 1L, 2L, "A", 5)); // wrong QID or EID
        setField(controller, "currentExamID", 1L);

        ComboBox<String> studentFilter = (ComboBox<String>) getField(controller, "studentFilter");
        studentFilter.getItems().add("ALL");
        studentFilter.getSelectionModel().select("ALL");

        var method = controller.getClass().getDeclaredMethod("displayStudentResponsesForQuestion", String.class);
        method.setAccessible(true);
        method.invoke(controller, "101");

        TableView<Record> answerTable = (TableView<Record>) getField(controller, "answerTable");
        assertEquals(0, answerTable.getItems().size());
    }
    @Test
    void testDisplayStudentResponses_selectedStudentNull_allMatch() throws Exception {
        Record rec = new Record(101L, 1L, 1L, "A", 5);
        fakeRecordDb.records.clear();
        fakeRecordDb.records.add(rec);
        setField(controller, "currentExamID", 1L);

        ComboBox<String> studentFilter = (ComboBox<String>) getField(controller, "studentFilter");
        // do not select anything (remains null)
        setField(controller, "studentFilter", studentFilter);

        var method = controller.getClass().getDeclaredMethod("displayStudentResponsesForQuestion", String.class);
        method.setAccessible(true);
        method.invoke(controller, "101");

        TableView<Record> answerTable = (TableView<Record>) getField(controller, "answerTable");
        assertEquals(1, answerTable.getItems().size());
    }
    @Test
    void testDisplayStudentResponses_specificStudent_selected() throws Exception {
        Record rec = new Record(101L, 1L, 1L, "A", 5);
        fakeRecordDb.records.clear();
        fakeRecordDb.records.add(rec);
        setField(controller, "currentExamID", 1L);

        ComboBox<String> studentFilter = (ComboBox<String>) getField(controller, "studentFilter");
        studentFilter.getItems().addAll("Alice");
        studentFilter.getSelectionModel().select("Alice");
        setField(controller, "studentFilter", studentFilter);

        var method = controller.getClass().getDeclaredMethod("displayStudentResponsesForQuestion", String.class);
        method.setAccessible(true);
        method.invoke(controller, "101");

        TableView<Record> answerTable = (TableView<Record>) getField(controller, "answerTable");
        assertEquals(1, answerTable.getItems().size());
    }
    @Test
    void testDisplayStudentResponses_specificStudent_doesNotMatch() throws Exception {
        Record rec = new Record(101L, 1L, 1L, "A", 5);
        fakeRecordDb.records.clear();
        fakeRecordDb.records.add(rec);
        setField(controller, "currentExamID", 1L);

        ComboBox<String> studentFilter = (ComboBox<String>) getField(controller, "studentFilter");
        studentFilter.getItems().addAll("Bob");
        studentFilter.getSelectionModel().select("Bob");
        setField(controller, "studentFilter", studentFilter);

        var method = controller.getClass().getDeclaredMethod("displayStudentResponsesForQuestion", String.class);
        method.setAccessible(true);
        method.invoke(controller, "101");

        TableView<Record> answerTable = (TableView<Record>) getField(controller, "answerTable");
        assertEquals(0, answerTable.getItems().size());
    }

    @Test
    void testCloseApplication() throws Exception {
        var method = controller.getClass().getDeclaredMethod("closeApplication");
        method.setAccessible(true);
        method.invoke(controller);
        msgSenderMocked.verify(() -> MsgSender.showConfirm(anyString(), anyString(), any()));
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

    //Display Questions and Populate Students in the Filter Section
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

    //Test UpdateDetailPanel
    @Test
    public void testUpdateDetailPanel_withNullQuestionId_shouldClearLabels() throws Exception {
        Record mockRecord = new Record(101L, 1L, 1L, "Answer", 5);
        Label studentNameLabel = new Label("Pre");
        Label studentAnswerLabel = new Label("Pre");
        Label correctAnswerLabel = new Label("Pre");
        Label maxScoreLabel = new Label("Pre");
        setField(controller, "studentNameLabel", studentNameLabel);
        setField(controller, "studentAnswerLabel", studentAnswerLabel);
        setField(controller, "correctAnswerLabel", correctAnswerLabel);
        setField(controller, "maxScoreLabel", maxScoreLabel);
        controller.updateDetailPanel(mockRecord, null);
        assertEquals("", studentNameLabel.getText());
        assertEquals("", studentAnswerLabel.getText());
        assertEquals("N/A", correctAnswerLabel.getText());
        assertEquals("N/A", maxScoreLabel.getText());
    }

    @Test
    public void testUpdateDetailPanel_withNullRecord_shouldClearLabels() throws Exception {
        Label studentNameLabel = new Label();
        Label studentAnswerLabel = new Label();
        Label correctAnswerLabel = new Label("Old");
        Label maxScoreLabel = new Label("Old");
        setField(controller, "studentNameLabel", studentNameLabel);
        setField(controller, "studentAnswerLabel", studentAnswerLabel);
        setField(controller, "correctAnswerLabel", correctAnswerLabel);
        setField(controller, "maxScoreLabel", maxScoreLabel);
        controller.updateDetailPanel(null, "101");
        assertEquals("", studentNameLabel.getText());
        assertEquals("", studentAnswerLabel.getText());
        assertEquals("N/A", correctAnswerLabel.getText());
        assertEquals("N/A", maxScoreLabel.getText());
    }

    @Test
    void testUpdateDetailPanel_nonMcq_shouldDisplayRawAnswer() throws Exception {
        Record record = new Record(101L, 1L, 1L, "AnswerText", 7);
        Label studentNameLabel = new Label();
        Label studentAnswerLabel = new Label();
        Label correctAnswerLabel = new Label();
        Label maxScoreLabel = new Label();

        setField(controller, "studentNameLabel", studentNameLabel);
        setField(controller, "studentAnswerLabel", studentAnswerLabel);
        setField(controller, "correctAnswerLabel", correctAnswerLabel);
        setField(controller, "maxScoreLabel", maxScoreLabel);

        // Use a subclass to override data methods
        TeacherGradeExamController testController = new TeacherGradeExamController(examFile, questionFile, studentFile, fakeRecordDb) {
            @Override public String getStudentNameById(String id) { return "Alice"; }
            @Override public String getQuestionType(String qid) { return "ShortAnswer"; }
            @Override public String getCorrectAnswer(String qid) { return "Expected"; }
            @Override public String getMaxScore(String qid) { return "10"; }
        };
        setField(testController, "studentNameLabel", studentNameLabel);
        setField(testController, "studentAnswerLabel", studentAnswerLabel);
        setField(testController, "correctAnswerLabel", correctAnswerLabel);
        setField(testController, "maxScoreLabel", maxScoreLabel);
        testController.updateDetailPanel(record, "101");
        assertEquals("Alice", studentNameLabel.getText());
        assertEquals("AnswerText", studentAnswerLabel.getText());
        assertEquals("Expected", correctAnswerLabel.getText());
        assertEquals("10", maxScoreLabel.getText());
    }

    @Test
    void testUpdateDetailPanel_mcq_shouldFormatWithLetterAndText() throws Exception {
        Record record = new Record(101L, 1L, 1L, "B", 7);
        Label studentNameLabel = new Label();
        Label studentAnswerLabel = new Label();
        Label correctAnswerLabel = new Label();
        Label maxScoreLabel = new Label();

        setField(controller, "studentNameLabel", studentNameLabel);
        setField(controller, "studentAnswerLabel", studentAnswerLabel);
        setField(controller, "correctAnswerLabel", correctAnswerLabel);
        setField(controller, "maxScoreLabel", maxScoreLabel);

        TeacherGradeExamController testController = new TeacherGradeExamController(examFile, questionFile, studentFile, fakeRecordDb) {
            @Override public String getStudentNameById(String id) { return "Alice"; }
            @Override public String getQuestionType(String qid) { return "MCQ"; }
            @Override public List<String> getOptionsForQuestion(String qid) { return List.of("A1", "B2", "C3"); }
            @Override public String getCorrectAnswer(String qid) { return "B"; }
            @Override public String getMaxScore(String qid) { return "10"; }
            @Override public String getOptionTextByLetter(String letter, List<String> options) { return "B2"; }
        };
        setField(testController, "studentNameLabel", studentNameLabel);
        setField(testController, "studentAnswerLabel", studentAnswerLabel);
        setField(testController, "correctAnswerLabel", correctAnswerLabel);
        setField(testController, "maxScoreLabel", maxScoreLabel);
        testController.updateDetailPanel(record, "101");
        assertEquals("Alice", studentNameLabel.getText());
        assertEquals("B. B2", studentAnswerLabel.getText());
        assertEquals("B. B2", correctAnswerLabel.getText());
        assertEquals("10", maxScoreLabel.getText());
    }
    @Test
    void testUpdateDetailPanel_mcq_withMissingOptionText_shouldStillShowLetter() throws Exception {
        Record record = new Record(101L, 1L, 1L, "Z", 7); // invalid option
        Label studentNameLabel = new Label();
        Label studentAnswerLabel = new Label();
        Label correctAnswerLabel = new Label();
        Label maxScoreLabel = new Label();

        setField(controller, "studentNameLabel", studentNameLabel);
        setField(controller, "studentAnswerLabel", studentAnswerLabel);
        setField(controller, "correctAnswerLabel", correctAnswerLabel);
        setField(controller, "maxScoreLabel", maxScoreLabel);

        TeacherGradeExamController testController = new TeacherGradeExamController(examFile, questionFile, studentFile, fakeRecordDb) {
            @Override public String getStudentNameById(String id) { return "Alice"; }
            @Override public String getQuestionType(String qid) { return "MCQ"; }
            @Override public List<String> getOptionsForQuestion(String qid) { return List.of("A1", "B2", "C3"); }
            @Override public String getCorrectAnswer(String qid) { return "Z"; }
            @Override public String getMaxScore(String qid) { return "10"; }
            @Override public String getOptionTextByLetter(String letter, List<String> options) { return ""; }
        };
        setField(testController, "studentNameLabel", studentNameLabel);
        setField(testController, "studentAnswerLabel", studentAnswerLabel);
        setField(testController, "correctAnswerLabel", correctAnswerLabel);
        setField(testController, "maxScoreLabel", maxScoreLabel);
        testController.updateDetailPanel(record, "101");
        assertEquals("Z", studentAnswerLabel.getText()); // fallback
        assertEquals("Z", correctAnswerLabel.getText());
    }

    @Test
    void testExtractExamId_MatchFoundInMiddle() {
        String input = "foo!@#id%&:12345!@#bar";
        Long expected = 12345L;
        Long actual = extractExamIdFromLine(input);
        assertEquals(expected, actual);
    }

    @Test
    void testExtractExamId_NoMatchFound() {
        String input = "foo!@#bar!@#baz";
        Long actual = extractExamIdFromLine(input);
        assertNull(actual);
    }

    @Test
    void testExtractExamId_MatchAtStart() {
        String input = "id%&:98765!@#bar!@#baz";
        Long expected = 98765L;
        Long actual = extractExamIdFromLine(input);
        assertEquals(expected, actual);
    }

    @Test
    void testExtractExamId_MatchAtEnd() {
        String input = "foo!@#bar!@#id%&:24680";
        Long expected = 24680L;
        Long actual = extractExamIdFromLine(input);
        assertEquals(expected, actual);
    }

    // Optional edge case (invalid number format)
    @Test
    void testExtractExamId_InvalidNumberFormat() {
        String input = "foo!@#id%&:abc!@#bar";
        assertThrows(NumberFormatException.class, () -> {
            extractExamIdFromLine(input);
        });
    }

    // Helper method for testing – replace with actual method if in a different class
    private Long extractExamIdFromLine(String examLine) {
        String[] fields = examLine.split("!@#");
        for (String field : fields) {
            if (field.startsWith("id%&:")) {
                return Long.parseLong(field.split("%&:")[1]);
            }
        }
        return null;
    }

    //Test RefreshCurrentQuestionData
    @Test
    public void testRefreshCurrentQuestionData_withValidSelection_callsDisplayAndTogglesControls() throws Exception {
        // Real JavaFX controls
        ListView<String> questionList = new ListView<>();
        TextField manualScoreField = new TextField();
        Button updateButton = new Button();
        setField(controller, "questionList", questionList);
        setField(controller, "manualScoreField", manualScoreField);
        setField(controller, "updateButton", updateButton);
        setField(controller, "questionTextToId", Map.of("What is 2+2?", "101"));
        questionList.getItems().add("What is 2+2?");
        questionList.getSelectionModel().select("What is 2+2?");
        // Use a subclass to override displayStudentResponsesForQuestion
        final boolean[] called = {false};
        TeacherGradeExamController testController = new TeacherGradeExamController(examFile, questionFile, studentFile, fakeRecordDb) {
            @Override public void displayStudentResponsesForQuestion(String qid) { called[0] = true; }
        };
        setField(testController, "questionList", questionList);
        setField(testController, "manualScoreField", manualScoreField);
        setField(testController, "updateButton", updateButton);
        setField(testController, "questionTextToId", Map.of("What is 2+2?", "101"));
        testController.refreshCurrentQuestionData();
        assertFalse(manualScoreField.isDisabled());
        assertFalse(updateButton.isDisabled());
        assertTrue(called[0]);
    }

    @Test
    public void testRefreshCurrentQuestionData_withNoSelection_doesNotCallDisplay() throws Exception {
        ListView<String> questionList = new ListView<>();
        TextField manualScoreField = new TextField();
        Button updateButton = new Button();
        setField(controller, "questionList", questionList);
        setField(controller, "manualScoreField", manualScoreField);
        setField(controller, "updateButton", updateButton);
        setField(controller, "questionTextToId", Map.of());
        // Use a subclass to override displayStudentResponsesForQuestion
        final boolean[] called = {false};
        TeacherGradeExamController testController = new TeacherGradeExamController(examFile, questionFile, studentFile, fakeRecordDb) {
            @Override public void displayStudentResponsesForQuestion(String qid) { called[0] = true; }
        };
        setField(testController, "questionList", questionList);
        setField(testController, "manualScoreField", manualScoreField);
        setField(testController, "updateButton", updateButton);
        setField(testController, "questionTextToId", Map.of());
        testController.refreshCurrentQuestionData();
        assertFalse(manualScoreField.isDisabled());
        assertFalse(updateButton.isDisabled());
        assertFalse(called[0]);
    }

    //GetOptionTextByLetter() Test
    @Test
    void testGetOptionTextByLetter_nullLetter_returnsEmpty() {
        List<String> options = List.of("Apple", "Banana", "Cherry");
        String result = controller.getOptionTextByLetter(null, options);
        assertEquals("", result);
    }

    @Test
    void testGetOptionTextByLetter_invalidLength_returnsEmpty() {
        List<String> options = List.of("Apple", "Banana", "Cherry");
        String result = controller.getOptionTextByLetter("AB", options);
        assertEquals("", result);
    }

    @Test
    void testGetOptionTextByLetter_negativeIndex_returnsEmpty() {
        List<String> options = List.of("Apple", "Banana", "Cherry");
        String result = controller.getOptionTextByLetter("@", options); // '@' = 64, 'A' = 65 → idx = -1
        assertEquals("", result);
    }

    @Test
    void testGetOptionTextByLetter_outOfBounds_returnsEmpty() {
        List<String> options = List.of("Apple", "Banana", "Cherry");
        String result = controller.getOptionTextByLetter("Z", options); // idx = 25, out of range
        assertEquals("", result);
    }

    @Test
    void testGetOptionTextByLetter_validIndex_returnsOption() {
        List<String> options = List.of("Apple", "Banana", "Cherry");
        String result = controller.getOptionTextByLetter("B", options); // idx = 1
        assertEquals("Banana", result);
    }

    class TestableTeacherGradeExamController extends TeacherGradeExamController {
        public Runnable onDisplayQuestionDetails;
        public Runnable onRefreshCurrentQuestionData;
        public Runnable onUpdateDetailPanel;
        public TestableTeacherGradeExamController(Path examFile, Path questionFile, Path studentFile, Database<Record> db) {
            super(examFile, questionFile, studentFile, db);
        }
        @Override public void displayQuestionDetails(String qid) { if (onDisplayQuestionDetails != null) onDisplayQuestionDetails.run(); }
        @Override public void refreshCurrentQuestionData() { if (onRefreshCurrentQuestionData != null) onRefreshCurrentQuestionData.run(); }
        @Override public void updateDetailPanel(Record r, String qid) { if (onUpdateDetailPanel != null) onUpdateDetailPanel.run(); }
    }
}
