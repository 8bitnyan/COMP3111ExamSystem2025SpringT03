package comp3111.examsystem.controller;

import comp3111.examsystem.entity.*;
import comp3111.examsystem.tools.Database;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * The controller for the student main page. Handles UI logic for taking exams, checking results, viewing statistics, logging out, and exiting the application.
 */
public class StudentMainController implements Initializable {
    public boolean confirmExit;
    @FXML private ComboBox<String> TakeExamComboBox;
    @FXML private ComboBox<String> CheckResultComboBox;
    @FXML private Button startExamButton;
    @FXML private Button checkResultButton;
    @FXML private Button viewStatisticsButton;
    @FXML private Button logoutButton;
    @FXML private Button exitButton;
    
    private Student student;

    // Dependency-injected databases
    private Database<Exam> examDB;
    private Database<Course> courseDB;
    private Database<comp3111.examsystem.entity.Record> recordDB;

    // For testability: allow patching alert logic
    @FunctionalInterface
    interface AlertShower {
        void show(Alert.AlertType type, String title, String content);
    }
    AlertShower showAlert = null;

    // Dependency injection constructor
    public StudentMainController(Database<Exam> examDB, Database<Course> courseDB, Database<comp3111.examsystem.entity.Record> recordDB) {
        this.examDB = examDB;
        this.courseDB = courseDB;
        this.recordDB = recordDB;
    }

