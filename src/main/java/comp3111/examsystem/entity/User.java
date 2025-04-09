package comp3111.examsystem.entity;

import comp3111.examsystem.data.*;

/**
 * The User class represents a user in the system.
 * A base class for Teacher and Student.
 */
public class User extends Person {
    private String name;
    private Gender gender;
    private Integer age;
    private Department department;

    /**
     * Constructor for database utilities. Do NOT call this.
     */
    public User() {
        super();
    }

    /**
     * The intended constructor for User.
     * @param id The entity id of the user recorded in the database.
     * @param username The username for login.
     * @param password The password for login.
     * @param name The name for display.
     * @param gender The gender. Has Enum type Gender.
     * @param age The age in integer.
     * @param department The department user is in, has Enum type Department.
     */
    public User(Long id, String username, String password, String name, Gender gender, Integer age, Department department) {
        super(id, username, password);
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.department = department;
    }

    /**
     * Gets the name of the user.
     * @return name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the user.
     * @param name the new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the gender of the user.
     * @return gender Enum.
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Sets the gender of the user.
     * @param gender the new gender.
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * Sets the gender of the user, for reading data from database only.
     * @param genderString the gender string read from database.
     */
    public void setGender(String genderString) {
        this.gender = Gender.fromString(genderString);
    }

    /**
     * Gets the age of the user.
     * @return age integer.
     */
    public Integer getAge() {
        return age;
    }

    /**
     * Sets the age of the user.
     * @param age the new age.
     */
    public void setAge(Integer age) {
        this.age = age;
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

    // Utility functions

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
