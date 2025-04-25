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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller for teacher exam management page.
 */
public class TeacherExamMgmtController implements Initializable {
    private Teacher teacher;
    private Exam selectedExam;
    private Question selectedQuestion;

    @FXML private VBox mainbox;

    @FXML private TextField filterQuestionTxt, filterQuestionScoreTxt;
    @FXML private ComboBox<String> filterQuestionTypeCmb;

    @FXML private TextField filterExamNameTxt;
    @FXML private ComboBox<String> filterCourseIDCmb, filterIsPublishedCmb;

    @FXML private TableView<Exam> examTable;
    @FXML private TableColumn<Exam, String> colExamName, colCourseID, colPublish;
    @FXML private TableColumn<Exam, Integer> colExamTime;

    @FXML private TableView<Question> examQuestions;
    @FXML private TableColumn<Question, String> colQInExam, colQInExamType;
    @FXML private TableColumn<Question, Integer> colQInExamScore;

    @FXML private TableView<Question> questions;
    @FXML private TableColumn<Question, String> colQ, colQType;
    @FXML private TableColumn<Question, Integer> colQScore;

    @FXML private TextField examNameTxt, examTimeTxt;
    @FXML private ComboBox<String> courseIDCmb, isPublishedCmb;

    /**
     * Initializes the teacher exam management page UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        filterQuestionTypeCmb.setItems(FXCollections.observableArrayList("Any", "MCQ", "Short Answer"));
        filterIsPublishedCmb.setItems(FXCollections.observableArrayList("Any", "Yes", "No"));
        isPublishedCmb.setItems(FXCollections.observableArrayList("Yes", "No"));

        loadCourseCodes();

        examTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        examQuestions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        questions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colExamName.setCellValueFactory(new PropertyValueFactory<>("examName"));
        colCourseID.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colExamTime.setCellValueFactory(new PropertyValueFactory<>("examTime"));
        colPublish.setCellValueFactory(cellData -> {
            boolean published = cellData.getValue().getIsPublished();
            String displayText = published ? "Yes" : "No";
            return new ReadOnlyStringWrapper(displayText);
        });

        colQInExam.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        colQInExamType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colQInExamScore.setCellValueFactory(new PropertyValueFactory<>("score"));

        colQ.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        colQType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colQScore.setCellValueFactory(new PropertyValueFactory<>("score"));

        Database<Question> questionDB = new Database<>(Question.class);
        List<Question> allQuestions = questionDB.getAllEnabled();
        examQuestions.getItems().clear();
        questions.getItems().clear();
        questions.getItems().addAll(allQuestions);

        Database<Exam> examDB = new Database<>(Exam.class);
        List<Exam> allExams = examDB.getAllEnabled();
        examTable.getItems().clear();
        examTable.getItems().addAll(allExams);

        examTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedExam = newSelection;
                fillSelectedExam(newSelection);
            }
        });
        handleEFReset();
    }

    private void loadCourseCodes() {
        try {
            Database<Course> courseDB = new Database<>(Course.class);
            List<Course> courses = courseDB.getAllEnabled();

            Set<String> courseCodes = courses.stream()
                    .map(Course::getCourseCode)
                    .collect(Collectors.toSet());

            ObservableList<String> observableCodes = FXCollections.observableArrayList(courseCodes);
            filterCourseIDCmb.setItems(observableCodes);
            courseIDCmb.setItems(observableCodes);
        } catch (Exception e) {
            e.printStackTrace();
            MsgSender.showMsg("Failed to load course codes!");
        }
    }

    private void fillSelectedExam(Exam exam) {
        Database<Question> questionDB = new Database<>(Question.class);
        List<Question> allQuestions = questionDB.getAllEnabled();
        this.selectedExam = exam;
        List<Long> includedIds = exam.getQuestions();
        List<Question> included = allQuestions.stream()
                .filter(q -> includedIds.contains(Long.parseLong(Long.toString(q.getId()))))
                .collect(Collectors.toList());
        List<Question> excluded = allQuestions.stream()
                .filter(q -> !includedIds.contains(Long.parseLong(Long.toString(q.getId()))))
                .collect(Collectors.toList());
        examQuestions.getItems().clear();
        examQuestions.getItems().addAll(included);
        questions.getItems().clear();
        questions.getItems().addAll(excluded);

        examNameTxt.setText(exam.getExamName());
        examTimeTxt.setText(Long.toString(exam.getExamTime()));
        courseIDCmb.setValue(exam.getCourseCode());
        String p;
        if (exam.getIsPublished()) {
            p = "Yes";
        } else {
            p = "No";
        }
        isPublishedCmb.setValue(p);
    }

    private int parseFilterScore(String input) {
        if (input.isEmpty()) {
            return -1; // 빈 문자열은 필터 미적용
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            MsgSender.showMsg("Invalid score input! Please enter a number.");
            return -1; // 오류 시 필터 미적용
        }
    }

    private boolean matchesType(Question q, String filterType) {
        return filterType == null ||
                filterType.equals("Any") ||
                q.getType().equalsIgnoreCase(filterType);
    }

    private boolean matchesKeyword(Question q, String keyword) {
        return keyword.isEmpty() ||
                q.getQuestionText().toLowerCase().contains(keyword);
    }

    private boolean matchesScore(Question q, int filterScore) {
        return filterScore == -1 || q.getScore() == filterScore;
    }

    private List<Long> getIncludedQuestionIds() {
        return examQuestions.getItems().stream()
                .map(Question::getId)
                .collect(Collectors.toList());
    }

    /**
     * Sets the teacher object and initializes the UI.
     * @param teacher The teacher object that is operating the page.
     */
    public void presetController(Teacher teacher) {
        this.teacher = teacher;
        handleQFReset();
    }