    // Default constructor for production
    public StudentMainController() {
        this(new comp3111.examsystem.tools.Database<>(Exam.class),
             new comp3111.examsystem.tools.Database<>(Course.class),
             new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Record.class));
    }

    /**
     * Initializes the student main page UI. Sets up event handlers and disables buttons until selections are made.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
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
     * Sets the student object and initializes the UI with available quizzes and results.
     *
     * @param student The student that is operating the page.
     */
    public void preSetController(Student student) {
        this.student = student;
        
        // Load available quizzes (in a real application, this would be from a database)
        loadAvailableQuizzes();
        loadAvailableResults();
    }
    
    /**
     * Loads available quizzes for the student. Only quizzes from the student's department that have not been attempted are shown.
     * This is a placeholder implementation; in a real application, this would fetch quizzes from a database.
     */
    private void loadAvailableQuizzes() {
        // Get the student's department
        if (student == null || student.getDepartment() == null) {
            return;
        }
        String studentDepartment = student.getDepartment().toString();
        // Use injected DBs
        List<Exam> allExams = examDB.getAllEnabled();
        List<String> availableQuizzes = new ArrayList<>();
        // Get all records for this student
        List<comp3111.examsystem.entity.Record> studentRecords = recordDB.queryByField("studentID", student.getId().toString());
        // Build a set of attempted exam IDs
        List<Long> attemptedExamIds = new ArrayList<>();
        for (comp3111.examsystem.entity.Record record : studentRecords) {
            if (record.getExamID() != null) {
                attemptedExamIds.add(record.getExamID());
            }
        }
        for (Exam exam : allExams) {
            if (exam.getIsPublishedInt() == null || exam.getIsPublishedInt() == 0) continue;
            String courseCode = exam.getCourseCode();
            if (courseCode == null || courseCode.isEmpty()) continue;
            List<Course> courses = courseDB.queryByField("courseCode", courseCode);
            if (courses.isEmpty()) continue;
            Course course = courses.get(0);
            if (course.getDepartment() != null && course.getDepartment().toString().equals(studentDepartment)) {
                // Only add if student has NOT attempted this exam
                if (!attemptedExamIds.contains(exam.getId())) {
                    availableQuizzes.add(exam.getName());
                }
            }
        }
        TakeExamComboBox.getItems().clear();
        TakeExamComboBox.getItems().addAll(availableQuizzes);
        if (availableQuizzes.isEmpty()) {
            System.out.println("No exams available for department: " + studentDepartment);
        }
    }
    
    /**
     * Loads available quiz results for the student. Only completed quizzes from the student's department are shown.
     * This is a placeholder implementation; in a real application, this would fetch quiz results from a database.
     */
    private void loadAvailableResults() {
        // Get the student's department
        if (student == null || student.getDepartment() == null) {
            return;
        }
        // Use injected DBs
        List<Exam> allExams = examDB.getAllEnabled();
        List<comp3111.examsystem.entity.Record> studentRecords = recordDB.queryByField("studentID", student.getId().toString());
        List<String> completedExamNames = new ArrayList<>();
        for (Exam exam : allExams) {
            if (exam.getIsPublishedInt() == null || exam.getIsPublishedInt() == 0) {
                continue;
            }
            String courseCode = exam.getCourseCode();
            if (courseCode == null || courseCode.isEmpty()) {
                continue;
            }
            List<Course> courses = courseDB.queryByField("courseCode", courseCode);
            if (courses.isEmpty()) {
                continue;
            }
            Course course = courses.get(0);
            if (course.getDepartment() == null || 
                !course.getDepartment().toString().equals(student.getDepartment().toString())) {
                continue;
            }
            boolean hasRecord = false;
            for (comp3111.examsystem.entity.Record record : studentRecords) {
                if (record.getExamID() != null && record.getExamID().equals(exam.getId())) {
                    hasRecord = true;
                    break;
                }
            }
            if (hasRecord) {
                completedExamNames.add(exam.getName());
            }
        }
        CheckResultComboBox.getItems().clear();
        CheckResultComboBox.getItems().addAll(completedExamNames);
        if (completedExamNames.isEmpty()) {
            System.out.println("No completed exams found for student: " + student.getName());
        }
    }
    
    /**
     * Handles the start exam button click. Loads the selected exam and navigates to the quiz UI if possible.
     *
     * @param event The action event.
     */
    @FXML
    public void handleStartExam(ActionEvent event) {
        String selectedQuizName = TakeExamComboBox.getValue();
        if (selectedQuizName == null || selectedQuizName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Quiz Selected", "Please select a quiz before starting.");
            return;
        }
        try {
            // Use injected DB
            List<Exam> exams = examDB.getAllEnabled();
            Exam selectedExam = null;
            for (Exam exam : exams) {
                if (selectedQuizName.equals(exam.getName())) {
                    selectedExam = exam;
                    break;
                }
            }
            if (selectedExam == null) {
                showAlert(Alert.AlertType.ERROR, "Exam Not Found", "Could not find the selected exam in the database.");
                return;
            }
            // Double-check: has the student already attempted this exam?
            List<comp3111.examsystem.entity.Record> studentRecords = recordDB.queryByField("studentID", student.getId().toString());
            boolean alreadyAttempted = false;
            for (comp3111.examsystem.entity.Record record : studentRecords) {
                if (record.getExamID() != null && record.getExamID().equals(selectedExam.getId())) {
                    alreadyAttempted = true;
                    break;
                }
            }
            if (alreadyAttempted) {
                showAlert(Alert.AlertType.WARNING, "Already Attempted", "You have already attempted this exam. Only one attempt is allowed.");
                loadAvailableQuizzes(); // Refresh the list
                return;
            }
            List<StudentQuizController.QuizQuestion> quizQuestions = loadQuestionsForExam(selectedExam);
            if (quizQuestions.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No Questions", "This exam does not have any questions.");
                return;
            }
            FXMLLoader loader = createFXMLLoader("/comp3111/examsystem/StudentQuizUI.fxml");
            Parent root = loader.load();
            StudentQuizController controller = loader.getController();
            controller.preSetController(student, selectedQuizName, quizQuestions, selectedExam.getDuration());
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
     * Loads the questions for an exam from the database.
     *
     * @param exam The exam to load questions for.
     * @return A list of quiz questions.
     */
    private List<StudentQuizController.QuizQuestion> loadQuestionsForExam(Exam exam) {
        List<StudentQuizController.QuizQuestion> quizQuestions = new ArrayList<>();
        Database<Question> questionDB = new Database<>(Question.class);
        
        try {
            // Get the questions directly from the database using the exam's question string
            String questionsStr = exam.getQuestions();
            
            if (questionsStr == null || questionsStr.isEmpty()) {
                System.out.println("No questions found for exam: " + exam.getName());
                return quizQuestions;
            }
            
            // Split the question IDs string
            String[] questionIdArr = questionsStr.split(",");
            
            // Process each question ID
            for (String questionIdStr : questionIdArr) {
                try {
                    // Trim any whitespace
                    questionIdStr = questionIdStr.trim();
                    if (questionIdStr.isEmpty()) continue;
                    
                    // Use the questionIdStr directly with queryByKey
                    Question dbQuestion = questionDB.queryByKey(questionIdStr);
                    
                    if (dbQuestion == null) {
                        System.out.println("Question not found with ID: " + questionIdStr);
                        continue;
                    }
                    int maxScore = dbQuestion.getScore() != null ? dbQuestion.getScore() : 1;
                    if ("MCQ".equalsIgnoreCase(dbQuestion.getType())) {
                        // Multiple choice question
                        quizQuestions.add(new StudentQuizController.QuizQuestion(
                            dbQuestion.getQuestionText(),
                            dbQuestion.getOptions(),
                            maxScore,
                            dbQuestion.getAnswer()
                        ));
                    } else {
                        // Short answer question
                        quizQuestions.add(new StudentQuizController.QuizQuestion(
                            dbQuestion.getQuestionText(),
                            maxScore
                        ));
                    }
                } catch (Exception e) {
                    System.err.println("Error processing question ID: " + questionIdStr + ". Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading questions for exam: " + exam.getName() + ". Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return quizQuestions;
    }
    
    /**
     * Handles the check result button click. Loads the selected exam's results and navigates to the result UI if possible.
     *
     * @param event The action event.
     */
    void handleCheckResult(ActionEvent event) {
        String selectedQuizName = CheckResultComboBox.getValue();
        if (selectedQuizName == null || selectedQuizName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Result Selected", "Please select a quiz result to check.");
            return;
        }
        try {
            // Use injected DB
            List<Exam> exams = examDB.getAllEnabled();
            Exam selectedExam = null;
            for (Exam exam : exams) {
                if (selectedQuizName.equals(exam.getName())) {
                    selectedExam = exam;
                    break;
                }
            }
            if (selectedExam == null) {
                showAlert(Alert.AlertType.ERROR, "Exam Not Found", "Could not find the selected exam in the database.");
                return;
            }
            List<StudentQuizResultController.QuizResult> results = loadQuizResults(selectedExam);
            if (results.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No Results", "No results found for this exam.");
                return;
            }
            FXMLLoader loader = createFXMLLoader("/comp3111/examsystem/StudentQuizResultUI.fxml");
            Parent root = loader.load();
            StudentQuizResultController controller = loader.getController();
            controller.preSetController(student, selectedQuizName, results);
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
     * Loads quiz results for an exam from the database.
     *
     * @param exam The exam to load results for.
     * @return A list of quiz results.
     */
    private List<StudentQuizResultController.QuizResult> loadQuizResults(Exam exam) {
        List<StudentQuizResultController.QuizResult> results = new ArrayList<>();
        
        try {
            // Get the database connections
            Database<comp3111.examsystem.entity.Record> recordDB = new Database<>(comp3111.examsystem.entity.Record.class);
            Database<Question> questionDB = new Database<>(Question.class);
            
            // Get questions string from the exam
            String questionsStr = exam.getQuestions();
            
            if (questionsStr == null || questionsStr.isEmpty()) {
                System.out.println("No questions found for exam: " + exam.getName());
                return results;
            }
            
            // Split the question IDs string
            String[] questionIdArr = questionsStr.split(",");
            
            // Process each question ID
            for (String questionIdStr : questionIdArr) {
                try {
                    // Trim any whitespace
                    questionIdStr = questionIdStr.trim();
                    if (questionIdStr.isEmpty()) continue;
                    
                    // Query the database with the question ID string directly
                    Question question = questionDB.queryByKey(questionIdStr);
                    
                    if (question == null) {
                        System.out.println("Question not found with ID: " + questionIdStr);
                        continue;
                    }
                    
                    // Get the student's record for this question and exam
                    List<comp3111.examsystem.entity.Record> records = recordDB.getAll();
                    comp3111.examsystem.entity.Record studentRecord = null;
                    
                    // Get the question ID as Long for comparison with record IDs
                    Long questionId = null;
                    try {
                        questionId = Long.parseLong(questionIdStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Could not parse question ID to Long: " + questionIdStr);
                        continue;
                    }
                    
                    for (comp3111.examsystem.entity.Record record : records) {
                        if (record.getStudentID() != null && record.getStudentID().equals(student.getId()) &&
                            record.getExamID() != null && record.getExamID().equals(exam.getId()) &&
                            record.getQuestionID() != null && record.getQuestionID().equals(questionId)) {
                            studentRecord = record;
                            break;
                        }
                    }
                    
                    // If no record found, skip this question
                    if (studentRecord == null) {
                        continue;
                    }
                    
                    // Create a quiz result for this question
                    double score = studentRecord.getScore() != null ? studentRecord.getScore() : 0;
                    double maxScore = question.getScore() != null ? question.getScore() : 1;
                    
                    String studentAnswer = studentRecord.getResponse() != null ? studentRecord.getResponse() : "No answer provided";
                    String correctAnswer = question.getAnswer() != null ? question.getAnswer() : "Answer not available";
                    
                    results.add(new StudentQuizResultController.QuizResult(
                        question.getQuestionText(),
                        studentAnswer,
                        correctAnswer,
                        score, // actual assigned score
                        maxScore, // actual max score
                        generateFeedback(score, maxScore)
                    ));
                } catch (Exception e) {
                    System.err.println("Error processing result for question ID: " + questionIdStr + ". Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading results for exam: " + exam.getName() + ". Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * Generates feedback based on the student's score for a question.
     *
     * @param score The student's score.
     * @param maxScore The maximum possible score.
     * @return Feedback message.
     */
    private String generateFeedback(double score, double maxScore) {
        if (score == 0) {
            return "Incorrect answer. Please review this question.";
        } else if (score < maxScore) {
            return "Partially correct. You received " + score + " out of " + maxScore + " points.";
        } else {
            return "Excellent! Your answer is correct.";
        }
    }
    
    /**
     * Handles the view statistics button click. Navigates to the grade statistics UI.
     *
     * @param event The action event.
     */
    @FXML
    public void handleViewStatistics(ActionEvent event) {
        try {
            // Load the grade statistics screen
            FXMLLoader loader = createFXMLLoader("/comp3111/examsystem/StudentGradeStatisticsUI.fxml");
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
     * Handles the logout button click. Navigates back to the login UI.
     *
     * @param event The action event.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            // Load the login screen (assuming there is a LoginUI.fxml)
            Parent root = createFXMLLoader("/comp3111/examsystem/LoginUI.fxml").load();
            
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
     * Handles the exit button click. Prompts the user to confirm before closing the application.
     *
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
     * Shows an alert dialog with the specified type, title, and content.
     *
     * @param type    The type of the alert.
     * @param title   The title of the alert.
     * @param content The content of the alert.
     */
    void showAlert(Alert.AlertType type, String title, String content) {
        if (showAlert != null) {
            showAlert.show(type, title, content);
        } else {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }

    /**
     * For testability: create an FXMLLoader for the given resource path (can be overridden in tests).
     *
     * @param resource The resource path to the FXML file.
     * @return The FXMLLoader for the resource.
     * @throws IOException If the resource cannot be loaded.
     */
    protected FXMLLoader createFXMLLoader(String resource) throws IOException {
        return new FXMLLoader(getClass().getResource(resource));
    }
}
