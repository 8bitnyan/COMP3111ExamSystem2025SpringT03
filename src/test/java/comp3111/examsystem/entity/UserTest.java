package comp3111.examsystem.entity;

import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    @Test
    void testConstructorAndGetters() {
        User user = new User(1L, "user1", "pass1234", "Alice Smith", Gender.FEMALE, 25, Department.CSE);
        assertEquals("Alice Smith", user.getName());
        assertEquals(Gender.FEMALE, user.getGender());
        assertEquals(25, user.getAge());
        assertEquals(Department.CSE, user.getDepartment());
    }

    @Test
    void testSetters() {
        User user = new User();
        user.setName("Bob Lee");
        user.setGender(Gender.MALE);
        user.setAge(30);
        user.setDepartment(Department.MATH);
        assertEquals("Bob Lee", user.getName());
        assertEquals(Gender.MALE, user.getGender());
        assertEquals(30, user.getAge());
        assertEquals(Department.MATH, user.getDepartment());
    }

    @Test
    void testSetGenderFromString() {
        User user = new User();
        user.setGender("Female");
        assertEquals(Gender.FEMALE, user.getGender());
    }

    @Test
    void testSetDepartmentFromString() {
        User user = new User();
        user.setDepartment("CSE");
        assertEquals(Department.CSE, user.getDepartment());
    }

    @Test
    void testValidateWithMessage() {
        // Valid input
        StringBuilder msg = User.validateWithMessage("user1", "password1", "Alice Smith", "25", Department.CSE);
        assertEquals(0, msg.length());
        // Invalid username
        msg = User.validateWithMessage("u1", "password1", "Alice Smith", "25", Department.CSE);
        assertTrue(msg.toString().contains("Invalid username"));
        // Invalid password
        msg = User.validateWithMessage("user1", "pass", "Alice Smith", "25", Department.CSE);
        assertTrue(msg.toString().contains("Invalid password"));
        // Invalid name
        msg = User.validateWithMessage("user1", "password1", "Alice123", "25", Department.CSE);
        assertTrue(msg.toString().contains("Invalid name"));
        // Invalid age
        msg = User.validateWithMessage("user1", "password1", "Alice Smith", "0", Department.CSE);
        assertTrue(msg.toString().contains("Invalid age"));
        // Invalid department
        msg = User.validateWithMessage("user1", "password1", "Alice Smith", "25", Department.ANY);
        assertTrue(msg.toString().contains("Invalid department"));
    }
} 