    @FXML
    public void handleQFReset() {
        filterQuestionTxt.clear();
        filterQuestionScoreTxt.clear();
        filterQuestionTypeCmb.setValue("Any");

        Database<Question> questionDB = new Database<>(Question.class);
        List<Question> allQuestions = questionDB.getAllEnabled();
        examQuestions.getItems().clear();
        questions.getItems().clear();
        questions.getItems().addAll(allQuestions);
    }

    @FXML
    public void handleQFFilter() {
        String filterType = filterQuestionTypeCmb.getValue();
        String filterKeyword = filterQuestionTxt.getText().trim().toLowerCase();
        int filterScore = parseFilterScore(filterQuestionScoreTxt.getText());

        handleQFReset();

        if (filterScore != -1) {
            filterQuestionScoreTxt.setText(Integer.toString(filterScore));
        } else {
            filterQuestionScoreTxt.clear();
        }

        filterQuestionTxt.setText(filterKeyword);
        filterQuestionTypeCmb.setValue(filterType);

        ObservableList<Question> included = examQuestions.getItems();
        ObservableList<Question> excluded = questions.getItems();

        ObservableList<Question> filteredIncluded = included.stream()
                .filter(q -> matchesType(q, filterType))
                .filter(q -> matchesKeyword(q, filterKeyword))
                .filter(q -> matchesScore(q, filterScore))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        ObservableList<Question> filteredExcluded = excluded.stream()
                .filter(q -> matchesType(q, filterType))
                .filter(q -> matchesKeyword(q, filterKeyword))
                .filter(q -> matchesScore(q, filterScore))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        examQuestions.getItems().clear();
        examQuestions.getItems().addAll(filteredIncluded);
        questions.getItems().clear();
        questions.getItems().addAll(filteredExcluded);
    }

    @FXML
    public void handleEFReset() {
        filterExamNameTxt.clear();
        filterCourseIDCmb.setValue("Any");
        filterIsPublishedCmb.setValue("Any");
        handleEFFilter();

        Database<Exam> ExamDB = new Database<>(Exam.class);
        List<Exam> allExam = ExamDB.getAllEnabled();
        examTable.getItems().clear();
        examTable.getItems().addAll(allExam);
    }

