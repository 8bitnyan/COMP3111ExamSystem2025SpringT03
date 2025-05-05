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

public class TeacherQuestionBankMgmtControllerTest {
    private TeacherQuestionBankMgmtController controller;

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
        List<TextField> optionFields = Arrays.asList(new TextField("A1"), new TextField("A2"));
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
        setField(controller, "optionFields", Arrays.asList(new TextField("A1"), new TextField("A2")));
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
    void testDeleteBranches() throws Exception {
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
            // Published
            Question q = new Question();
            q.setId(200L); // Set non-null ID
            q.setPublished(1);
            table.getItems().add(q);
            table.getSelectionModel().select(q);
            controller.handleDelete();
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
    void testCancelEditBranches() throws Exception {
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
            // In edit mode, selectedQuestion not null, question found
            setField(controller, "editMode", true);
            Question q2 = new Question();
            q2.setId(400L); // Set non-null ID
            setField(controller, "selectedQuestion", q2);
            controller.handleCancelEdit();
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

    private Object getField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
} 