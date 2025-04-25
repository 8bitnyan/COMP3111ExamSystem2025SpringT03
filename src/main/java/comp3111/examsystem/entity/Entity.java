package comp3111.examsystem.entity;

import comp3111.examsystem.tools.Database;

/**
 * The Entity class represents any entity in the database.
 * It contains the entity id used for database queries and a boolean value indicating whether the entity is an active record.
 * The entity id is a long integer.
 * The isAble is a boolean value representing the status of the entity (true means active, false means inactive).
 * The Entity class bridges the functionalities of the exam system with the database.
 */
public class Entity implements java.io.Serializable, Comparable<Entity> {

    protected Long id = null;
    protected Boolean isAble = true;

    /**
     * Gets the entity id recorded in the database.
     * @return A Long type representing the entity id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the entity id in the database. Should only be used internally in the database.
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the status of the entity.
     * @return true if active, false otherwise.
     */
    public Boolean getIsAble() {
        return isAble;
    }

    /**
     * Constructor for database utilities. Do NOT call this.
     */
    public Entity() {
        super();
    }

    /**
     * The intended constructor for Entity.
     * @param id The entity id.
     */
    public Entity(Long id) {
        super();
        this.id = id;
        this.isAble = true;
    }

    /**
     * Compares the id of this entity with another entity. Deprecated.
     * @param o the other entity to be compared.
     * @return -1 if this.id is smaller than o.id, 0 if equal, 1 otherwise.
     */
    public int compareTo(Entity o) {
        return Long.compare(this.id, o.id);
    }

    /**
     * Checks whether a string contains any of ListItemSeparator, PropertySeparator, or NameValueSeparator. For protecting database representation.
     * @param str The string to be checked.
     * @return true if str contains any of these special strings, false otherwise.
     */
    public static Boolean containsSpecialString(String str) {
        return str.contains(Database.ListItemSeparator) || str.contains(Database.PropertySeparator)
                || str.contains(Database.NameValueSeparator);
    }

    /**
     * Overrides Object.equals().
     * Checks whether the two entity have the same id.
     * @param other The other entity to be compared with.
     * @return true if they have same id and are of Entity class, false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Entity) {
            // Handle null IDs safely
            Entity otherEntity = (Entity) other;
            if (this.id == null || otherEntity.getId() == null) {
                return this == other; // If either ID is null, only equal if same object
            }
            return this.id.equals(otherEntity.getId());
        }
        else return false;
    }
}
