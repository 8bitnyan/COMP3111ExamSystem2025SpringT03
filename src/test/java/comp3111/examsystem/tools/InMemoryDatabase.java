package comp3111.examsystem.tools;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryDatabase<T> extends Database<T> {
    private final List<T> store = new ArrayList<>();

    public InMemoryDatabase(Class<T> entity) {
        super(entity);
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(store);
    }

    @Override
    public List<T> getAllEnabled() {
        return store.stream()
            .filter(e -> {
                try {
                    var field = e.getClass().getDeclaredField("isAble");
                    field.setAccessible(true);
                    Object value = field.get(e);
                    return value == null || Boolean.TRUE.equals(value);
                } catch (Exception ex) {
                    return true;
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    public void add(T entity) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            if (idField.get(entity) == null) {
                idField.set(entity, System.currentTimeMillis());
            }
            var isAbleField = entity.getClass().getDeclaredField("isAble");
            isAbleField.setAccessible(true);
            if (isAbleField.get(entity) == null) {
                isAbleField.set(entity, true);
            }
        } catch (Exception ignored) {}
        store.add(entity);
    }

    @Override
    public void delByKey(String key) {
        for (T e : store) {
            try {
                var idField = e.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                if (idField.get(e).toString().equals(key)) {
                    var isAbleField = e.getClass().getDeclaredField("isAble");
                    isAbleField.setAccessible(true);
                    isAbleField.set(e, false);
                }
            } catch (Exception ignored) {}
        }
    }
} 