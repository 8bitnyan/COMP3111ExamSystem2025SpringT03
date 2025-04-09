package comp3111.examsystem;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import comp3111.examsystem.controller.StudentQuizControllerTest;

/**
 * Test runner class to execute JUnit tests and report results.
 */
public class TestRunner {
    
    /**
     * Main method to run the tests.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(StudentQuizControllerTest.class);
        
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
        
        System.out.println("\nNote: For coverage reports, run 'mvn test' to generate JaCoCo coverage reports.");
        System.out.println("Coverage reports can be found in target/site/jacoco/index.html after running the command.");
    }
} 