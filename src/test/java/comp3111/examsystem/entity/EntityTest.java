package comp3111.examsystem.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EntityTest {
    @Test
    void testDefaultConstructor() {
        Entity entity = new Entity();
        assertNull(entity.getId());
        assertTrue(entity.getIsAble());
    }

    @Test
    void testConstructorWithId() {
        Entity entity = new Entity(42L);
        assertEquals(42L, entity.getId());
        assertTrue(entity.getIsAble());
    }

    @Test
    void testSettersAndGetters() {
        Entity entity = new Entity();
        entity.setId(100L);
        assertEquals(100L, entity.getId());
    }

    @Test
    void testEquals() {
        Entity e1 = new Entity(1L);
        Entity e2 = new Entity(1L);
        Entity e3 = new Entity(2L);
        assertTrue(e1.equals(e2));
        assertFalse(e1.equals(e3));
        assertFalse(e1.equals(null));
        assertFalse(e1.equals("not an entity"));
        // Null id case
        Entity e4 = new Entity();
        Entity e5 = new Entity();
        assertFalse(e4.equals(e5));
        assertTrue(e4.equals(e4));
    }

    @Test
    void testCompareTo() {
        Entity e1 = new Entity(1L);
        Entity e2 = new Entity(2L);
        assertTrue(e1.compareTo(e2) < 0);
        assertTrue(e2.compareTo(e1) > 0);
        assertEquals(0, e1.compareTo(new Entity(1L)));
    }

    @Test
    void testContainsSpecialString() {
        assertTrue(Entity.containsSpecialString("abc" + comp3111.examsystem.tools.Database.ListItemSeparator));
        assertTrue(Entity.containsSpecialString("abc" + comp3111.examsystem.tools.Database.PropertySeparator));
        assertTrue(Entity.containsSpecialString("abc" + comp3111.examsystem.tools.Database.NameValueSeparator));
        assertFalse(Entity.containsSpecialString("normalstring"));
    }
} 