    @FXML
    public void handleEFFilter() {
        String name = filterExamNameTxt.getText().trim();
        String courseID = filterCourseIDCmb.getValue();
        String isPublished = filterIsPublishedCmb.getValue();

        Database<Exam> examDB = new Database<>(Exam.class);

        List<Exam> nameFiltered;
        if (!name.isEmpty()) {
            nameFiltered = examDB.queryFuzzyByField("examName", name);
        } else {
            nameFiltered = examDB.getAllEnabled();
        }

        List<Exam> courseFiltered;
        if (courseID != null && !courseID.equalsIgnoreCase("Any")) {
            courseFiltered = examDB.queryByField("courseCode", courseID);
        } else {
            courseFiltered = examDB.getAllEnabled();
        }

        List<Exam> isPublishedFiltered;
        if (isPublished != null && !isPublished.equalsIgnoreCase("Any")) {
            isPublishedFiltered = examDB.queryByField("isPublished", isPublished);
        } else {
            isPublishedFiltered = examDB.getAllEnabled();
        }

        List<Exam> temp = examDB.join(nameFiltered, courseFiltered);
        List<Exam> finalList = examDB.join(temp, isPublishedFiltered);

        examTable.getItems().clear();
        examTable.getItems().addAll(finalList);
    }

    @FXML
    public void addToExam() {
        Question selected = questions.getSelectionModel().getSelectedItem();

        if (selected != null) {
            ObservableList<Question> included = FXCollections.observableArrayList(examQuestions.getItems());
            ObservableList<Question> excluded = FXCollections.observableArrayList(questions.getItems());
            included.add(selected);
            excluded.remove(selected);

            examQuestions.getItems().clear();
            examQuestions.getItems().addAll(included);
            questions.getItems().clear();
            questions.getItems().addAll(excluded);
        } else {
            MsgSender.showMsg("No question selected!");
        }
    }

    @FXML
    public void removeFromExam() {
        Question selected = examQuestions.getSelectionModel().getSelectedItem();

        if (selected != null) {
            ObservableList<Question> included = FXCollections.observableArrayList(examQuestions.getItems());
            ObservableList<Question> excluded = FXCollections.observableArrayList(questions.getItems());
            included.remove(selected);
            excluded.add(selected);

            examQuestions.getItems().clear();
            examQuestions.getItems().addAll(included);
            questions.getItems().clear();
            questions.getItems().addAll(excluded);
        } else {
            MsgSender.showMsg("No question selected!");
            return;
        }
    }

    @FXML
    public void handleDelete() {
        Exam selected = examTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            MsgSender.showMsg("No exam selected!");
            return;
        } else if (selected.getIsPublished()) {
            MsgSender.showMsg("Published exam cannot be deleted!");
            return;
        }

        Database<Exam> examDB = new Database<>(Exam.class);

        String name = selected.getExamName();

        examDB.delByField("examName", name);

        Database<Exam> ExamDB2 = new Database<>(Exam.class);
        List<Exam> allExam = ExamDB2.getAllEnabled();
        examTable.getItems().clear();
        examTable.getItems().addAll(allExam);

