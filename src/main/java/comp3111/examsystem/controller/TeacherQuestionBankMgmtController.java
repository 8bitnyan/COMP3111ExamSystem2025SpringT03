package comp3111.examsystem.controller;

import comp3111.examsystem.Main;
import comp3111.examsystem.entity.*;
import comp3111.examsystem.tools.UIhelper;
import comp3111.examsystem.tools.MsgSender;
import comp3111.examsystem.tools.Database;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.Region;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.geometry.Bounds;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeacherQuestionBankMgmtController implements Initializable {

    private Teacher teacher;
    private Question selectedQuestion;
    private boolean editMode = false; // Track if we're in edit mode

    @FXML private VBox mainbox;

    @FXML private TextField filterQuestionTxt;
    @FXML private ComboBox<String> filterTypeCmb;
    @FXML private TextField filterScoreTxt;

    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, String> colQuestion, colOptionA, colOptionB, colOptionC, colOptionD, colOptionE, colType, colAnswer;
    @FXML private TableColumn<Question, Integer> colScore;

    @FXML private ComboBox<String> typeCmb;
    @FXML private VBox options;
    @FXML private VBox optionsContainer;
    @FXML private Button addOptionBtn;
    @FXML private TextArea questionTxt;
    @FXML private TextField scoreTxt, answerTxt;
    
    // List to keep track of dynamic option text fields
    private List<TextField> optionFields = new ArrayList<>();
    private static final int MIN_OPTIONS = 2;
    private static final int MAX_OPTIONS = 5; // Maximum of 5 options (A through E)

    @FXML private Button addBtn, updateBtn, editBtn, cancelEditBtn; // Add cancelEditBtn declaration

    /**
     * Sets the teacher object and initializes the UI.
     * @param teacher The teacher object that is operating the page.
     */
    public void presetController(Teacher teacher) {
        this.teacher = teacher;
        handleRefreshWithoutMsg();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterTypeCmb.setItems(FXCollections.observableArrayList("Any", "MCQ", "Short Answer"));
        filterTypeCmb.setValue("Any");
        typeCmb.setItems(FXCollections.observableArrayList("MCQ", "Short Answer"));
        questionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Initialize button states safely
        try {
            if (updateBtn != null) {
                updateBtn.setDisable(true); // Disable update button until edit mode is activated
            }
            if (editBtn != null) {
                editBtn.setDisable(true); // Disable edit button until a question is selected
            }
            if (cancelEditBtn != null) {
                cancelEditBtn.setDisable(true); // Disable cancel button until in edit mode
            }
            if (addBtn != null) {
                // Make the Add New button more prominent with styling
                addBtn.setStyle("-fx-background-color: #4CAF50; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 1);");
                
                // Add a tooltip to guide new users
                addBtn.setTooltip(new Tooltip("Click here to create a new question"));
            }
        } catch (Exception e) {
            System.err.println("Error initializing button states: " + e.getMessage());
        }

        colQuestion.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colOptionA.setCellValueFactory(new PropertyValueFactory<>("optionA"));
        colOptionB.setCellValueFactory(new PropertyValueFactory<>("optionB"));
        colOptionC.setCellValueFactory(new PropertyValueFactory<>("optionC"));
        colOptionD.setCellValueFactory(new PropertyValueFactory<>("optionD"));
        colOptionE.setCellValueFactory(new PropertyValueFactory<>("optionE"));
        colAnswer.setCellValueFactory(new PropertyValueFactory<>("answer"));
        
        // Add null-safe cell factories for all columns
        colQuestion.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });
        
        colType.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });
        
        colScore.setCellFactory(column -> new TableCell<Question, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
        
        colOptionA.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });
        
        colOptionB.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });
        
        colOptionC.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });
        
        colOptionD.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });
        
        colOptionE.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });
        
        colAnswer.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });

        updateOptionFieldsVisibility(typeCmb.getValue());

        typeCmb.setOnAction(e -> {
            String selected = typeCmb.getValue();
            updateOptionFieldsVisibility(selected);
            
            // Update answer field hint based on question type
            updateAnswerHint(selected);
        });

        // Improve table selection handling to prevent UI issues
        questionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // Don't process selection changes during edit mode
            if (editMode) {
                return;
            }
            
            if (newSelection != null) {
                // Store the new selection
                selectedQuestion = newSelection;
                
                // Display the selected question in read-only mode
                Platform.runLater(() -> {
                    // Just display the selected question in read-only mode
                    displaySelectedQuestionReadOnly(selectedQuestion);
                });
            } else if (oldSelection != null && newSelection == null) {
                // Selection was cleared
                selectedQuestion = null;
                Platform.runLater(this::clearFormFields);
            }
        });
        
        // Initialize with default 2 options for MCQ
        initializeOptions(MIN_OPTIONS);
        
        // Set initial answer hint
        updateAnswerHint(typeCmb.getValue());
        
        // Add context menu for hard refresh
        ContextMenu contextMenu = new ContextMenu();
        MenuItem hardRefreshItem = new MenuItem("Force Hard Refresh");
        hardRefreshItem.setOnAction(e -> handleHardRefresh());
        contextMenu.getItems().add(hardRefreshItem);
        
        // Attach context menu to the refresh button
        Button refreshButton = null;
        for (Node node : mainbox.lookupAll("Button")) {
            if (node instanceof Button && ((Button) node).getText().equals("Refresh")) {
                refreshButton = (Button) node;
                break;
            }
        }
        
        if (refreshButton != null) {
            refreshButton.setContextMenu(contextMenu);
        }
        
        // Set form fields to be initially read-only
        setFormFieldsEditable(false);
        
        // Add click handlers to show popup hints for read-only fields
        addClickHandlersToFormFields();
    }
    
    /**
     * Initializes the options container with the specified number of options.
     * @param count The number of options to create.
     */
    private void initializeOptions(int count) {
        optionsContainer.getChildren().clear();
        optionFields.clear();
        
        // Create the specified number of options
        for (int i = 0; i < count; i++) {
            addOptionFieldToUI(i, "");
        }
    }
    
    /**
     * Adds a new option field to the UI with the given index and value.
     * @param index The index of the option (0-based).
     * @param value The value for the option.
     */
    private void addOptionFieldToUI(int index, String value) {
        HBox optionBox = new HBox(10);
        optionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        optionBox.setMinWidth(250);
        optionBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        // Create option label (A, B, C, D, E)
        Label optionLabel = new Label("Option " + (char)('A' + index) + ":");
        optionLabel.getStyleClass().add("header-label");
        optionLabel.setMinWidth(80);
        
        // Create option text field with wrapping capability
        TextField optionField = new TextField(value);
        optionField.setPrefWidth(200);
        optionField.setMinWidth(150);
        // Allow the text field to grow as needed
        HBox.setHgrow(optionField, javafx.scene.layout.Priority.ALWAYS);
        
        // Add "Add New" guidance tooltip when in read-only mode
        if (!editMode && selectedQuestion == null) {
            optionField.setTooltip(new Tooltip("Click 'Add New' to create a question"));
            
            // Add click handler for guidance
            optionField.setOnMouseClicked(event -> {
                if (!editMode && selectedQuestion == null && !optionField.isEditable()) {
                    MsgSender.showMsg("Click 'Add New' to create a question");
                    
                    // Highlight the Add New button
                    String originalStyle = addBtn.getStyle();
                    addBtn.setStyle(originalStyle + " -fx-background-color: #69F0AE; -fx-scale-x: 1.1; -fx-scale-y: 1.1;");
                    
                    // Reset style after a short delay
                    new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    addBtn.setStyle(originalStyle);
                                });
                            }
                        }, 
                        1500 // Reset after 1.5 seconds
                    );
                }
            });
        }
        
        optionFields.add(optionField);
        
        // Add the label and field to the option box
        optionBox.getChildren().addAll(optionLabel, optionField);
        
        // Add delete button if we have more than the minimum number of options
        if (optionFields.size() > MIN_OPTIONS) {
            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("login-button");
            final int optionIndex = index;
            deleteBtn.setOnAction(e -> handleDeleteOption(optionIndex));
            optionBox.getChildren().add(deleteBtn);
        }
        
        // Add option box to the container
        optionsContainer.getChildren().add(optionBox);
        
        // Update Add Option button visibility
        addOptionBtn.setDisable(optionFields.size() >= MAX_OPTIONS);
    }
    
    /**
     * Handles adding a new option.
     */
    @FXML
    public void handleAddOption() {
        if (optionFields.size() < MAX_OPTIONS) {
            addOptionFieldToUI(optionFields.size(), "");
        }
    }
    
    /**
     * Handles deleting an option.
     * @param index The index of the option to delete.
     */
    private void handleDeleteOption(int index) {
        if (optionFields.size() <= MIN_OPTIONS) {
            return; // Don't allow fewer than MIN_OPTIONS
        }
        
        // Remove from UI
        optionsContainer.getChildren().remove(index);
        
        // Remove from our list
        optionFields.remove(index);
        
        // Update all remaining option labels (A, B, C, D, E)
        for (int i = index; i < optionsContainer.getChildren().size(); i++) {
            HBox optionBox = (HBox) optionsContainer.getChildren().get(i);
            Label label = (Label) optionBox.getChildren().get(0);
            label.setText("Option " + (char)('A' + i) + ":");
            
            // Update delete button action if present
            if (optionBox.getChildren().size() > 2) {
                Button deleteBtn = (Button) optionBox.getChildren().get(2);
                final int optionIndex = i;
                deleteBtn.setOnAction(e -> handleDeleteOption(optionIndex));
            }
        }
        
        // Update Add Option button visibility
        addOptionBtn.setDisable(optionFields.size() >= MAX_OPTIONS);
    }

    private void updateOptionFieldsVisibility(String selectedType) {
        boolean isMCQ = "MCQ".equalsIgnoreCase(selectedType);
        options.setManaged(isMCQ);
        options.setVisible(isMCQ);
        
        // Ensure options are properly shown for MCQ questions
        if (isMCQ) {
            // If question is MCQ but no option fields, initialize them
            if (optionFields.isEmpty()) {
                initializeOptions(MIN_OPTIONS);
            }
            
            // Ensure the options container is visible
            optionsContainer.setVisible(true);
            optionsContainer.setManaged(true);
            
            // Make add option button available based on option count, regardless of edit mode
            // The actual add functionality will still only work in edit mode (controlled in setFormFieldsEditable)
            addOptionBtn.setDisable(optionFields.size() >= MAX_OPTIONS);
        }
    }

    private void fillFieldsFromSelectedQuestion(Question question) {
        // If null question, clear fields and return
        if (question == null) {
            clearFormFields();
            return;
        }
        
        try {
            // Make fields editable if in edit mode
            setFormFieldsEditable(editMode);
            
            // Clear any existing selection events first
            questionsTable.getSelectionModel().clearSelection();
            
            // Set text fields safely
            questionTxt.setText(question.getQuestionText() != null ? question.getQuestionText() : "");
            scoreTxt.setText(question.getScore() != null ? String.valueOf(question.getScore()) : "");
            answerTxt.setText(question.getAnswer() != null ? question.getAnswer() : "");
            
            final String questionType = question.getType() != null ? question.getType() : "MCQ";
            
            // Set type in the UI thread to ensure proper event handling
            Platform.runLater(() -> {
                typeCmb.setValue(questionType);
                
                // Handle options for MCQ questions
                if ("MCQ".equalsIgnoreCase(questionType)) {
                    // Get the options from the question
                    List<String> options = question.getOptions();
                    
                    // If options list is empty but we have legacy fields, try to use those
                    if (options.isEmpty()) {
                        // Create a new list with legacy fields
                        options = new ArrayList<>();
                        if (question.getOptionA() != null && !question.getOptionA().isEmpty()) options.add(question.getOptionA());
                        if (question.getOptionB() != null && !question.getOptionB().isEmpty()) options.add(question.getOptionB());
                        if (question.getOptionC() != null && !question.getOptionC().isEmpty()) options.add(question.getOptionC());
                        if (question.getOptionD() != null && !question.getOptionD().isEmpty()) options.add(question.getOptionD());
                        if (question.getOptionE() != null && !question.getOptionE().isEmpty()) options.add(question.getOptionE());
                    }
                    
                    // Ensure we have at least MIN_OPTIONS
                    int optionCount = Math.max(options.size(), MIN_OPTIONS);
                    
                    // Clear existing options container and rebuild
                    optionsContainer.getChildren().clear();
                    optionFields.clear();
                    
                    // Create fresh option fields
                    for (int i = 0; i < optionCount; i++) {
                        String optionValue = i < options.size() ? options.get(i) : "";
                        addOptionFieldToUI(i, optionValue);
                    }
                    
                    // Update add option button state based on edit mode
                    addOptionBtn.setDisable(!editMode || optionFields.size() >= MAX_OPTIONS);
                    
                    // Show options UI for MCQ questions
                    this.options.setVisible(true);
                    this.options.setManaged(true);
                    optionsContainer.setVisible(true);
                    optionsContainer.setManaged(true);
                } else {
                    // For non-MCQ questions, hide options section
                    this.options.setVisible(false);
                    this.options.setManaged(false);
                }
                
                // Update form based on question type
                updateOptionFieldsVisibility(questionType);
                updateAnswerHint(questionType);
                
                // Only select the question in the table if not in edit mode
                if (!editMode) {
                    for (int i = 0; i < questionsTable.getItems().size(); i++) {
                        Question q = questionsTable.getItems().get(i);
                        if (q != null && question.getId() != null && question.getId().equals(q.getId())) {
                            questionsTable.getSelectionModel().select(i);
                            questionsTable.scrollTo(i);
                            break;
                        }
                    }
                }
                
                // If in edit mode, disable table selection
                questionsTable.setDisable(editMode);
                
                // Update button states
                if (updateBtn != null) updateBtn.setDisable(!editMode);
                if (editBtn != null) editBtn.setDisable(editMode || (selectedQuestion != null && selectedQuestion.getPublished() != 0));
            });
        } catch (Exception e) {
            System.err.println("Error filling form: " + e.getMessage());
            e.printStackTrace();
            clearFormFields(); // Fallback to clearing the form
        }
    }

    @FXML
    public void handleReset() {
        filterQuestionTxt.clear();
        filterScoreTxt.clear();
        filterTypeCmb.setValue("Any");
        handleRefreshWithoutMsg();
    }

    @FXML
    public void handleFilter() {
        String questionKeyword = filterQuestionTxt.getText().trim().toLowerCase();
        String typeSelected = filterTypeCmb.getValue();
        String scoreText = filterScoreTxt.getText().trim();

        Database<Question> temp = new Database<>(Question.class);
        List<Question> questionDB = temp.queryByField("teacherId", Long.toString(teacher.getId()));

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

        List<Question> filteredList = questionDB.stream()
                .filter(q -> q.getQuestionText() != null && q.getQuestionText().toLowerCase().contains(questionKeyword))
                .filter(q -> typeSelected.equals("Any") || (q.getType() != null && q.getType().equals(typeSelected)))
                .filter(q -> finalScoreFilter == -1 || (q.getScore() != null && q.getScore() == finalScoreFilter))
                .collect(Collectors.toList());

        questionsTable.getItems().clear();
        questionsTable.getItems().addAll(filteredList);
    }

    @FXML
    public void handleDelete() {
        Question selectedQuestion = questionsTable.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            MsgSender.showMsg("No question selected to delete.");
            return;
        } else if (selectedQuestion.getPublished() != 0) {
            MsgSender.showMsg("Published question cannot be deleted.");
            return;
        }

        // Show confirmation dialog using the correct method
        MsgSender.showConfirm(
            "Delete Question", 
            "Are you sure you want to delete this question?\nThis action cannot be undone.",
            () -> {
                Database<Question> questionDB = new Database<>(Question.class);
                Object idObj = selectedQuestion.getId();
                if (idObj == null) {
                    MsgSender.showMsg("Selected question has no ID.");
                    return;
                }
                String idStr = idObj.toString();
                
                // Store the ID for a potential refresh check
                final Long deletedQuestionId = selectedQuestion.getId();
                
                // Get the index of the selected question first
                int selectedIndex = questionsTable.getSelectionModel().getSelectedIndex();
                
                // Clear selected question reference and form
                this.selectedQuestion = null;
                clearFormFields();
                
                // If we have a valid index, remove by index instead of object
                if (selectedIndex >= 0) {
                    questionsTable.getItems().remove(selectedIndex);
                    questionsTable.refresh();
                }
                
                // Then delete from database
                questionDB.delByKey(idStr);
                
                // Perform a verification to ensure the question was actually deleted
                Platform.runLater(() -> {
                    Question checkDeleted = questionDB.queryByKey(deletedQuestionId.toString());
                    if (checkDeleted != null) {
                        // If question still exists, try refreshing the entire list
                        handleRefreshWithoutMsg();
                    }
                    
                    // Show success message
                    MsgSender.showMsg("Question deleted successfully.");
                });
            }
        );
    }

    @FXML
    public void handleHardRefresh() {
        // Clear all selections and data first
        questionsTable.getSelectionModel().clearSelection();
        questionsTable.getItems().clear();
        clearFormFields();
        
        // Force garbage collection to free resources
        System.gc();
        
        // Create a fresh database instance
        Database<Question> freshDB = new Database<>(Question.class);
        
        // Forcefully load all questions for this teacher
        List<Question> allQuestions = freshDB.queryByField("teacherId", Long.toString(this.teacher.getId()));
        
        // Populate the table with fresh data
        questionsTable.getItems().addAll(allQuestions);
        questionsTable.refresh();
        
        MsgSender.showMsg("Hard refresh complete. Loaded " + allQuestions.size() + " questions.");
    }

    @FXML
    public void handleRefresh() {
        // Check if teacher is null and handle it
        if (this.teacher == null) {
            MsgSender.showMsg("Error: Teacher information not available. Please log in again.");
            System.err.println("Teacher object is null in handleRefresh");
            
            // Attempt to recover by returning to login screen
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/comp3111/examsystem/LoginUI.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) mainbox.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                System.err.println("Failed to return to login screen: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }
        
        // Save selected item and selection index if any
        Question selectedItem = questionsTable.getSelectionModel().getSelectedItem();
        Long selectedId = selectedItem != null ? selectedItem.getId() : null;
        
        // Track initial question count for verification
        int initialCount = questionsTable.getItems().size();
        
        // Create new database instance to ensure fresh data
        Database<Question> questionDB = new Database<>(Question.class);
        
        try {
            // Force reload all questions for this teacher from the database
            List<Question> freshQuestions = questionDB.queryByField("teacherId", Long.toString(this.teacher.getId()));
            
            // Check if we got reasonable data
            if (freshQuestions == null || (initialCount > 0 && freshQuestions.isEmpty())) {
                // Something went wrong - try a harder refresh
                MsgSender.showMsg("Regular refresh failed. Trying hard refresh...");
                handleHardRefresh();
                return;
            }
            
            // Store original selection index for later restoration
            int originalIndex = questionsTable.getSelectionModel().getSelectedIndex();
            
            // Clear selection first to avoid any selection events during refresh
            questionsTable.getSelectionModel().clearSelection();
            
            // Clear and refill the table
            questionsTable.getItems().clear();
            
            // Add the fresh data
            questionsTable.getItems().addAll(freshQuestions);
            
            // Force JavaFX to process the update now
            Platform.runLater(() -> {
                // Force table refresh
                questionsTable.refresh();
                
                // Attempt to restore selection in multiple ways for robustness
                if (selectedId != null) {
                    // Try to find the question with same ID
                    for (int i = 0; i < questionsTable.getItems().size(); i++) {
                        Question q = questionsTable.getItems().get(i);
                        if (q != null && selectedId.equals(q.getId())) {
                            questionsTable.getSelectionModel().select(i);
                            questionsTable.scrollTo(i);
                            // Reapply the form data from the freshly loaded question
                            fillFieldsFromSelectedQuestion(q);
                            return; // Selection restored
                        }
                    }
                }
                
                // If we couldn't restore by ID, try by index if it's valid
                if (originalIndex >= 0 && originalIndex < questionsTable.getItems().size()) {
                    questionsTable.getSelectionModel().select(originalIndex);
                    questionsTable.scrollTo(originalIndex);
                    Question selected = questionsTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        selectedQuestion = selected;
                        fillFieldsFromSelectedQuestion(selected);
                    } else {
                        // If selection is invalid, clear the form
                        clearFormFields();
                    }
                } else {
                    // If no selection could be restored, clear the form
                    clearFormFields();
                }
            });
            
            // Provide user feedback
            MsgSender.showMsg("Question bank refreshed successfully!");
        } catch (Exception e) {
            System.err.println("Error refreshing questions: " + e.getMessage());
            e.printStackTrace();
            MsgSender.showMsg("Error refreshing questions: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefreshWithoutMsg() {
        // Check if teacher is null and handle it
        if (this.teacher == null) {
            MsgSender.showMsg("Error: Teacher information not available. Please log in again.");
            System.err.println("Teacher object is null in handleRefresh");

            // Attempt to recover by returning to login screen
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/comp3111/examsystem/LoginUI.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) mainbox.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                System.err.println("Failed to return to login screen: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        // Save selected item and selection index if any
        Question selectedItem = questionsTable.getSelectionModel().getSelectedItem();
        Long selectedId = selectedItem != null ? selectedItem.getId() : null;

        // Track initial question count for verification
        int initialCount = questionsTable.getItems().size();

        // Create new database instance to ensure fresh data
        Database<Question> questionDB = new Database<>(Question.class);

        try {
            // Force reload all questions for this teacher from the database
            List<Question> freshQuestions = questionDB.queryByField("teacherId", Long.toString(this.teacher.getId()));

            // Check if we got reasonable data
            if (freshQuestions == null || (initialCount > 0 && freshQuestions.isEmpty())) {
                // Something went wrong - try a harder refresh
                MsgSender.showMsg("Regular refresh failed. Trying hard refresh...");
                handleHardRefresh();
                return;
            }

            // Store original selection index for later restoration
            int originalIndex = questionsTable.getSelectionModel().getSelectedIndex();

            // Clear selection first to avoid any selection events during refresh
            questionsTable.getSelectionModel().clearSelection();

            // Clear and refill the table
            questionsTable.getItems().clear();

            // Add the fresh data
            questionsTable.getItems().addAll(freshQuestions);

            // Force JavaFX to process the update now
            Platform.runLater(() -> {
                // Force table refresh
                questionsTable.refresh();

                // Attempt to restore selection in multiple ways for robustness
                if (selectedId != null) {
                    // Try to find the question with same ID
                    for (int i = 0; i < questionsTable.getItems().size(); i++) {
                        Question q = questionsTable.getItems().get(i);
                        if (q != null && selectedId.equals(q.getId())) {
                            questionsTable.getSelectionModel().select(i);
                            questionsTable.scrollTo(i);
                            // Reapply the form data from the freshly loaded question
                            fillFieldsFromSelectedQuestion(q);
                            return; // Selection restored
                        }
                    }
                }

                // If we couldn't restore by ID, try by index if it's valid
                if (originalIndex >= 0 && originalIndex < questionsTable.getItems().size()) {
                    questionsTable.getSelectionModel().select(originalIndex);
                    questionsTable.scrollTo(originalIndex);
                    Question selected = questionsTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        selectedQuestion = selected;
                        fillFieldsFromSelectedQuestion(selected);
                    } else {
                        // If selection is invalid, clear the form
                        clearFormFields();
                    }
                } else {
                    // If no selection could be restored, clear the form
                    clearFormFields();
                }
            });

        } catch (Exception e) {
            System.err.println("Error refreshing questions: " + e.getMessage());
            e.printStackTrace();
            MsgSender.showMsg("Error refreshing questions: " + e.getMessage());
        }
    }

    @FXML
    public void handleAdd() {
        // First clear form and selection to start fresh
        clearFormFields();
        
        // Set to add mode (not edit mode)
        editMode = false;
        
        // Clear selected question and selection in table
        selectedQuestion = null;
        questionsTable.getSelectionModel().clearSelection();
        
        // Disable table to prevent confusion during add
        questionsTable.setDisable(true);
        
        // Enable form fields for adding new question
        setFormFieldsEditable(true);
        
        // Update button states for add mode
        if (addBtn != null) addBtn.setDisable(true);
        if (updateBtn != null) updateBtn.setDisable(false);
        if (cancelEditBtn != null) cancelEditBtn.setDisable(false);
        if (editBtn != null) editBtn.setDisable(true);
        
        MsgSender.showMsg("Fill in the form and click 'Save Changes' to add a new question.");
        return;
    }

    @FXML
    public void handleUpdate() {
        // Common validation for both adding and updating
        String questionText = questionTxt.getText().trim();
        String type = typeCmb.getValue();
        String scoreText = scoreTxt.getText().trim();
        String answer = answerTxt.getText().trim();

        if (questionText.isEmpty() || type == null || type.isEmpty() || scoreText.isEmpty() || answer.isEmpty()) {
            MsgSender.showMsg("Please fill in all required fields.");
            return;
        }
        
        // For MCQ, validate options
        List<String> optionValues = new ArrayList<>();
        if ("MCQ".equalsIgnoreCase(type)) {
            for (TextField field : optionFields) {
                String value = field.getText().trim();
                if (value.isEmpty()) {
                    MsgSender.showMsg("Please fill in all MCQ options.");
                    return;
                }
                optionValues.add(value);
            }
            
            // Validate that answer matches one of the options
            boolean validAnswer = false;
            for (int i = 0; i < optionValues.size(); i++) {
                String optionLetter = String.valueOf((char)('A' + i));
                if (answer.equalsIgnoreCase(optionLetter)) {
                    validAnswer = true;
                    break;
                }
            }
            
            if (!validAnswer) {
                MsgSender.showMsg("Answer must be one of the option letters (A, B, C, etc.)");
                return;
            }
        }

        int score;
        try {
            score = Integer.parseInt(scoreText);
            if (score < 0) {
                MsgSender.showMsg("Score must be a non-negative integer.");
                return;
            }
        } catch (NumberFormatException e) {
            MsgSender.showMsg("Score should be an integer.");
            return;
        }
        
        // Determine if we're adding a new question or updating an existing one
        boolean isAdding = !editMode && selectedQuestion == null;

        if (isAdding) {
            // Adding a new question
            addNewQuestion(questionText, type, score, answer, optionValues);
        } else {
            // Updating an existing question - validate edit mode
            if (!editMode) {
                MsgSender.showMsg("Please click Edit first to modify a question.");
                return;
            }
            
            if (selectedQuestion == null) {
                MsgSender.showMsg("No question selected to update.");
                editMode = false;
                setFormFieldsEditable(false);
                questionsTable.setDisable(false);
                return;
            }
            
            if (selectedQuestion.getPublished() != 0) {
                MsgSender.showMsg("Published question cannot be modified.");
                editMode = false;
                setFormFieldsEditable(false);
                questionsTable.setDisable(false);
                return;
            }

            // Store the index for re-selection
            int selectedIndex = questionsTable.getSelectionModel().getSelectedIndex();
            
            // Store question ID for later verification
            final Long questionId = selectedQuestion.getId();
            
            // Create a copy of the question to avoid race conditions
            final Question questionToUpdate = selectedQuestion;
            
            // Update question properties
            questionToUpdate.setQuestionText(questionText);
            questionToUpdate.setType(type);
            questionToUpdate.setScore(score);
            questionToUpdate.setAnswer(answer);
            
            // For MCQ, set the options
            if ("MCQ".equalsIgnoreCase(type)) {
                questionToUpdate.setOptions(optionValues);
            } else {
                // Clear options for non-MCQ questions
                questionToUpdate.setOptions(new ArrayList<>());
            }

            // Update in the database
            Database<Question> questionDB = new Database<>(Question.class);
            questionDB.update(questionToUpdate);

            // Exit edit mode
            editMode = false;
            
            // First clear the form to prevent UI conflicts
            Platform.runLater(() -> {
                try {
                    // Enable table selection again
                    questionsTable.setDisable(false);
                    
                    // Set form back to read-only mode
                    setFormFieldsEditable(false);
                    
                    // Temporarily clear selection to avoid event conflicts
                    questionsTable.getSelectionModel().clearSelection();
                    
                    // Clear the form
                    clearFormFields();
                    
                    // Reload the question from the database to ensure we have all properties
                    if (questionId != null) {
                        Question updatedQuestion = questionDB.queryByKey(questionId.toString());
                        if (updatedQuestion != null) {
                            // Replace in the table
                            if (selectedIndex >= 0 && selectedIndex < questionsTable.getItems().size()) {
                                questionsTable.getItems().set(selectedIndex, updatedQuestion);
                                
                                // Force refresh the table to show updated data
                                questionsTable.refresh();
                                
                                // Display success message
                                MsgSender.showMsg("Question updated successfully!");
                                
                                // Wait a moment to let UI settle, then reselect the question
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    // Ignore
                                }
                                
                                // Select the updated question
                                questionsTable.getSelectionModel().select(selectedIndex);
                                questionsTable.scrollTo(selectedIndex);
                                
                                // Display the question in read-only mode
                                selectedQuestion = updatedQuestion;
                                displaySelectedQuestionReadOnly(updatedQuestion);
                                
                                // Update edit button state
                                if (editBtn != null) {
                                    editBtn.setDisable(false);
                                }
                            } else {
                                // If index is out of bounds, do a full refresh
                                handleHardRefresh();
                                MsgSender.showMsg("Question updated successfully!");
                            }
                        } else {
                            // If question not found, refresh the entire table
                            handleHardRefresh();
                            MsgSender.showMsg("Question updated - refresh completed.");
                        }
                    } else {
                        // No valid ID, refresh everything
                        handleHardRefresh();
                        MsgSender.showMsg("Question update attempted - full refresh completed.");
                    }
                } catch (Exception e) {
                    System.err.println("Error in handleUpdate: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Exit edit mode and re-enable table
                    editMode = false;
                    questionsTable.setDisable(false);
                    setFormFieldsEditable(false);
                    
                    handleHardRefresh();
                }
            });
        }
    }
    
    /**
     * Helper method to add a new question to the database
     */
    private void addNewQuestion(String questionText, String type, int score, String answer, List<String> optionValues) {
        Long teacherID = teacher.getId();
        Long ID = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        
        Question newQuestion = new Question();
        newQuestion.setId(ID);
        newQuestion.setTeacherId(teacherID);
        newQuestion.setQuestionText(questionText);
        newQuestion.setType(type);
        newQuestion.setScore(score);
        newQuestion.setAnswer(answer);
        
        // For MCQ, set the options
        if ("MCQ".equalsIgnoreCase(type)) {
            newQuestion.setOptions(optionValues);
        }

        // Add to database first
        Database<Question> questionDB = new Database<>(Question.class);
        questionDB.add(newQuestion);

        // Use Platform.runLater to update UI safely
        Platform.runLater(() -> {
            try {
                // First clear selection to avoid side effects
                questionsTable.getSelectionModel().clearSelection();
                
                // Enable table selection
                questionsTable.setDisable(false);
                
                // Then add to table
                questionsTable.getItems().add(newQuestion);
                questionsTable.refresh();
                
                // Clear the form completely
                clearFormFields();
                
                // Return to read-only mode
                setFormFieldsEditable(false);
                
                // Reset edit mode flag
                editMode = false;
                
                // Display success message
                MsgSender.showMsg("Question added successfully!");
            } catch (Exception e) {
                System.err.println("Error updating UI after adding question: " + e.getMessage());
                e.printStackTrace();
                // Try a hard refresh as fallback
                handleHardRefresh();
            }
        });
    }

    @FXML
    public void handleBack() {
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

    @FXML
    public void handleCloseApplication() {
        Platform.exit();
        System.exit(0);
    }

    private void clearFormFields() {
        try {
            // First clear the selection to avoid selection events
            if (questionsTable != null) {
                questionsTable.getSelectionModel().clearSelection();
            }
            
            // Exit edit mode
            editMode = false;
            
            // Clear text fields
            if (questionTxt != null) questionTxt.clear();
            if (scoreTxt != null) scoreTxt.clear();
            if (answerTxt != null) answerTxt.clear();
            
            // Clear selected question reference
            selectedQuestion = null;
            
            // Completely rebuild the options section
            if (optionsContainer != null) {
                // Remove all existing option fields
                optionsContainer.getChildren().clear();
                
                // Clear the fields list
                if (optionFields != null) {
                    optionFields.clear();
                }
                
                // Recreate from scratch with empty fields
                initializeOptions(MIN_OPTIONS);
            }
            
            // Set to default type if the combo box is initialized
            if (typeCmb != null) {
                Platform.runLater(() -> {
                    typeCmb.setValue("MCQ");
                    // Update UI for MCQ
                    updateOptionFieldsVisibility("MCQ");
                    updateAnswerHint("MCQ");
                });
            }
            
            // Update the Add Option button state to reflect current options count
            if (addOptionBtn != null) {
                addOptionBtn.setDisable(optionFields.size() >= MAX_OPTIONS);
            }
            
            // Enable table selection
            if (questionsTable != null) {
                questionsTable.setDisable(false);
            }
            
            // Update button states
            if (addBtn != null) addBtn.setDisable(false);
            if (updateBtn != null) updateBtn.setDisable(true);
            if (editBtn != null) editBtn.setDisable(true);
            if (cancelEditBtn != null) cancelEditBtn.setDisable(true);
            
            // Set form to read-only mode
            setFormFieldsEditable(false);
        } catch (Exception e) {
            System.err.println("Error clearing form fields: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the hint/label for the answer field based on question type
     * @param questionType The type of question
     */
    private void updateAnswerHint(String questionType) {
        // Find the answer label (which is right before the answer text field)
        VBox form = (VBox) answerTxt.getParent();
        int answerLabelIndex = form.getChildren().indexOf(answerTxt) - 1;
        
        if (answerLabelIndex >= 0 && form.getChildren().get(answerLabelIndex) instanceof Label) {
            Label answerLabel = (Label) form.getChildren().get(answerLabelIndex);
            
            if ("MCQ".equalsIgnoreCase(questionType)) {
                answerLabel.setText("Answer (enter option letter A, B, C, etc.):");
            } else {
                answerLabel.setText("Answer:");
            }
        }
    }

    /**
     * Displays a question in read-only mode (for viewing only)
     * @param question The question to display
     */
    private void displaySelectedQuestionReadOnly(Question question) {
        // If null question, clear fields and return
        if (question == null) {
            clearFormFields();
            return;
        }
        
        try {
            // Make fields read-only
            setFormFieldsEditable(false);
            
            // Set text fields safely
            questionTxt.setText(question.getQuestionText() != null ? question.getQuestionText() : "");
            scoreTxt.setText(question.getScore() != null ? String.valueOf(question.getScore()) : "");
            answerTxt.setText(question.getAnswer() != null ? question.getAnswer() : "");
            
            final String questionType = question.getType() != null ? question.getType() : "MCQ";
            
            // Set type in the UI thread to ensure proper event handling
            typeCmb.setValue(questionType);
            
            // Handle options for MCQ questions
            if ("MCQ".equalsIgnoreCase(questionType)) {
                // Get the options from the question
                List<String> options = question.getOptions();
                
                // If options list is empty but we have legacy fields, try to use those
                if (options.isEmpty()) {
                    // Create a new list with legacy fields
                    options = new ArrayList<>();
                    if (question.getOptionA() != null && !question.getOptionA().isEmpty()) options.add(question.getOptionA());
                    if (question.getOptionB() != null && !question.getOptionB().isEmpty()) options.add(question.getOptionB());
                    if (question.getOptionC() != null && !question.getOptionC().isEmpty()) options.add(question.getOptionC());
                    if (question.getOptionD() != null && !question.getOptionD().isEmpty()) options.add(question.getOptionD());
                    if (question.getOptionE() != null && !question.getOptionE().isEmpty()) options.add(question.getOptionE());
                }
                
                // Ensure we have at least MIN_OPTIONS
                int optionCount = Math.max(options.size(), MIN_OPTIONS);
                
                // Clear existing options container and rebuild
                optionsContainer.getChildren().clear();
                optionFields.clear();
                
                // Create fresh option fields
                for (int i = 0; i < optionCount; i++) {
                    String optionValue = i < options.size() ? options.get(i) : "";
                    addOptionFieldToUI(i, optionValue);
                }
                
                // Show options UI for MCQ questions
                this.options.setVisible(true);
                this.options.setManaged(true);
                optionsContainer.setVisible(true);
                optionsContainer.setManaged(true);
            } else {
                // For non-MCQ questions, hide options section
                this.options.setVisible(false);
                this.options.setManaged(false);
            }
            
            // Update form based on question type
            updateOptionFieldsVisibility(questionType);
            updateAnswerHint(questionType);
            
            // Update Edit button state specifically
            if (editBtn != null) {
                editBtn.setDisable(question.getPublished() != 0);
            }
            
            // Make sure the table has this question selected
            int index = questionsTable.getItems().indexOf(question);
            if (index >= 0) {
                questionsTable.getSelectionModel().select(index);
                questionsTable.scrollTo(index);
            }
        } catch (Exception e) {
            System.err.println("Error displaying question: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets whether form fields are editable or read-only
     * @param editable True to make editable, false for read-only
     */
    private void setFormFieldsEditable(boolean editable) {
        // Set field editability
        questionTxt.setEditable(editable);
        scoreTxt.setEditable(editable);
        answerTxt.setEditable(editable);
        // Keep type ComboBox always enabled
        // typeCmb.setDisable(!editable);
        addOptionBtn.setDisable(!editable);
        
        // Update form header with prompt
        updateFormHeaderPrompt(editable);
        
        // Add tooltips to guide users when in read-only mode
        String tooltip = editable ? null : "Click 'Add New' to create a new question";
        questionTxt.setTooltip(tooltip != null ? new Tooltip(tooltip) : null);
        scoreTxt.setTooltip(tooltip != null ? new Tooltip(tooltip) : null);
        answerTxt.setTooltip(tooltip != null ? new Tooltip(tooltip) : null);
        typeCmb.setTooltip(tooltip != null ? new Tooltip(tooltip) : null);
        
        // Visual cues for read-only state
        String style = editable ? "" : "-fx-background-color: #f5f5f5;";
        questionTxt.setStyle(style);
        scoreTxt.setStyle(style);
        answerTxt.setStyle(style);
        
        // Make option fields read-only/editable
        for (TextField field : optionFields) {
            field.setEditable(editable);
            field.setTooltip(tooltip != null ? new Tooltip(tooltip) : null);
            field.setStyle(style);
        }
        
        // Update the delete buttons on option fields
        for (Node node : optionsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox box = (HBox) node;
                for (Node child : box.getChildren()) {
                    if (child instanceof Button) {
                        child.setDisable(!editable);
                    }
                }
            }
        }
        
        // Update button states based on current mode
        if (editable) {
            // In editing mode (either adding new or editing existing)
            if (addBtn != null) addBtn.setDisable(true);
            if (updateBtn != null) updateBtn.setDisable(false);
            if (editBtn != null) editBtn.setDisable(true);
            if (cancelEditBtn != null) cancelEditBtn.setDisable(false);
        } else {
            // In viewing mode
            if (addBtn != null) addBtn.setDisable(false);
            
            boolean hasSelection = selectedQuestion != null;
            boolean isPublished = hasSelection && selectedQuestion.getPublished() != 0;
            
            if (updateBtn != null) updateBtn.setDisable(true);
            if (editBtn != null) editBtn.setDisable(!hasSelection || isPublished);
            if (cancelEditBtn != null) cancelEditBtn.setDisable(true);
        }
    }
    
    /**
     * Updates the form header to show a prompt when in read-only mode
     * @param editable Whether the form is currently editable
     */
    private void updateFormHeaderPrompt(boolean editable) {
        try {
            // Find the form header label
            VBox form = (VBox) questionTxt.getParent();
            if (form != null && form.getChildren().size() > 0) {
                Node firstNode = form.getChildren().get(0);
                if (firstNode instanceof HBox) {
                    HBox headerBox = (HBox) firstNode;
                    for (Node node : headerBox.getChildren()) {
                        if (node instanceof Label) {
                            Label headerLabel = (Label) node;
                            if (editable) {
                                // In edit mode, show normal header
                                headerLabel.setText("Question Details");
                                headerLabel.setStyle("-fx-font-size: 16px;");
                            } else {
                                // In read-only mode, show prompt
                                headerLabel.setText("Question Details (Click 'Add New' to create)");
                                headerLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1976D2;");
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating form header: " + e.getMessage());
        }
    }

    /**
     * Handles the Edit button click to enable editing of the selected question
     */
    @FXML
    public void handleEdit() {
        if (selectedQuestion == null) {
            MsgSender.showMsg("No question selected to edit.");
            return;
        }
        
        if (selectedQuestion.getPublished() != 0) {
            MsgSender.showMsg("Published questions cannot be modified.");
            return;
        }
        
        // Enter edit mode
        editMode = true;
        
        // Make form fields editable
        setFormFieldsEditable(true);
        
        // Fill fields with the selected question for editing (not needed if already filled)
        // fillFieldsFromSelectedQuestion(selectedQuestion);
        
        // Disable table selection while editing
        questionsTable.setDisable(true);
        
        // Update button states
        if (addBtn != null) addBtn.setDisable(true);
        if (updateBtn != null) updateBtn.setDisable(false);
        if (editBtn != null) editBtn.setDisable(true);
        if (cancelEditBtn != null) cancelEditBtn.setDisable(false);
        
        MsgSender.showMsg("Now editing question. Make changes and click Save Changes when done.");
    }

    /**
     * Handles canceling the edit operation without saving changes
     */
    @FXML
    public void handleCancelEdit() {
        // If we're not in edit mode, do nothing
        if (!editMode && selectedQuestion != null) {
            return; // Nothing to cancel for viewing existing question
        }
        
        // Exit edit mode
        editMode = false;
        
        // Re-enable table selection
        questionsTable.setDisable(false);
        
        // Set form fields to read-only
        setFormFieldsEditable(false);
        
        // If selectedQuestion is null, we were in add mode, just clear the form
        if (selectedQuestion == null) {
            clearFormFields();
            MsgSender.showMsg("Adding new question cancelled.");
            return;
        }
        
        // Otherwise, we were editing an existing question, reload it
        // Fetch a fresh copy from the database to discard any edits
        Database<Question> questionDB = new Database<>(Question.class);
        Question freshQuestion = questionDB.queryByKey(selectedQuestion.getId().toString());
        if (freshQuestion != null) {
            // Display the fresh question data
            displaySelectedQuestionReadOnly(freshQuestion);
            
            // Enable edit button
            if (editBtn != null) {
                editBtn.setDisable(freshQuestion.getPublished() != 0);
            }
            
            MsgSender.showMsg("Edit cancelled. No changes were saved.");
        } else {
            // If question not found, clear the form
            clearFormFields();
            MsgSender.showMsg("Question could not be found. Edit cancelled.");
        }
    }

    /**
     * Adds click handlers to all form fields to guide users when fields are read-only
     */
    private void addClickHandlersToFormFields() {
        // Create handler for each form field
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> clickHandler = event -> {
            if (!editMode && selectedQuestion == null) {
                Node source = (Node) event.getSource();
                if (!source.isDisabled() && !((source instanceof TextInputControl) && ((TextInputControl)source).isEditable())) {
                    // Show message to guide the user
                    MsgSender.showMsg("Click 'Add New' to create a question");
                    
                    // Highlight the Add New button
                    String originalStyle = addBtn.getStyle();
                    addBtn.setStyle(originalStyle + " -fx-background-color: #69F0AE; -fx-scale-x: 1.1; -fx-scale-y: 1.1;");
                    
                    // Reset style after a short delay
                    new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    addBtn.setStyle(originalStyle);
                                });
                            }
                        }, 
                        1500 // Reset after 1.5 seconds
                    );
                }
            }
        };
        
        // Apply handler to all form fields except the type ComboBox
        questionTxt.setOnMouseClicked(clickHandler);
        scoreTxt.setOnMouseClicked(clickHandler);
        answerTxt.setOnMouseClicked(clickHandler);
    }
}