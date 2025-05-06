package comp3111.examsystem.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ManagerTest {
    @Test
    void testSingletonInstance() {
        Manager m1 = Manager.getInstance();
        Manager m2 = Manager.getInstance();
        assertSame(m1, m2);
    }

    @Test
    void testValidateLogin() {
        assertTrue(Manager.validateLogin("root", "root"));
        assertFalse(Manager.validateLogin("admin", "root"));
        assertFalse(Manager.validateLogin("root", "wrong"));
    }

    @Test
    void testPersonMethods() {
        Manager m = Manager.getInstance();
        assertEquals("root", m.getUsername());
        assertEquals("root", m.getPassword());
    }
} 