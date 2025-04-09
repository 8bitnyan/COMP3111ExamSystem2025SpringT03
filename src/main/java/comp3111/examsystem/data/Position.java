package comp3111.examsystem.data;

/**
 * Represents the position of a teacher user.
 * This enum provides a list of possible positions for a teacher.
 */
public enum Position {
    /**
     * The list of positions.
     */
    AP("Associate Professor"),
    DH("Department Head"),
    IA("Instructional Assistant"),
    L("Lecturer"),
    P("Professor"),
    SL("Senior Lecturer"),
    TA("Teaching Associate");

    /**
     * The name of the position
     */
    private final String positionName;

    /**
     * The constructor for Position Enum.
     * @param positionName The name of the position.
     */
    Position(String positionName) { this.positionName = positionName; }

    /**
     * Gets the string representation of the Position.
     * @return The string representation of the Position.
     */
    @Override
    public String toString() {
        return positionName;
    }

    /**
     * Converts a string to a Position Enum.
     * @param positionString the string to be converted.
     * @return The corresponding Position Enum. If the string is invalid, Position.AP is returned.
     */
    public static Position fromString(String positionString) {
        for (Position position : Position.values()) {
            if (position.toString().equals(positionString)) {
                return position;
            }
        }
        return AP;
    }
}
