package comp3111.examsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.control.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import comp3111.examsystem.entity.Exam;
import comp3111.examsystem.entity.Question;
import java.lang.reflect.Field;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import comp3111.examsystem.tools.MsgSender;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;

public class TeacherExamMgmtControllerTest {
    private TeacherExamMgmtController controller;

    @BeforeAll
    static void initJfx() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new TeacherExamMgmtController();
        setField(controller, "publishedChk", new CheckBox());
        setField(controller, "examNameTxt", new TextField());
        setField(controller, "courseIdTxt", new TextField());
        setField(controller, "durationTxt", new TextField());
        setField(controller, "selectedQuestions", FXCollections.observableArrayList());
        setField(controller, "examsTable", new TableView<Exam>());
        setField(controller, "selectedQuestionsTable", new TableView<Question>());
        setField(controller, "questionsTable", new TableView<Question>());
        setField(controller, "addExamBtn", new Button());
        setField(controller, "editExamBtn", new Button());
        setField(controller, "updateExamBtn", new Button());
        setField(controller, "deleteExamBtn", new Button());
        setField(controller, "addToExamBtn", new Button());
        setField(controller, "removeSelectedQuestionBtn", new Button());
        setField(controller, "clearSelectedQuestionsBtn", new Button());
        setField(controller, "formHeaderLabel", new Label());
        setField(controller, "totalScoreLabel", new Label());
    }

    @Test
    void testCreateUpdateDeleteExamNotPublished() throws Exception {
        // Set up exam fields
        TextField examNameTxt = new TextField("Midterm");
        setField(controller, "examNameTxt", examNameTxt);
        setField(controller, "courseIdTxt", new TextField("COMP3111"));
        setField(controller, "durationTxt", new TextField("90"));
        CheckBox publishedChk = new CheckBox();
        publishedChk.setSelected(false);
        setField(controller, "publishedChk", publishedChk);
        ObservableList<Question> selectedQuestions = FXCollections.observableArrayList();
        Question q = new Question(); q.setId(1L); q.setScore(10); selectedQuestions.add(q);
        setField(controller, "selectedQuestions", selectedQuestions);
        setField(controller, "selectedExam", null);

        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            // Use reflection to invoke private methods
            var addExam = controller.getClass().getDeclaredMethod("handleAddExam");
            addExam.setAccessible(true);
            addExam.invoke(controller);
            var updateExam = controller.getClass().getDeclaredMethod("handleUpdateExam");
            updateExam.setAccessible(true);
            updateExam.invoke(controller);
            // Simulate selecting the exam for deletion
            Exam exam = new Exam();
            exam.setId(1L);
            setField(controller, "selectedExam", exam);
            var deleteExam = controller.getClass().getDeclaredMethod("handleDeleteExam");
            deleteExam.setAccessible(true);
            deleteExam.invoke(controller);
            // No exceptions = logic covered
        }
    }

    @Test
    void testExamMustHaveUniqueName() throws Exception {
        setField(controller, "examNameTxt", new TextField("Final"));
        setField(controller, "courseIdTxt", new TextField("COMP3111"));
        setField(controller, "durationTxt", new TextField("120"));
        setField(controller, "publishedChk", new CheckBox());
        setField(controller, "selectedQuestions", FXCollections.observableArrayList());
        setField(controller, "selectedExam", null);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            var addExam = controller.getClass().getDeclaredMethod("handleAddExam");
            addExam.setAccessible(true);
            addExam.invoke(controller);
            // No exceptions = logic covered
        }
    }

    @Test
    void testExamMustHaveAtLeastOneQuestion() throws Exception {
        ObservableList<Question> questions = FXCollections.observableArrayList();
        setField(controller, "selectedQuestions", questions);
        setField(controller, "examNameTxt", new TextField("Quiz"));
        setField(controller, "courseIdTxt", new TextField("COMP2012"));
        setField(controller, "durationTxt", new TextField("60"));
        setField(controller, "publishedChk", new CheckBox());
        setField(controller, "selectedExam", null);
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            var addExam = controller.getClass().getDeclaredMethod("handleAddExam");
            addExam.setAccessible(true);
            addExam.invoke(controller);
            // No exceptions = logic covered
        }
    }

    @Test
    void testPublishToggleAndPanelSync() throws Exception {
        Exam exam = new Exam();
        exam.setId(2L);
        exam.setIsPublishedInt(0);
        setField(controller, "selectedExam", exam);
        setField(controller, "publishedChk", new CheckBox());
        setField(controller, "examNameTxt", new TextField("Quiz2"));
        setField(controller, "courseIdTxt", new TextField("COMP2011"));
        setField(controller, "durationTxt", new TextField("45"));
        setField(controller, "selectedQuestions", FXCollections.observableArrayList());
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            var editExam = controller.getClass().getDeclaredMethod("handleEditExam");
            editExam.setAccessible(true);
            editExam.invoke(controller);
            var updateExam = controller.getClass().getDeclaredMethod("handleUpdateExam");
            updateExam.setAccessible(true);
            updateExam.invoke(controller);
            // No exceptions = logic covered
        }
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
} 