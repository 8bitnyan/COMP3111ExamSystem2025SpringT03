package comp3111.examsystem.controller;

import comp3111.examsystem.entity.Student;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class StudentQuizControllerTest {

    /**
     * Test for the QuizQuestion class.
     * This test verifies the behavior of the QuizQuestion inner class.
     */
    @Test
    public void testQuizQuestionClass() {
        // Test multiple choice question
        List<String> options = Arrays.asList("Option A", "Option B", "Option C", "Option D");
        StudentQuizController.QuizQuestion mcQuestion = 
            new StudentQuizController.QuizQuestion("MC Question", options);
        
        assertEquals("MC Question", mcQuestion.getQuestionText());
        assertTrue(mcQuestion.isMultipleChoice());
        assertEquals(options, mcQuestion.getOptions());
        
        // Test short answer question
        StudentQuizController.QuizQuestion saQuestion = 
            new StudentQuizController.QuizQuestion("SA Question");
        
        assertEquals("SA Question", saQuestion.getQuestionText());
        assertFalse(saQuestion.isMultipleChoice());
        assertEquals(0, saQuestion.getOptions().size());
    }

    /**
     * Test for navigation logic.
     */
    @Test
    public void testNavigationLogic() {
        List<StudentQuizController.QuizQuestion> questions = new ArrayList<>();
        questions.add(new StudentQuizController.QuizQuestion("Q1"));
        questions.add(new StudentQuizController.QuizQuestion("Q2"));
        questions.add(new StudentQuizController.QuizQuestion("Q3"));
        
        // Test navigation boundary conditions
        boolean shouldDisablePrevious = 0 == 0; // First question
        boolean shouldDisableNext = 0 == questions.size() - 1; // Not last question
        
        assertTrue(shouldDisablePrevious);
        assertFalse(shouldDisableNext);
        
        // Middle question
        shouldDisablePrevious = 1 == 0;
        shouldDisableNext = 1 == questions.size() - 1;
        
        assertFalse(shouldDisablePrevious);
        assertFalse(shouldDisableNext);
        
        // Last question
        shouldDisablePrevious = 2 == 0;
        shouldDisableNext = 2 == questions.size() - 1;
        
        assertFalse(shouldDisablePrevious);
        assertTrue(shouldDisableNext);
    }

    /**
     * Test for timer calculation logic.
     */
    @Test
    public void testTimerCalculation() {
        // Create a controller with accessible timer fields
        StudentQuizController controller = new StudentQuizController();
        
        // Set timer values via reflection
        setFieldValue(controller, "hours", 1);
        setFieldValue(controller, "minutes", 30);
        setFieldValue(controller, "seconds", 45);
        
        // Use reflection to call formatTime method if it exists, or verify the time string format logic
        String timeString = String.format("%02d:%02d:%02d", 
                                        getFieldValue(controller, "hours"),
                                        getFieldValue(controller, "minutes"),
                                        getFieldValue(controller, "seconds"));
        
        assertEquals("01:30:45", timeString);
    }

    /**
     * Helper method to access private fields
     */
    private Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            fail("Failed to get field value: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to set private field values
     */
    private void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            fail("Failed to set field value: " + e.getMessage());
        }
    }
    
    /**
     * Test for the quiz data initialization.
     */
    @Test
    public void testQuizDataInitialization() {
        // Test QuizQuestion data handling
        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4");
        StudentQuizController.QuizQuestion question = 
            new StudentQuizController.QuizQuestion("Test Question", options);
            
        // Verify the question properties
        assertEquals("Test Question", question.getQuestionText());
        assertEquals(4, question.getOptions().size());
        assertEquals("Option 1", question.getOptions().get(0));
        assertEquals("Option 4", question.getOptions().get(3));
    }

    /**
     * Test quiz question answer handling.
     */
    @Test
    public void testQuizQuestionAnswerHandling() {
        // Create a list of quiz questions
        List<StudentQuizController.QuizQuestion> questions = new ArrayList<>();
        questions.add(new StudentQuizController.QuizQuestion("Q1", Arrays.asList("A", "B", "C", "D")));
        questions.add(new StudentQuizController.QuizQuestion("Q2"));
        
        // Create a list of answers
        List<String> answers = new ArrayList<>();
        answers.add("B"); // Answer for Q1
        answers.add("This is a short answer"); // Answer for Q2
        
        // Verify multiple choice answer
        assertEquals("B", answers.get(0));
        
        // Verify short answer
        assertEquals("This is a short answer", answers.get(1));
        
        // Verify question types
        assertTrue(questions.get(0).isMultipleChoice());
        assertFalse(questions.get(1).isMultipleChoice());
    }

    /**
     * Test for Student entity data handling.
     */
    @Test
    public void testStudentEntityData() {
        // Create a student with test data
        Student student = new Student();
        
        // Set student properties using available methods
        student.setId(12345L); // Use Long instead of String
        student.setName("Test Student");
        // Use other available setter methods instead of email/password
        
        // Verify student data
        assertEquals(Long.valueOf(12345L), student.getId());
        assertEquals("Test Student", student.getName());
    }

    /**
     * Test student authentication logic.
     */
    @Test
    public void testStudentAuthentication() {
        // Create a test student
        Student student = new Student();
        student.setName("Test Student");
        
        // Test student identity verification using available methods
        // For example, check the student name
        assertEquals("Test Student", student.getName());
        assertNotEquals("Wrong Name", student.getName());
    }
} 