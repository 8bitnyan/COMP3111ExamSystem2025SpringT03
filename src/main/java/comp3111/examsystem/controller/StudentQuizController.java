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
import java.util.*;
import java.util.ResourceBundle;

/**
 * The controller for the student quiz page. Handles quiz navigation, answering, timing, and submission logic for students.
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
    @FXML private RadioButton option5;
    @FXML private TextArea shortAnswerField;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button submitButton;
    @FXML private ToggleGroup answerGroup;
    @FXML private Text maxScoreText;
    
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
    private boolean isSubmitting = false;
    
    // For testability: allow patching alert logic
    @FunctionalInterface
    interface AlertShower {
        void show(Alert.AlertType type, String title, String content);
    }
    AlertShower showAlert = null;
    
    @FunctionalInterface
    public interface ConfirmDialogShower {
        boolean show(String title, String header, String content);
    }
    private ConfirmDialogShower confirmDialogShower = null;
    
    /**
     * Initializes the student quiz page UI. Sets up listeners for MCQ and short answer fields.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup MCQ listeners
        option1.setToggleGroup(answerGroup);
        option2.setToggleGroup(answerGroup);
        option3.setToggleGroup(answerGroup);
        option4.setToggleGroup(answerGroup);
        option5.setToggleGroup(answerGroup);
        RadioButton[] btns = {option1, option2, option3, option4, option5};
        answerGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null && isCurrentQuestionMCQ()) {
                for (int i = 0; i < btns.length; i++) {
                    if (btns[i] == newToggle) {
                        studentAnswers.set(currentQuestionIndex, String.valueOf((char)('A' + i)));
                        break;
                    }
                }
                updateSidebarStyles();
            }
        });
        // Short answer listener
        shortAnswerField.textProperty().addListener((obs, oldText, newText) -> {
            if (!isCurrentQuestionMCQ()) {
                studentAnswers.set(currentQuestionIndex, newText);
                updateSidebarStyles();
            }
        });
    }
    
    /**
     * Sets up the quiz controller with the required data for the student and quiz.
     *
     * @param student   The student taking the quiz.
     * @param quizName  The name of the quiz.
     * @param questions The list of quiz questions.
     * @param timeLimit The time limit for the quiz in minutes.
     */
    public void preSetController(Student student, String quizName, List<QuizQuestion> questions, int timeLimit) {
        this.student = student;
        this.quizName = quizName;
        this.questions = questions;
        this.totalQuestions = questions.size();
        this.studentAnswers = new ArrayList<>(Collections.nCopies(totalQuestions, ""));
        quizNameText.setText(quizName);
        totalQuestionsText.setText(String.valueOf(totalQuestions));
        // Timer
        this.hours = timeLimit / 60;
        this.minutes = timeLimit % 60;
        this.seconds = 0;
        setupTimer();
        // Sidebar
        setupSidebar();
        // Load first question
        loadQuestion(0);
        // Window close handler
        Platform.runLater(() -> {
            Stage stage = (Stage) quizNameText.getScene().getWindow();
            if (stage != null) {
                stage.setOnCloseRequest(this::handleWindowClose);
            }
        });
    }
    
    /**
     * Sets up the question list for navigation in the sidebar.
     */
    private void setupSidebar() {
        questionListContainer.getChildren().clear();
        for (int i = 0; i < questions.size(); i++) {
            final int idx = i;
            Button btn = new Button("Question " + (i + 1));
            btn.setPrefWidth(180);
            btn.setOnAction(e -> loadQuestion(idx));
            btn.getStyleClass().add("question-button");
            questionListContainer.getChildren().add(btn);
        }
        updateSidebarStyles();
    }
    
    /**
     * Updates the question navigation buttons to indicate which questions have been answered and which is current.
     */
    private void updateSidebarStyles() {
        for (int i = 0; i < questionListContainer.getChildren().size(); i++) {
            Button btn = (Button) questionListContainer.getChildren().get(i);
            boolean answered = !studentAnswers.get(i).isEmpty();
            if (answered) {
                if (!btn.getStyleClass().contains("answered-question")) btn.getStyleClass().add("answered-question");
            } else {
                btn.getStyleClass().remove("answered-question");
            }
            if (i == currentQuestionIndex) {
                if (!btn.getStyleClass().contains("current-question")) btn.getStyleClass().add("current-question");
            } else {
                btn.getStyleClass().remove("current-question");
            }
        }
    }
    
    /**
     * Loads a question at the specified index and updates the UI.
     *
     * @param index The index of the question to load.
     */
    private void loadQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        saveCurrentAnswer();
        currentQuestionIndex = index;
        QuizQuestion q = questions.get(currentQuestionIndex);
        questionText.setText("Question " + (currentQuestionIndex + 1) + ": " + q.getQuestionText());
        maxScoreText.setText("Max Score: " + q.getMaxScore());
        if (q.isMultipleChoice()) {
            multipleChoiceContainer.setVisible(true);
            shortAnswerContainer.setVisible(false);
            List<String> opts = q.getOptions();
            RadioButton[] btns = {option1, option2, option3, option4, option5};
            answerGroup.selectToggle(null);
            for (int i = 0; i < btns.length; i++) {
                if (i < opts.size()) {
                    btns[i].setText(opts.get(i));
                    btns[i].setVisible(true);
                } else {
                    btns[i].setText("");
                    btns[i].setVisible(false);
                }
            }
            // Restore selection by letter
            String saved = studentAnswers.get(currentQuestionIndex);
            if (saved != null && saved.length() == 1) {
                int idx = saved.charAt(0) - 'A';
                if (idx >= 0 && idx < btns.length && btns[idx].isVisible()) {
                    btns[idx].setSelected(true);
                }
            }
        } else {
            multipleChoiceContainer.setVisible(false);
            shortAnswerContainer.setVisible(true);
            shortAnswerField.setText(studentAnswers.get(currentQuestionIndex));
        }
        previousButton.setDisable(currentQuestionIndex == 0);
        nextButton.setDisable(currentQuestionIndex == questions.size() - 1);
        previousButton.setVisible(currentQuestionIndex != 0);
        nextButton.setVisible(currentQuestionIndex != questions.size() - 1);
        updateSidebarStyles();
    }
    
    /**
     * Saves the current answer before moving to another question.
     */
    private void saveCurrentAnswer() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) return;
        QuizQuestion q = questions.get(currentQuestionIndex);
        if (q.isMultipleChoice()) {
            RadioButton selected = (RadioButton) answerGroup.getSelectedToggle();
            RadioButton[] btns = {option1, option2, option3, option4, option5};
            String letter = "";
            for (int i = 0; i < btns.length; i++) {
                if (btns[i] == selected) {
                    letter = String.valueOf((char)('A' + i));
                    break;
                }
            }
            studentAnswers.set(currentQuestionIndex, letter);
        } else {
            studentAnswers.set(currentQuestionIndex, shortAnswerField.getText());
        }
    }
    
    /**
     * Checks if the current question is a multiple choice question.
     *
     * @return True if the current question is multiple choice, false otherwise.
     */
    private boolean isCurrentQuestionMCQ() {
        return questions != null && currentQuestionIndex >= 0 && currentQuestionIndex < questions.size() && questions.get(currentQuestionIndex).isMultipleChoice();
    }
    
    /**
     * Sets up the timer for the quiz and starts countdown.
     */
    private void setupTimer() {
        updateTimerDisplay();
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
                timeline.stop();
                handleSubmit(null);
            }
            updateTimerDisplay();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    /**
     * Updates the timer display in the UI.
     */
    private void updateTimerDisplay() {
        timerText.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        if (hours == 0 && minutes < 5) timerText.setStyle("-fx-fill: red;");
        else timerText.setStyle("-fx-fill: black;");
    }
    
    /**
     * Handles the submit button action. Prompts for confirmation and submits the quiz if confirmed.
     *
     * @param event The action event.
     */
    @FXML
    public void handleSubmit(ActionEvent event) {
        saveCurrentAnswer();
        if (isSubmitting) return;
        isSubmitting = true;
        if (timeline != null) timeline.stop();
        if (showAlert != null) {
            showAlert.show(Alert.AlertType.CONFIRMATION, "Submit Quiz", "Are you sure you want to submit this quiz?");
            // In tests, the handler should call submitQuiz() or reset isSubmitting as needed
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Submit Quiz");
            confirm.setHeaderText("Submit Quiz");
            confirm.setContentText("Are you sure you want to submit this quiz?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                submitQuiz();
            } else {
                isSubmitting = false;
                if (timeline != null) timeline.play();
            }
        }
    }
    
    /**
     * Handles the window close event. Prompts for confirmation and submits the quiz if confirmed.
     *
     * @param event The window event.
     */
    private void handleWindowClose(WindowEvent event) {
        saveCurrentAnswer();
        if (isSubmitting) return;
        if (timeline != null) timeline.stop();
        boolean confirmed;
        if (confirmDialogShower != null) {
            confirmed = confirmDialogShower.show(
                "Submit Quiz",
                "Submit Quiz",
                "Closing the window will submit your quiz. Are you sure you want to continue?"
            );
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Submit Quiz");
            confirm.setHeaderText("Submit Quiz");
            confirm.setContentText("Closing the window will submit your quiz. Are you sure you want to continue?");
            Optional<ButtonType> result = confirm.showAndWait();
            confirmed = result.isPresent() && result.get() == ButtonType.OK;
        }
        if (confirmed) {
            submitQuiz();
        } else {
            if (timeline != null) timeline.play();
            event.consume();
        }
    }
    
    /**
     * Submits the quiz, saves results, and shows the completion message.
     */
    private void submitQuiz() {
        saveCurrentAnswer();
        saveQuizResultsToDatabase(student, quizName, questions, studentAnswers);
        if (showAlert != null) {
            showAlert.show(Alert.AlertType.INFORMATION, "Quiz Completed", "You have completed the quiz: " + quizName);
            // In tests, the handler can simulate the dialog flow
        } else {
            Alert completion = new Alert(Alert.AlertType.INFORMATION);
            completion.setTitle("Quiz Completed");
            completion.setHeaderText("Quiz Submitted Successfully");
            completion.setContentText("You have completed the quiz: " + quizName);
            completion.showAndWait().ifPresent(btn -> {
                Alert results = new Alert(Alert.AlertType.INFORMATION);
                results.setTitle("Quiz Results");
                results.setHeaderText("Results Available After Grading");
                results.setContentText("You will be able to see your quiz results after your teacher grades this quiz.");
                results.showAndWait().ifPresent(finalBtn -> returnToMainPage());
            });
        }
    }
    
    /**
     * Returns to the student main page after quiz submission.
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Saves the quiz results to a database or persistent storage. This is a placeholder method.
     *
     * @param student        The student who took the quiz.
     * @param quizName       The name of the quiz.
     * @param questions      The list of questions in the quiz.
     * @param studentAnswers The list of answers provided by the student.
     */
    private void saveQuizResultsToDatabase(Student student, String quizName, List<QuizQuestion> questions, List<String> studentAnswers) {
        try {
            Database<Exam> examDB = new Database<>(Exam.class);
            List<Exam> exams = examDB.getAllEnabled();
            Exam exam = null;
            for (Exam e : exams) {
                if (quizName.equals(e.getName())) {
                    exam = e;
                    break;
                }
            }
            if (exam == null) return;
            String questionsStr = exam.getQuestions();
            if (questionsStr == null || questionsStr.isEmpty()) return;
            String[] questionIdArr = questionsStr.split(",");
            if (questionIdArr.length != questions.size()) return;
            Database<comp3111.examsystem.entity.Record> recordDB = new Database<>(comp3111.examsystem.entity.Record.class);
            // Delete any existing records for this student and exam (enforces one attempt)
            List<comp3111.examsystem.entity.Record> existingRecords = recordDB.getAll();
            for (comp3111.examsystem.entity.Record record : existingRecords) {
                if (record.getStudentID() != null && record.getStudentID().equals(student.getId()) &&
                    record.getExamID() != null && record.getExamID().equals(exam.getId())) {
                    recordDB.delByKey(record.getId().toString());
                }
            }
            // Create new records for this attempt
            for (int i = 0; i < questions.size() && i < questionIdArr.length && i < studentAnswers.size(); i++) {
                try {
                    String questionIdStr = questionIdArr[i].trim();
                    if (questionIdStr.isEmpty()) continue;
                    Long questionId = Long.parseLong(questionIdStr);
                    QuizQuestion quizQ = questions.get(i);
                    comp3111.examsystem.entity.Record record = new comp3111.examsystem.entity.Record();
                    record.setStudent(student.getId());
                    record.setExamID(exam.getId());
                    record.setQuestionID(questionId);
                    record.setResponse(studentAnswers.get(i)); // now a letter for MCQ
                    // Auto-grade MCQ
                    int score = 0;
                    if (quizQ.isMultipleChoice()) {
                        String studentAns = studentAnswers.get(i);
                        String correctAns = quizQ.getCorrectAnswer();
                        if (studentAns != null && correctAns != null && studentAns.trim().equalsIgnoreCase(correctAns.trim())) {
                            score = quizQ.getMaxScore();
                        }
                    }
                    record.setScore(score); // Short answer: 0 by default
                    recordDB.add(record);
                } catch (NumberFormatException e) {
                    // skip
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the next button action. Loads the next question.
     *
     * @param event The action event.
     */
    @FXML
    public void handleNext(ActionEvent event) {
        loadQuestion(currentQuestionIndex + 1);
    }
    
    /**
     * Handles the previous button action. Loads the previous question.
     *
     * @param event The action event.
     */
    @FXML
    public void handlePrevious(ActionEvent event) {
        loadQuestion(currentQuestionIndex - 1);
    }
    
    /**
     * QuizQuestion class to represent a question in the quiz.
     */
    public static class QuizQuestion {
        private String questionText;
        private boolean isMultipleChoice;
        private List<String> options;
        private int maxScore;
        private String correctAnswer;
        
        /**
         * Constructor for a multiple choice question.
         *
         * @param questionText  The text of the question.
         * @param options       The list of options for the question.
         * @param maxScore      The max score for the question.
         * @param correctAnswer The correct answer for the question.
         */
        public QuizQuestion(String questionText, List<String> options, int maxScore, String correctAnswer) {
            this.questionText = questionText;
            this.isMultipleChoice = true;
            this.options = options;
            this.maxScore = maxScore;
            this.correctAnswer = correctAnswer;
        }
        
        /**
         * Constructor for a short answer question.
         *
         * @param questionText The text of the question.
         * @param maxScore     The max score for the question.
         */
        public QuizQuestion(String questionText, int maxScore) {
            this.questionText = questionText;
            this.isMultipleChoice = false;
            this.options = new ArrayList<>();
            this.maxScore = maxScore;
            this.correctAnswer = null;
        }
        
        /**
         * Gets the question text.
         *
         * @return The question text.
         */
        public String getQuestionText() {
            return questionText;
        }
        
        /**
         * Checks if the question is a multiple choice question.
         *
         * @return True if the question is multiple choice, false otherwise.
         */
        public boolean isMultipleChoice() {
            return isMultipleChoice;
        }
        
        /**
         * Gets the options for the question.
         *
         * @return The list of options.
         */
        public List<String> getOptions() {
            return options;
        }
        
        /**
         * Gets the max score for the question.
         *
         * @return The max score for the question.
         */
        public int getMaxScore() {
            return maxScore;
        }
        
        /**
         * Gets the correct answer for the question.
         *
         * @return The correct answer for the question.
         */
        public String getCorrectAnswer() {
            return correctAnswer;
        }
    }

    /**
     * Gets the max score for the current question.
     *
     * @return The max score for the current question, or 0 if not available.
     */
    public int getCurrentQuestionMaxScore() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex).getMaxScore();
        }
        return 0;
    }

    /**
     * Gets the correct answer for the current question (for MCQ).
     *
     * @return The correct answer for the current question, or null if not available.
     */
    public String getCurrentQuestionCorrectAnswer() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex).getCorrectAnswer();
        }
        return null;
    }
} 