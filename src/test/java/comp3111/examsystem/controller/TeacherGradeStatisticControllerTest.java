package comp3111.examsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import comp3111.examsystem.entity.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import comp3111.examsystem.tools.MsgSender;
import java.lang.reflect.Field;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.BeforeAll;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.data.Department;

public class TeacherGradeStatisticControllerTest {
    private TeacherGradeStatisticController controller;
    private InMemoryDB<comp3111.examsystem.entity.Record> recordDB;
    private InMemoryDB<Student> studentDB;
    private InMemoryDB<Exam> examDB;
    private InMemoryDB<Course> courseDB;
    private InMemoryDB<Question> questionDB;

    @BeforeAll
    static void initJfx() {
        new javafx.embed.swing.JFXPanel();
    }

    @BeforeEach
    void setUp() throws Exception {
        recordDB = new InMemoryDB<>(comp3111.examsystem.entity.Record.class);
        studentDB = new InMemoryDB<>(Student.class);
        examDB = new InMemoryDB<>(Exam.class);
        courseDB = new InMemoryDB<>(Course.class);
        questionDB = new InMemoryDB<>(Question.class);
        // Pre-populate with minimal valid data
        Student student = new Student(1L, "user1", "pass1", "Alice", Gender.FEMALE, 20, Department.CSE);
        studentDB.add(student);
        Course course = new Course(1L, "COMP3111", "Software Engineering", Department.CSE);
        courseDB.add(course);
        Exam exam = new Exam(1L, "Alice", "COMP3111", 60, 1, new ArrayList<>());
        exam.setExamName("Midterm");
        examDB.add(exam);
        Question question = new Question(1L, "MCQ", "What is 2+2?", "2", "3", "4", "5", "4", 5);
        questionDB.add(question);
        comp3111.examsystem.entity.Record record = new comp3111.examsystem.entity.Record(1L, 1L, 1L, "4", 5);
        recordDB.add(record);
        controller = new TeacherGradeStatisticController(recordDB, studentDB, examDB, courseDB, questionDB);
        setField(controller, "courseCmb", new ComboBox<>());
        setField(controller, "examCmb", new ComboBox<>());
        setField(controller, "studentCmb", new ComboBox<>());
        setField(controller, "recordTable", new TableView<Stats>());
        setField(controller, "barChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));
        setField(controller, "lineChart", new LineChart<>(new CategoryAxis(), new NumberAxis()));
        setField(controller, "colStudent", new TableColumn<>());
        setField(controller, "colCourse", new TableColumn<>());
        setField(controller, "colExam", new TableColumn<>());
        setField(controller, "colScore", new TableColumn<>());
        setField(controller, "colMaxScore", new TableColumn<>());
        setField(controller, "colTimeSpent", new TableColumn<>());
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    void testInitializationAndMockData() {
        // Should generate mock data if DBs are empty
        controller.initialize(null, null);
        TableView<Stats> table = (TableView<Stats>) getField(controller, "recordTable");
        assertFalse(table.getItems().isEmpty(), "Table should be populated with mock data");
    }

    @Test
    void testFilterLogic() throws Exception {
        controller.initialize(null, null);
        TableView<Stats> table = (TableView<Stats>) getField(controller, "recordTable");
        ComboBox<String> courseCmb = (ComboBox<String>) getField(controller, "courseCmb");
        ComboBox<String> examCmb = (ComboBox<String>) getField(controller, "examCmb");
        ComboBox<String> studentCmb = (ComboBox<String>) getField(controller, "studentCmb");
        Stats sample = table.getItems().get(0);
        courseCmb.setValue(sample.getCourseCode());
        examCmb.setValue(sample.getExamName());
        studentCmb.setValue(sample.getStudentName());
        // Call private handleFilter via reflection
        var method = controller.getClass().getDeclaredMethod("handleFilter");
        method.setAccessible(true);
        method.invoke(controller);
        assertTrue(table.getItems().stream().allMatch(s ->
            s.getCourseCode().equals(sample.getCourseCode()) &&
            s.getExamName().equals(sample.getExamName()) &&
            s.getStudentName().equals(sample.getStudentName())
        ));
    }

    @Test
    void testResetLogic() throws Exception {
        controller.initialize(null, null);
        TableView<Stats> table = (TableView<Stats>) getField(controller, "recordTable");
        ComboBox<String> courseCmb = (ComboBox<String>) getField(controller, "courseCmb");
        ComboBox<String> examCmb = (ComboBox<String>) getField(controller, "examCmb");
        ComboBox<String> studentCmb = (ComboBox<String>) getField(controller, "studentCmb");
        courseCmb.setValue("SomeCourse");
        examCmb.setValue("SomeExam");
        studentCmb.setValue("SomeStudent");
        // Call private handleReset via reflection
        var method = controller.getClass().getDeclaredMethod("handleReset");
        method.setAccessible(true);
        method.invoke(controller);
        assertEquals("Any", courseCmb.getValue());
        assertEquals("Any", examCmb.getValue());
        assertEquals("Any", studentCmb.getValue());
        assertEquals(table.getItems().size(), ((List<?>)getField(controller, "allStats")).size());
    }

    // Simple in-memory DB for test isolation
    static class InMemoryDB<T> extends comp3111.examsystem.tools.Database<T> {
        private final List<T> items = new ArrayList<>();
        public InMemoryDB(Class<T> clazz) { super(clazz); }
        @Override public List<T> getAll() { return new ArrayList<>(items); }
        @Override public List<T> getAllEnabled() { return new ArrayList<>(items); }
        @Override public void add(T t) { items.add(t); }
        @Override public void delByKey(String key) { items.clear(); }
        @Override public T queryByKey(String key) { return items.isEmpty() ? null : items.get(0); }
        @Override public List<T> queryByField(String field, String value) { return getAll(); }
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
} 