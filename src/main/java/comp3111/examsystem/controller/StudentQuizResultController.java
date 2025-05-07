package comp3111.examsystem.controller;

import comp3111.examsystem.entity.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The controller for student quiz result page.
 */
public class StudentQuizResultController implements Initializable {
    // UI Components
    @FXML private Text quizNameText;
    @FXML private Text totalQuestionsText;
    @FXML private Text scoreText;
    @FXML private VBox questionListContainer;
    @FXML private Label questionNumberLabel;
    @FXML private Label questionLabel;
    @FXML private Label studentAnswerLabel;
    @FXML private Label correctAnswerLabel;
    @FXML private Label questionScoreLabel;
    @FXML private TextArea feedbackTextArea;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button backButton;
    
    // Data
    private Student student;
    private String quizName;
    private List<QuizResult> quizResults;
    private int currentQuestionIndex = 0;
    private int totalQuestions = 0;
    private double totalScore = 0;
    
    // For testability: allow patching alert logic
    @FunctionalInterface
    interface AlertShower {
        void show(Alert.AlertType type, String title, String content);
    }
    AlertShower showAlert = null;
    
    /**
     * Initializes the student quiz result page UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize UI elements that don't require data
    }
    
    /**
     * Sets up the controller with the required data.
     * @param student The student viewing the results.
     * @param quizName The name of the quiz.
     * @param quizResults The list of quiz question results.
     */
    public void preSetController(Student student, String quizName, List<QuizResult> quizResults) {
        this.student = student;
        this.quizName = quizName;
        this.quizResults = quizResults;
        this.totalQuestions = quizResults.size();
        // Calculate total score and max score
        calculateTotalScore();
        double totalMaxScore = 0;
        for (QuizResult result : quizResults) {
            totalMaxScore += result.getMaxScore();
        }
        // Set quiz name, total questions, and score
        quizNameText.setText(quizName);
        totalQuestionsText.setText(String.valueOf(totalQuestions));
        double percent = (totalMaxScore > 0) ? (totalScore / totalMaxScore) * 100.0 : 0.0;
        scoreText.setText(String.format("%.1f%%", percent));
        // Create question navigation buttons
        setupQuestionList();
        // Load the first question result
        loadQuestionResult(0);
    }
    
    /**
     * Calculates the total score for the quiz.
     */
    private void calculateTotalScore() {
        totalScore = 0;
        for (QuizResult result : quizResults) {
            totalScore += result.getScore();
        }
    }
    
    /**
     * Sets up the question list for navigation.
     */
    private void setupQuestionList() {
        questionListContainer.getChildren().clear();
        for (int i = 0; i < quizResults.size(); i++) {
            final int questionIndex = i;
            Button questionButton = new Button("Question " + (i + 1));
            questionButton.setPrefWidth(180);
            questionButton.setOnAction(e -> loadQuestionResult(questionIndex));
            questionListContainer.getChildren().add(questionButton);
        }
    }
    
    /**
     * Loads a question result at the specified index.
     * @param index The index of the question result to load.
     */
    private void loadQuestionResult(int index) {
        if (index < 0 || index >= quizResults.size()) {
            return;
        }
        
        // Update current question index
        currentQuestionIndex = index;
        
        // Get the current question result
        QuizResult result = quizResults.get(currentQuestionIndex);
        
        // Update UI with result details
        questionNumberLabel.setText(String.valueOf(currentQuestionIndex + 1));
        questionLabel.setText(result.getQuestionText());
        studentAnswerLabel.setText(result.getStudentAnswer());
        correctAnswerLabel.setText(result.getCorrectAnswer());
        questionScoreLabel.setText(result.getScore() + "/" + result.getMaxScore());
        feedbackTextArea.setText(result.getFeedback());
        
        // Update navigation buttons
        updateNavigationButtons();
    }
    
    /**
     * Updates the navigation buttons based on the current question index.
     */
    private void updateNavigationButtons() {
        previousButton.setDisable(currentQuestionIndex == 0);
        nextButton.setDisable(currentQuestionIndex == quizResults.size() - 1);
    }
    
    /**
     * Handles the next button action.
     * @param event The action event.
     */
    @FXML
    public void handleNext(ActionEvent event) {
        if (currentQuestionIndex < quizResults.size() - 1) {
            loadQuestionResult(currentQuestionIndex + 1);
        }
    }
    
    /**
     * Handles the previous button action.
     * @param event The action event.
     */
    @FXML
    public void handlePrevious(ActionEvent event) {
        if (currentQuestionIndex > 0) {
            loadQuestionResult(currentQuestionIndex - 1);
        }
    }
    
    /**
     * Handles the back button action.
     * @param event The action event.
     */
    @FXML
    public void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comp3111/examsystem/StudentMainUI.fxml"));
            Parent root = loader.load();
            StudentMainController controller = loader.getController();
            controller.preSetController(student);
            // Show the student main page
            if (backButton.getScene() == null || backButton.getScene().getWindow() == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot return to main page: No window attached.");
                return;
            }
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to main page: " + e.getMessage());
        }
    }
    
    /**
     * Shows an alert dialog.
     * @param type The type of the alert.
     * @param title The title of the alert.
     * @param content The content of the alert.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
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
     * QuizResult class to represent a result for a quiz question.
     */
    public static class QuizResult {
        private String questionText;
        private String studentAnswer;
        private String correctAnswer;
        private double score;
        private double maxScore;
        private String feedback;
        
        /**
         * Constructor for a quiz result.
         * @param questionText The text of the question.
         * @param studentAnswer The student's answer.
         * @param correctAnswer The correct answer.
         * @param score The score earned for the question.
         * @param maxScore The maximum possible score for the question.
         * @param feedback The teacher's feedback for the answer.
         */
        public QuizResult(String questionText, String studentAnswer, String correctAnswer, 
                         double score, double maxScore, String feedback) {
            this.questionText = questionText;
            this.studentAnswer = studentAnswer;
            this.correctAnswer = correctAnswer;
            this.score = score;
            this.maxScore = maxScore;
            this.feedback = feedback;
        }
        
        /**
         * Gets the question text.
         * @return The question text.
         */
        public String getQuestionText() {
            return questionText;
        }
        
        /**
         * Gets the student's answer.
         * @return The student's answer.
         */
        public String getStudentAnswer() {
            return studentAnswer;
        }
        
        /**
         * Gets the correct answer.
         * @return The correct answer.
         */
        public String getCorrectAnswer() {
            return correctAnswer;
        }
        
        /**
         * Gets the score earned for the question.
         * @return The score.
         */
        public double getScore() {
            return score;
        }
        
        /**
         * Gets the maximum possible score for the question.
         * @return The maximum possible score.
         */
        public double getMaxScore() {
            return maxScore;
        }
        
        /**
         * Gets the teacher's feedback for the answer.
         * @return The feedback.
         */
        public String getFeedback() {
            return feedback;
        }
    }
} 