        selected = null;
    }

    @FXML
    public void handleRefresh() {
        Database<Question> questionDB = new Database<>(Question.class);
        List<Question> allQuestions = questionDB.getAllEnabled();
        examQuestions.getItems().clear();
        questions.getItems().clear();
        questions.getItems().addAll(allQuestions);

        Database<Exam> ExamDB = new Database<>(Exam.class);
        List<Exam> allExam = ExamDB.getAllEnabled();
        examTable.getItems().clear();
        examTable.getItems().addAll(allExam);

        selectedQuestion = null;
        selectedExam = null;

        examNameTxt.clear();
        examTimeTxt.clear();
        isPublishedCmb.setValue("No");

        handleQFReset();
        handleEFReset();
    }

    @FXML
    public void handleAdd() {
        String name = examNameTxt.getText().trim();
        Long time;
        String courseCode = courseIDCmb.getValue();
        String tp = isPublishedCmb.getValue();
        Boolean isPublished = false;
        ObservableList<Question> included = examQuestions.getItems();
        try {
            time = Long.parseLong(examTimeTxt.getText());
        } catch (NumberFormatException e) {
            MsgSender.showMsg("Exam Time should be positive integer!");
            return;
        }
        if (name.isEmpty()) {
            MsgSender.showMsg("Exam Name should be specified!");
            return;
        }
        if (tp.equalsIgnoreCase("Yes")) {
            isPublished = true;
        }
        if (included.isEmpty()) {
            MsgSender.showMsg("An exam should included at least one question!");
            return;
        }
        List<Long> questions = getIncludedQuestionIds();

        Exam newExam = new Exam(courseCode, name, teacher.getId(), time, questions, isPublished);

        Database<Exam> examDB = new Database<>(Exam.class);
        examDB.add(newExam);

        List<String> keys = questions.stream().map(e -> Long.toString(e)).collect(Collectors.toList());

        if (isPublished) {
            Database<Question> questionDB = new Database<>(Question.class);
            List<Question> questionList = questionDB.queryByKeys(keys);

            for (Question q : questionList) {
                q.setPublished(q.getPublished() + 1);
                questionDB.update(q);
            }
        }

        examNameTxt.clear();
        examTimeTxt.clear();
        isPublishedCmb.setValue("No");
        Database<Exam> examDB2 = new Database<>(Exam.class);
        List<Exam> allExam = examDB2.getAllEnabled();
        examTable.getItems().clear();
        examTable.getItems().addAll(allExam);

        handleRefresh();
    }

    @FXML
    public void handleUpdate() {
        if (selectedExam == null) {
            MsgSender.showMsg("Please select an exam.");
            return;
        }
        if (selectedExam.getIsPublished()) {
            MsgSender.showMsg("Published exam cannot be modified.");
            return;
        }

        String newName = examNameTxt.getText().trim();
        Long newTime;
        String newCourseCode = courseIDCmb.getValue();
        String newPublished = isPublishedCmb.getValue();
        List<Long> newQuestions = getIncludedQuestionIds();
        ObservableList<Question> included = examQuestions.getItems();

        if (newName.isEmpty()) {
            MsgSender.showMsg("Exam Name should be specified.");
            return;
        }
        try {
            newTime = Long.parseLong(examTimeTxt.getText());
        } catch (NumberFormatException e) {
            MsgSender.showMsg("Exam Time should be positive integer!");
            return;
        }
        if (included.isEmpty()) {
            MsgSender.showMsg("An exam should included at least one question!");
            return;
        }

        if (!selectedExam.getIsPublished() && newPublished.equalsIgnoreCase("Yes")) {
            List<String> keys = newQuestions.stream().map(e -> Long.toString(e)).collect(Collectors.toList());
            Database<Question> questionDB = new Database<>(Question.class);
            List<Question> questions = questionDB.queryByKeys(keys);

            for (Question q : questions) {
                q.setPublished(q.getPublished() + 1);
                questionDB.update(q);
            }
        }

        selectedExam.setExamName(newName);
        selectedExam.setIsPublished(newPublished.equalsIgnoreCase("Yes"));
        selectedExam.setCourseCode(newCourseCode);
        selectedExam.setExamTime(newTime);
        selectedExam.setQuestions(newQuestions);

        Database<Exam> examDB = new Database<>(Exam.class);
        examDB.update(selectedExam);

        examNameTxt.clear();
        examTimeTxt.clear();
        isPublishedCmb.setValue("No");
        Database<Exam> examDB2 = new Database<>(Exam.class);
        List<Exam> allExam = examDB2.getAllEnabled();
        examTable.getItems().clear();
        examTable.getItems().addAll(allExam);

        handleRefresh();

        selectedExam = null;
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
