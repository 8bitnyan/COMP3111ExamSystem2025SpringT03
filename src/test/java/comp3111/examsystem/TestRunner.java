package comp3111.examsystem;

import org.junit.BeforeClass;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import comp3111.examsystem.TestUtils;

/**
 * Test runner class for the exam system.
 * This class sets up the JavaFX environment for headless testing
 * and runs all the test classes in the project.
 * 
 * Note: For headless JavaFX testing to work properly, you need to add the following dependency to pom.xml:
 * 
 * <dependency>
 *     <groupId>org.testfx</groupId>
 *     <artifactId>openjfx-monocle</artifactId>
 *     <version>jdk-12.0.1+2</version> <!-- Use this version for Java 11+ -->
 *     <scope>test</scope>
 * </dependency>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    // Controllers
    // comp3111.examsystem.controller.TeacherLoginControllerTest.class,
    // comp3111.examsystem.controller.ManagerLoginControllerTest.class,
    comp3111.examsystem.controller.StudentQuizControllerTest.class,
    
    // Entities
    // comp3111.examsystem.entity.StudentTest.class,
    
    // Tools & Data
    // comp3111.examsystem.tools.DatabaseTest.class,
    // comp3111.examsystem.tools.FileUtilTest.class,
    // comp3111.examsystem.data.DepartmentTest.class
})
public class TestRunner {
    
    @BeforeClass
    public static void setupClass() throws Exception {
        // Set headless mode properties first
        System.setProperty("java.awt.headless", "true");
        System.setProperty("javafx.headless", "true");
        System.setProperty("testfx.headless", "true");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        
        // Disable hardware acceleration and enable software rendering
        System.setProperty("javafx.animation.fullspeed", "false");
        System.setProperty("quantum.multithreaded", "false");
        
        // Check if we have the necessary classes
        try {
            Class.forName("com.sun.glass.ui.monocle.MonoclePlatformFactory");
            System.out.println("Monocle platform is available for testing");
        } catch (ClassNotFoundException e) {
            System.out.println("WARNING: Monocle platform not found. Make sure you have the openjfx-monocle dependency in your pom.xml");
            System.out.println("Tests requiring JavaFX may fail. Add this dependency to your pom.xml:");
            System.out.println("<dependency>");
            System.out.println("    <groupId>org.testfx</groupId>");
            System.out.println("    <artifactId>openjfx-monocle</artifactId>");
            System.out.println("    <version>jdk-12.0.1+2</version>");
            System.out.println("    <scope>test</scope>");
            System.out.println("</dependency>");
        }
        
        // Initialize JavaFX toolkit
        TestUtils.setupJavaFX();
    }
    
    /**
     * Main method to run the tests.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestRunner.class);
        
        System.out.println("========== TEST REPORT ==========");
        System.out.println("Total tests run: " + result.getRunCount());
        System.out.println("Tests passed: " + (result.getRunCount() - result.getFailureCount()));
        System.out.println("Tests failed: " + result.getFailureCount());
        System.out.println("Run time: " + result.getRunTime() + "ms");
        
        if (!result.wasSuccessful()) {
            System.out.println("\nFailed tests:");
            for (Failure failure : result.getFailures()) {
                System.out.println("- " + failure.toString());
            }
        }
        
        System.out.println("\nTest " + (result.wasSuccessful() ? "SUCCESSFUL" : "FAILED"));
        System.out.println("================================");
        
        System.out.println("\nCoverage reports can be found in target/site/jacoco/index.html after running 'mvn test'.");
        
        // Exit with appropriate status code
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
} 