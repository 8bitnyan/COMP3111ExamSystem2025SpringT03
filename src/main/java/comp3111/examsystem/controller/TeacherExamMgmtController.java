package comp3111.examsystem.controller;

import comp3111.examsystem.Main;
import comp3111.examsystem.entity.*;
import comp3111.examsystem.tools.UIhelper;
import comp3111.examsystem.tools.MsgSender;
import comp3111.examsystem.tools.Database;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller for teacher exam management page.
 */
public class TeacherExamMgmtController implements Initializable {
    private Teacher teacher;
    public Exam selectedExam;
    public boolean editMode = false;
    
    // ObservableList for the selected questions in the current exam
    private ObservableList<Question> selectedQuestions = FXCollections.observableArrayList();
    
    @FXML private VBox mainbox;

    // Filter fields
    @FXML private TextField filterQuestionTxt;
    @FXML private TextField filterScoreTxt;
    @FXML private ComboBox<String> filterTypeCmb;

    @FXML private TextField filterExamNameTxt;
    @FXML private TextField filterCourseIdTxt;
    @FXML private ComboBox<String> filterStatusCmb;

    // Tables
    @FXML public TableView<Exam> examsTable;
    @FXML public TableColumn<Exam, String> colExamName;
    @FXML public TableColumn<Exam, String> colCourseId;
    @FXML public TableColumn<Exam, Integer> colDuration;
    @FXML public TableColumn<Exam, String> colPublished;
    @FXML public TableColumn<Exam, Integer> colQuestionCount;

    @FXML public TableView<Question> selectedQuestionsTable;
    @FXML public TableColumn<Question, String> colSelectedQuestion;
    @FXML public TableColumn<Question, String> colSelectedType;
    @FXML public TableColumn<Question, Integer> colSelectedScore;

    @FXML public TableView<Question> questionsTable;
    @FXML public TableColumn<Question, String> colQuestion;
    @FXML public TableColumn<Question, String> colType;
    @FXML public TableColumn<Question, Integer> colScore;

    // Form fields
    @FXML public Label formHeaderLabel;
    @FXML public TextField examNameTxt;
    @FXML public TextField courseIdTxt;
    @FXML public TextField durationTxt;
    @FXML public CheckBox publishedChk;
    @FXML public Label totalScoreLabel;

    // Buttons
    @FXML public Button addExamBtn;
    @FXML public Button editExamBtn;
    @FXML public Button updateExamBtn;
    @FXML public Button deleteExamBtn;
    @FXML public Button addToExamBtn;
    @FXML public Button removeSelectedQuestionBtn;
    @FXML public Button clearSelectedQuestionsBtn;

    // Main entity collections
    private ObservableList<Exam> allExams = FXCollections.observableArrayList();
    private ObservableList<Question> availableQuestions = FXCollections.observableArrayList();
    
    // Database reference for exams
    private Database<Exam> examDB;

