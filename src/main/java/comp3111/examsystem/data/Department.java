package comp3111.examsystem.data;

/**
 * An Enum representing the department the users and courses are in.
 */
public enum Department {
    /**
     * The list of departments.
     */
    ANY, ACCT, CBE, CHEM, CIVL, CSE, ECON, ECE, FINA, IEDA, ISOM, MGMT, MARK, MATH, MAE, OCES, PHYS, EMIA, ENVR, HUMA, ISD, LIFS, PPOL, SOSC;

    /**
     * Converts a string to a Department Enum.
     * @param departmentString The string to be converted.
     * @return The corresponding Department Enum. If the string is invalid, Department.ANY is returned.
     */
    public static Department fromString(String departmentString) {
        for (Department department : Department.values()) {
            if (department.toString().equals(departmentString)) {
                return department;
            }
        }
        return ANY;
    }
}
