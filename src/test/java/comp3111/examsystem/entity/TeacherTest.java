package comp3111.examsystem.entity;

import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.data.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TeacherTest {
    @Test
    void testConstructorAndGetters() {
        Teacher teacher = new Teacher(3L, "teach1", "pass9999", "Dana Wong", Gender.FEMALE, 40, Department.ECE, Position.P);
        assertEquals("Dana Wong", teacher.getName());
        assertEquals(Gender.FEMALE, teacher.getGender());
        assertEquals(40, teacher.getAge());
        assertEquals(Department.ECE, teacher.getDepartment());
        assertEquals(Position.P, teacher.getPosition());
        assertEquals("teach1", teacher.getUsername());
        assertEquals("pass9999", teacher.getPassword());
    }

    @Test
    void testSetters() {
        Teacher teacher = new Teacher();
        teacher.setPosition(Position.L);
        assertEquals(Position.L, teacher.getPosition());
    }

    @Test
    void testValidateWithMessage_noDuplicate() {
        StringBuilder msg = Teacher.validateWithMessage(1L, "teach2", "password3", "Eve Chan", "35", Department.CSE);
        assertEquals(0, msg.length());
    }
    // Note: For real duplicate username test, would need to mock Database<Teacher>.queryByField
} 