    /**
     * Initializes the teacher exam management page UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize filter ComboBoxes
        filterTypeCmb.setItems(FXCollections.observableArrayList("Any", "MCQ", "Short Answer"));
        filterTypeCmb.setValue("Any");
        
        filterStatusCmb.setItems(FXCollections.observableArrayList("Any", "Published", "Unpublished"));
        filterStatusCmb.setValue("Any");

        // Configure table columns
        colExamName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colPublished.setCellValueFactory(cellData -> {
            Integer isPublished = cellData.getValue().getIsPublishedInt();
            String displayText = isPublished != null && isPublished > 0 ? "Yes" : "No";
            return new ReadOnlyStringWrapper(displayText);
        });
        colQuestionCount.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuestionCount()));

        // Configure selected questions table columns
        colSelectedQuestion.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        colSelectedType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSelectedScore.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Configure available questions table columns
        colQuestion.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Set up exam selection handler
        examsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (editMode) {
                // Don't change selection during edit mode
                if (oldSelection != null) {
                    Platform.runLater(() -> examsTable.getSelectionModel().select(oldSelection));
                }
                return;
            }
            
            if (newSelection != null) {
                selectedExam = newSelection;
                loadExamDetails(newSelection);
                
                // Enable/disable buttons based on publish status
                boolean isPublished = newSelection.getIsPublishedInt() != null && newSelection.getIsPublishedInt() > 0;
                editExamBtn.setDisable(isPublished);
                updateExamBtn.setDisable(true); // Initially disable until Edit is clicked
                deleteExamBtn.setDisable(isPublished);
                
                // Also disable add/remove question buttons if published
                addToExamBtn.setDisable(isPublished);
                removeSelectedQuestionBtn.setDisable(isPublished);
                clearSelectedQuestionsBtn.setDisable(isPublished);
            } else {
                selectedExam = null;
                clearForm();
                
                // Disable buttons that require selection
                editExamBtn.setDisable(true);
                updateExamBtn.setDisable(true);
                deleteExamBtn.setDisable(true);
            }
        });

        // Set selected questions to display in the middle table
        selectedQuestionsTable.setItems(selectedQuestions);

        //
        questionsTable.setItems(availableQuestions);
        
        // Add listener to update total score when questions change
        selectedQuestions.addListener((javafx.collections.ListChangeListener.Change<? extends Question> c) -> {
            updateTotalScore();
        });
        
        // Initialize UI state
        setFormEditable(false);
        clearForm();
        
        // Set initial button states
        editExamBtn.setDisable(true);
        updateExamBtn.setDisable(true);
        deleteExamBtn.setDisable(true);
        
        // Make Add Exam button more prominent
        addExamBtn.setStyle("-fx-background-color: #4CAF50; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 1);");
        addExamBtn.setTooltip(new Tooltip("Click here to create a new exam"));
        
        // Add tooltips to other buttons
        editExamBtn.setTooltip(new Tooltip("Edit the selected exam"));
        updateExamBtn.setTooltip(new Tooltip("Save changes to the exam"));
        
        // Initialize database reference
        examDB = new Database<>(Exam.class);
    }

    /**
     * Sets the teacher object and initializes the UI.
     * @param teacher The teacher object that is operating the page.
     */
    public void presetController(Teacher teacher) {
        this.teacher = teacher;
        handleRefresh();
    }

    /**
     * Updates the total score display based on selected questions
     */
    public void updateTotalScore() {
        int total = selectedQuestions.stream()
                .mapToInt(q -> q.getScore() != null ? q.getScore() : 0)
                .sum();
        totalScoreLabel.setText(String.valueOf(total));
    }

    /**
     * Sets the form fields to be editable or read-only
     * @param editable True to make editable, false for read-only
     */
    public void setFormEditable(boolean editable) {
        examNameTxt.setEditable(editable);
        courseIdTxt.setEditable(editable);
        durationTxt.setEditable(editable);
        publishedChk.setDisable(!editable);
        
        // Set visual cues for form state
        String style = editable ? "" : "-fx-background-color: #f5f5f5;";
        examNameTxt.setStyle(style);
        courseIdTxt.setStyle(style);
        durationTxt.setStyle(style);
        
        // Update form header based on state
        formHeaderLabel.setText(editable ? 
            (selectedExam == null ? "Create New Exam" : "Edit Exam") : 
            "Exam Details (Select an exam or click 'Add New Exam')");
        
        // Add tooltips when read-only
        String tooltip = editable ? null : "Click 'Add New Exam' to create a new exam";
        examNameTxt.setTooltip(tooltip != null ? new Tooltip(tooltip) : null);
        courseIdTxt.setTooltip(tooltip != null ? new Tooltip(tooltip) : null);
        durationTxt.setTooltip(tooltip != null ? new Tooltip(tooltip) : null);
        
        // Update button states
        if (editable) {
            addExamBtn.setDisable(true);
            editExamBtn.setDisable(true);
            updateExamBtn.setDisable(false);
        } else {
            addExamBtn.setDisable(false);
            
            // Enable Edit button only for unpublished exams
            editExamBtn.setDisable(selectedExam == null || 
                (selectedExam.getIsPublishedInt() != null && selectedExam.getIsPublishedInt() > 0));
                
            // Disable Update button when not in edit mode
            updateExamBtn.setDisable(true);
        }
        
        // Set table disabled state
        examsTable.setDisable(editable);
    }

    /**
     * Loads exam details into the form
     * @param exam The exam to load
     */
    public void loadExamDetails(Exam exam) {
        if (exam == null) {
            clearForm();
            return;
        }
        
        // Set form fields
        examNameTxt.setText(exam.getName() != null ? exam.getName() : "");
        courseIdTxt.setText(exam.getCourseCode() != null ? exam.getCourseCode() : "");
        durationTxt.setText(exam.getDuration() != null ? exam.getDuration().toString() : "");
        publishedChk.setSelected(exam.getIsPublishedInt() != null && exam.getIsPublishedInt() > 0);

        // Load the exam questions
        loadExamQuestions(exam);
        
        // Update form state
        setFormEditable(false);
    }

