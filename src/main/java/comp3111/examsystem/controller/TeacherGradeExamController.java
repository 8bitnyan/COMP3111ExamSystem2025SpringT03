package comp3111.examsystem.controller;
import comp3111.examsystem.Main;
import comp3111.examsystem.entity.Teacher;
import comp3111.examsystem.entity.Record;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import comp3111.examsystem.tools.UIhelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller class for the Teacher Grade Exam page.
 * This controller allows a logged-in teacher to:
 * - Filter exams by course and student.
 * - View and select questions from a specific exam.
 * - Display and grade student responses to each question.
 * - Update scores and view correct answers and maximum scores.
 * It uses a `Database<Record>` to retrieve and update student answers,
 * and parses structured text files to read exams, questions, and student information.
 */
public class TeacherGradeExamController implements Initializable {
    private Teacher teacher;
    private Long currentExamID;
    @FXML private ListView<String> questionList;
    @FXML private ComboBox<String> courseFilter;
    @FXML private ComboBox<String> examFilter;
    @FXML private ComboBox<String> studentFilter;
    private final Map<String, String> examLineMap = new HashMap<>();
    private final Map<String, List<String>> courseToExamsMap = new HashMap<>();
    @FXML private TableView<Record> answerTable;
    @FXML private TableColumn<Record, String> colStudentName;
    @FXML private TableColumn<Record, String> colStudentAnswer;
    @FXML private TableColumn<Record, Integer> colScore;
    @FXML private Label correctAnswerLabel;
    @FXML private Label maxScoreLabel;
    private final Map<String, String> questionTextToId = new HashMap<>();
    private final Database<Record> recordDatabase = new Database<>(Record.class);
    private Record selectedRecord;
    @FXML private TextField manualScoreField;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load all questions from the exam for display
        loadExamOptions();
        courseFilter.setOnAction(e -> {
            String selectedCourse = courseFilter.getSelectionModel().getSelectedItem();
            examFilter.getItems().clear();
            if (selectedCourse != null && courseToExamsMap.containsKey(selectedCourse)) {
                examFilter.getItems().addAll(courseToExamsMap.get(selectedCourse));
            }
        });
        // Retrieve student's response to a particular question
        colStudentName.setCellValueFactory(cellData -> {
            Long id = cellData.getValue().getStudentID();
            return new SimpleStringProperty(getStudentNameById(String.valueOf(id)));
        });
        colStudentAnswer.setCellValueFactory(new PropertyValueFactory<>("response"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Handle question selection
        questionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && questionTextToId.containsKey(newVal)) {
                String qid = questionTextToId.get(newVal);
                displayQuestionDetails(qid);
                displayStudentResponsesForQuestion(qid);
            }
        });

        //If a question is selected -> Display question details, all the students' response for that question.
        questionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && questionTextToId.containsKey(newVal)) {
                String qid = questionTextToId.get(newVal);
                displayQuestionDetails(qid);
            }
        });
        answerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedRecord = newVal;
                manualScoreField.setText(String.valueOf(newVal.getScore()));
            }
        });
        studentFilter.setOnAction(e -> {
            String selectedQuestion = questionList.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null && questionTextToId.containsKey(selectedQuestion)) {
                String qid = questionTextToId.get(selectedQuestion);
                displayStudentResponsesForQuestion(qid);
            }
        });
    }

    public void presetController(Teacher teacher) {
        this.teacher = teacher;
    }

    /**
     * Loads all enabled exams from the database file and populates filter options.
     * Organizes exams by course in a map for later filtering.
     */
    private void loadExamOptions() {
        Path examPath = Paths.get("src", "main", "resources", "database", "exam.txt");
        try {
            List<String> lines = Files.readAllLines(examPath);
            courseFilter.getItems().clear();
            examFilter.getItems().clear();
            examLineMap.clear();
            courseToExamsMap.clear();
            for (String line : lines) {
                // Only load if isAble is true
                if (!line.contains("isAble%&:true")) continue;

                String courseCode = extractField(line, "courseCode");
                String examName = extractField(line, "examName");
                if (courseCode != null && examName != null) {
                    examLineMap.put(courseCode + "|" + examName, line);
                    courseToExamsMap.putIfAbsent(courseCode, new ArrayList<>());
                    courseToExamsMap.get(courseCode).add(examName);
                }
            }
            courseFilter.getItems().addAll(courseToExamsMap.keySet());
        } catch (IOException e) {
            MsgSender.showMsg("Failed to load exams.");
        }
    }

    /**
     * Displays all questions from the selected exam.
     * - Reads question IDs from the exam.
     * - Looks up question text from the question file.
     * - Populates the question list and question ID mappings.
     * @param examLine The serialized exam data line.
     */
    private void displayQuestionsFromExamLine(String examLine) {
        questionList.getItems().clear();
        questionTextToId.clear();
        String questionSection = extractField(examLine, "questions");
        if (questionSection == null || questionSection.isBlank()) return;

        // Parse question IDs from the comma-separated string
        List<String> questionIds = Arrays.stream(questionSection.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList());

        Path questionDB = Paths.get("src", "main", "resources", "database", "question.txt");
        List<String> questionLines;
        try {
            questionLines = Files.readAllLines(questionDB);
        } catch (IOException e) {
            MsgSender.showMsg("Failed to read question database.");
            return;
        }

        //Extract the question from the question list
        for (String qid : questionIds) {
            for (String line : questionLines) {
                String id = extractField(line, "id");
                String isAble = extractField(line, "isAble");
                if (id != null && id.equals(qid) && "true".equalsIgnoreCase(isAble)) {
                    String questionText = extractField(line, "questionText");
                    if (questionText != null) {
                        questionList.getItems().add(questionText);
                        questionTextToId.put(questionText, qid);
                    }
                    break;
                }
            }
        }

        // Get the students record
        Set<String> uniqueStudentNames = new HashSet<>();
        List<Record> allRecords = recordDatabase.getAllEnabled();
        for (Record r : allRecords) {
            if (r.getExamID() != null && r.getExamID().equals(currentExamID)) {
                String studentName = getStudentNameById(String.valueOf(r.getStudentID()));
                uniqueStudentNames.add(studentName);
            }
        }
        studentFilter.getItems().clear();
        studentFilter.getItems().add("ALL");
        studentFilter.getItems().addAll(uniqueStudentNames);
        studentFilter.getSelectionModel().select("ALL");

    }

    /**
     * Extracts the value of a specific field from a serialized data line.
     * @param line The line of text.
     * @param fieldName The field name to extract.
     * @return The value of the field, or null if not found.
     */
    private String extractField(String line, String fieldName) {
        String[] fields = line.split("!@#");
        for (String field : fields) {
            if (field.startsWith(fieldName + "%&:")) {
                return field.split("%&:")[1];
            }
        }
        return null;
    }

    /**
     * Filters and loads an exam based on selected course and exam name.
     * Triggers display of related questions.
     */
    @FXML
    private void filterExam() {
        String selectedCourse = courseFilter.getSelectionModel().getSelectedItem();
        String selectedExam = examFilter.getSelectionModel().getSelectedItem();

        if (selectedCourse != null && selectedExam != null) {
            String key = selectedCourse + "|" + selectedExam;
            if (examLineMap.containsKey(key)) {
                String examLine = examLineMap.get(key);
                currentExamID = extractExamIdFromLine(examLine); // new function
                displayQuestionsFromExamLine(examLine);
            } else {
                MsgSender.showMsg("No exam found for the selected course and exam.");
            }
        } else {
            MsgSender.showMsg("Please select both a course and an exam.");
        }
    }

    /**
     * Extracts the exam ID from a serialized exam line.
     * @param examLine The serialized exam data line.
     * @return The exam ID as a Long.
     */
    private Long extractExamIdFromLine(String examLine) {
        String[] fields = examLine.split("!@#");
        for (String field : fields) {
            if (field.startsWith("id%&:")) {
                return Long.parseLong(field.split("%&:")[1]);
            }
        }
        return null;
    }

    /**
     * Resets the filter input.
     */
    @FXML
    private void resetFilter() {
        courseFilter.getSelectionModel().clearSelection();
        examFilter.getSelectionModel().clearSelection();
        examFilter.getItems().clear();
        questionList.getItems().clear();
    }

    /**
     * Displays all student responses to the selected question.
     * Optionally filters by student if selected in the student dropdown.
     * @param questionId The ID of the selected question.
     */
    private void displayStudentResponsesForQuestion(String questionId) {
        String selectedStudentName = studentFilter.getSelectionModel().getSelectedItem();
        List<Record> allRecords = recordDatabase.getAllEnabled();
        List<Record> result = new ArrayList<>();
        for (Record r : allRecords) {
            if (r.getQuestionID() != null && r.getExamID() != null &&
                    r.getQuestionID().toString().equals(questionId) &&
                    r.getExamID().equals(currentExamID)) {

                String name = getStudentNameById(String.valueOf(r.getStudentID()));
                if (selectedStudentName == null || selectedStudentName.equals("ALL") || name.equals(selectedStudentName)) {
                    result.add(r);
                }
            }
        }
        answerTable.setItems(FXCollections.observableArrayList(result));
    }

    /**
     * Looks up and returns a student's name by ID.
     * Reads from the student database file.
     * @param studentId The student ID.
     * @return The student's name, or "Unknown" if not found.
     */
    private String getStudentNameById(String studentId) {
        Path studentFile = Paths.get("src", "main", "resources", "database", "student.txt");
        try {
            for (String line : Files.readAllLines(studentFile)) {
                if (line.contains("id%&:" + studentId + "!@#")) {
                    return extractField(line, "name");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    /**
     * Displays the correct answer and maximum score for the selected question.
     * @param questionId The question's unique ID.
     */
    private void displayQuestionDetails(String questionId) {
        Path questionDB = Paths.get("src", "main", "resources", "database", "question.txt");
        try {
            List<String> questionLines = Files.readAllLines(questionDB);
            for (String line : questionLines) {
                if (line.contains("id%&:" + questionId + "!@#") && line.contains("isAble%&:true")) {
                    String answer = extractField(line, "answer");
                    String score = extractField(line, "score");

                    correctAnswerLabel.setText(answer != null ? answer : "N/A");
                    maxScoreLabel.setText(score != null ? score : "N/A");
                    return;
                }
            }
        } catch (IOException e) {
            MsgSender.showMsg("Failed to load question details.");
        }
        correctAnswerLabel.setText("N/A");
        maxScoreLabel.setText("N/A");
    }

    /**
     * Updates the score of the selected student answer.
     * Validates against the maximum possible score and applies the change to the database.
     */
    @FXML
    private void updateScore() {
        if (selectedRecord == null) {
            MsgSender.showMsg("Please select a student answer first.");
            return;
        }
        String newScoreText = manualScoreField.getText().trim();
        if (newScoreText.isEmpty()) {
            MsgSender.showMsg("Score cannot be empty.");
            return;
        }
        try {
            int newScore = Integer.parseInt(newScoreText);
            //Validation
            String maxScoreText = maxScoreLabel.getText().trim();
            if (!maxScoreText.equals("N/A")) {
                try {
                    int maxScore = Integer.parseInt(maxScoreText);
                    if (newScore < 0 || newScore > maxScore) {
                        MsgSender.showMsg("Score must be between 0 and " + maxScore);
                        return;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            selectedRecord.setScore(newScore);
            recordDatabase.update(selectedRecord);
            answerTable.refresh();  // Refresh table view
            MsgSender.showMsg("Score updated successfully.");
        } catch (NumberFormatException e) {
            MsgSender.showMsg("Please enter a valid numeric score.");
        }
    }

    /**
     * Navigates back to the Teacher main screen.
     * @param e The triggered action event.
     */
    @FXML
    void back(ActionEvent e) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherMainUI.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Back");
            stage.setScene(new Scene(fxmlLoader.load()));
            TeacherMainController controller = fxmlLoader.getController();
            controller.presetController(teacher);
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Closes the application with a confirmation dialog.
     */
    @FXML
    void closeApplication() {
        MsgSender.showConfirm(
                "Exit Confirmation",
                "Are you sure you want to exit?\nClick OK to exit the application.",
                Platform::exit
        );
    }
}
