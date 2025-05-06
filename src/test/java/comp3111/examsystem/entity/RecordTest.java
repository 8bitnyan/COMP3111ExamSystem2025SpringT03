package comp3111.examsystem.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RecordTest {
    @Test
    void testDefaultConstructor() {
        Record r = new Record();
        assertNull(r.getQuestionID());
        assertNull(r.getStudentID());
        assertNull(r.getExamID());
        assertNull(r.getResponse());
        assertNull(r.getScore());
    }

    @Test
    void testConstructorAndGetters() {
        Record r = new Record(1L, 2L, 3L, "A", 10);
        assertEquals(1L, r.getQuestionID());
        assertEquals(2L, r.getStudentID());
        assertEquals(3L, r.getExamID());
        assertEquals("A", r.getResponse());
        assertEquals(10, r.getScore());
    }

    @Test
    void testSetters() {
        Record r = new Record();
        r.setQuestionID(4L);
        r.setStudent(5L);
        r.setExamID(6L);
        r.setResponse("B");
        r.setScore(20);
        assertEquals(4L, r.getQuestionID());
        assertEquals(5L, r.getStudentID());
        assertEquals(6L, r.getExamID());
        assertEquals("B", r.getResponse());
        assertEquals(20, r.getScore());
    }
} 