    /**
     * Loads the questions for the given exam
     * @param exam The exam to load questions for
     */
    public void loadExamQuestions(Exam exam) {
        selectedQuestions.clear();

        // Fetch all questions from the database
        Database<Question> questionDB = new Database<>(Question.class);
        List<Question> allQuestions = questionDB.getAllEnabled();
        availableQuestions.setAll(allQuestions); // Reset available questions to all

        if (exam != null && exam.getQuestionIds() != null && !exam.getQuestionIds().isEmpty()) {
            // Extract valid question IDs from the exam (Don't know why but they are String)
            List<Long> selectedQuestionIds = exam.getQuestionIds();

            // Split questions into selected (for the exam) and remaining (available)
            List<Question> remainingQuestions = new ArrayList<>();
            List<Question> selected = new ArrayList<>();

            for (Question q : allQuestions) {
                // MsgSender.showMsg(q.getId().getClass().getName());
                if (selectedQuestionIds.contains(Long.toString(q.getId()))) {
                    selected.add(q); // Add to selected exam questions
                } else {
                    remainingQuestions.add(q); // Add to available questions
                }
            }

            // Update the lists and tables
            availableQuestions.setAll(remainingQuestions);
            selectedQuestions.setAll(selected);

            // MsgSender.showMsg(String.valueOf(availableQuestions.size()));
        }

        // Refresh the tables
        questionsTable.setItems(availableQuestions);
        selectedQuestionsTable.setItems(selectedQuestions);
        updateTotalScore();
    }

    /**
     * Clears the form fields
     */
    public void clearForm() {
        examNameTxt.clear();
        courseIdTxt.clear();
        durationTxt.clear();
        publishedChk.setSelected(false);
        selectedQuestions.clear();
        
        // Update UI
        updateTotalScore();
        setFormEditable(false);
    }

