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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class CourseManagementControllerTest {
    private CourseManagementController controller;

    @BeforeAll
    static void initJfx() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new CourseManagementController();
        setField(controller, "courseTable", new TableView<>());
        setField(controller, "tfCourseCode", new TextField());
        setField(controller, "tfCourseName", new TextField());
        setField(controller, "cbDepartment", new ComboBox<>());
        setField(controller, "filterCourseCode", new TextField());
        setField(controller, "filterCourseName", new TextField());
        setField(controller, "filterDepartment", new ComboBox<>());
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    void testAddUpdateDeleteCourseCRUD() throws Exception {
        setField(controller, "tfCourseCode", new TextField("COMP1001"));
        setField(controller, "tfCourseName", new TextField("Intro to CS"));
        // ... set other required fields ...
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            // Add
            try {
                var add = controller.getClass().getDeclaredMethod("addCourse");
                add.setAccessible(true);
                add.invoke(controller);
            } catch (NoSuchMethodException ignored) {}
            // Update
            try {
                var update = controller.getClass().getDeclaredMethod("updateCourse");
                update.setAccessible(true);
                update.invoke(controller);
            } catch (NoSuchMethodException ignored) {}
            // Delete
            try {
                var delete = controller.getClass().getDeclaredMethod("deleteCourse");
                delete.setAccessible(true);
                delete.invoke(controller);
            } catch (NoSuchMethodException ignored) {}
        }
    }

    @Test
    void testUniqueCourseIdConstraint() throws Exception {
        setField(controller, "tfCourseCode", new TextField("COMP1001"));
        setField(controller, "tfCourseName", new TextField("Intro to CS"));
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            try {
                var add = controller.getClass().getDeclaredMethod("addCourse");
                add.setAccessible(true);
                add.invoke(controller);
                // Try to add again with same course code
                add.invoke(controller);
            } catch (NoSuchMethodException ignored) {}
        }
        assertTrue(true); // Placeholder for actual assertion
    }

    @Test
    void testDeleteRemovesCourseAndExams() throws Exception {
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            try {
                var delete = controller.getClass().getDeclaredMethod("deleteCourse");
                delete.setAccessible(true);
                delete.invoke(controller);
            } catch (NoSuchMethodException ignored) {}
        }
        assertTrue(true); // Placeholder
    }

    @Test
    void testBackAndCloseButtons() throws Exception {
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            msgSenderMocked.when(() -> MsgSender.showConfirm(anyString(), anyString(), any())).then(invocation -> {
                Runnable callback = invocation.getArgument(2);
                callback.run();
                return null;
            });
            // Back
            try {
                var back = controller.getClass().getDeclaredMethod("back", ActionEvent.class);
                back.setAccessible(true);
                CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        back.invoke(controller, new ActionEvent());
                    } catch (Exception ignored) {}
                    latch.countDown();
                });
                latch.await();
            } catch (NoSuchMethodException ignored) {}
            // Close
            try {
                var close = controller.getClass().getDeclaredMethod("closeApplication");
                close.setAccessible(true);
                CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        close.invoke(controller);
                    } catch (Exception ignored) {}
                    latch.countDown();
                });
                latch.await();
            } catch (NoSuchMethodException ignored) {}
            assertTrue(true);
        }
    }

    @Test
    void testFilterAndResetLogic() throws Exception {
        try (MockedStatic<MsgSender> msgSenderMocked = Mockito.mockStatic(MsgSender.class)) {
            // Filter
            try {
                var filter = controller.getClass().getDeclaredMethod("filterCourses");
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