package comp3111.examsystem.entity;

import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StudentTest {
    @Test
    void testConstructorAndGetters() {
        Student student = new Student(2L, "stud1", "pass5678", "Bob Chan", Gender.MALE, 20, Department.MATH);
        assertEquals("Bob Chan", student.getName());
        assertEquals(Gender.MALE, student.getGender());
        assertEquals(20, student.getAge());
        assertEquals(Department.MATH, student.getDepartment());
        assertEquals("stud1", student.getUsername());
        assertEquals("pass5678", student.getPassword());
    }

    @Test
    void testValidateWithMessage_noDuplicate() {
        // Should be valid if username is unique (simulate empty DB)
        StringBuilder msg = Student.validateWithMessage(1L, "stud2", "password2", "Charlie Lee", "22", Department.CSE);
        assertEquals(0, msg.length());
    }

    // Note: For real duplicate username test, would need to mock Database<Student>.queryByField
    // This is a simple coverage test for the static method path
} 