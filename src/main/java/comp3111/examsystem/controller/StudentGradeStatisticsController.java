package comp3111.examsystem.controller;

import comp3111.examsystem.entity.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller for the student grade statistics page. Handles UI logic for displaying and filtering student quiz grades, statistics, and charts.
 */
public class StudentGradeStatisticsController implements Initializable {
    
    // UI Components - Filtering
    @FXML private ComboBox<String> courseComboBox;
    @FXML private TextField minScoreField;
    @FXML private TextField maxScoreField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button filterButton;
    @FXML private Button resetButton;
    @FXML private Button refreshButton;
    
    // UI Components - Quiz List
    @FXML private ListView<String> quizListView;
    
    // UI Components - Chart
    @FXML private BarChart<String, Number> gradeChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    
    // UI Components - Statistics
    @FXML private Label averageScoreLabel;
    @FXML private Label highestScoreLabel;
    @FXML private Label lowestScoreLabel;
    
    // UI Components - Navigation
    @FXML private Button backButton;
    @FXML private Button closeButton;
    
    // Data
    private Student student;
    private List<QuizGrade> allQuizGrades;
    private List<QuizGrade> filteredQuizGrades;
    
    // Add database fields for fetching records and exams
    private comp3111.examsystem.tools.Database<comp3111.examsystem.entity.Record> recordDB = new comp3111.examsystem.tools.Database<>(comp3111.examsystem.entity.Record.class);
    private comp3111.examsystem.tools.Database<Exam> examDB = new comp3111.examsystem.tools.Database<>(Exam.class);
    private comp3111.examsystem.tools.Database<Course> courseDB = new comp3111.examsystem.tools.Database<>(Course.class);
    
    // For testability: allow patching alert logic
    @FunctionalInterface
    interface AlertShower {
        void show(Alert.AlertType type, String title, String content);
    }
    AlertShower showAlert = null;
    
    /**
     * Initializes the student grade statistics page UI. Sets up input validation, loads courses, and applies the initial filter.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize filter components
        minScoreField.setPromptText("0");
        maxScoreField.setPromptText("100");
        
        // Configure listeners for input validation
        minScoreField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                minScoreField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        maxScoreField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                maxScoreField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        // Initialize empty data
        allQuizGrades = new ArrayList<>();
        filteredQuizGrades = new ArrayList<>();
        loadCourses();
        applyFilter();
    }
    
    /**
     * Sets up the controller with the required data for the given student.
     *
     * @param student The student viewing the grade statistics.
     */
    public void preSetController(Student student) {
        this.student = student;
        // Fetch all records for this student
        List<comp3111.examsystem.entity.Record> studentRecords = recordDB.queryByField("studentID", String.valueOf(student.getId()));
        // Group records by examID
        Map<Long, List<comp3111.examsystem.entity.Record>> recordsByExam = studentRecords.stream()
                .filter(r -> r.getExamID() != null)
                .collect(Collectors.groupingBy(comp3111.examsystem.entity.Record::getExamID));
        List<QuizGrade> quizGrades = new ArrayList<>();
        for (Map.Entry<Long, List<comp3111.examsystem.entity.Record>> entry : recordsByExam.entrySet()) {
            Long examId = entry.getKey();
            List<comp3111.examsystem.entity.Record> records = entry.getValue();
            Exam exam = examDB.queryByKey(String.valueOf(examId));
            if (exam == null) continue;
            String quizName = exam.getExamName();
            String course = exam.getCourseCode();
            double score = records.stream().mapToDouble(r -> r.getScore() != null ? r.getScore() : 0).sum();
            // Calculate total possible score for the exam
            double totalScore = 0;
            // Fetch all questions for this exam
            comp3111.examsystem.tools.Database<Question> questionDB = new comp3111.examsystem.tools.Database<>(Question.class);
            for (Object qidObj : exam.getQuestionIds()) {
                Long qid = null;
                if (qidObj instanceof Long) {
                    qid = (Long) qidObj;
                } else if (qidObj instanceof String) {
                    try {
                        qid = Long.parseLong((String) qidObj);
                    } catch (NumberFormatException e) {
                        continue; // skip invalid
                    }
                }
                if (qid != null) {
                    Question q = questionDB.queryByKey(String.valueOf(qid));
                    if (q != null && q.getScore() != null) {
                        totalScore += q.getScore();
                    }
                }
            }
            // Calculate percentage score out of 100
            double percentScore = (totalScore > 0) ? (score / totalScore) * 100.0 : 0.0;
            // No date field in Record, so use LocalDate.now() as placeholder
            LocalDate date = LocalDate.now();
            quizGrades.add(new QuizGrade(quizName, course, percentScore, date));
        }
        this.allQuizGrades = quizGrades;
        loadCourses();
        applyFilter();
    }
    
