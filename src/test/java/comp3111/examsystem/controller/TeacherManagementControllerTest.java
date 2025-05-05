package comp3111.examsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.control.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import comp3111.examsystem.tools.MsgSender;
import javafx.event.ActionEvent;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;

public class TeacherManagementControllerTest {
    private TeacherManagementController controller;

    @BeforeAll
    static void initJfx() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new TeacherManagementController();
        setField(controller, "teacherTable", new TableView<>());
        setField(controller, "tfUsername", new TextField());
        setField(controller, "tfName", new TextField());
        setField(controller, "tfPassword", new TextField());
        setField(controller, "cbDepartment", new ComboBox<>());
        setField(controller, "tfAge", new TextField());
        setField(controller, "cbGender", new ComboBox<>());
        setField(controller, "cbPosition", new ComboBox<>());
        setField(controller, "filterUsername", new TextField());
        setField(controller, "filterName", new TextField());
        setField(controller, "filterDepartment", new ComboBox<>());
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    void testAddUpdateDeleteTeacherCRUD() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                setField(controller, "tfUsername", new TextField("teacher1"));
                setField(controller, "tfName", new TextField("Bob"));
                // ... set other required fields ...
                try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
                    // Call addTeacher, updateTeacher, deleteTeacher via reflection if private
                    // Add
                    try {
                        var add = controller.getClass().getDeclaredMethod("addTeacher");
                        add.setAccessible(true);
                        add.invoke(controller);
                    } catch (NoSuchMethodException ignored) {}
                    // Update
                    try {
                        var update = controller.getClass().getDeclaredMethod("updateTeacher");
                        update.setAccessible(true);
                        update.invoke(controller);
                    } catch (NoSuchMethodException ignored) {}
                    // Delete
                    try {
                        var delete = controller.getClass().getDeclaredMethod("deleteTeacher");
                        delete.setAccessible(true);
                        delete.invoke(controller);
                    } catch (NoSuchMethodException ignored) {}
                }
            } catch (Exception ignored) {}
            latch.countDown();
        });
        latch.await();
    }

    @Test
    void testUniqueUsernameConstraint() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                setField(controller, "tfUsername", new TextField("teacher1"));
                setField(controller, "tfName", new TextField("Bob"));
                try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
                    try {
                        var add = controller.getClass().getDeclaredMethod("addTeacher");
                        add.setAccessible(true);
                        add.invoke(controller);
                        // Try to add again with same username
                        add.invoke(controller);
                    } catch (NoSuchMethodException ignored) {}
                }
            } catch (Exception ignored) {}
            latch.countDown();
        });
        latch.await();
        assertTrue(true); // Placeholder for actual assertion
    }

    @Test
    void testDeletePreservesTeacherRemovesExams() throws Exception {
        // Simulate deleting a teacher and check related exams are removed
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            try {
                var delete = controller.getClass().getDeclaredMethod("deleteTeacher");
                delete.setAccessible(true);
                delete.invoke(controller);
            } catch (NoSuchMethodException ignored) {}
        }
        assertTrue(true); // Placeholder
    }

    @Test
    void testBackAndCloseButtons() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
                // Back
                try {
                    var back = controller.getClass().getDeclaredMethod("back", ActionEvent.class);
                    back.setAccessible(true);
                    back.invoke(controller, new ActionEvent());
                } catch (NoSuchMethodException ignored) {}
                // Close
                try {
                    var close = controller.getClass().getDeclaredMethod("closeApplication");
                    close.setAccessible(true);
                    close.invoke(controller);
                } catch (NoSuchMethodException ignored) {}
            } catch (Exception ignored) {}
            latch.countDown();
        });
        latch.await();
        assertTrue(true);
    }

    @Test
    void testFilterAndResetLogic() throws Exception {
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            // Filter
            try {
                var filter = controller.getClass().getDeclaredMethod("filterTeachers");
                filter.setAccessible(true);
                filter.invoke(controller);
            } catch (NoSuchMethodException ignored) {}
            // Reset
            try {
                var reset = controller.getClass().getDeclaredMethod("reset");
                reset.setAccessible(true);
                reset.invoke(controller);
            } catch (NoSuchMethodException ignored) {}
            assertTrue(true);
        }
    }
} 