    /**
     * Handles filtering exams
     */
    @FXML
    public void handleFilterExams() {
        String nameFilter = filterExamNameTxt.getText().trim().toLowerCase();
        String courseIdFilter = filterCourseIdTxt.getText().trim().toLowerCase();
        String statusFilter = filterStatusCmb.getValue();

        Database<Course> courseDB = new Database<>(Course.class);
        if (courseDB.queryByField("courseCode", courseIdFilter).isEmpty()) {
            MsgSender.showMsg("No such course found.");
            return;
        }
        Database<Exam> examDB = new Database<>(Exam.class);
        List<Exam> allExams = examDB.queryByField("teacherId", teacher.getId().toString());
        List<Exam> filteredExams = allExams.stream()
            .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(nameFilter))
            .filter(e -> e.getCourseCode() != null && e.getCourseCode().toLowerCase().contains(courseIdFilter))
            .filter(e -> {
                if ("Any".equals(statusFilter)) return true;
                boolean isPublished = e.getIsPublishedInt() != null && e.getIsPublishedInt() > 0;
                return ("Published".equals(statusFilter) && isPublished) || 
                       ("Unpublished".equals(statusFilter) && !isPublished);
            })
            .collect(Collectors.toList());
        examsTable.getItems().clear();
        examsTable.getItems().addAll(filteredExams);
    }

    /**
     * Handles resetting the exam filter
     */
    @FXML
    public void handleResetExamFilter() {
        filterExamNameTxt.clear();
        filterCourseIdTxt.clear();
        filterStatusCmb.setValue("Any");
        loadAllExams();
    }

    /**
     * Handles filtering questions
     */
    @FXML
    public void handleFilterQuestions() {
        String questionFilter = filterQuestionTxt.getText().trim().toLowerCase();
        String typeFilter = filterTypeCmb.getValue();
        String scoreText = filterScoreTxt.getText().trim();
        Database<Question> questionDB = new Database<>(Question.class);
        List<Question> allQuestions = questionDB.queryByField("teacherId", teacher.getId().toString());
        int scoreFilter = -1;
        if (!scoreText.isEmpty()) {
            try {
                scoreFilter = Integer.parseInt(scoreText);
            } catch (NumberFormatException e) {
                MsgSender.showMsg("Score filter should be an integer or left blank.");
                filterScoreTxt.clear();
            }
        }
        final int finalScoreFilter = scoreFilter;
        List<Question> filteredQuestions = allQuestions.stream()
            .filter(q -> q.getQuestionText() != null && q.getQuestionText().toLowerCase().contains(questionFilter))
            .filter(q -> "Any".equals(typeFilter) || (q.getType() != null && q.getType().equals(typeFilter)))
            .filter(q -> finalScoreFilter == -1 || (q.getScore() != null && q.getScore() == finalScoreFilter))
            .collect(Collectors.toList());
        questionsTable.getItems().clear();
        questionsTable.getItems().addAll(filteredQuestions);
    }

    /**
     * Handles resetting the question filter
     */
    @FXML
    public void handleResetQuestionFilter() {
        filterQuestionTxt.clear();
        filterTypeCmb.setValue("Any");
        filterScoreTxt.clear();
        loadAllQuestions();
    }

    /**
     * Loads all exams for the current teacher
     */
    public void loadAllExams() {
        Database<Exam> examDB = new Database<>(Exam.class);
        List<Exam> exams = examDB.queryByField("teacherId", teacher.getId().toString());
        examsTable.getItems().clear();
        examsTable.getItems().addAll(exams);
    }

    /**
     * Loads all questions for the current teacher
     */
    public void loadAllQuestions() {
        Database<Question> questionDB = new Database<>(Question.class);
        List<Question> questions = questionDB.queryByField("teacherId", teacher.getId().toString());
        availableQuestions.clear();
        selectedQuestions.clear();
        availableQuestions.setAll(questions);
    }

    /**
     * Handles adding a question to the exam
     */
    @FXML
    private void handleAddToExam() {
        Question selectedQuestion = questionsTable.getSelectionModel().getSelectedItem();
        
        if (selectedQuestion == null) {
            MsgSender.showMsg("Please select a question to add to the exam.");
            return;
        }
        
        // Check if the question is already in the selected questions
        boolean alreadySelected = selectedQuestions.stream()
            .anyMatch(q -> q.getId().equals(selectedQuestion.getId()));
        
        if (alreadySelected) {
            MsgSender.showMsg("This question is already added to the exam.");
            return;
        }
        
        // Add the question to the selected questions
        selectedQuestions.add(selectedQuestion);

        questionsTable.getItems().remove(selectedQuestion);
        
        // Update total score
        updateTotalScore();
    }

    /**
     * Handles removing a selected question from the exam
     */
    @FXML
    private void handleRemoveSelectedQuestion() {
        Question questionToRemove = selectedQuestionsTable.getSelectionModel().getSelectedItem();
        
        if (questionToRemove == null) {
            MsgSender.showMsg("Please select a question to remove from the exam.");
            return;
        }
        
        // Remove the question from the selected questions
        selectedQuestions.remove(questionToRemove);

        questionsTable.getItems().add(questionToRemove);
        
        // Update total score
        updateTotalScore();
    }

    /**
     * Handles clearing all selected questions
     */
    @FXML
    public void handleClearSelectedQuestions() {
        if (selectedQuestions.isEmpty()) {
            return;
        }
        
        MsgSender.showConfirm(
            "Clear Selected Questions", 
            "Are you sure you want to remove all questions from this exam?", 
            () -> {
                selectedQuestions.clear();
                loadAllQuestions();
                updateTotalScore();
            }
        );
    }

    /**
     * Handles adding a new exam
     */
    @FXML
    public void handleAddExam() {
        // Clear form and set to edit mode
        clearForm();
        selectedExam = null;
        loadAllQuestions();
        editMode = true;
        setFormEditable(true);
        
        // Update UI
        formHeaderLabel.setText("Create New Exam");
        formHeaderLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
    }

    /**
     * Handles editing an existing exam
     */
    @FXML
    public void handleEditExam() {
        if (selectedExam == null) {
            MsgSender.showMsg("Please select an exam to edit.");
            return;
        }
        
        if (selectedExam.getIsPublishedInt() != null && selectedExam.getIsPublishedInt() > 0) {
            MsgSender.showMsg("Published exams cannot be edited.");
            return;
        }
        
        // Enter edit mode
        editMode = true;
        setFormEditable(true);
        
        // Update UI
        formHeaderLabel.setText("Edit Exam");
        formHeaderLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
    }

    /**
     * Handles creating a new exam or updating an existing one
     */
    @FXML
    public void handleUpdateExam() {
        // Validate input
        if (examNameTxt.getText().isEmpty() || courseIdTxt.getText().isEmpty() || durationTxt.getText().isEmpty()) {
            MsgSender.showMsg("Please fill in all required fields.");
            return;
        }
        
        try {
            Integer duration = Integer.parseInt(durationTxt.getText());
            if (duration <= 0) {
                MsgSender.showMsg("Duration must be a positive number.");
                return;
            }
            
            if (selectedQuestions.isEmpty()) {
                MsgSender.showMsg("Please add at least one question to the exam.");
                return;
            }

            // Check whether the course exists
            Database<Course> courseDB = new Database<>(Course.class);
            if (courseDB.queryByField("courseCode", courseIdTxt.getText()).isEmpty()) {
                MsgSender.showMsg("No such course found.");
                return;
            }
            
            // Create or update exam
            if (selectedExam == null) {
                // Create new exam
                Exam newExam = new Exam();
                newExam.setTeacherId(teacher.getId());
                newExam.setName(examNameTxt.getText());
                newExam.setCourseCode(courseIdTxt.getText());
                newExam.setDuration(duration);
                newExam.setExamTime(duration.toString()); // Sync with duration field
                newExam.setIsPublishedInt(publishedChk.isSelected() ? 1 : 0);
                
                // Set questions
                List<Long> questionIds = selectedQuestions.stream()
                    .map(Question::getId)
                    .collect(Collectors.toList());
                newExam.setQuestionIds(questionIds);
                
                // Save exam to database
                examDB.add(newExam);
                MsgSender.showMsg("Exam created successfully!");
                
                // Add to list and select it
                allExams.add(newExam);
                Platform.runLater(() -> examsTable.getSelectionModel().select(newExam));
            } else {
                // Update existing exam
                selectedExam.setName(examNameTxt.getText());
                selectedExam.setCourseCode(courseIdTxt.getText());
                selectedExam.setDuration(duration);
                selectedExam.setExamTime(duration.toString()); // Sync with duration field
                selectedExam.setIsPublishedInt(publishedChk.isSelected() ? 1 : 0);
                
                // Set questions
                List<Long> questionIds = selectedQuestions.stream()
                    .map(Question::getId)
                    .collect(Collectors.toList());
                selectedExam.setQuestionIds(questionIds);
                
                // Update in database
                examDB.update(selectedExam);
                MsgSender.showMsg("Exam updated successfully!");
                
                // Refresh table
                examsTable.refresh();
            }
            
            // Exit edit mode
            editMode = false;
            setFormEditable(false);
            
            // Refresh exam list
            loadAllExams();
            
        } catch (NumberFormatException e) {
            MsgSender.showMsg("Please enter a valid number for duration.");
        } catch (Exception e) {
            MsgSender.showMsg("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles deleting an exam
     */
    @FXML
    public void handleDeleteExam() {
        if (selectedExam == null) {
            MsgSender.showMsg("Please select an exam to delete.");
            return;
        }
        
        if (selectedExam.getIsPublishedInt() != null && selectedExam.getIsPublishedInt() > 0) {
            MsgSender.showMsg("Published exams cannot be deleted.");
            return;
        }
        
        MsgSender.showConfirm(
            "Delete Exam", 
            "Are you sure you want to delete this exam? This action cannot be undone.", 
            () -> {
                examDB.delByKey(selectedExam.getId().toString());
                
                // Clear selection and form
                selectedExam = null;
                clearForm();
                
                // Refresh UI
                loadAllExams();
                
                // Show success message
                MsgSender.showMsg("Exam deleted successfully!");
            }
        );
    }

    /**
     * Handles refreshing the UI
     */
    @FXML
    public void handleRefresh() {
        // Save selected exam ID if any
        Long selectedExamId = selectedExam != null ? selectedExam.getId() : null;

        // Clear added questions
        selectedQuestionsTable.getItems().clear();
        
        // Reload data
        loadAllExams();
        loadAllQuestions();
        
        // Restore selection if possible
        if (selectedExamId != null) {
            for (Exam exam : examsTable.getItems()) {
                if (exam.getId().equals(selectedExamId)) {
                    examsTable.getSelectionModel().select(exam);
                    examsTable.scrollTo(exam);
                    break;
                }
            }
        }
        
        // Reset edit mode
        editMode = false;
        setFormEditable(false);
    }

    /**
     * Handles navigating back to the teacher main menu
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherMainUI.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Welcome to HKUST Examination System");
            stage.setScene(new Scene(fxmlLoader.load()));
            TeacherMainController newController = fxmlLoader.getController();
            if (teacher == null) {
                MsgSender.showMsg("NULL Teacher");
            }
            newController.presetController(teacher); // Pass the Teacher object to the next controller
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) mainbox.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles closing the application
     */
    @FXML
    private void handleCloseApplication() {
        Platform.exit();
        System.exit(0);
    }

    // for testing
    public Teacher getTeacher() {
        return teacher;
    }
} 