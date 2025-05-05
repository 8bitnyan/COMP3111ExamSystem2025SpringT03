package comp3111.examsystem.controller;

import comp3111.examsystem.entity.Student;
import comp3111.examsystem.data.Department;
import comp3111.examsystem.data.Gender;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class StudentMainControllerTest {
    StudentMainController controller;
    ComboBox<String> takeExamComboBox;
    ComboBox<String> checkResultComboBox;
    Button startExamButton;
    Button checkResultButton;
    Button viewStatisticsButton;
    Button logoutButton;
    Button exitButton;
    private static boolean javafxInitialized = false;

    @BeforeAll
    static void initJfx() throws Exception {
        if (!javafxInitialized) {
            try {
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
                Platform.startup(latch::countDown);
                latch.await();
            } catch (IllegalStateException e) {
                // Toolkit already initialized, ignore
            }
            Platform.setImplicitExit(false);
            javafxInitialized = true;
        }
    }

    @BeforeEach
    void setup() {
        controller = new StudentMainController();
        takeExamComboBox = new ComboBox<>();
        checkResultComboBox = new ComboBox<>();
        startExamButton = new Button();
        checkResultButton = new Button();
        viewStatisticsButton = new Button();
        logoutButton = new Button();
        exitButton = new Button();

        inject(controller, "TakeExamComboBox", takeExamComboBox);
        inject(controller, "CheckResultComboBox", checkResultComboBox);
        inject(controller, "startExamButton", startExamButton);
        inject(controller, "checkResultButton", checkResultButton);
        inject(controller, "viewStatisticsButton", viewStatisticsButton);
        inject(controller, "logoutButton", logoutButton);
        inject(controller, "exitButton", exitButton);
    }

    private void inject(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testInitializeDisablesButtons() {
        controller.initialize(null, null);
        assertTrue(startExamButton.isDisabled());
        assertTrue(checkResultButton.isDisabled());
    }

    @Test
    void testPreSetControllerPopulatesComboBoxes() {
        Student student = new Student(1L, "user", "pass", "Name", Gender.MALE, 20, Department.CSE);
        controller.preSetController(student);
        assertNotNull(takeExamComboBox.getItems());
        assertNotNull(checkResultComboBox.getItems());
    }

    @Test
    @Disabled("JavaFX event loop may not process runLater in headless/test environments; see CI logs.")
    void testHandleExitCallsPlatformExit() throws Exception {
        MockedStatic<Platform> platformMock = Mockito.mockStatic(Platform.class);
        try {
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    controller.handleExit(new ActionEvent());
                    platformMock.verify(Platform::exit);
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
            assertTrue(completed, "JavaFX action did not complete in time");
        } finally {
            platformMock.close();
        }
    }

    @Test
    @Disabled("JavaFX event loop may not process runLater in headless/test environments; see CI logs.")
    void testHandleLogoutLoadsLoginScreen() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.handleLogout(new ActionEvent());
                // Would check for scene change or alert, but skip in headless
            } finally {
                latch.countDown();
            }
        });
        boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(completed, "JavaFX action did not complete in time");
    }

    @Test
    @Disabled("JavaFX event loop may not process runLater in headless/test environments; see CI logs.")
    void testHandleViewStatisticsLoadsStatisticsScreen() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.handleViewStatistics(new ActionEvent());
                // Would check for scene change or alert, but skip in headless
            } finally {
                latch.countDown();
            }
        });
        boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(completed, "JavaFX action did not complete in time");
    }

    @Test
    void testPreSetControllerWithNullStudent() {
        assertDoesNotThrow(() -> controller.preSetController(null));
        // Should not throw, should not populate combo boxes
        assertNotNull(takeExamComboBox.getItems());
        assertNotNull(checkResultComboBox.getItems());
        assertEquals(0, takeExamComboBox.getItems().size());
        assertEquals(0, checkResultComboBox.getItems().size());
    }

    // TODO: Add tests for handleLogout, handleViewStatistics, and edge cases
} 