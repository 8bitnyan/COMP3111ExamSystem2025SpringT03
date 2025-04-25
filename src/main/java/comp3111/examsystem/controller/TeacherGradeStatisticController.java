package comp3111.examsystem.controller;
import comp3111.examsystem.Main;
import comp3111.examsystem.entity.*;
import comp3111.examsystem.entity.Record;
import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import comp3111.examsystem.tools.UIhelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller for teacher grade statistic page.
 */
public class TeacherGradeStatisticController implements Initializable {
    private Teacher teacher;
    private Database<Record> recordDB;
    private Database<Student> studentDB;
    private Database<Exam> examDB;
    private Database<Course> courseDB;
    private Database<Question> questionDB;

    @FXML private VBox mainbox;

    @FXML private ComboBox<String> courseCmb, examCmb, studentCmb;

    @FXML private TableView<Stats> recordTable;
    @FXML private TableColumn<Stats, String> colStudent, colCourse, colExam, colTimeSpent;
    @FXML private TableColumn<Stats, Integer> colScore, colMaxScore;

    @FXML private BarChart<String, Integer> barChart;
    @FXML private LineChart<String, Integer> lineChart;

    /**
     * Initializes the page and loads the data into the table and charts.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recordDB = new Database<>(Record.class);
        studentDB = new Database<>(Student.class);
        examDB = new Database<>(Exam.class);
        courseDB = new Database<>(Course.class);
        questionDB = new Database<>(Question.class);

        courseCmb.setValue("Any");
        examCmb.setValue("Any");
        studentCmb.setValue("Any");

        loadCourseCodes();
        loadExam();
        loadStudent();

        setupTableColumns();
        recordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colExam.setCellValueFactory(new PropertyValueFactory<>("examName"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colMaxScore.setCellValueFactory(new PropertyValueFactory<>("maxScore"));
        colTimeSpent.setCellValueFactory(new PropertyValueFactory<>("timeSpent"));
        setUpBarGraph();
        setUpLineGraph();
    }

    /**
     * Sets the teacher object and initializes the UI.
     * @param teacher The teacher object that is operating this page.
     */
    public void presetController(Teacher teacher) {
        this.teacher = teacher;
    }

    private void loadCourseCodes() {
        try {
            Database<Course> courseDB = new Database<>(Course.class);
            List<Course> courses = courseDB.getAllEnabled();

            Set<String> courseCodes = courses.stream()
                    .map(Course::getCourseCode)
                    .collect(Collectors.toSet());
            courseCodes.add("Any");

            ObservableList<String> observableCodes = FXCollections.observableArrayList(courseCodes);
            courseCmb.setItems(observableCodes);
        } catch (Exception e) {
            e.printStackTrace();
            MsgSender.showMsg("Failed to load course codes!");
        }
    }

    private void loadExam() {
        try {
            Database<Exam> examDB = new Database<>(Exam.class);
            List<Exam> exams = examDB.getAllEnabled();

            Set<String> examNames = exams.stream()
                    .map(Exam::getExamName)
                    .collect(Collectors.toSet());
            examNames.add("Any");

            ObservableList<String> observableNames = FXCollections.observableArrayList(examNames);
            examCmb.setItems(observableNames);
        } catch (Exception e) {
            e.printStackTrace();
            MsgSender.showMsg("Failed to load course codes!");
        }
    }

    private void loadStudent() {
        try {
            Database<Student> studentDB = new Database<>(Student.class);
            List<Student> students = studentDB.getAllEnabled();

            Set<String> names = students.stream()
                    .map(Student::getName)
                    .collect(Collectors.toSet());
            names.add("Any");

            ObservableList<String> observableNames = FXCollections.observableArrayList(names);
            studentCmb.setItems(observableNames);
        } catch (Exception e) {
            e.printStackTrace();
            MsgSender.showMsg("Failed to load course codes!");
        }
    }

    private boolean matchesSID(Record r, Long SID) {
        return SID != null && Objects.equals(r.getStudentID(), SID);
    }

    private boolean matchesEID(Record r, Long EID) {
        return EID != null && Objects.equals(r.getExamID(), EID);
    }

