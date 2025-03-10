package hausfix.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testDefaultConstructorAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("secret");

        assertEquals(1L, user.getId(), "ID sollte 1L sein.");
        assertEquals("testuser", user.getUsername(), "Username sollte 'testuser' sein.");
        assertEquals("secret", user.getPassword(), "Password sollte 'secret' sein.");
    }

    @Test
    public void testParameterizedConstructor() {
        User user = new User("paramUser", "paramPass");
        // Bei der parameterisierten Konstruktor wird keine ID gesetzt, also sollte sie null sein.
        assertNull(user.getId(), "ID sollte null sein, wenn sie nicht explizit gesetzt wird.");
        assertEquals("paramUser", user.getUsername(), "Username sollte 'paramUser' sein.");
        assertEquals("paramPass", user.getPassword(), "Password sollte 'paramPass' sein.");
    }

    @Test
    public void testToString() {
        User user = new User("example", "password");
        user.setId(42L);
        String str = user.toString();
        assertTrue(str.contains("42"), "toString sollte die ID enthalten.");
        assertTrue(str.contains("example"), "toString sollte den Username enthalten.");
        // Das Passwort wird aus Sicherheitsgründen in toString() nicht ausgegeben.
        assertFalse(str.contains("password"), "toString sollte das Passwort nicht enthalten.");
    }
}
