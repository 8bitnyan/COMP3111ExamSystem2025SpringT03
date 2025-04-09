package comp3111.examsystem.entity;

/**
 * A singleton class that represents the manager of the system.
 * The manager is a special person that has the highest authority in the system.
 * The manager can query, add, update, and delete student, teacher and course in the system.
 */
public final class Manager extends Person {
    private static final Manager instance = new Manager();

    /**
     * Since Manager is a singleton class, it should not be constructed in other places.
     * The record is hardcoded.
     */
    private Manager() {
        super(-2L, "root", "root");
    }

    /**
     * For calling non-static functions from Person/User class, if somehow needed.
     * @return A Manager instance that can call non-static functions.
     */
    public static Manager getInstance() {
        return instance;
    }

    /**
     * Validates Manager login.
     * @param username The input username.
     * @param password The input password.
     * @return true if both the username and password is correct, false otherwise.
     */
    public static Boolean validateLogin(String username, String password) {
        return instance.getUsername().equals(username) && instance.getPassword().equals(password);
    }
}
