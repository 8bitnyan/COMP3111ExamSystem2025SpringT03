package comp3111.examsystem.controller;

import comp3111.examsystem.entity.*;
import comp3111.examsystem.tools.Database;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * The controller for student quiz page.
 */
public class StudentQuizController implements Initializable {
    // UI Components
    @FXML private Text quizNameText;
    @FXML private Text totalQuestionsText;
    @FXML private Text timerText;
    @FXML private VBox questionListContainer;
    @FXML private Text questionText;
    @FXML private VBox multipleChoiceContainer;
    @FXML private VBox shortAnswerContainer;
    @FXML private RadioButton option1;
    @FXML private RadioButton option2;
    @FXML private RadioButton option3;
    @FXML private RadioButton option4;
    @FXML private TextArea shortAnswerField;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button submitButton;
    @FXML private ToggleGroup answerGroup;
    
    // Data
    private Student student;
    private String quizName;
    private List<QuizQuestion> questions;
    private List<String> studentAnswers;
    private int currentQuestionIndex = 0;
    private int totalQuestions = 0;
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private Timeline timeline;
    
    /**
     * Initializes the student quiz page UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup timer
        setupTimer();
        
        // We'll set up the window close handler in preSetController after UI is fully loaded
    }
    
    /**
     * Sets up the quiz controller with the required data.
     * @param student The student taking the quiz.
     * @param quizName The name of the quiz.
     * @param questions The list of quiz questions.
     * @param timeLimit The time limit for the quiz in minutes.
     */
    public void preSetController(Student student, String quizName, List<QuizQuestion> questions, int timeLimit) {
        this.student = student;
        this.quizName = quizName;
        this.questions = questions;
        this.totalQuestions = questions.size();
        this.studentAnswers = new ArrayList<>(totalQuestions);
        
        // Initialize student answers
        for (int i = 0; i < totalQuestions; i++) {
            studentAnswers.add("");
        }
        
        // Set quiz name and total questions
        quizNameText.setText(quizName);
        totalQuestionsText.setText(String.valueOf(totalQuestions));
        
        // Setup timer with the time limit
        if (timeLimit > 0) {
            this.hours = timeLimit / 60;
            this.minutes = timeLimit % 60;
            this.seconds = 0;
            updateTimerDisplay();
            
            // Start the timer
            if (timeline != null) {
                timeline.play();
            }
        }
        
        // Create question navigation buttons
        setupQuestionList();
        
        // Load the first question
        loadQuestion(0);
        
        // Apply initial styles to question buttons
        updateQuestionButtonStyles();
        
        // Setup window close handler after UI is fully loaded
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) questionText.getScene().getWindow();
                if (stage != null) {
                    stage.setOnCloseRequest(this::handleWindowClose);
                }
            } catch (Exception e) {
                System.err.println("Failed to set up window close handler: " + e.getMessage());
            }
        });
    }
    
    /**
     * Sets up the question list for navigation.
     */
    private void setupQuestionList() {
        questionListContainer.getChildren().clear();
        for (int i = 0; i < questions.size(); i++) {
            final int questionIndex = i;
            Button questionButton = new Button("Question " + (i + 1));
            questionButton.setPrefWidth(180);
            questionButton.setOnAction(e -> loadQuestion(questionIndex));
            
            // Add a style class to the button
            questionButton.getStyleClass().add("question-button");
            
            questionListContainer.getChildren().add(questionButton);
        }
    }
    
    /**
     * Updates the question navigation buttons to indicate which questions have been answered.
     */
    private void updateQuestionButtonStyles() {
        for (int i = 0; i < questionListContainer.getChildren().size(); i++) {
            Button button = (Button) questionListContainer.getChildren().get(i);
            
            // Check if this question has been answered
            boolean isAnswered = i < studentAnswers.size() && !studentAnswers.get(i).isEmpty();
            
            // Add or remove the "answered" style class
            if (isAnswered) {
                if (!button.getStyleClass().contains("answered-question")) {
                    button.getStyleClass().add("answered-question");
                }
            } else {
                button.getStyleClass().remove("answered-question");
            }
            
            // Highlight current question
            if (i == currentQuestionIndex) {
                if (!button.getStyleClass().contains("current-question")) {
                    button.getStyleClass().add("current-question");
                }
            } else {
                button.getStyleClass().remove("current-question");
            }
        }
    }
    
    /**
     * Loads a question at the specified index.
     * @param index The index of the question to load.
     */
    private void loadQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            return;
        }
        
        // Save current answer before loading new question
        saveCurrentAnswer();
        
        // Update current question index
        currentQuestionIndex = index;
        
        // Get the current question
        QuizQuestion question = questions.get(currentQuestionIndex);
        
        // Set question text
        questionText.setText("Question " + (currentQuestionIndex + 1) + ": " + question.getQuestionText());
        
        // Update UI based on question type
        if (question.isMultipleChoice()) {
            multipleChoiceContainer.setVisible(true);
            shortAnswerContainer.setVisible(false);
            
            // Set multiple choice options
            List<String> options = question.getOptions();
            
            // Clear all options first
            option1.setText("");
            option2.setText("");
            option3.setText("");
            option4.setText("");
            
            // Set options based on available count
            int optionsCount = options.size();
            if (optionsCount > 0) option1.setText(options.get(0));
            if (optionsCount > 1) option2.setText(options.get(1));
            if (optionsCount > 2) option3.setText(options.get(2));
            if (optionsCount > 3) option4.setText(options.get(3));
            
            // Set visibility based on available options
            option1.setVisible(optionsCount > 0);
            option2.setVisible(optionsCount > 1);
            option3.setVisible(optionsCount > 2);
            option4.setVisible(optionsCount > 3);
            
            // Set selected option if an answer exists
            String savedAnswer = studentAnswers.get(currentQuestionIndex);
            if (!savedAnswer.isEmpty()) {
                if (savedAnswer.equals(option1.getText()) && !option1.getText().isEmpty()) {
                    option1.setSelected(true);
                } else if (savedAnswer.equals(option2.getText()) && !option2.getText().isEmpty()) {
                    option2.setSelected(true);
                } else if (savedAnswer.equals(option3.getText()) && !option3.getText().isEmpty()) {
                    option3.setSelected(true);
                } else if (savedAnswer.equals(option4.getText()) && !option4.getText().isEmpty()) {
                    option4.setSelected(true);
                }
            } else {
                answerGroup.selectToggle(null);
            }
        } else {
            multipleChoiceContainer.setVisible(false);
            shortAnswerContainer.setVisible(true);
            
            // Set short answer text if an answer exists
            shortAnswerField.setText(studentAnswers.get(currentQuestionIndex));
        }
        
        // Update navigation buttons
        updateNavigationButtons();
        
        // Update question button styles
        updateQuestionButtonStyles();
    }
    
    /**
     * Saves the current answer before moving to another question.
     */
    private void saveCurrentAnswer() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            if (questions.get(currentQuestionIndex).isMultipleChoice()) {
                RadioButton selectedButton = (RadioButton) answerGroup.getSelectedToggle();
                if (selectedButton != null) {
                    studentAnswers.set(currentQuestionIndex, selectedButton.getText());
                } else {
                    studentAnswers.set(currentQuestionIndex, "");
                }
            } else {
                studentAnswers.set(currentQuestionIndex, shortAnswerField.getText());
            }
            
            // Update the question button styles
            updateQuestionButtonStyles();
        }
    }
    
    /**
     * Updates the navigation buttons based on the current question index.
     */
    private void updateNavigationButtons() {
        previousButton.setDisable(currentQuestionIndex == 0);
        nextButton.setDisable(currentQuestionIndex == questions.size() - 1);
    }
    
    /**
     * Sets up the timer for the quiz.
     */
    private void setupTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (seconds > 0) {
                seconds--;
            } else if (minutes > 0) {
                minutes--;
                seconds = 59;
            } else if (hours > 0) {
                hours--;
                minutes = 59;
                seconds = 59;
            } else {
                // Time's up - submit the quiz automatically
                timeline.stop();
                handleSubmit(null);
            }
            updateTimerDisplay();
        }));
        
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    /**
     * Updates the timer display.
     */
    private void updateTimerDisplay() {
        timerText.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        
        // Change timer color to red when less than 5 minutes remain
        if (hours == 0 && minutes < 5) {
            timerText.setStyle("-fx-fill: red;");
        } else {
            timerText.setStyle("-fx-fill: black;");
        }
    }
    
    /**
     * Handles the submit button action.
     * @param event The action event.
     */
    @FXML
    public void handleSubmit(ActionEvent event) {
        // Save current answer before submitting
        saveCurrentAnswer();
        
        // Stop the timer
        if (timeline != null) {
            timeline.stop();
        }
        
        // Show confirmation dialog
        Alert confirmSubmit = new Alert(Alert.AlertType.CONFIRMATION);
        confirmSubmit.setTitle("Submit Quiz");
        confirmSubmit.setHeaderText("Submit Quiz");
        confirmSubmit.setContentText("Are you sure you want to submit this quiz?");
        
        Optional<ButtonType> result = confirmSubmit.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Submit the quiz
            submitQuiz();
        } else {
            // Resume the timer if submission was cancelled
            if (timeline != null) {
                timeline.play();
            }
        }
    }
    
    /**
     * Handles the window close event.
     * @param event The window event.
     */
    private void handleWindowClose(WindowEvent event) {
        // Stop the timer
        if (timeline != null) {
            timeline.stop();
        }
        
        // Show confirmation dialog
        Alert confirmClose = new Alert(Alert.AlertType.CONFIRMATION);
        confirmClose.setTitle("Submit Quiz");
        confirmClose.setHeaderText("Submit Quiz");
        confirmClose.setContentText("Closing the window will submit your quiz. Are you sure you want to continue?");
        
        Optional<ButtonType> result = confirmClose.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Save current answer and submit the quiz
            saveCurrentAnswer();
            saveQuizResultsToDatabase(student, quizName, questions, studentAnswers);
            
            // The window will close automatically since we don't consume the event
        } else {
            // Resume the timer and consume the event to prevent window from closing
            if (timeline != null) {
                timeline.play();
            }
            event.consume();
        }
    }
    
    /**
     * Submits the quiz and shows the completion message.
     */
    private void submitQuiz() {
        // Save all answers one final time
        saveCurrentAnswer();
        
        // In a real application, you would save the quiz results to a database here
        saveQuizResultsToDatabase(student, quizName, questions, studentAnswers);
        
        // Show completion message
        Alert completionMessage = new Alert(Alert.AlertType.INFORMATION);
        completionMessage.setTitle("Quiz Completed");
        completionMessage.setHeaderText("Quiz Submitted Successfully");
        completionMessage.setContentText("You have completed the quiz: " + quizName);
        
        completionMessage.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Show final message about viewing results
                Alert resultsMessage = new Alert(Alert.AlertType.INFORMATION);
                resultsMessage.setTitle("Quiz Results");
                resultsMessage.setHeaderText("Results Available After Grading");
                resultsMessage.setContentText("You will be able to see your quiz results after your teacher grades this quiz.");
                
                resultsMessage.showAndWait().ifPresent(finalButton -> {
                    // Return to student main page
                    returnToMainPage();
                });
            }
        });
    }
    
    /**
     * Returns to the student main page.
     */
    private void returnToMainPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comp3111/examsystem/StudentMainUI.fxml"));
            Parent root = loader.load();
            StudentMainController controller = loader.getController();
            controller.preSetController(student);
            Scene currentScene = quizNameText.getScene();
            if (currentScene != null && currentScene.getWindow() != null) {
                Stage stage = (Stage) currentScene.getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } else {
                // In test or headless mode, just skip window operations
                System.err.println("No scene/window available for navigation (likely in test mode).");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Saves the quiz results to a database or persistent storage.
     * This is a placeholder method that would be implemented in a real application.
     * 
     * @param student The student who took the quiz
     * @param quizName The name of the quiz
     * @param questions The list of questions in the quiz
     * @param studentAnswers The list of answers provided by the student
     */
    private void saveQuizResultsToDatabase(Student student, String quizName, List<QuizQuestion> questions, List<String> studentAnswers) {
        try {
            // Get the exam from the database
            Database<Exam> examDB = new Database<>(Exam.class);
            List<Exam> exams = examDB.getAllEnabled();
            Exam exam = null;
            
            // Find the exam with the matching name
            for (Exam e : exams) {
                if (quizName.equals(e.getName())) {
                    exam = e;
                    break;
                }
            }
            
            if (exam == null) {
                System.err.println("Error: Could not find exam with name: " + quizName);
                return;
            }
            
            // Get the questions string from the exam
            String questionsStr = exam.getQuestions();
            
            if (questionsStr == null || questionsStr.isEmpty()) {
                System.err.println("Error: No questions found for exam: " + quizName);
                return;
            }
            
            // Split the question IDs string
            String[] questionIdArr = questionsStr.split(",");
            
            if (questionIdArr.length != questions.size()) {
                System.err.println("Error: Question count mismatch. Database: " + questionIdArr.length + ", Actual: " + questions.size());
                return;
            }
            
            // Create records for each question
            Database<comp3111.examsystem.entity.Record> recordDB = new Database<>(comp3111.examsystem.entity.Record.class);
            
            // Delete any existing records for this student and exam (in case they're retaking the quiz)
            List<comp3111.examsystem.entity.Record> existingRecords = recordDB.getAll();
            for (comp3111.examsystem.entity.Record record : existingRecords) {
                if (record.getStudentID() != null && record.getStudentID().equals(student.getId()) &&
                    record.getExamID() != null && record.getExamID().equals(exam.getId())) {
                    recordDB.delByKey(record.getId().toString());
                }
            }
            
            // Create new records
            for (int i = 0; i < questions.size() && i < questionIdArr.length && i < studentAnswers.size(); i++) {
                try {
                    String questionIdStr = questionIdArr[i].trim();
                    if (questionIdStr.isEmpty()) continue;
                    
                    // Parse the question ID string to Long
                    Long questionId = Long.parseLong(questionIdStr);
                    
                    comp3111.examsystem.entity.Record record = new comp3111.examsystem.entity.Record();
                    record.setStudent(student.getId());
                    record.setExamID(exam.getId());
                    record.setQuestionID(questionId);
                    record.setResponse(studentAnswers.get(i));
                    
                    // For now, set a default score of 0 - teacher will grade later
                    record.setScore(0);
                    
                    // Save the record to the database
                    recordDB.add(record);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing question ID: " + questionIdArr[i] + ". Skipping this question.");
                }
            }
            
            // Also save time spent on the quiz (for future implementations)
            Database<TimeSpent> timeSpentDB = new Database<>(TimeSpent.class);
            TimeSpent timeSpent = new TimeSpent();
            timeSpent.setStudentId(student.getId());
            timeSpent.setExamId(exam.getId());
            
            // Calculate time spent in minutes (initial time limit minus remaining time)
            int initialTimeLimit = exam.getDuration() != null ? exam.getDuration() : 60;
            int remainingMinutes = hours * 60 + minutes;
            int timeSpentMinutes = initialTimeLimit - remainingMinutes;
            
            // Make sure time spent is positive
            if (timeSpentMinutes < 0) {
                timeSpentMinutes = 0;
            }
            
            timeSpent.setTimeSpent(timeSpentMinutes);
            timeSpentDB.add(timeSpent);
            
            System.out.println("Successfully saved quiz results for student: " + student.getName());
        } catch (Exception e) {
            System.err.println("Error saving quiz results: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the next button action.
     * @param event The action event.
     */
    @FXML
    public void handleNext(ActionEvent event) {
        if (currentQuestionIndex < questions.size() - 1) {
            loadQuestion(currentQuestionIndex + 1);
        }
    }
    
    /**
     * Handles the previous button action.
     * @param event The action event.
     */
    @FXML
    public void handlePrevious(ActionEvent event) {
        if (currentQuestionIndex > 0) {
            loadQuestion(currentQuestionIndex - 1);
        }
    }
    
    /**
     * QuizQuestion class to represent a question in the quiz.
     */
    public static class QuizQuestion {
        private String questionText;
        private boolean isMultipleChoice;
        private List<String> options;
        
        /**
         * Constructor for a multiple choice question.
         * @param questionText The text of the question.
         * @param options The list of options for the question.
         */
        public QuizQuestion(String questionText, List<String> options) {
            this.questionText = questionText;
            this.isMultipleChoice = true;
            this.options = options;
        }
        
        /**
         * Constructor for a short answer question.
         * @param questionText The text of the question.
         */
        public QuizQuestion(String questionText) {
            this.questionText = questionText;
            this.isMultipleChoice = false;
            this.options = new ArrayList<>();
        }
        
        /**
         * Gets the question text.
         * @return The question text.
         */
        public String getQuestionText() {
            return questionText;
        }
        
        /**
         * Checks if the question is a multiple choice question.
         * @return True if the question is multiple choice, false otherwise.
         */
        public boolean isMultipleChoice() {
            return isMultipleChoice;
        }
        
        /**
         * Gets the options for the question.
         * @return The list of options.
         */
        public List<String> getOptions() {
            return options;
        }
    }
} 