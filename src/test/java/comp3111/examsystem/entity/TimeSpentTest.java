package comp3111.examsystem.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TimeSpentTest {
    @Test
    void testDefaultConstructor() {
        TimeSpent ts = new TimeSpent();
        assertNull(ts.getStudentId());
        assertNull(ts.getExamId());
        assertNull(ts.getTimeSpent());
    }

    @Test
    void testConstructorAndGetters() {
        TimeSpent ts = new TimeSpent(10L, 20L, 90);
        assertEquals(10L, ts.getStudentId());
        assertEquals(20L, ts.getExamId());
        assertEquals(90, ts.getTimeSpent());
    }

    @Test
    void testSetters() {
        TimeSpent ts = new TimeSpent();
        ts.setStudentId(11L);
        ts.setExamId(21L);
        ts.setTimeSpent(120);
        assertEquals(11L, ts.getStudentId());
        assertEquals(21L, ts.getExamId());
        assertEquals(120, ts.getTimeSpent());
    }
} 