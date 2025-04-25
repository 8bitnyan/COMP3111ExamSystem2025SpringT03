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

    @FXML private VBox mainbox;

    @FXML private TextField filterQuestionTxt;
    @FXML private ComboBox<String> filterTypeCmb;
    @FXML private TextField filterScoreTxt;

    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, String> colQuestion, colOptionA, colOptionB, colOptionC, colOptionD, colType, colAnswer;
    @FXML private TableColumn<Question, Integer> colScore;

    @FXML private ComboBox<String> typeCmb;
    @FXML private VBox options;
    @FXML private TextField questionTxt, scoreTxt, ATxt, BTxt, CTxt, DTxt, answerTxt;

    /**
     * Sets the teacher object and initializes the UI.
     * @param teacher The teacher object that is operating the page.
     */
    public void presetController(Teacher teacher) {
        this.teacher = teacher;
        handleRefresh();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        filterTypeCmb.setItems(FXCollections.observableArrayList("Any", "MCQ", "Short Answer"));
        filterTypeCmb.setValue("Any");
        typeCmb.setItems(FXCollections.observableArrayList("MCQ", "Short Answer"));
        questionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colQuestion.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colOptionA.setCellValueFactory(new PropertyValueFactory<>("optionA"));
        colOptionB.setCellValueFactory(new PropertyValueFactory<>("optionB"));
        colOptionC.setCellValueFactory(new PropertyValueFactory<>("optionC"));
        colOptionD.setCellValueFactory(new PropertyValueFactory<>("optionD"));
        colAnswer.setCellValueFactory(new PropertyValueFactory<>("answer"));

        updateOptionFieldsVisibility(typeCmb.getValue());

        typeCmb.setOnAction(e -> {
            String selected = typeCmb.getValue();
            updateOptionFieldsVisibility(selected);
        });

        questionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedQuestion = newSelection;
                fillFieldsFromSelectedQuestion(newSelection);
            }
        });
    }

    private void updateOptionFieldsVisibility(String selectedType) {
        boolean isMCQ = "MCQ".equalsIgnoreCase(selectedType);
        options.setManaged(isMCQ);
        options.setVisible(isMCQ);
        if(!isMCQ) {
            ATxt.clear();
            BTxt.clear();
            CTxt.clear();
            DTxt.clear();
        }
    }

    private void fillFieldsFromSelectedQuestion(Question question) {
        questionTxt.setText(question.getQuestionText());
        typeCmb.setValue(question.getType());
        scoreTxt.setText(String.valueOf(question.getScore()));
        answerTxt.setText(question.getAnswer());
        ATxt.setText(question.getOptionA());
        BTxt.setText(question.getOptionB());
        CTxt.setText(question.getOptionC());
        DTxt.setText(question.getOptionD());

        updateOptionFieldsVisibility(typeCmb.getValue());
    }


    @FXML
    public void handleReset() {
        filterScoreTxt.clear();
        filterScoreTxt.clear();
        filterTypeCmb.setValue("Any");
    }

    @FXML
    public void handleFilter() {
        String questionKeyword = filterQuestionTxt.getText().trim();
        String typeSelected = filterTypeCmb.getValue();
        String scoreText = filterScoreTxt.getText().trim();

        Database<Question> temp = new Database<>(Question.class);
        List<Question> questionDB = temp.queryByField("TeacherID", Long.toString(teacher.getId()));

        List<Question> filteredList = questionDB.stream()
                .filter(q ->
                        q.getQuestionText().toLowerCase().contains(questionKeyword) &&
                                (typeSelected.equals("Any") || q.getType().equals(typeSelected)) &&
                                (Integer.parseInt(scoreText) == -1 || q.getScore() == Integer.parseInt(scoreText))
                )
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
            MsgSender.showMsg("Published question cannot be delete.");
            return;
        }

        Database<Question> questionDB = new Database<>(Question.class);

        Object idObj = selectedQuestion.getId();
        if (idObj == null) {
            MsgSender.showMsg("Selected question has no ID.");
            return;
        }
        String idStr = idObj.toString();

        questionDB.delByKey(idStr);

        handleRefresh();
        /*
        Database<Question> questionDB2 = new Database<>(Question.class);
        List<Question> allQuestions = questionDB2.getAllEnabled();
        questionsTable.getItems().clear();
        questionsTable.getItems().addAll(allQuestions);
        */

        selectedQuestion = null;
    }

    @FXML
    public void handleRefresh() {
        Database<Question> questionDB = new Database<>(Question.class);
        //List<Question> allQuestions = questionDB.getAllEnabled();
        List<Question> allQuestions = questionDB.queryByField("teacherId", Long.toString(this.teacher.getId()));
        questionsTable.getItems().clear();
        questionsTable.getItems().addAll(allQuestions);
    }

    @FXML
    public void handleAdd() {
        Long teacherID = teacher.getId();
        Long ID = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        String questionText = questionTxt.getText().trim();
        String type = typeCmb.getValue();
        String scoreText = scoreTxt.getText().trim();
        String A = ATxt.getText().trim();
        String B = BTxt.getText().trim();
        String C = CTxt.getText().trim();
        String D = DTxt.getText().trim();
        String answer = answerTxt.getText().trim();

        if (questionText.isEmpty() || type == null || type.isEmpty() || scoreText.isEmpty() || answer.isEmpty()) {
            MsgSender.showMsg("Please fill in all fields.");
            return;
        }
        if ("MCQ".equalsIgnoreCase(type)) {
            if (A.isEmpty() || B.isEmpty() || C.isEmpty() || D.isEmpty()) {
                MsgSender.showMsg("Please fill in all fields.");
                return;
            }
        }

        int score;
        try {
            score = Integer.parseInt(scoreText);
        } catch (NumberFormatException e) {
            MsgSender.showMsg("Score should be an integer.");
            return;
        }

        Question newQuestion = new Question();
        newQuestion.setId(ID);
        newQuestion.setTeacherId(teacherID);
        newQuestion.setQuestionText(questionText);
        newQuestion.setType(type);
        newQuestion.setScore(score);
        newQuestion.setA(A);
        newQuestion.setB(B);
        newQuestion.setC(C);
        newQuestion.setD(D);
        newQuestion.setAnswer(answer);

        Database<Question> questionDB = new Database<>(Question.class);
        questionDB.add(newQuestion);

        questionTxt.clear();
        scoreTxt.clear();
        answerTxt.clear();
        typeCmb.setValue("MCQ");

        handleRefresh();
        /*
        Database<Question> questionDB2 = new Database<>(Question.class);
        List<Question> allQuestions = questionDB2.getAllEnabled();  // 활성화된(삭제되지 않은) 질문들 조회
        questionsTable.getItems().clear();
        questionsTable.getItems().addAll(allQuestions);
        */
    }

    @FXML
    public void handleUpdate() {
        if (selectedQuestion == null) {
            MsgSender.showMsg("Please select a question.");
            return;
        }
        if (selectedQuestion.getPublished() != 0) {
            MsgSender.showMsg("Published question cannot be modified.");
            return;
        }

        String newQuestionText = questionTxt.getText().trim();
        String newType = typeCmb.getValue();
        String newScoreText = scoreTxt.getText().trim();
        String newA = ATxt.getText().trim();
        String newB = BTxt.getText().trim();
        String newC = CTxt.getText().trim();
        String newD = DTxt.getText().trim();
        String newAnswer = answerTxt.getText().trim();

        if (newQuestionText.isEmpty() || newType == null || newType.isEmpty() || newScoreText.isEmpty() || newAnswer.isEmpty()) {
            MsgSender.showMsg("Please fill in all fields.");
            return;
        }
        if ("MCQ".equalsIgnoreCase(newType)) {
            if (newA.isEmpty() || newB.isEmpty() || newC.isEmpty() || newD.isEmpty()) {
                MsgSender.showMsg("Please fill in all fields.");
                return;
            }
        }

        int newScore;
        try {
            newScore = Integer.parseInt(newScoreText);
        } catch (NumberFormatException e) {
            MsgSender.showMsg("Score should be integer.");
            return;
        }

        selectedQuestion.setQuestionText(newQuestionText);
        selectedQuestion.setType(newType);
        selectedQuestion.setScore(newScore);
        selectedQuestion.setA(newA);
        selectedQuestion.setB(newB);
        selectedQuestion.setC(newC);
        selectedQuestion.setD(newD);
        selectedQuestion.setAnswer(newAnswer);

        Database<Question> questionDB = new Database<>(Question.class);
        questionDB.update(selectedQuestion);

        questionTxt.clear();
        scoreTxt.clear();
        answerTxt.clear();
        typeCmb.setValue("MCQ");

        handleRefresh();

        /*
        Database<Question> questionDB2 = new Database<>(Question.class);
        List<Question> allQuestions = questionDB2.getAllEnabled();
        questionsTable.getItems().clear();
        questionsTable.getItems().addAll(allQuestions);
        */

        selectedQuestion = null;
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
}