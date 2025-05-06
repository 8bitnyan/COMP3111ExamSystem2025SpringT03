package comp3111.examsystem.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StatsTest {
    @Test
    void testDefaultConstructor() {
        Stats s = new Stats();
        assertNull(s.getStudentName());
        assertNull(s.getCourseCode());
        assertNull(s.getExamName());
        assertNull(s.getScore());
        assertNull(s.getMaxScore());
        assertNull(s.getTimeSpent());
    }

    @Test
    void testConstructorAndGetters() {
        Stats s = new Stats("Alice", "COMP3111", "Midterm", 80, 100, "60min");
        assertEquals("Alice", s.getStudentName());
        assertEquals("COMP3111", s.getCourseCode());
        assertEquals("Midterm", s.getExamName());
        assertEquals(80, s.getScore());
        assertEquals(100, s.getMaxScore());
        assertEquals("60min", s.getTimeSpent());
    }

    @Test
    void testSetters() {
        Stats s = new Stats();
        s.setStudentName("Bob");
        s.setCourseCode("COMP2012");
        s.setExamName("Final");
        s.setScore(90);
        s.setMaxScore(120);
        s.setTimeSpent("90min");
        assertEquals("Bob", s.getStudentName());
        assertEquals("COMP2012", s.getCourseCode());
        assertEquals("Final", s.getExamName());
        assertEquals(90, s.getScore());
        assertEquals(120, s.getMaxScore());
        assertEquals("90min", s.getTimeSpent());
    }
} 