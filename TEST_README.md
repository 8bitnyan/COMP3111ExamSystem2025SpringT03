# Testing the Exam System

This document explains how to run the test cases for the Exam System application and how to interpret the test coverage reports.

## Overview of Test Cases

The test suite focuses on testing the `StudentQuizController` class, which handles the quiz-taking functionality in the student interface. The following aspects are tested:

1. **Timer Display**: Tests if the timer display is correctly formatted and updated.
2. **Controller Initialization**: Tests if the controller is correctly initialized with quiz data.
3. **Navigation Buttons**: Tests if the navigation buttons are correctly enabled/disabled based on the current question index.
4. **Answer Saving**: Tests if the student's selected answers are correctly saved.

## Running the Tests

There are two ways to run the tests:

### Method 1: Using the TestRunner

1. Compile the project using Maven:
   ```
   mvn compile
   ```

2. Run the TestRunner class:
   ```
   mvn exec:java -Dexec.mainClass="comp3111.examsystem.TestRunner"
   ```

   This will execute the tests and display a summary report in the console.

### Method 2: Using Maven

1. Run the tests using Maven's test goal:
   ```
   mvn test
   ```

   This will execute all the tests in the project and generate a JaCoCo coverage report.

## Viewing Test Coverage Reports

The project is configured to use JaCoCo for code coverage analysis. After running the tests with `mvn test`, you can view the coverage reports:

1. The coverage report is generated in HTML format at:
   ```
   target/site/jacoco/index.html
   ```

2. Open this file in a web browser to view detailed coverage information, including:
   - Lines covered/missed
   - Branches covered/missed
   - Complexity metrics
   - Coverage per class and method

## Evaluating Test Effectiveness

The effectiveness of the test cases can be evaluated using the following criteria:

1. **Line Coverage**: Percentage of code lines executed during tests.
2. **Branch Coverage**: Percentage of code branches (if statements, etc.) covered.
3. **Method Coverage**: Percentage of methods called during tests.

The JaCoCo report provides detailed metrics for each of these aspects, allowing you to identify areas of the code that may need additional testing.

### Current Test Coverage for StudentQuizController

The implemented tests focus on core functionality:

- The timer mechanism
- Quiz initialization
- Navigation between questions
- Answer saving logic

Areas requiring additional testing (for future improvement):
- Event handling for button clicks
- Quiz submission flow
- Error handling scenarios

## Writing Additional Tests

To expand the test suite:

1. Create new test methods in the existing test class, or create new test classes for other controllers.
2. Follow the JUnit 4 test patterns with appropriate assertions.
3. Use Mockito for mocking when testing components that interact with UI elements.

## Troubleshooting

Common issues when running tests:

1. **JavaFX Initialization**: The tests use JFXPanel to initialize the JavaFX toolkit. If you encounter JavaFX-related errors, ensure that JavaFX is properly set up in your environment.

2. **Reflection Issues**: The tests use reflection to access private fields and methods. If class structure changes, update the tests accordingly.

3. **Maven Configuration**: If you encounter problems with Maven, check the pom.xml configuration for proper test dependencies. 