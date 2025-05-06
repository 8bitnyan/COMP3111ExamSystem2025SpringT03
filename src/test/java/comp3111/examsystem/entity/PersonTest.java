package comp3111.examsystem.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PersonTest {
    @Test
    void testDefaultConstructor() {
        Person p = new Person();
        assertNull(p.getUsername());
        assertNull(p.getPassword());
    }

    @Test
    void testConstructorAndGetters() {
        Person p = new Person(5L, "user5", "pass5");
        assertEquals("user5", p.getUsername());
        assertEquals("pass5", p.getPassword());
    }

    @Test
    void testSetters() {
        Person p = new Person();
        p.setUsername("user6");
        p.setPassword("pass6");
        assertEquals("user6", p.getUsername());
        assertEquals("pass6", p.getPassword());
    }
} 