    private void setupTableColumns() {
        recordTable.getItems().clear();
        List<Record> records = recordDB.getAll();
        Set<Long> studentID = records.stream()
                .map(Record::getStudentID)
                .collect(Collectors.toSet());
        for (Long SID : studentID) {
            List<Record> recordStudent = recordDB.queryByField("studentID", Long.toString(SID));
            Set<Long> examID = recordStudent.stream()
                    .map(Record::getExamID)
                    .collect(Collectors.toSet());
            for (Long EID : examID) {
                List<Record> t = recordDB.getAll();
                List<Record> filteredRecord = t.stream()
                        .filter(r -> matchesSID(r, SID))
                        .filter(r -> matchesEID(r, EID))
                        .collect(Collectors.toList());
                String name = studentDB.queryByKey(Long.toString(SID)).getName();
                String courseCode = examDB.queryByKey(Long.toString(EID)).getCourseCode();
                String examName = examDB.queryByKey(Long.toString(EID)).getExamName();
                Integer maxScore = 0;
                Integer score = 0;
                for (Record f : filteredRecord) {
                    score += f.getScore();
                    maxScore += (questionDB.queryByKey(Long.toString(f.getQuestionID()))).getScore();
                }
                Stats example = new Stats(name, courseCode, examName, score, maxScore, "");
                recordTable.getItems().add(example);
            }
        }
    }

    private void setUpBarGraph() {
        ObservableList<Stats> now = recordTable.getItems();
        barChart.getData().clear();
        Set<String> courses = now.stream()
                .map(Stats::getCourseCode)
                .collect(Collectors.toSet());
        XYChart.Series<String, Integer> newBar = new XYChart.Series<String, Integer>();
        for (String c : courses) {
            int sum = 0;
            List<Integer> marks = now.stream()
                    .filter(s -> s.getCourseCode() == c)
                    .map(s -> s.getScore() * 100 / s.getMaxScore())
                    .collect(Collectors.toList());
            for (Integer i : marks) {
                sum += i;
            }
            XYChart.Data<String, Integer> t = new XYChart.Data<>(c, sum/ marks.size());
            newBar.getData().add(t);
        }
        barChart.getData().add(newBar);
    }

    private void setUpLineGraph() {
        ObservableList<Stats> now = recordTable.getItems();
        lineChart.getData().clear();
        Set<String> exams = now.stream()
                .map(Stats::getExamName)
                .collect(Collectors.toSet());
        XYChart.Series<String, Integer> newLine = new XYChart.Series<String, Integer>();
        for (String e : exams) {
            Set<String> courses = now.stream()
                    .filter(s -> Objects.equals(s.getExamName(), e))
                    .map(Stats::getCourseCode)
                    .collect(Collectors.toSet());
            for (String c : courses) {

                List<Integer> marks = now.stream()
                        .filter(s -> Objects.equals(s.getCourseCode(), c))
                        .filter(s -> Objects.equals(s.getExamName(), e))
                        .map(s -> s.getScore() * 100 / s.getMaxScore())
                        .collect(Collectors.toList());
                Integer sum = 0;
                for (Integer i : marks) {
                    sum += i;
                }
                XYChart.Data<String, Integer> t = new XYChart.Data<>(c + " - " + e, sum/marks.size());
                newLine.getData().add(t);
            }
        }
        lineChart.getData().add(newLine);
    }

    @FXML
    private void handleReset() {
        courseCmb.setValue("Any");
        examCmb.setValue("Any");
        studentCmb.setValue("Any");

        setupTableColumns();
        setUpLineGraph();
        setUpBarGraph();
    }

    @FXML
    private void handleFilter() {
        String filterCourse = courseCmb.getValue();
        String filterExam = examCmb.getValue();
        String filterStudent = studentCmb.getValue();

        handleReset();
        ObservableList<Stats> stats = recordTable.getItems();

        courseCmb.setValue(filterCourse);
        examCmb.setValue(filterExam);
        studentCmb.setValue(filterStudent);

        ObservableList<Stats> filterStats = stats.stream()
                .filter(s -> (filterCourse.equals("Any") || filterCourse.equals(s.getCourseCode())))
                .filter(s -> (filterExam.equals("Any") || filterExam.equals(s.getExamName())))
                .filter(s -> (filterStudent.equals("Any") || filterStudent.equals(s.getStudentName())))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        
        recordTable.setItems(filterStats);
        setUpBarGraph();
        setUpLineGraph();
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
