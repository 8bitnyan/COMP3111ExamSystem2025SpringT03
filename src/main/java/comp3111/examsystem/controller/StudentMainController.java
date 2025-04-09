package comp3111.examsystem.controller;

import comp3111.examsystem.entity.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The controller for student main page.
 */
public class StudentMainController implements Initializable {
    @FXML private ComboBox<String> TakeExamComboBox;
    @FXML private ComboBox<String> CheckResultComboBox;
    @FXML private Button startExamButton;
    @FXML private Button checkResultButton;
    @FXML private Button viewStatisticsButton;
    @FXML private Button logoutButton;
    @FXML private Button exitButton;
    
    private Student student;

    /**
     * Initializes the student main page UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    public void initialize(URL location, ResourceBundle resources) {
        // Add event handlers for buttons
        startExamButton.setOnAction(this::handleStartExam);
        checkResultButton.setOnAction(this::handleCheckResult);
        
        // Disable buttons initially until quizzes are selected
        startExamButton.setDisable(true);
        checkResultButton.setDisable(true);
        
        // Add listeners to combo boxes
        TakeExamComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            startExamButton.setDisable(newVal == null);
        });
        
        CheckResultComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            checkResultButton.setDisable(newVal == null);
        });
    }

    /**
     * Sets the student object and initializes the UI.
     * @param student The student that is operating the page.
     */
    public void preSetController(Student student) {
        this.student = student;
        
        // Load available quizzes (in a real application, this would be from a database)
        loadAvailableQuizzes();
        loadAvailableResults();
    }
    
    /**
     * Loads available quizzes for the student.
     * This is a placeholder implementation - in a real application, 
     * this would fetch quizzes from a database.
     */
    private void loadAvailableQuizzes() {
        // Demo quizzes - these would come from a database in a real application
        List<String> demoQuizzes = Arrays.asList(
            "Midterm Quiz",
            "Final Exam",
            "Chapter 1 Assessment",
            "Programming Quiz"
        );
        
        TakeExamComboBox.getItems().clear();
        TakeExamComboBox.getItems().addAll(demoQuizzes);
    }
    
    /**
     * Loads available quiz results for the student.
     * This is a placeholder implementation - in a real application,
     * this would fetch quiz results from a database.
     */
    private void loadAvailableResults() {
        // Demo results - these would come from a database in a real application
        List<String> demoResults = Arrays.asList(
            "Week 1 Quiz",
            "Week 2 Quiz",
            "Project Assessment"
        );
        
        CheckResultComboBox.getItems().clear();
        CheckResultComboBox.getItems().addAll(demoResults);
    }
    
    /**
     * Handles the start exam button click.
     * @param event The action event.
     */
    private void handleStartExam(ActionEvent event) {
        String selectedQuiz = TakeExamComboBox.getValue();
        if (selectedQuiz == null || selectedQuiz.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Quiz Selected", "Please select a quiz before starting.");
            return;
        }
        
        try {
            // Load the quiz screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comp3111/examsystem/StudentQuizUI.fxml"));
            Parent root = loader.load();
            
            StudentQuizController controller = loader.getController();
            
            // Create demo quiz questions
            List<StudentQuizController.QuizQuestion> questions = createDemoQuizQuestions(selectedQuiz);
            
            // Initialize the controller
            controller.preSetController(student, selectedQuiz, questions, 60); // 60 minutes time limit
            
            // Show the quiz screen
            Stage stage = (Stage) startExamButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load quiz screen: " + e.getMessage());
        }
    }
    
