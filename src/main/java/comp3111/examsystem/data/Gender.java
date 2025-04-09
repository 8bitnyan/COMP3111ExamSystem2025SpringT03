package comp3111.examsystem.data;
/**
 * A Enum representing the gender of users.
 */
public enum Gender {
    /**
     * The list of genders.
     */
    MALE("Male"), FEMALE("Female");

    private final String genderText;

    /**
     * The constructor for Gender Enum.
     * @param genderText The gender of the user.
     */
    Gender(String genderText) { this.genderText = genderText; }

    /**
     * Gets the string representation of the gender.
     * @return The string representation of the gender.
     */
    @Override
    public String toString() {
        return genderText;
    }

    /**
     * Converts a string to a gender Enum.
     * @param genderString The string to be converted.
     * @return The corresponding gender Enum. If the string is invalid, Gender.OTHER is returned.
     */
    public static Gender fromString(String genderString) {
        for (Gender gender : Gender.values()) {
            if (gender.toString().equals(genderString)) {
                return gender;
            }
        }
        return MALE;
    }
}
