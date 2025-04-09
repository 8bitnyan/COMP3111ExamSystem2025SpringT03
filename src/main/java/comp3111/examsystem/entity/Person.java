package comp3111.examsystem.entity;

/**
 * The Person class represents a person in the system.
 * It contains the username and password of the person.
 * The username is a string of English letters and numbers.
 * The password is a string of English letters and numbers.
 */
public class Person extends Entity {
    private String username;
    private String password;

    /**
     * Constructor for database utilities. Do NOT call this.
     */
    public Person() {
        super();
    }

    /**
     * The intended constructor for Person.
     * @param id The entity id of the person recorded in the database.
     * @param username The username of the person.
     * @param password The password of the person.
     */
    public Person(Long id, String username, String password) {
        super(id);
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username of the person.
     * @return A string representing the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the person.
     * @param username A string representing the username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password of the person.
     * @return A string representing the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the person.
     * @param password A string representing the password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
