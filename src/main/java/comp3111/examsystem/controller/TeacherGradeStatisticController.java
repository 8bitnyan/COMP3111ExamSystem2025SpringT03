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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    
    private ObservableList<Stats> allStats = FXCollections.observableArrayList();

    // Default constructor for production
    public TeacherGradeStatisticController() {
        // fields will be initialized in initialize()
    }

    // Constructor for tests
    public TeacherGradeStatisticController(Database<Record> recordDB, Database<Student> studentDB, Database<Exam> examDB, Database<Course> courseDB, Database<Question> questionDB) {
        this.recordDB = recordDB;
        this.studentDB = studentDB;
        this.examDB = examDB;
        this.courseDB = courseDB;
        this.questionDB = questionDB;
    }

    /**
     * Initializes the page and loads the data into the table and charts.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize databases
            if (recordDB == null) recordDB = new Database<>(Record.class);
            if (studentDB == null) studentDB = new Database<>(Student.class);
            if (examDB == null) examDB = new Database<>(Exam.class);
            if (courseDB == null) courseDB = new Database<>(Course.class);
            if (questionDB == null) questionDB = new Database<>(Question.class);

            // Initialize default values for filters
            courseCmb.setValue("Any");
            examCmb.setValue("Any");
            studentCmb.setValue("Any");

            // Load filter options (with error handling)
            try {
                loadCourseCodes();
            } catch (Exception e) {
                System.err.println("Error loading course codes: " + e.getMessage());
            }
            
            try {
                loadExam();
            } catch (Exception e) {
                System.err.println("Error loading exams: " + e.getMessage());
            }
            
            try {
                loadStudent();
            } catch (Exception e) {
                System.err.println("Error loading students: " + e.getMessage());
            }

            // Setup table
            recordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Configure table columns
            colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
            colCourse.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
            colExam.setCellValueFactory(new PropertyValueFactory<>("examName"));
            colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
            colMaxScore.setCellValueFactory(new PropertyValueFactory<>("maxScore"));
            colTimeSpent.setCellValueFactory(new PropertyValueFactory<>("timeSpent"));
            
            // Do NOT generate mock data if empty
            // generateMockDataIfEmpty();
            
            // Initialize data and charts
            setupTableColumns();
            setUpBarGraph();
            setUpLineGraph();
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
            MsgSender.showMsg("There was an error loading the grade statistics. Please try again.");
        }
    }

    /**
     * Sets the teacher object and initializes the UI.
     * @param teacher The teacher object that is operating this page.
     */
    public void presetController(Teacher teacher) {
        this.teacher = teacher;
        // Refresh data after teacher is set
        Platform.runLater(this::refreshData);
    }

    /**
     * Loads course codes into the course filter combobox
     */
    private void loadCourseCodes() {
        try {
            List<Course> courses = courseDB.getAllEnabled();
            Set<String> courseCodes = new HashSet<>();

            // Add course codes from available courses
            if (courses != null && !courses.isEmpty()) {
                courseCodes = courses.stream()
                        .filter(c -> c != null && c.getCourseCode() != null && !c.getCourseCode().trim().isEmpty())
                    .map(Course::getCourseCode)
                    .collect(Collectors.toSet());
            }
            
            // Always add "Any" option
            courseCodes.add("Any");

            ObservableList<String> observableCodes = FXCollections.observableArrayList(courseCodes);
            courseCmb.setItems(observableCodes);
        } catch (Exception e) {
            System.err.println("Error loading course codes: " + e.getMessage());
            
            // Ensure at least "Any" option is available
            ObservableList<String> fallback = FXCollections.observableArrayList("Any");
            courseCmb.setItems(fallback);
        }
    }

    /**
     * Loads exam names into the exam filter combobox
     */
    private void loadExam() {
        try {
            List<Exam> exams = examDB.getAllEnabled();
            Set<String> examNames = new HashSet<>();

            // Add exam names from available exams
            if (exams != null && !exams.isEmpty()) {
                examNames = exams.stream()
                        .filter(e -> e != null && e.getExamName() != null && !e.getExamName().trim().isEmpty())
                    .map(Exam::getExamName)
                    .collect(Collectors.toSet());
            }
            
            // Always add "Any" option
            examNames.add("Any");

            ObservableList<String> observableNames = FXCollections.observableArrayList(examNames);
            examCmb.setItems(observableNames);
        } catch (Exception e) {
            System.err.println("Error loading exam names: " + e.getMessage());
            
            // Ensure at least "Any" option is available
            ObservableList<String> fallback = FXCollections.observableArrayList("Any");
            examCmb.setItems(fallback);
        }
    }

    /**
     * Loads student names into the student filter combobox
     */
    private void loadStudent() {
        try {
            List<Student> students = studentDB.getAllEnabled();
            Set<String> names = new HashSet<>();

            // Add student names from available students
            if (students != null && !students.isEmpty()) {
                names = students.stream()
                        .filter(s -> s != null && s.getName() != null && !s.getName().trim().isEmpty())
                    .map(Student::getName)
                    .collect(Collectors.toSet());
            }
            
            // Always add "Any" option
            names.add("Any");

            ObservableList<String> observableNames = FXCollections.observableArrayList(names);
            studentCmb.setItems(observableNames);
        } catch (Exception e) {
            System.err.println("Error loading student names: " + e.getMessage());
            
            // Ensure at least "Any" option is available
            ObservableList<String> fallback = FXCollections.observableArrayList("Any");
            studentCmb.setItems(fallback);
        }
    }

    /**
     * Checks if a record matches the given student ID
     */
    private boolean matchesSID(Record r, Long SID) {
        return SID != null && Objects.equals(r.getStudentID(), SID);
    }

    /**
     * Checks if a record matches the given exam ID
     */
    private boolean matchesEID(Record r, Long EID) {
        return EID != null && Objects.equals(r.getExamID(), EID);
    }

    /**
     * Loads and calculates all statistics data from the database
     */
    private void setupTableColumns() {
        recordTable.getItems().clear();
        allStats.clear();
        
        try {
        List<Record> records = recordDB.getAll();
            
            if (records.isEmpty()) {
                // No records found, just return without trying to process
                return;
            }
            
        Set<Long> studentID = records.stream()
                    .filter(r -> r != null && r.getStudentID() != null)
                .map(Record::getStudentID)
                .collect(Collectors.toSet());
                    
        for (Long SID : studentID) {
                try {
            List<Record> recordStudent = recordDB.queryByField("studentID", Long.toString(SID));
            Set<Long> examID = recordStudent.stream()
                            .filter(r -> r != null && r.getExamID() != null)
                    .map(Record::getExamID)
                    .collect(Collectors.toSet());
                            
            for (Long EID : examID) {
                        try {
                            List<Record> filteredRecord = records.stream()
                                    .filter(r -> r != null && matchesSID(r, SID) && matchesEID(r, EID))
                        .collect(Collectors.toList());
                                    
                            Student student = studentDB.queryByKey(Long.toString(SID));
                            Exam exam = examDB.queryByKey(Long.toString(EID));
                            
                            if (student != null && exam != null) {
                                String name = student.getName();
                                String courseCode = exam.getCourseCode();
                                String examName = exam.getExamName();
                                
                                // Calculate total score and max possible score
                Integer maxScore = 0;
                Integer score = 0;
                for (Record f : filteredRecord) {
                                    if (f.getScore() != null) {
                    score += f.getScore();
                                    }
                                    
                                    if (f.getQuestionID() != null) {
                                        Question question = questionDB.queryByKey(Long.toString(f.getQuestionID()));
                                        if (question != null && question.getScore() != null) {
                                            maxScore += question.getScore();
                                        }
                                    }
                                }
                                
                                // Calculate time spent (mock data for now)
                                String timeSpent = generateMockTimeSpent();
                                
                                Stats examStat = new Stats(name, courseCode, examName, score, maxScore, timeSpent);
                                allStats.add(examStat);
                            }
                        } catch (Exception ex) {
                            // Log the error but continue processing other records
                            System.err.println("Error processing exam ID " + EID + ": " + ex.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    // Log the error but continue processing other students
                    System.err.println("Error processing student ID " + SID + ": " + ex.getMessage());
                }
            }
            
            recordTable.setItems(allStats);
        } catch (Exception e) {
            // Catch any database-related exceptions
            System.err.println("Error loading statistics data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates mock time spent for demo purposes
     */
    private String generateMockTimeSpent() {
        Random random = new Random();
        int minutes = 10 + random.nextInt(50); // Between 10-60 minutes
        return minutes + " min";
    }

    /**
     * Sets up the bar chart with current data
     */
    private void setUpBarGraph() {
        ObservableList<Stats> filteredStats = recordTable.getItems();
        barChart.getData().clear();
        
        // Group by course
        Map<String, List<Stats>> courseGroups = filteredStats.stream()
                .collect(Collectors.groupingBy(Stats::getCourseCode));
                
        XYChart.Series<String, Integer> avgScoreSeries = new XYChart.Series<>();
        avgScoreSeries.setName("Average Score (%)");
        
        XYChart.Series<String, Integer> passRateSeries = new XYChart.Series<>();
        passRateSeries.setName("Pass Rate (%)");
        
        for (Map.Entry<String, List<Stats>> entry : courseGroups.entrySet()) {
            String courseCode = entry.getKey();
            List<Stats> courseStats = entry.getValue();
            
            // Calculate average percentage score for this course
            double avgPercentage = courseStats.stream()
                    .mapToDouble(s -> (s.getScore() * 100.0) / Math.max(1, s.getMaxScore()))
                    .average()
                    .orElse(0);
                    
            // Calculate pass rate (score >= 50%)
            double passRate = courseStats.stream()
                    .filter(s -> (s.getScore() * 100.0) / Math.max(1, s.getMaxScore()) >= 50)
                    .count() * 100.0 / courseStats.size();
                    
            avgScoreSeries.getData().add(new XYChart.Data<>(courseCode, (int)Math.round(avgPercentage)));
            passRateSeries.getData().add(new XYChart.Data<>(courseCode, (int)Math.round(passRate)));
        }
        
        barChart.getData().add(avgScoreSeries);
        barChart.getData().add(passRateSeries);
    }

    /**
     * Sets up the line chart with current data
     */
    private void setUpLineGraph() {
        ObservableList<Stats> filteredStats = recordTable.getItems();
        lineChart.getData().clear();
        
        // Group first by course
        Map<String, List<Stats>> coursesMap = filteredStats.stream()
                .collect(Collectors.groupingBy(Stats::getCourseCode));
                
        // For each course, create a series
        for (Map.Entry<String, List<Stats>> courseEntry : coursesMap.entrySet()) {
            String courseCode = courseEntry.getKey();
            List<Stats> courseStats = courseEntry.getValue();
            
            // Group by exam within this course
            Map<String, List<Stats>> examGroups = courseStats.stream()
                    .collect(Collectors.groupingBy(Stats::getExamName));
                    
            XYChart.Series<String, Integer> courseSeries = new XYChart.Series<>();
            courseSeries.setName(courseCode);
            
            // For each exam in this course, add a data point
            for (Map.Entry<String, List<Stats>> examEntry : examGroups.entrySet()) {
                String examName = examEntry.getKey();
                List<Stats> examStats = examEntry.getValue();
                
                // Calculate average percentage for this exam
                double avgPercentage = examStats.stream()
                        .mapToDouble(s -> (s.getScore() * 100.0) / Math.max(1, s.getMaxScore()))
                        .average()
                        .orElse(0);
                        
                courseSeries.getData().add(new XYChart.Data<>(examName, (int)Math.round(avgPercentage)));
            }
            
            lineChart.getData().add(courseSeries);
        }
    }
    
    /**
     * Refreshes all data and chart displays
     */
    private void refreshData() {
        try {
            setupTableColumns();
            applyFilters();
        } catch (Exception e) {
            System.err.println("Error refreshing data: " + e.getMessage());
            e.printStackTrace();
            MsgSender.showMsg("There was an error refreshing the data. Please try again.");
        }
    }

    /**
     * Resets all filters and refreshes the data
     */
    @FXML
    private void handleReset() {
        courseCmb.setValue("Any");
        examCmb.setValue("Any");
        studentCmb.setValue("Any");

        recordTable.setItems(allStats);
        setUpBarGraph();
        setUpLineGraph();
    }

    /**
     * Applies the current filters to the data
     */
    @FXML
    private void handleFilter() {
        applyFilters();
    }
    
    /**
     * Applies filters based on selected combobox values
     */
    private void applyFilters() {
        String selectedCourse = courseCmb.getValue();
        String selectedExam = examCmb.getValue();
        String selectedStudent = studentCmb.getValue();
        
        // Start with all stats
        ObservableList<Stats> filteredStats = FXCollections.observableArrayList(allStats);
        
        // Apply course filter
        if (selectedCourse != null && !selectedCourse.equals("Any")) {
            filteredStats = filteredStats.filtered(
                stats -> stats.getCourseCode().equals(selectedCourse)
            );
        }
        
        // Apply exam filter
        if (selectedExam != null && !selectedExam.equals("Any")) {
            filteredStats = filteredStats.filtered(
                stats -> stats.getExamName().equals(selectedExam)
            );
        }
        
        // Apply student filter
        if (selectedStudent != null && !selectedStudent.equals("Any")) {
            filteredStats = filteredStats.filtered(
                stats -> stats.getStudentName().equals(selectedStudent)
            );
        }
        
        // Update table and charts
        recordTable.setItems(filteredStats);
        setUpBarGraph();
        setUpLineGraph();
    }

    /**
     * Exports the current filtered data to a CSV file
     */
    @FXML
    private void handleExportData() {
        try {
            // Get current filtered data
            ObservableList<Stats> dataToExport = recordTable.getItems();
            
            if (dataToExport.isEmpty()) {
                MsgSender.showMsg("No data to export!");
                return;
            }
            
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Grade Statistics");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            
            // Generate default filename with timestamp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
            fileChooser.setInitialFileName("grade_statistics_" + timestamp + ".csv");
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(mainbox.getScene().getWindow());
            
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    // Write header
                    writer.write("Student,Course,Exam,Score,MaxScore,ScorePercentage,TimeSpent\n");
                    
                    // Write data rows
                    for (Stats stat : dataToExport) {
                        double percentage = (stat.getScore() * 100.0) / Math.max(1, stat.getMaxScore());
                        writer.write(String.format("\"%s\",\"%s\",\"%s\",%d,%d,%.2f,\"%s\"\n",
                                stat.getStudentName(),
                                stat.getCourseCode(),
                                stat.getExamName(),
                                stat.getScore(),
                                stat.getMaxScore(),
                                percentage,
                                stat.getTimeSpent()));
                    }
                    
                    MsgSender.showMsg("Data exported successfully to " + file.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            MsgSender.showMsg("Error exporting data: " + e.getMessage());
        }
    }

    /**
     * Handles the back button to return to teacher main UI
     */
    @FXML
    public void handleBack() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("TeacherMainUI.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Welcome to HKUST Examination System");
            stage.setScene(new Scene(fxmlLoader.load()));
            TeacherMainController controller = fxmlLoader.getController();
            controller.presetController(teacher); // Pass the Teacher object to the next controller
            UIhelper.expandToFullScreen(stage);
            stage.show();
            ((Stage) mainbox.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the close application button to exit the application
     */
    @FXML
    public void handleCloseApplication() {
        Platform.exit();
        System.exit(0);
    }
}