    /**
     * Creates demo quiz questions for testing.
     * @param quizName The name of the quiz.
     * @return A list of demo quiz questions.
     */
    private List<StudentQuizController.QuizQuestion> createDemoQuizQuestions(String quizName) {
        List<StudentQuizController.QuizQuestion> questions = new ArrayList<>();
        
        // Add demo questions based on quiz name
        if (quizName.equals("Midterm Quiz")) {
            // Multiple choice questions
            questions.add(new StudentQuizController.QuizQuestion(
                "What is the main purpose of version control systems?",
                Arrays.asList(
                    "To track changes in source code over time",
                    "To optimize code execution",
                    "To automatically fix bugs",
                    "To generate documentation"
                )
            ));
            
            questions.add(new StudentQuizController.QuizQuestion(
                "Which statement about Java is correct?",
                Arrays.asList(
                    "Java is platform-independent",
                    "Java programs are compiled directly to machine code",
                    "Java does not support object-oriented programming",
                    "Java cannot run on mobile devices"
                )
            ));
            
            // Short answer question
            questions.add(new StudentQuizController.QuizQuestion(
                "Explain the concept of inheritance in object-oriented programming."
            ));
            
            // Multiple choice questions
            questions.add(new StudentQuizController.QuizQuestion(
                "Which data structure follows the FIFO principle?",
                Arrays.asList(
                    "Queue",
                    "Stack",
                    "Tree",
                    "Heap"
                )
            ));
            
            questions.add(new StudentQuizController.QuizQuestion(
                "What is the time complexity of binary search?",
                Arrays.asList(
                    "O(log n)",
                    "O(n)",
                    "O(n log n)",
                    "O(n²)"
                )
            ));
        } else if (quizName.equals("Final Exam")) {
            // Multiple choice questions
            questions.add(new StudentQuizController.QuizQuestion(
                "Which design pattern is used when you need to create objects without specifying their concrete classes?",
                Arrays.asList(
                    "Factory Method",
                    "Singleton",
                    "Observer",
                    "Decorator"
                )
            ));
            
            // Short answer question
            questions.add(new StudentQuizController.QuizQuestion(
                "Describe the Model-View-Controller (MVC) architecture pattern."
            ));
            
            // Multiple choice questions
            questions.add(new StudentQuizController.QuizQuestion(
                "Which of the following is NOT a principle of SOLID?",
                Arrays.asList(
                    "Scalable Programming",
                    "Single Responsibility",
                    "Open/Closed",
                    "Liskov Substitution"
                )
            ));
            
            questions.add(new StudentQuizController.QuizQuestion(
                "What is the purpose of dependency injection?",
                Arrays.asList(
                    "To reduce coupling between classes",
                    "To increase performance",
                    "To encrypt data",
                    "To compress code"
                )
            ));
            
            // Short answer question
            questions.add(new StudentQuizController.QuizQuestion(
                "Explain the difference between unit testing and integration testing."
            ));
        } else {
            // Default questions
            questions.add(new StudentQuizController.QuizQuestion(
                "Which of the following is a characteristic of an object-oriented programming language?",
                Arrays.asList(
                    "Encapsulation",
                    "Procedural programming",
                    "Linear execution",
                    "Machine-dependent code"
                )
            ));
            
            questions.add(new StudentQuizController.QuizQuestion(
                "What does IDE stand for?",
                Arrays.asList(
                    "Integrated Development Environment",
                    "Interactive Design Elements",
                    "Integrated Debugging Engine",
                    "Interface Development Environment"
                )
            ));
            
            // Short answer question
            questions.add(new StudentQuizController.QuizQuestion(
                "What is the purpose of unit testing in software development?"
            ));
        }
        
        return questions;
    }
    
    /**
     * Handles the check result button click.
     * @param event The action event.
     */
    private void handleCheckResult(ActionEvent event) {
        String selectedResult = CheckResultComboBox.getValue();
        if (selectedResult == null || selectedResult.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Result Selected", "Please select a result to check.");
            return;
        }
        
        try {
            // Load the quiz result screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comp3111/examsystem/StudentQuizResultUI.fxml"));
            Parent root = loader.load();
            
            StudentQuizResultController controller = loader.getController();
            
            // Create demo quiz results
            List<StudentQuizResultController.QuizResult> results = createDemoQuizResults(selectedResult);
            
            // Initialize the controller
            controller.preSetController(student, selectedResult, results);
            
            // Show the quiz result screen
            Stage stage = (Stage) checkResultButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load quiz result screen: " + e.getMessage());
        }
    }
    