    /**
     * Loads available courses from the quiz grades and populates the course combo box.
     */
    private void loadCourses() {
        Set<String> courses = allQuizGrades.stream()
                .map(QuizGrade::getCourse)
                .collect(Collectors.toSet());
        
        ObservableList<String> courseList = FXCollections.observableArrayList(new ArrayList<>(courses));
        courseList.add(0, "All Courses");
        courseComboBox.setItems(courseList);
        courseComboBox.getSelectionModel().select(0);
    }
    
    /**
     * Applies the filter based on the selected criteria (course, score range, date range).
     * Updates the quiz list, chart, and statistics accordingly.
     */
    private void applyFilter() {
        filteredQuizGrades = new ArrayList<>(allQuizGrades);
        
        // Apply course filter
        String selectedCourse = courseComboBox.getValue();
        if (selectedCourse != null && !selectedCourse.equals("All Courses")) {
            filteredQuizGrades = filteredQuizGrades.stream()
                    .filter(grade -> grade.getCourse().equals(selectedCourse))
                    .collect(Collectors.toList());
        }
        
        // Apply score range filter
        try {
            double minScore = minScoreField.getText().isEmpty() ? 0 : Double.parseDouble(minScoreField.getText());
            double maxScore = maxScoreField.getText().isEmpty() ? 100 : Double.parseDouble(maxScoreField.getText());
            
            filteredQuizGrades = filteredQuizGrades.stream()
                    .filter(grade -> grade.getScore() >= minScore && grade.getScore() <= maxScore)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter valid numbers for score range.");
        }
        
        // Apply date range filter
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate != null) {
            filteredQuizGrades = filteredQuizGrades.stream()
                    .filter(grade -> grade.getDate().isEqual(startDate) || grade.getDate().isAfter(startDate))
                    .collect(Collectors.toList());
        }
        
        if (endDate != null) {
            filteredQuizGrades = filteredQuizGrades.stream()
                    .filter(grade -> grade.getDate().isEqual(endDate) || grade.getDate().isBefore(endDate))
                    .collect(Collectors.toList());
        }
        
