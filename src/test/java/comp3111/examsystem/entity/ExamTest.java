package comp3111.examsystem.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExamTest {
    private Exam exam;

    @BeforeEach
    void setUp() {
        exam = new Exam(1L, "Midterm", "COMP3111", 90, 0, new java.util.ArrayList<>(java.util.Arrays.asList(1L, 2L, 3L)));
    }

    @Test
    void testAddQuestion_NewAndDuplicate() {
        assertTrue(exam.addQuestion(4L));
        assertFalse(exam.addQuestion(2L)); // Already exists
    }

    @Test
    void testRemoveQuestion_ExistingAndNonExisting() {
        assertTrue(exam.removeQuestion(2L));
        assertFalse(exam.removeQuestion(99L));
    }

    @Test
    void testGetQuestionCount() {
        assertEquals(3, exam.getQuestionCount());
        exam.addQuestion(4L);
        assertEquals(4, exam.getQuestionCount());
    }

    @Test
    void testSetDurationAndExamTime() {
        exam.setDuration(120);
        assertEquals(120, exam.getDuration());
        assertEquals("120 minutes", exam.getExamTime());
        exam.setExamTime("60 minutes");
        assertEquals(60, exam.getDuration());
        exam.setExamTime("invalid"); // Should not throw
        assertEquals(60, exam.getDuration()); // Unchanged
    }

    @Test
    void testSetIsPublished_ValidAndInvalid() {
        exam.setIsPublished("1");
        assertEquals(1, exam.getIsPublishedInt());
        assertTrue(exam.getPublished());
        exam.setIsPublished("notANumber");
        assertEquals(0, exam.getIsPublishedInt());
        assertFalse(exam.getPublished());
    }

    @Test
    void testSetIsPublishedInt() {
        exam.setIsPublishedInt(2);
        assertEquals("2", exam.getIsPublished());
        assertTrue(exam.getPublished());
        exam.setIsPublishedInt(null);
        assertEquals("0", exam.getIsPublished());
        assertFalse(exam.getPublished());
    }

    @Test
    void testSetNameAndExamNameSync() {
        exam.setName("Final");
        assertEquals("Final", exam.getExamName());
        exam.setExamName("Quiz");
        assertEquals("Quiz", exam.getName());
    }

    @Test
    void testSetQuestionIds_NullAndNonLongList() {
        exam.setQuestionIds(null);
        assertEquals(0, exam.getQuestionIds().size());
        exam.setQuestionIds(new java.util.ArrayList<>(java.util.Arrays.asList("10", "20")));
        List<Long> ids = exam.getQuestionIds();
        assertEquals(2, ids.size());
        assertEquals(10L, ids.get(0));
        assertEquals(20L, ids.get(1));
    }

    @Test
    void testSetQuestions_ValidAndInvalid() {
        exam.setQuestions("100,200,300");
        List<Long> ids = exam.getQuestionIds();
        assertEquals(3, ids.size());
        assertEquals(100L, ids.get(0));
        exam.setQuestions("");
        assertEquals(3, exam.getQuestionIds().size());
        exam.setQuestions("notANumber");
        assertEquals(3, exam.getQuestionIds().size());
    }

    @Test
    void testEnabledAndIsAble() {
        exam.setEnabled(false);
        assertFalse(exam.getEnabled());
        assertFalse(exam.getIsAble());
        exam.setEnabled(true);
        assertTrue(exam.getEnabled());
        assertTrue(exam.getIsAble());
    }

    // SKELETONS FOR UNCOVERED ENTITY CLASSES
    // TODO: Add tests for User
    // TODO: Add tests for Entity
    // TODO: Add tests for Student
    // TODO: Add tests for Teacher
    // TODO: Add tests for TimeSpent
    // TODO: Add tests for Course
    // TODO: Add tests for Manager
    // TODO: Add tests for Person
    // TODO: Add tests for Record
    // TODO: Add tests for Stats
} 