    /**
     * Creates demo quiz results for testing.
     * @param quizName The name of the quiz.
     * @return A list of demo quiz results.
     */
    private List<StudentQuizResultController.QuizResult> createDemoQuizResults(String quizName) {
        List<StudentQuizResultController.QuizResult> results = new ArrayList<>();
        
        if (quizName.equals("Week 1 Quiz")) {
            // Create demo results for Week 1 Quiz
            results.add(new StudentQuizResultController.QuizResult(
                "What is the primary purpose of a constructor in Java?",
                "To initialize object variables",
                "To initialize the fields or state of an object when it is created",
                0.8, 1.0,
                "Good answer, but could be more specific about when initialization occurs."
            ));
            
            results.add(new StudentQuizResultController.QuizResult(
                "Which keyword is used to inherit a class in Java?",
                "extend",
                "extends",
                0.5, 1.0,
                "Close! The correct keyword is 'extends' (with an 's' at the end)."
            ));
            
            results.add(new StudentQuizResultController.QuizResult(
                "Explain the concept of method overloading.",
                "Method overloading is when you have multiple methods with the same name but different parameters.",
                "Method overloading is when multiple methods in the same class have the same name but different parameters (different number or types).",
                1.0, 1.0,
                "Excellent explanation of method overloading!"
            ));
        } else if (quizName.equals("Week 2 Quiz")) {
            // Create demo results for Week 2 Quiz
            results.add(new StudentQuizResultController.QuizResult(
                "What is polymorphism in object-oriented programming?",
                "Polymorphism is the ability of an object to take many forms.",
                "Polymorphism is the ability of objects to take different forms or have multiple behaviors based on their context, typically through method overriding and interfaces.",
                0.7, 1.0,
                "Good basic definition, but could include more about how polymorphism is achieved."
            ));
            
            results.add(new StudentQuizResultController.QuizResult(
                "Which collection class would you use to maintain insertion order?",
                "ArrayList",
                "LinkedList or ArrayList",
                1.0, 1.0,
                "Correct! Both maintain insertion order, with LinkedList being more efficient for insertions/deletions and ArrayList for random access."
            ));
            
            results.add(new StudentQuizResultController.QuizResult(
                "What is the difference between == and .equals() in Java?",
                "== compares references while equals compares content",
                "== compares object references (memory addresses) while .equals() compares the content or value of objects based on their implementation of the method.",
                0.9, 1.0,
                "Very good understanding of the distinction!"
            ));
            
            results.add(new StudentQuizResultController.QuizResult(
                "What is a static method?",
                "A method that belongs to a class rather than an instance",
                "A static method belongs to the class rather than instances of the class, can be called without creating an object, and cannot access instance variables directly.",
                0.8, 1.0,
                "Good answer, but could mention that static methods cannot access instance variables directly."
            ));
        } else {
            // Create demo results for other quizzes
            results.add(new StudentQuizResultController.QuizResult(
                "What is the Model-View-Controller (MVC) pattern?",
                "It's a design pattern that separates an application into three components: model, view, and controller.",
                "MVC is an architectural pattern that separates an application into three components: the Model (data and business logic), the View (user interface), and the Controller (handles user input and updates model and view).",
                0.8, 1.0,
                "Good basic definition, but could explain each component's responsibility in more detail."
            ));
            
            results.add(new StudentQuizResultController.QuizResult(
                "What is the purpose of JUnit?",
                "Testing Java code",
                "JUnit is a unit testing framework for Java used to write and run repeatable automated tests to ensure code works as expected.",
                0.6, 1.0,
                "Correct but very brief. Could explain what type of testing JUnit is used for and its importance."
            ));
            
            results.add(new StudentQuizResultController.QuizResult(
                "What does SOLID stand for in object-oriented design?",
                "Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion",
                "Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion",
                1.0, 1.0,
                "Perfect answer! You correctly identified all five principles."
            ));
        }
        
        return results;
    }
    
    /**
     * Handles the view statistics button click.
     * @param event The action event.
     */
    @FXML
    public void handleViewStatistics(ActionEvent event) {
        try {
            // Load the grade statistics screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comp3111/examsystem/StudentGradeStatisticsUI.fxml"));
            Parent root = loader.load();
            
            StudentGradeStatisticsController controller = loader.getController();
            
            // Initialize the controller
            controller.preSetController(student);
            
            // Show the grade statistics screen
            Stage stage = (Stage) viewStatisticsButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load grade statistics screen: " + e.getMessage());
        }
    }
    
    /**
     * Handles the logout button click.
     * @param event The action event.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            // Load the login screen (assuming there is a LoginUI.fxml)
            Parent root = FXMLLoader.load(getClass().getResource("/comp3111/examsystem/LoginUI.fxml"));
            
            // Show the login screen
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to login page: " + e.getMessage());
        }
    }
    
    /**
     * Handles the exit button click.
     * @param event The action event.
     */
    @FXML
    public void handleExit(ActionEvent event) {
        // Confirm before closing the application
        Alert confirmExit = new Alert(Alert.AlertType.CONFIRMATION);
        confirmExit.setTitle("Exit Application");
        confirmExit.setHeaderText("Exit Application");
        confirmExit.setContentText("Are you sure you want to exit the application?");
        
        confirmExit.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Close the application
                Platform.exit();
            }
        });
    }
    
    /**
     * Shows an alert dialog.
     * @param type The type of the alert.
     * @param title The title of the alert.
     * @param content The content of the alert.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
