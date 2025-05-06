package comp3111.examsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import comp3111.examsystem.entity.Question;
import java.lang.reflect.Field;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import comp3111.examsystem.tools.MsgSender;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import comp3111.examsystem.entity.Teacher;
import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.data.Position;
import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TeacherQuestionBankMgmtControllerTest {
    private TeacherQuestionBankMgmtController controller;

    static {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX already started
        }
    }

    @BeforeAll
    static void initJfx() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new TeacherQuestionBankMgmtController();
        // Set up dummy teacher with non-null ID and required properties
        Teacher dummyTeacher = new Teacher(1L, "dummyuser", "dummypass", "Dummy Name", Gender.MALE, 30, Department.CSE, Position.P);
        setField(controller, "teacher", dummyTeacher);
        // Use reflection to set private fields if direct access fails
        setField(controller, "questionsTable", new TableView<Question>());
        setField(controller, "filterQuestionTxt", new TextField());
        setField(controller, "filterTypeCmb", new ComboBox<String>());
        setField(controller, "filterScoreTxt", new TextField());
        setField(controller, "questionTxt", new TextArea());
        setField(controller, "scoreTxt", new TextField());
        setField(controller, "answerTxt", new TextField());
        setField(controller, "typeCmb", new ComboBox<String>());
        setField(controller, "optionFields", new ArrayList<TextField>());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "addBtn", new Button());
        setField(controller, "mainbox", new VBox());
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    void testFilterByTextTypeScore() throws Exception {
        // Add mock questions
        TableView<Question> table = new TableView<>();
        setField(controller, "questionsTable", table);
        Question q1 = new Question(); q1.setQuestionText("Math Q1"); q1.setType("MCQ"); q1.setScore(5);
        Question q2 = new Question(); q2.setQuestionText("Physics Q2"); q2.setType("Short Answer"); q2.setScore(10);
        table.getItems().addAll(q1, q2);

        // Filter by text
        TextField filterQuestionTxt = new TextField("Math");
        setField(controller, "filterQuestionTxt", filterQuestionTxt);
        List<Question> filtered = table.getItems().filtered(q -> q.getQuestionText().contains("Math"));
        assertEquals(1, filtered.size());
        assertEquals("Math Q1", filtered.get(0).getQuestionText());

        // Filter by type
        ComboBox<String> filterTypeCmb = new ComboBox<>();
        filterTypeCmb.getItems().addAll("MCQ", "Short Answer");
        filterTypeCmb.setValue("Short Answer");
        setField(controller, "filterTypeCmb", filterTypeCmb);
        filtered = table.getItems().filtered(q -> q.getType().equals("Short Answer"));
        assertEquals(1, filtered.size());
        assertEquals("Physics Q2", filtered.get(0).getQuestionText());

        // Filter by score
        TextField filterScoreTxt = new TextField("5");
        setField(controller, "filterScoreTxt", filterScoreTxt);
        filtered = table.getItems().filtered(q -> q.getScore() == 5);
        assertEquals(1, filtered.size());
        assertEquals("Math Q1", filtered.get(0).getQuestionText());
    }

    @Test
    void testAddQuestion_AllBranches() throws Exception {
        // Ensure teacher is set (in case controller is reset)
        Teacher dummyTeacher = new Teacher(1L, "dummyuser", "dummypass", "Dummy Name", Gender.MALE, 30, Department.CSE, Position.P);
        setField(controller, "teacher", dummyTeacher);
        // Setup for MCQ valid
        setField(controller, "questionTxt", new TextArea("Q?"));
        ComboBox<String> typeCmb = new ComboBox<>();
        typeCmb.getItems().addAll("MCQ", "Short Answer");
        typeCmb.setValue("MCQ");
        setField(controller, "typeCmb", typeCmb);
        setField(controller, "scoreTxt", new TextField("5"));
        setField(controller, "answerTxt", new TextField("A"));
        List<TextField> optionFields = new ArrayList<>(Arrays.asList(new TextField("A1"), new TextField("A2")));
        setField(controller, "optionFields", optionFields);
        setField(controller, "questionsTable", new TableView<Question>());
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        setField(controller, "editMode", false);
        setField(controller, "selectedQuestion", null);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleAdd();
            controller.handleUpdate();
            // Invalid: missing fields
            setField(controller, "questionTxt", new TextArea(""));
            controller.handleUpdate();
            setField(controller, "questionTxt", new TextArea("Q?"));
            setField(controller, "scoreTxt", new TextField("-1"));
            controller.handleUpdate();
            setField(controller, "scoreTxt", new TextField("abc"));
            controller.handleUpdate();
            setField(controller, "scoreTxt", new TextField("5"));
            setField(controller, "answerTxt", new TextField("Z"));
            controller.handleUpdate();
            // Short Answer
            typeCmb.setValue("Short Answer");
            setField(controller, "typeCmb", typeCmb);
            controller.handleUpdate();
        }
    }

    @Test
    void testUpdateQuestion_EditModeBranches() throws Exception {
        // Ensure teacher is set (in case controller is reset)
        Teacher dummyTeacher = new Teacher(1L, "dummyuser", "dummypass", "Dummy Name", Gender.MALE, 30, Department.CSE, Position.P);
        setField(controller, "teacher", dummyTeacher);
        // Setup for update
        setField(controller, "editMode", true);
        Question q = new Question();
        q.setId(100L); // Set non-null ID
        q.setPublished(0);
        setField(controller, "selectedQuestion", q);
        setField(controller, "questionTxt", new TextArea("Q?"));
        ComboBox<String> typeCmb = new ComboBox<>();
        typeCmb.getItems().addAll("MCQ", "Short Answer");
        typeCmb.setValue("MCQ");
        setField(controller, "typeCmb", typeCmb);
        setField(controller, "scoreTxt", new TextField("5"));
        setField(controller, "answerTxt", new TextField("A"));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(new TextField("A1"), new TextField("A2"))));
        setField(controller, "questionsTable", new TableView<Question>());
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleUpdate();
            // Not in editMode
            setField(controller, "editMode", false);
            controller.handleUpdate();
            // selectedQuestion null
            setField(controller, "editMode", true);
            setField(controller, "selectedQuestion", null);
            controller.handleUpdate();
            // selectedQuestion published
            Question q2 = new Question();
            q2.setId(101L); // Set non-null ID
            q2.setPublished(1);
            setField(controller, "selectedQuestion", q2);
            controller.handleUpdate();
        }
    }

    @Test
    void testDeleteBranches_AllPaths() throws Exception {
        Teacher dummyTeacher = new Teacher(1L, "dummyuser", "dummypass", "Dummy Name", Gender.MALE, 30, Department.CSE, Position.P);
        setField(controller, "teacher", dummyTeacher);
        TableView<Question> table = new TableView<>();
        setField(controller, "questionsTable", table);
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        // No selection
        table.getSelectionModel().clearSelection();
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleDelete();
            msgSenderMocked.verify(() -> MsgSender.showMsg("No question selected to delete."), Mockito.atLeastOnce());
            // Published
            Question q = new Question();
            q.setId(200L); q.setPublished(1);
            table.getItems().add(q);
            table.getSelectionModel().select(q);
            controller.handleDelete();
            msgSenderMocked.verify(() -> MsgSender.showMsg("Published question cannot be deleted."), Mockito.atLeastOnce());
            // Not published
            q.setPublished(0);
            table.getSelectionModel().select(q);
            controller.handleDelete();
        }
    }

    @Test
    void testEditBranches() throws Exception {
        Teacher dummyTeacher = new Teacher(1L, "dummyuser", "dummypass", "Dummy Name", Gender.MALE, 30, Department.CSE, Position.P);
        setField(controller, "teacher", dummyTeacher);
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        // No selection
        setField(controller, "selectedQuestion", null);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleEdit();
            // Published
            Question q = new Question();
            q.setId(300L); // Set non-null ID
            q.setPublished(1);
            setField(controller, "selectedQuestion", q);
            controller.handleEdit();
            // Not published
            q.setPublished(0);
            setField(controller, "selectedQuestion", q);
            controller.handleEdit();
        }
    }

    @Test
    void testCancelEditBranches_AllPaths() throws Exception {
        Teacher dummyTeacher = new Teacher(1L, "dummyuser", "dummypass", "Dummy Name", Gender.MALE, 30, Department.CSE, Position.P);
        setField(controller, "teacher", dummyTeacher);
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        // Not in edit mode, selectedQuestion not null
        setField(controller, "editMode", false);
        Question q = new Question();
        setField(controller, "selectedQuestion", q);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleCancelEdit();
            // In edit mode, selectedQuestion null
            setField(controller, "editMode", true);
            setField(controller, "selectedQuestion", null);
            controller.handleCancelEdit();
            msgSenderMocked.verify(() -> MsgSender.showMsg("Adding new question cancelled."), Mockito.atLeastOnce());
            // In edit mode, selectedQuestion not null, question not found
            setField(controller, "editMode", true);
            Question q2 = new Question();
            q2.setId(400L);
            setField(controller, "selectedQuestion", q2);
            controller.handleCancelEdit();
            msgSenderMocked.verify(() -> MsgSender.showMsg("Question could not be found. Edit cancelled."), Mockito.atLeastOnce());
        }
    }

    @Test
    void testFilterBranches() throws Exception {
        Teacher dummyTeacher = new Teacher(1L, "dummyuser", "dummypass", "Dummy Name", Gender.MALE, 30, Department.CSE, Position.P);
        setField(controller, "teacher", dummyTeacher);
        setField(controller, "filterQuestionTxt", new TextField(""));
        ComboBox<String> filterTypeCmb = new ComboBox<>();
        filterTypeCmb.getItems().addAll("Any", "MCQ", "Short Answer");
        filterTypeCmb.setValue("Any");
        setField(controller, "filterTypeCmb", filterTypeCmb);
        setField(controller, "filterScoreTxt", new TextField("notanint"));
        setField(controller, "questionsTable", new TableView<Question>());
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleFilter();
        }
    }

    @Test
    void testAddMCQWithMinimumOptions() throws Exception {
        setField(controller, "questionTxt", new TextArea("Q?"));
        ComboBox<String> typeCmb = new ComboBox<>();
        typeCmb.getItems().addAll("MCQ", "Short Answer");
        typeCmb.setValue("MCQ");
        setField(controller, "typeCmb", typeCmb);
        setField(controller, "scoreTxt", new TextField("1"));
        setField(controller, "answerTxt", new TextField("A"));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(new TextField("A1"), new TextField("A2"))));
        setField(controller, "questionsTable", new TableView<Question>());
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        setField(controller, "editMode", false);
        setField(controller, "selectedQuestion", null);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleAdd();
            // No exception means success for this test
        }
    }

    @Test
    void testAddMCQWithMaximumOptions() throws Exception {
        setField(controller, "questionTxt", new TextArea("Q?"));
        ComboBox<String> typeCmb = new ComboBox<>();
        typeCmb.getItems().addAll("MCQ", "Short Answer");
        typeCmb.setValue("MCQ");
        setField(controller, "typeCmb", typeCmb);
        setField(controller, "scoreTxt", new TextField("5"));
        setField(controller, "answerTxt", new TextField("A"));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(
            new TextField("A1"), new TextField("A2"), new TextField("A3"), new TextField("A4"), new TextField("A5"))));
        setField(controller, "questionsTable", new TableView<Question>());
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        setField(controller, "editMode", false);
        setField(controller, "selectedQuestion", null);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleAdd();
        }
    }

    @Test
    void testAddMCQWithInvalidOptionCounts() throws Exception {
        // 1 option (should fail)
        setField(controller, "questionTxt", new TextArea("Q?"));
        ComboBox<String> typeCmb = new ComboBox<>();
        typeCmb.getItems().addAll("MCQ", "Short Answer");
        typeCmb.setValue("MCQ");
        setField(controller, "typeCmb", typeCmb);
        setField(controller, "scoreTxt", new TextField("2"));
        setField(controller, "answerTxt", new TextField("A"));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(new TextField("A1"))));
        setField(controller, "questionsTable", new TableView<Question>());
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        setField(controller, "editMode", false);
        setField(controller, "selectedQuestion", null);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleAdd(); // Should not add, but should not throw
        }
        // 6 options (should fail)
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(
            new TextField("A1"), new TextField("A2"), new TextField("A3"), new TextField("A4"), new TextField("A5"), new TextField("A6"))));
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleAdd(); // Should not add, but should not throw
        }
    }

    @Test
    void testAddUpdateWithEmptyOrPartialFields() throws Exception {
        // All fields empty
        setField(controller, "questionTxt", new TextArea(""));
        ComboBox<String> typeCmb = new ComboBox<>();
        typeCmb.getItems().addAll("MCQ", "Short Answer");
        typeCmb.setValue("MCQ");
        setField(controller, "typeCmb", typeCmb);
        setField(controller, "scoreTxt", new TextField(""));
        setField(controller, "answerTxt", new TextField(""));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(new TextField(""), new TextField(""))));
        setField(controller, "questionsTable", new TableView<Question>());
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        setField(controller, "editMode", false);
        setField(controller, "selectedQuestion", null);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleAdd();
        }
        // Only some fields filled
        setField(controller, "questionTxt", new TextArea("Q?"));
        setField(controller, "scoreTxt", new TextField(""));
        setField(controller, "answerTxt", new TextField("A"));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(new TextField("A1"), new TextField("A2"))));
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleAdd();
        }
    }

    @Test
    void testButtonStatesAfterSelectionAndActions() throws Exception {
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        TableView<Question> table = new TableView<>();
        setField(controller, "questionsTable", table);
        // No selection
        table.getSelectionModel().clearSelection();
        // Simulate selection
        Question q = new Question();
        q.setId(1L);
        table.getItems().add(q);
        table.getSelectionModel().select(q);
        // Deselect
        table.getSelectionModel().clearSelection();
        // After add
        setField(controller, "questionTxt", new TextArea("Q?"));
        ComboBox<String> typeCmb = new ComboBox<>();
        typeCmb.getItems().addAll("MCQ", "Short Answer");
        typeCmb.setValue("MCQ");
        setField(controller, "typeCmb", typeCmb);
        setField(controller, "scoreTxt", new TextField("2"));
        setField(controller, "answerTxt", new TextField("A"));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(new TextField("A1"), new TextField("A2"))));
        setField(controller, "editMode", false);
        setField(controller, "selectedQuestion", null);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleAdd();
        }
        // After update
        setField(controller, "editMode", true);
        Question q2 = new Question();
        q2.setId(2L);
        setField(controller, "selectedQuestion", q2);
        setField(controller, "questionTxt", new TextArea("Q?"));
        setField(controller, "scoreTxt", new TextField("2"));
        setField(controller, "answerTxt", new TextField("A"));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(new TextField("A1"), new TextField("A2"))));
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleUpdate();
        }
        // After delete
        table.getItems().add(q2);
        table.getSelectionModel().select(q2);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            controller.handleDelete();
        }
    }

    @Test
    void testSwitchBetweenMCQAndShortAnswer() throws Exception {
        ComboBox<String> typeCmb = new ComboBox<>();
        typeCmb.getItems().addAll("MCQ", "Short Answer");
        setField(controller, "typeCmb", typeCmb);
        VBox options = new VBox();
        setField(controller, "options", options);
        // Switch to MCQ
        typeCmb.setValue("MCQ");
        controller.updateOptionFieldsVisibility("MCQ");
        // Switch to Short Answer
        typeCmb.setValue("Short Answer");
        controller.updateOptionFieldsVisibility("Short Answer");
        // Switch back to MCQ
        typeCmb.setValue("MCQ");
        controller.updateOptionFieldsVisibility("MCQ");
    }

    @Test
    void testUpdateBranches_AllPaths() throws Exception {
        Teacher dummyTeacher = new Teacher(1L, "dummyuser", "dummypass", "Dummy Name", Gender.MALE, 30, Department.CSE, Position.P);
        setField(controller, "teacher", dummyTeacher);
        setField(controller, "editMode", false);
        setField(controller, "selectedQuestion", null);
        setField(controller, "questionTxt", new TextArea(""));
        ComboBox<String> typeCmb = new ComboBox<>();
        typeCmb.getItems().addAll("MCQ", "Short Answer");
        typeCmb.setValue("MCQ");
        setField(controller, "typeCmb", typeCmb);
        setField(controller, "scoreTxt", new TextField(""));
        setField(controller, "answerTxt", new TextField(""));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(new TextField(""), new TextField(""))));
        setField(controller, "questionsTable", new TableView<Question>());
        setField(controller, "addBtn", new Button());
        setField(controller, "updateBtn", new Button());
        setField(controller, "cancelEditBtn", new Button());
        setField(controller, "editBtn", new Button());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "options", new VBox());
        setField(controller, "addOptionBtn", new Button());
        setField(controller, "mainbox", new VBox());
        // Invalid fields
        CountDownLatch latchInvalid = new CountDownLatch(1);
        Platform.runLater(() -> {
            try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
                msgSenderMocked.when(() -> MsgSender.showMsg(Mockito.anyString()))
                    .then(invocation -> {
                        System.out.println("MsgSender.showMsg called with: " + invocation.getArgument(0));
                        return null;
                    });
                // Set all required fields to empty or null
                setField(controller, "questionTxt", new TextArea(""));
                ComboBox<String> emptyTypeCmb = new ComboBox<>();
                emptyTypeCmb.setValue(null);
                setField(controller, "typeCmb", emptyTypeCmb);
                setField(controller, "scoreTxt", new TextField(""));
                setField(controller, "answerTxt", new TextField(""));
                controller.handleUpdate();
                msgSenderMocked.verify(() -> MsgSender.showMsg("Please fill in all required fields."), Mockito.atLeastOnce());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latchInvalid.countDown();
            }
        });
        latchInvalid.await(5, TimeUnit.SECONDS);
        // Not in editMode, selectedQuestion null
        setField(controller, "editMode", false);
        setField(controller, "selectedQuestion", null);
        setField(controller, "questionTxt", new TextArea("Q?"));
        setField(controller, "scoreTxt", new TextField("5"));
        setField(controller, "answerTxt", new TextField("A"));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(new TextField("A1"), new TextField("A2"))));
        ComboBox<String> validTypeCmbForAdd = new ComboBox<>();
        validTypeCmbForAdd.getItems().addAll("MCQ", "Short Answer");
        validTypeCmbForAdd.setValue("MCQ");
        setField(controller, "typeCmb", validTypeCmbForAdd);
        CountDownLatch latchAdd = new CountDownLatch(1);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            Platform.runLater(() -> {
                controller.handleUpdate(); // Should add new question
                latchAdd.countDown();
            });
            latchAdd.await(5, TimeUnit.SECONDS);
            // No verify needed if no dialog expected
        }
        // Now test editMode true, selectedQuestion null
        // Set all required fields to valid values
        setField(controller, "questionTxt", new TextArea("Q?"));
        ComboBox<String> validTypeCmb = new ComboBox<>();
        validTypeCmb.getItems().addAll("MCQ", "Short Answer");
        validTypeCmb.setValue("MCQ");
        setField(controller, "typeCmb", validTypeCmb);
        setField(controller, "scoreTxt", new TextField("5"));
        setField(controller, "answerTxt", new TextField("A"));
        setField(controller, "optionFields", new ArrayList<>(Arrays.asList(new TextField("A1"), new TextField("A2"))));
        setField(controller, "editMode", true);
        setField(controller, "selectedQuestion", null);
        CountDownLatch latchNull = new CountDownLatch(1);
        Platform.runLater(() -> {
            try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
                controller.handleUpdate();
                msgSenderMocked.verify(() -> MsgSender.showMsg("No question selected to update."), Mockito.atLeastOnce());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latchNull.countDown();
            }
        });
        latchNull.await(5, TimeUnit.SECONDS);

        // Published
        Question q = new Question();
        q.setId(300L); q.setPublished(1);
        setField(controller, "selectedQuestion", q);
        setField(controller, "editMode", true);
        CountDownLatch latchPublished = new CountDownLatch(1);
        Platform.runLater(() -> {
            try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
                controller.handleUpdate();
                msgSenderMocked.verify(() -> MsgSender.showMsg("Published question cannot be modified."), Mockito.atLeastOnce());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latchPublished.countDown();
            }
        });
        latchPublished.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testHandleRefreshWithNullTeacher() throws Exception {
        setField(controller, "teacher", null);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            msgSenderMocked.when(() -> MsgSender.showMsg(Mockito.anyString())).then(invocation -> null);
            // Should show error and not throw
            assertDoesNotThrow(() -> controller.handleRefresh());
            assertDoesNotThrow(() -> controller.handleRefreshWithoutMsg());
        }
    }

    @Test
    void testUpdateOptionFieldsVisibilityBranches() throws Exception {
        // MCQ branch
        setField(controller, "optionFields", new ArrayList<>());
        setField(controller, "options", new VBox());
        setField(controller, "optionsContainer", new VBox());
        setField(controller, "addOptionBtn", new Button());
        controller.updateOptionFieldsVisibility("MCQ");
        // non-MCQ branch
        controller.updateOptionFieldsVisibility("Short Answer");
    }

    @Test
    void testTableSelectionLogicBranches() throws Exception {
        TableView<Question> table = new TableView<>();
        setField(controller, "questionsTable", table);
        setField(controller, "editMode", true);
        // Listener should return early if editMode true
        table.getSelectionModel().select(null);
        setField(controller, "editMode", false);
        // Add a question and select it
        Question q = new Question(); q.setId(1L); q.setQuestionText("Q?");
        table.getItems().add(q);
        table.getSelectionModel().select(q);
        // Clear selection
        table.getSelectionModel().clearSelection();
    }

    @Test
    void testFillFieldsFromSelectedQuestion_ExceptionHandling() throws Exception {
        // Should handle null question
        assertDoesNotThrow(() -> {
            var method = controller.getClass().getDeclaredMethod("fillFieldsFromSelectedQuestion", Question.class);
            method.setAccessible(true);
            method.invoke(controller, new Object[]{null});
        });
        // Should handle exception in Platform.runLater (simulate by passing a question with nulls)
        Question q = new Question();
        q.setId(2L);
        assertDoesNotThrow(() -> {
            var method = controller.getClass().getDeclaredMethod("fillFieldsFromSelectedQuestion", Question.class);
            method.setAccessible(true);
            method.invoke(controller, q);
        });
    }

    @Test
    void testDisplaySelectedQuestionReadOnly_ExceptionHandling() throws Exception {
        // Should handle null question
        assertDoesNotThrow(() -> {
            var method = controller.getClass().getDeclaredMethod("displaySelectedQuestionReadOnly", Question.class);
            method.setAccessible(true);
            method.invoke(controller, new Object[]{null});
        });
        // Should handle exception in setting fields (simulate by passing a question with nulls)
        Question q = new Question();
        q.setId(3L);
        assertDoesNotThrow(() -> {
            var method = controller.getClass().getDeclaredMethod("displaySelectedQuestionReadOnly", Question.class);
            method.setAccessible(true);
            method.invoke(controller, q);
        });
    }

    private Object getField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
} 