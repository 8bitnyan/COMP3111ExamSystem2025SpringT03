package comp3111.examsystem.tools;

import comp3111.examsystem.data.*;
import comp3111.examsystem.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Database class represents a database of some type of entity.
 * It is used for managing the logical and file representation of the database.
 * It supports database add, update, delete, and queries.
 * Note: For data recovery purposes, lazy delete is implemented instead.
 * Whether an entity is deleted depends on its "isAble" field.
 */
public class Database<T> {
    Class<T> entitySample;
    String tableName;
    String jsonFile;
    public static final String NameValueSeparator = "%&:";
    public static final String PropertySeparator = "!@#";
    public static final String ListItemSeparator = "-~,";

    /**
     * The constructor for Database.
     * @param entity An instance of the entity that the database represents. Only used to get field names and find the corresponding file.
     */
    public Database(Class<T> entity) {
        entitySample = entity;
        tableName = entitySample.getSimpleName().toLowerCase();
        jsonFile = Paths.get("src", "main", "resources", "database", tableName + ".txt").toString();
        File file = new File(jsonFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes all disabled entities from a list of entities.
     * @param list The list of entities to be processed.
     * @return A list containing only the active entities.
     */
    private List<T> filterDisable(List<T> list) {
        List<T> filteredList = new ArrayList<>();
        for (T item : list) {
            Object isAbleValue = getValue(item, "isAble");
            if (isAbleValue instanceof Boolean && (Boolean) isAbleValue) {
                filteredList.add(item);
            }
        }
        return filteredList;
    }

    /**
     * Queries the database based on entity id.
     * @param key The entity id of the queried entity.
     * @return An (enabled) entity object that has the queried entity id. Returns null if no entities satisfy the query.
     */
    public T queryByKey(String key) {
        List<String> slist = FileUtil.readFileByLines(jsonFile);

        T res = null;
        for (String s : slist) {
            T t = txtToEntity(s);
            Object tvalue = getValue(t, "id");
            Object isAbleValue = getValue(t, "isAble");
            if (tvalue.toString().equals(key) && (Boolean) isAbleValue) {
                res = t;
                break;
            }
        }
        return res;
    }

    /**
     * Queries the database based on entity ids.
     * @param keys The list of entity ids of the queried entities.
     * @return A list of (enabled) entity objects that have the queried entity ids. Returns empty list if no entities satisfy the query.
     */
    public List<T> queryByKeys(List<String> keys) {
        List<String> slist = FileUtil.readFileByLines(jsonFile);

        List<T> res = new ArrayList<>();
        for (String s : slist) {
            T t = txtToEntity(s);
            Object tvalue = getValue(t, "id");
            for (String key : keys) {
                if (tvalue.toString().equals(key)) {
                    res.add(t);
                    break;
                }
            }
        }
        return filterDisable(res);
    }

    /**
     * Queries the database based on field name and field values. Performs exact search.
     * @param fieldName The field name of the field in the entity class.
     * @param fieldValue The queried field value of the field in the entity class, in string format.
     * @return A list of (enabled) entity objects that has the corresponding field values in the queried field. Returns empty list if no entities satisfy the query.
     */
    public List<T> queryByField(String fieldName, String fieldValue) {
        List<T> list = getAllEnabled();
        List<T> resList = new ArrayList<>();
        for (T e : list) {
            Object value = getValue(e, fieldName);
            if ((value == null && fieldValue != null) || (value != null && fieldValue == null) || !value.toString().equals(fieldValue)) {
                continue;
            }
            resList.add(e);
        }
        list.clear();
        list.addAll(resList);
        return filterDisable(list);
    }

    /**
     * Queries the database fuzzily based on field name and field values. Performs substring matching.
     * @param fieldName The field name of the field in the entity class.
     * @param fieldValue The queried field value of the field in the entity class, in string format.
     * @return A list of (enabled) entity objects that has matching substrings in the corresponding field values in the queried field. Returns empty list if no entities satisfy the query.
     */
    public List<T> queryFuzzyByField(String fieldName, String fieldValue) {
        List<T> list = getAllEnabled();
        List<T> resList = new ArrayList<>();
        for (T e : list) {
            Object value = getValue(e, fieldName);
            if (fieldValue == null || value.toString().contains(fieldValue)) {
                resList.add(e);
            }
        }
        list.clear();
        list.addAll(resList);
        return filterDisable(list);
    }

    /**
     * Queries the database on all enabled entities.
     * @return A list of all enabled entities objects in the database. Returns empty list if no entities satisfy the query.
     */
    public List<T> getAllEnabled() {
        List<String> slist = FileUtil.readFileByLines(jsonFile);
        List<T> tlist = new ArrayList<>();
        for (String s : slist) {
            tlist.add(txtToEntity(s));
        }
        return filterDisable(tlist);
    }

    /**
     * Queries the database on all entities, both enabled and disabled. For uses inside Database class only!
     * @return A list of all entities objects in the database. Returns empty list if no entities are in the database.
     */
    public List<T> getAll() {
        List<String> slist = FileUtil.readFileByLines(jsonFile);
        List<T> tlist = new ArrayList<>();
        for (String s : slist) {
            tlist.add(txtToEntity(s));
        }
        return tlist;
    }

    /**
     * Finds the intersection of two lists of entities by entity id.
     * @param list1 The first list of entities.
     * @param list2 The second list of entities.
     * @return A list of entity objects in which their entity ids are in both list1 and list2.
     */
    public List<T> join(List<T> list1, List<T> list2) {
        List<T> resList = new ArrayList<>();
        for (T t : list1) {
            for (T value : list2) {
                Long id1 = (Long) getValue(t, "id");
                Long id2 = (Long) getValue(value, "id");
                if (id1.toString().equals(id2.toString())) {
                    resList.add(t);
                    break;
                }
            }
        }
        return resList;
    }

    /**
     * Deletes an entity by entity id from the database logically by setting "isAble" to false.
     * @param key The entity id of the entity to be deleted.
     */
    public void delByKey(String key) {
        List<T> tlist = getAll();
        for (T t : tlist) {
            Object value = getValue(t, "id");
            if (value.toString().equals(key) && (Boolean) getValue(t, "isAble")) {
                setValue(t, "isAble", false);
                break;
            }
        }
        try {
            FileUtil.writeTxtFile(listToStr(tlist), new File(jsonFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes all entities that has the exact field values in the field with the exact field name. Deletion is logical by setting "isAble" to false.
     * @param fieldName The field name of the field in the entity class.
     * @param fieldValue The desired field value of the field in the entity class, in string format.
     */
    public void delByField(String fieldName, String fieldValue) {
        List<T> tlist = getAll();
        for (T t : tlist) {
            Object value = getValue(t, fieldName);
            if (value.toString().equals(fieldValue)) {
                // If database records are all valid, should work fine
                setValue(t, "isAble", false);
            }
        }
        try {
            FileUtil.writeTxtFile(listToStr(tlist), new File(jsonFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an entity in the database by entity id.
     * @param entity The updated entity that has the same entity id as the entity that needs to be updated.
     */
    public void update(T entity) {
        Long key1 = (Long) getValue(entity, "id");
        List<T> tlist = getAll();
        for (int i = 0; i < tlist.size(); i++) {
            Long key = (Long) getValue(tlist.get(i), "id");
            if (key.toString().equals(key1.toString()) && (Boolean) getValue(tlist.get(i), "isAble")) {
                // Only update enabled records
                tlist.set(i, entity);
            }
        }
        try {
            FileUtil.writeTxtFile(listToStr(tlist), new File(jsonFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an entity into the database.
     * @param entity The entity to be added. If it has null entity id, then a new entity id is assigned to it (system time in milliseconds).
     */
    public void add(T entity) {
        setValue(entity, "id", System.currentTimeMillis());
        List<T> tlist = getAll();
        tlist.add(entity);
        try {
            FileUtil.writeTxtFile(listToStr(tlist), new File(jsonFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * An internal function to get the field value of a field of an entity by field name.
     * @param entity The entity that provides its field value.
     * @param fieldName The field name to look for.
     * @return The field value in Object type, if any.
     * @throws RuntimeException if the field name is not found or there is no way to access the desired field value.
     */
    private Object getValue(Object entity, String fieldName) {
        Object value;
        Class<?> clazz = entity.getClass();
        while (true) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                value = field.get(entity);
                break;
            }
            catch (NoSuchFieldException e) {
                if (clazz.equals(Object.class))
                    throw new RuntimeException(e);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            clazz = clazz.getSuperclass();
        }
        return value;
    }

    /**
     * An internal function to convert the value obtained from getValue() from Object type to fieldType.
     * @param fieldType The desired type to convert to.
     * @param fieldValue The value to be converted.
     * @return Supported types are: Long, Integer, Boolean, List of Integer, List of String, and Gender, Department, Position Enums. Returns fieldValue in Object type if parsing is unsuccessful.
     */
    private Object convertValue(Class<?> fieldType, Object fieldValue) {
        if (fieldType == Long.class || fieldType == long.class) {
            return Long.parseLong(fieldValue.toString());
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.parseInt(fieldValue.toString());
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return Boolean.parseBoolean(fieldValue.toString());
        } else if (fieldType == List.class) {
            if (fieldValue.toString().equals("[]")) {
                return new ArrayList<>();
            }
            String[] items = fieldValue.toString().substring(1, fieldValue.toString().length() - 1).split(ListItemSeparator, -1);
            try {
                String clazzName = items[0];
                if (!clazzName.equals("Integer")) throw new NumberFormatException();
                List<Integer> values = new ArrayList<>();
                for (int i = 1; i < items.length; ++i) values.add(Integer.parseInt(items[i]));
                return values;
            } catch (NumberFormatException e) {
                return new ArrayList<>(Arrays.asList(items).subList(1, items.length));
            }
        } else if (fieldType == Gender.class) {
            return Gender.fromString(fieldValue.toString());
        } else if (fieldType == Department.class) {
            return Department.fromString(fieldValue.toString());
        } else if (fieldType == Position.class) {
            return Position.fromString(fieldValue.toString());
        } else {
            return fieldValue;
        }
    }

    /**
     * An internal function to set fieldValue to fieldName for an entity.
     * @param entity The entity that is going to have its field with name fieldName set to fieldValue.
     * @param fieldName The field name of the field to be set.
     * @param fieldValue The value to be set.
     * @throws RuntimeException if the field does not exist or there is no way to access the field.
     */
    private void setValue(Object entity, String fieldName, Object fieldValue) {
        Class<?> clazz = entity.getClass();
        while (true) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                if (!field.getName().equals("id") || field.get(entity) == null) field.set(entity, convertValue(field.getType(), fieldValue));
                break;
            } catch (NoSuchFieldException e) {
                if (clazz.equals(Object.class))
                    throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * An internal function to convert a list of entities to a string to be written in the database files.
     * @param tlist The list of entities to be converted.
     * @return A string representing the list of entities.
     */
    private String listToStr(List<T> tlist) {
        StringBuilder sbf = new StringBuilder();
        for (T t : tlist) {
            sbf.append(entityToTxt(t)).append("\r\n");
        }
        return sbf.toString();
    }

    /**
     * An internal function to convert one line of database record to an entity.
     * @param txt The line of string to be converted.
     * @return An entity represented by this line.
     * @throws RuntimeException if there is a problem with instantiation, field access, or method calls in entity class.
     */
    private T txtToEntity(String txt) {
        T t;
        try {
            t = entitySample.getConstructor().newInstance();
            String[] pros = txt.split(PropertySeparator);
            for (String pro : pros) {
                String[] keyValue = pro.split(NameValueSeparator);
                setValue(t, keyValue[0], keyValue[1]);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    /**
     * An internal function to convert a list of unknown type into a string representation.
     * @param obj The list with unknown type.
     * @return A string representation of the list.
     */
    private String myToString(List<?> obj) {
        StringBuilder sb = new StringBuilder();
        for (Object item : obj) {
            if (item == null) {
                sb.append(ListItemSeparator);
            } else {
                sb.append(item).append(ListItemSeparator);
            }

        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - ListItemSeparator.length()); // Remove the trailing comma and space
        }
        return sb.toString();
    }

    /**
     * An internal function to convert an entity to one line of database record.
     * @param t The entity to be converted.
     * @return A string representation of the entity.
     */
    private String entityToTxt(T t) {
        StringBuilder sbf = new StringBuilder();
        Class<?> clazz = entitySample;
        while (true) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.getName().equals("dbutil")) {
                    Object obj = getValue(t, field.getName());
                    if (obj != null && !obj.toString().isEmpty()) {
                        if (obj instanceof List<?> list) {
                            if (list.isEmpty()) sbf.append(field.getName()).append(NameValueSeparator).append("[]").append(PropertySeparator);
                            else {
                                Class<?> listContentClass = list.getFirst().getClass();
                                sbf.append(field.getName()).append(NameValueSeparator + "[").append(listContentClass.getSimpleName()).append(ListItemSeparator).append(myToString(list)).append("]" + PropertySeparator);
                            }
                        }
                        // Possible attribute of integer or boolean
                        else if (obj instanceof Integer || obj instanceof Boolean) {
                            sbf.append(field.getName()).append(NameValueSeparator).append(obj).append(PropertySeparator);
                        }
                        // String
                        else {
                            sbf.append(field.getName()).append(NameValueSeparator).append(obj).append(PropertySeparator);
                        }
                    }
                }
            }
            if (clazz.equals(Entity.class)) {
                break;
            } else {
                clazz = clazz.getSuperclass();
            }
        }
        return sbf.toString();
    }
}