        // Update UI
        updateQuizList();
        updateChart();
        updateStatistics();
    }
    
    /**
     * Updates the quiz list view with filtered quiz grades.
     */
    private void updateQuizList() {
        List<String> quizNames = filteredQuizGrades.stream()
                .map(grade -> grade.getQuizName() + " (" + grade.getCourse() + ")")
                .collect(Collectors.toList());
        
        quizListView.setItems(FXCollections.observableArrayList(quizNames));
    }
    
    /**
     * Updates the bar chart with filtered quiz grades.
     */
    private void updateChart() {
        gradeChart.getData().clear();
        
        // Always set y-axis from 0 to 100 (percentage)
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);
        
        if (filteredQuizGrades.isEmpty()) return;
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        for (QuizGrade grade : filteredQuizGrades) {
            series.getData().add(new XYChart.Data<>(grade.getQuizName(), grade.getScore()));
        }
        
        gradeChart.getData().add(series);
        
        // Style the bars
        for (XYChart.Data<String, Number> data : series.getData()) {
            String color = getColorForScore((double) data.getYValue());
            if (data.getNode() != null) data.getNode().setStyle("-fx-bar-fill: " + color + ";");
            
            // Add tooltip showing the exact score
            Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue() + "%");
            Tooltip.install(data.getNode(), tooltip);
        }
    }
    
    /**
     * Returns a color based on the score for chart bar styling.
     *
     * @param score The score to get a color for.
     * @return A CSS color string representing the bar color.
     */
    private String getColorForScore(double score) {
        if (score >= 90) {
            return "#28a745"; // Green
        } else if (score >= 75) {
            return "#17a2b8"; // Blue
        } else if (score >= 60) {
            return "#ffc107"; // Yellow
        } else {
            return "#dc3545"; // Red
        }
    }
    
    /**
     * Updates the statistics labels (average, highest, lowest) based on filtered quiz grades.
     */
    private void updateStatistics() {
        if (filteredQuizGrades.isEmpty()) {
            averageScoreLabel.setText("N/A");
            highestScoreLabel.setText("N/A");
            lowestScoreLabel.setText("N/A");
            return;
        }
        
        // Calculate average score
        double totalScore = filteredQuizGrades.stream()
                .mapToDouble(QuizGrade::getScore)
                .sum();
        double averageScore = totalScore / filteredQuizGrades.size();
        
        // Find highest and lowest scores
        double highestScore = filteredQuizGrades.stream()
                .mapToDouble(QuizGrade::getScore)
                .max()
                .orElse(0);
        
        double lowestScore = filteredQuizGrades.stream()
                .mapToDouble(QuizGrade::getScore)
                .min()
                .orElse(0);
        
        // Update labels
        averageScoreLabel.setText(String.format("%.1f%%", averageScore));
        highestScoreLabel.setText(String.format("%.1f%%", highestScore));
        lowestScoreLabel.setText(String.format("%.1f%%", lowestScore));
    }
    
    /**
     * Handles the filter button action. Applies the current filter settings.
     *
     * @param event The action event.
     */
    @FXML
    public void handleFilter(ActionEvent event) {
        applyFilter();
    }
    
    /**
     * Handles the reset button action. Resets all filter fields to their default state.
     *
     * @param event The action event.
     */
    @FXML
    public void handleReset(ActionEvent event) {
        courseComboBox.getSelectionModel().select(0);
        minScoreField.clear();
        maxScoreField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        
        applyFilter();
    }
    
    /**
     * Handles the refresh button action. Reloads courses and reapplies the filter.
     *
     * @param event The action event.
     */
    @FXML
    public void handleRefresh(ActionEvent event) {
        // In a real application, this would reload data from the server
        // For now, just re-apply filter and reload courses
        loadCourses();
        applyFilter();
    }
    
    /**
     * Handles the quiz selection in the list view. Shows details for the selected quiz in an alert dialog.
     *
     * @param event The mouse event.
     */
    @FXML
    public void handleQuizSelection(MouseEvent event) {
        String selected = quizListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        
        // Extract quiz name (without course)
        String quizName = selected.substring(0, selected.lastIndexOf(" ("));
        
        // Find the corresponding quiz grade
        Optional<QuizGrade> selectedGrade = filteredQuizGrades.stream()
                .filter(grade -> grade.getQuizName().equals(quizName))
                .findFirst();
        
        if (selectedGrade.isPresent()) {
            QuizGrade grade = selectedGrade.get();
            
            // Show additional details in an alert dialog
            showAlert(Alert.AlertType.INFORMATION, "Quiz Details: " + grade.getQuizName(),
                    "Course: " + grade.getCourse() + "\n" +
                    "Score: " + grade.getScore() + "%\n" +
                    "Date: " + grade.getDate() + "\n\n" +
                    "Click 'Check' from the main screen to view detailed results for this quiz."
            );
        }
    }
    
    /**
     * Handles the back button action. Navigates back to the student main screen.
     *
     * @param event The action event.
     */
    @FXML
    public void handleBack(ActionEvent event) {
        try {
            // Load the student main screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comp3111/examsystem/StudentMainUI.fxml"));
            Parent root = loader.load();
            
            StudentMainController controller = loader.getController();
            controller.preSetController(student);
            
            // Show the student main screen
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
     * Handles the close button action. Prompts the user to confirm before closing the application.
     *
     * @param event The action event.
     */
    @FXML
    public void handleClose(ActionEvent event) {
        // Confirm before closing the application
        Alert confirmClose = new Alert(Alert.AlertType.CONFIRMATION);
        confirmClose.setTitle("Close Application");
        confirmClose.setHeaderText("Close Application");
        confirmClose.setContentText("Are you sure you want to exit the application?");
        
        confirmClose.showAndWait().ifPresent(response -> {
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
     * QuizGrade class to represent a grade for a quiz.
     */
    public static class QuizGrade {
        /**
         * The name of the quiz.
         */
        private String quizName;
        /**
         * The course the quiz belongs to.
         */
        private String course;
        /**
         * The score earned on the quiz.
         */
        private double score;
        /**
         * The date the quiz was taken.
         */
        private LocalDate date;
        /**
         * Constructor for a quiz grade.
         *
         * @param quizName The name of the quiz.
         * @param course   The course the quiz belongs to.
         * @param score    The score earned on the quiz.
         * @param date     The date the quiz was taken.
         */
        public QuizGrade(String quizName, String course, double score, LocalDate date) {
            this.quizName = quizName;
            this.course = course;
            this.score = score;
            this.date = date;
        }
        /**
         * Gets the quiz name.
         *
         * @return The quiz name.
         */
        public String getQuizName() {
            return quizName;
        }
        /**
         * Gets the course.
         *
         * @return The course.
         */
        public String getCourse() {
            return course;
        }
        /**
         * Gets the score.
         *
         * @return The score.
         */
        public double getScore() {
            return score;
        }
        /**
         * Gets the date.
         *
         * @return The date.
         */
        public LocalDate getDate() {
            return date;
        }
    }
} 