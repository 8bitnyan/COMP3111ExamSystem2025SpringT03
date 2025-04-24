package comp3111.examsystem.entity;

import comp3111.examsystem.data.*;

/**
 * The User class represents a user in the system.
 * A base class for Teacher and Student.
 */
public class Course extends Entity {
    private String courseCode;
    private String courseName;
    private Department department;

    /**
     * Constructor for database utilities. Do NOT call this.
     */
    public Course() {
        super();
    }

    public Course(Long id, String courseCode, String courseName, Department department) {
        super(id);
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.department = department;
    }

    public String getCourseCode() {
        return this.courseCode;
    }
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return this.courseName;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /**
     * Gets the department of the user.
     * @return department Enum.
     */
    public Department getDepartment() {
        return department;
    }

    /**
     * Sets the department of the user.
     * @param department the new department.
     */
    public void setDepartment(Department department) {
        this.department = department;
    }

    /**
     * Sets the department of the user, for reading data from database only.
     * @param departmentString the department string read from database.
     */
    public void setDepartment(String departmentString) {
        this.department = Department.fromString(departmentString);
    }

    /**
     * Validates a user.
     * Checks input format.
     * Does not check repetition of usernames.
     * @param username The username of the user.
     * @param password The password of the user.
     * @param name The name of the user.
     * @param age The age of the user.
     * @param department The department of the user.
     * @return A StringBuilder object containing the error messages for invalid input. If it is empty, then the user is valid.
     */
    public static StringBuilder validateWithMessage(String username, String password, String name, String age, Department department) {
        StringBuilder msg = new StringBuilder();
        if (!username.matches("^[a-zA-Z0-9]{4,20}$")) msg.append("Invalid username! (Required: 4-20 characters, alphanumeric only, no space characters)\n\n");
        if (!password.matches("^[a-zA-Z0-9]{8,20}$")) msg.append("Invalid password! (Required: 8-20 characters, alphanumeric only, no space characters)\n\n");
        if (!name.matches("^[A-Za-z]+( [A-Za-z]+)*$")) msg.append("Invalid name! (Required: English letters with 1 space character between each word, e.g. CHAN Tai Man)\n\n");
        try {
            if (Integer.parseInt(age) < 1 || Integer.parseInt(age) > 100)
                msg.append("Invalid age! (Required: 1-100, number only)\n\n");
        } catch (NumberFormatException e) {
            msg.append("Invalid age! (Required: 1-100, number only)\n\n");
        }
        if (department == Department.ANY) msg.append("Invalid department! (Department cannot be \"ANY\")\n\n");
        return msg;
    }
}