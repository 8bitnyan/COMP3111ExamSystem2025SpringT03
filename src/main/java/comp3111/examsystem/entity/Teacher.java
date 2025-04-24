package comp3111.examsystem.entity;

import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.data.Position;
import comp3111.examsystem.tools.Database;

import java.util.List;

/**
 * The Teacher class represents a teacher in the system.
 * It contains the position and a list of question IDs.
 * The teacher class provides functions to get the list of questions that belong to the teacher, add a question belonging to the teacher to the database, delete a question belonging to the teacher from the database, and update a question belonging to the teacher in the database.
 * It also supports grade modification and retrieval.
 * The teacher class extends the User class.
 */
public class Teacher extends User {
    private Position position;
    /**
     * Constructor for database utilities. Do NOT call this.
     */
    public Teacher() {
        super();
    }

    public Position getPosition() {
        return position;
    }
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * The intended constructor for Teacher.
     * @param id The entity id recorded in the database.
     * @param username The username of the teacher.
     * @param password The password of the teacher.
     * @param name The name of the teacher.
     * @param gender The gender of the teacher.
     * @param age The age of the teacher.
     * @param department The department of the teacher.
     * @param position The position of the teacher.
     */
    public Teacher(Long id, String username, String password, String name, Gender gender, Integer age, Department department, Position position) {
        super(id, username, password, name, gender, age, department);
        this.position = position;
    }

    /**
     * Validates the teacher with the given parameters. Also checks repetition of usernames.
     * @param id The entity id of the teacher recorded in the database.
     * @param username The username of the teacher.
     * @param password The password of the teacher.
     * @param name The name of the teacher.
     * @param age The age of the teacher.
     * @param department The department of the teacher.
     * @return A StringBuilder containing the error message or empty if no error.
     */
    public static StringBuilder validateWithMessage(Long id, String username, String password, String name, String age, Department department) {
        StringBuilder msg = User.validateWithMessage(username, password, name, age, department);
        List<Teacher> usernameList = (new Database<>(Teacher.class)).queryByField("username", username);
        if (!usernameList.isEmpty() && !usernameList.getFirst().getId().equals(id)) msg.append("Duplicate username found! Please make sure that usernames are unique.");
        return msg;
    }
}