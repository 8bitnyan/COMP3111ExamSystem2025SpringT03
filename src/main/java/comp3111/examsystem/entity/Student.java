package comp3111.examsystem.entity;

import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import comp3111.examsystem.tools.Database;

import java.util.List;

/**
 * The Student class represents a student in the system.
 * It also provides utility functions for student exam and grade management.
 */
public class Student extends User {

    /**
     * Constructor for database utilities. Do NOT call this.
     */
    public Student() {
        super();
    }

    /**
     * The intended constructor for Student.
     * @param id The entity id recorded in the database.
     * @param username The username of the student.
     * @param password The password of the student.
     * @param name The name of the student.
     * @param gender The gender of the student.
     * @param age The age of the student.
     * @param department The department of the student.
     */
    public Student(Long id, String username, String password, String name, Gender gender, Integer age, Department department) {
        super(id, username, password, name, gender, age, department);
    }

    /**
     * Validate the student input parameters. Also checks repetition of usernames.
     * @param id The entity id of the student recorded in the database.
     * @param username The username of the student.
     * @param password The password of the student.
     * @param name The name of the student.
     * @param age The age of the student.
     * @param department The department of the student.
     * @return A StringBuilder object containing the error message. If the StringBuilder is empty, the student information is valid.
     */
    public static StringBuilder validateWithMessage(Long id, String username, String password, String name, String age, Department department) {
        StringBuilder msg = User.validateWithMessage(username, password, name, age, department);
        List<Student> usernameList = (new Database<>(Student.class)).queryByField("username", username);
        if (!usernameList.isEmpty() && !usernameList.getFirst().getId().equals(id)) msg.append("Duplicate username found! Please make sure that usernames are unique.\n\n");
        return msg;
    }
}