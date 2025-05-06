package comp3111.examsystem.entity;

import comp3111.examsystem.data.Department;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CourseTest {
    @Test
    void testDefaultConstructor() {
        Course course = new Course();
        assertNull(course.getCourseCode());
        assertNull(course.getCourseName());
        assertNull(course.getDepartment());
    }

    @Test
    void testConstructorAndGetters() {
        Course course = new Course(1L, "COMP3111", "Software Engineering", Department.CSE);
        assertEquals("COMP3111", course.getCourseCode());
        assertEquals("Software Engineering", course.getCourseName());
        assertEquals(Department.CSE, course.getDepartment());
    }

    @Test
    void testSetters() {
        Course course = new Course();
        course.setCourseCode("COMP2012");
        course.setCourseName("OOP");
        course.setDepartment(Department.MATH);
        assertEquals("COMP2012", course.getCourseCode());
        assertEquals("OOP", course.getCourseName());
        assertEquals(Department.MATH, course.getDepartment());
    }

    @Test
    void testSetDepartmentFromString() {
        Course course = new Course();
        course.setDepartment("CSE");
        assertEquals(Department.CSE, course.getDepartment());
    }
} 