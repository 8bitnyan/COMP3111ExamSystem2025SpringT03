package comp3111.examsystem.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionTest {
    private Question question;

    @BeforeEach
    void setUp() {
        question = new Question(1L, "MCQ", "What is 2+2?", "2", "3", "4", "5", "C", 5);
    }

    @Test
    void testGetOption_ValidAndInvalidIndices() {
        assertEquals("2", question.getOption(0));
        assertEquals("3", question.getOption(1));
        assertNull(question.getOption(-1));
        assertNull(question.getOption(10));
    }

    @Test
    void testSetOption_ExistingAndNewIndex() {
        question.setOption(1, "42");
        assertEquals("42", question.getOption(1));
        question.setOption(4, "new");
        assertEquals("new", question.getOption(4));
    }

    @Test
    void testAddOption_UpToMax() {
        question.addOption("extra1");
        question.addOption("extra2"); // Should not add more than 5
        assertEquals(5, question.getOptions().size());
        question.addOption("shouldNotAdd");
        assertEquals(5, question.getOptions().size());
    }

    @Test
    void testRemoveOption_ValidAndInvalid() {
        // Remove valid
        assertTrue(question.removeOption(3));
        // Remove invalid (out of bounds)
        assertFalse(question.removeOption(10));
        // Remove invalid (would leave <2 options)
        question.removeOption(2);
        question.removeOption(1);
        assertFalse(question.removeOption(0));
    }

    @Test
    void testUpdateLegacyFieldsAndOptionsList() {
        question.setOption(0, "A");
        question.setOption(1, "B");
        question.setOption(2, "C");
        question.setOption(3, "D");
        question.setOption(4, "E");
        // Now clear options and update from legacy fields
        question.setOptions(new java.util.ArrayList<>());
        assertTrue(question.getOptions().isEmpty());
        question.setA("A");
        question.setB("B");
        question.setC("C");
        question.setD("D");
        question.setE("E");
        List<String> options = question.getOptions();
        assertEquals(5, options.size());
        assertEquals("A", options.get(0));
        assertEquals("E", options.get(4));
    }

    @Test
    void testSettersForAtoE() {
        question.setA("A1");
        assertEquals("A1", question.getOptionA());
        question.setB("B1");
        assertEquals("B1", question.getOptionB());
        question.setC("C1");
        assertEquals("C1", question.getOptionC());
        question.setD("D1");
        assertEquals("D1", question.getOptionD());
        question.setE("E1");
        assertEquals("E1", question.getOptionE());
    }

    @Test
    void testGetAndSetScoreAndAnswer() {
        question.setScore(10);
        assertEquals(10, question.getScore());
        question.setAnswer("A");
        assertEquals("A", question.getAnswer());
    }

    @Test
    void testGetAndSetPublished() {
        question.setPublished(1);
        assertEquals(1, question.getPublished());
    }
} 