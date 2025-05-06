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
            
            // Generate mock data for development/testing
            generateMockDataIfEmpty();
            
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
     * Generates mock data if the database is empty or if there's an error loading data
     */
    private void generateMockDataIfEmpty() {
        try {
            // Check if we have records
            List<Record> records = recordDB.getAll();
            
            if (records.isEmpty()) {
                // No records, generate mock data
                createMockData();
                return;
            }
            
            // Check if we have valid related data
            boolean hasRelatedData = true;
            
            // Try to get a sample student, course, exam and question to verify data integrity
            if (studentDB.getAllEnabled().isEmpty() || 
                courseDB.getAllEnabled().isEmpty() || 
                examDB.getAllEnabled().isEmpty() || 
                questionDB.getAllEnabled().isEmpty()) {
                hasRelatedData = false;
            }
            
            if (!hasRelatedData) {
                System.out.println("Missing related entity data, generating mock data...");
                createMockData();
            }
        } catch (Exception e) {
            System.err.println("Error checking for existing data: " + e.getMessage());
            System.out.println("Attempting to generate fallback mock data...");
            try {
                createMockData();
            } catch (Exception ex) {
                System.err.println("Failed to generate mock data: " + ex.getMessage());
                ex.printStackTrace();
                // At this point, we've tried our best to recover
            }
        }
    }

    /**
     * Creates detailed mock data for testing the grade statistics view
     */
    private void createMockData() {
        try {
            // Check if we already have students, courses, and exams
            List<Student> students = studentDB.getAllEnabled();
            List<Course> courses = courseDB.getAllEnabled();
            List<Exam> exams = examDB.getAllEnabled();
            List<Question> questions = questionDB.getAllEnabled();

            // If we don't have enough data to create meaningful statistics, create mock entities
            if (students.isEmpty() || students.size() < 10) {
                // Create mock students
                String[] studentNames = {
                    "Alice Smith", "Bob Johnson", "Carol Williams", "David Brown", "Eva Davis", 
                    "Frank Miller", "Grace Wilson", "Henry Taylor", "Isabella Anderson", "Jack Thomas"
                };
                for (String name : studentNames) {
                    Student student = new Student();
                    student.setName(name);
                    student.setUsername(name.toLowerCase().replace(" ", "."));
                    student.setPassword("password123");
                    studentDB.add(student);
                    students.add(student);
                }
            }

            if (courses.isEmpty() || courses.size() < 5) {
                // Create mock courses
                String[][] courseData = {
                    {"COMP1000", "Introduction to Computer Science"},
                    {"COMP2000", "Data Structures and Algorithms"},
                    {"COMP3000", "Software Engineering"},
                    {"MATH1010", "Calculus I"},
                    {"MATH2010", "Linear Algebra"}
                };
                
                for (String[] data : courseData) {
                    Course course = new Course();
                    course.setCourseCode(data[0]);
                    course.setCourseName(data[1]);
                    courseDB.add(course);
                    courses.add(course);
                }
            }

            if (exams.isEmpty() || exams.size() < 10) {
                // Create mock exams for each course
                for (Course course : courses) {
                    String[] examTypes = {"Midterm", "Final", "Quiz 1", "Quiz 2"};
                    for (String examType : examTypes) {
                        Exam exam = new Exam();
                        exam.setExamName(course.getCourseCode() + " " + examType);
                        exam.setCourseCode(course.getCourseCode());
                        exam.setDuration(examType.equals("Final") ? 120 : 60);
                        examDB.add(exam);
                        exams.add(exam);
                    }
                }
            }

            if (questions.isEmpty() || questions.size() < 30) {
                // Create mock questions with varying types
                createMockQuestions();
            }

            // Get fresh lists after creation
            students = studentDB.getAllEnabled();
            exams = examDB.getAllEnabled();
            questions = questionDB.getAllEnabled();

            // Create mock records for each student taking each exam
            Random random = new Random();
            
            // Clear existing records to prevent duplicates
            List<Record> existingRecords = recordDB.getAll();
            for (Record record : existingRecords) {
                recordDB.delByKey(record.getId().toString());
            }
            
            // Create new records with realistic distribution of scores
            for (Student student : students) {
                for (Exam exam : exams) {
                    // Student ability factor (0.5-1.5) to simulate different student capabilities
                    double studentFactor = 0.5 + random.nextDouble();
                    
                    // Exam difficulty factor (0.7-1.3) to simulate different exam difficulties
                    double examDifficulty = 0.7 + (random.nextDouble() * 0.6);
                    
                    // Select 5-15 questions for each exam depending on exam type
                    int questionCount = exam.getExamName().contains("Quiz") ? 
                        5 + random.nextInt(6) : 10 + random.nextInt(6);
                    
                    List<Question> examQuestions = new ArrayList<>();
                    for (int i = 0; i < questionCount && i < questions.size(); i++) {
                        examQuestions.add(questions.get(random.nextInt(questions.size())));
                    }
                    
                    // Create records for each question in the exam
                    for (Question question : examQuestions) {
                        Record record = new Record();
                        record.setStudent(student.getId());
                        record.setExamID(exam.getId());
                        record.setQuestionID(question.getId());
                        
                        // Generate mock answer
                        if (question.getType().equals("MCQ")) {
                            String[] options = {"A", "B", "C", "D", "E"};
                            record.setResponse(options[random.nextInt(options.length)]);
                        } else {
                            record.setResponse("This is a mock answer for " + question.getQuestionText());
                        }
                        
                        // Generate realistic score based on student ability and exam difficulty
                        int maxScore = question.getScore();
                        double scoreChance = studentFactor / examDifficulty;
                        
                        // Adjust score probability based on student and exam factors
                        int score;
                        if (scoreChance > 0.85) {
                            // High performing student gets full/nearly full score
                            score = random.nextDouble() < 0.8 ? maxScore : maxScore - 1;
                        } else if (scoreChance > 0.6) {
                            // Average student gets 60-100% of max score
                            score = (int)Math.round(maxScore * (0.6 + (random.nextDouble() * 0.4)));
                        } else {
                            // Struggling student gets 0-60% of max score
                            score = (int)Math.round(maxScore * (random.nextDouble() * 0.6));
                        }
                        
                        record.setScore(Math.max(0, Math.min(maxScore, score)));
                        recordDB.add(record);
                    }
                }
            }

            System.out.println("Mock grade data has been generated: " + students.size() + " students, " + 
                              exams.size() + " exams, " + questions.size() + " questions");
        } catch (Exception e) {
            System.err.println("Failed to generate complete mock data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a variety of mock questions for exams
     */
    private void createMockQuestions() {
        try {
            // MCQ questions for computer science
            String[] csQuestionTexts = {
                "Which data structure uses LIFO?",
                "What is the time complexity of quicksort in the average case?",
                "Which of the following is not an object-oriented programming language?",
                "What does SQL stand for?",
                "Which sorting algorithm has the best average-case performance?",
                "Which design pattern is used for database access?",
                "What is the primary purpose of normalization in database design?",
                "In Java, which keyword is used to inherit a class?",
                "Which of the following is not a principle of OOP?",
                "What is the purpose of JUnit?"
            };
            
            String[][] csOptions = {
                {"Array", "Queue", "Stack", "Tree", "List"},
                {"O(n)", "O(n log n)", "O(n²)", "O(log n)", "O(1)"},
                {"Java", "Python", "C", "C++", "C#"},
                {"Structured Query Language", "Simple Query Language", "Standard Query Language", "System Query Language", "Sequential Query Language"},
                {"Bubble Sort", "Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort"},
                {"Singleton", "Factory", "DAO", "Observer", "Decorator"},
                {"Reduce redundancy", "Improve performance", "Simplify queries", "All of these", "None of these"},
                {"implements", "extends", "inherits", "using", "import"},
                {"Encapsulation", "Inheritance", "Polymorphism", "Abstraction", "Fragmentation"},
                {"Testing", "Debugging", "Documentation", "Deployment", "All of these"}
            };
            
            String[] csAnswers = {"C", "B", "C", "A", "E", "C", "A", "B", "E", "A"};
            
            // MCQ questions for mathematics
            String[] mathQuestionTexts = {
                "What is the derivative of e^x?",
                "Which of the following is not a property of a positive definite matrix?",
                "What is the value of sin(π/2)?",
                "Which method is used to find the maximum or minimum value of a function?",
                "What is the rank of a 3x3 identity matrix?",
                "Which of the following series converges?",
                "What is the solution to the differential equation dy/dx = 2x?",
                "What is the determinant of a singular matrix?",
                "Which of the following is a property of eigenvectors?",
                "What is the integral of 1/x?"
            };
            
            String[][] mathOptions = {
                {"x*e^x", "e^x", "e^(x-1)", "ln(x)", "None of these"},
                {"Symmetric", "All eigenvalues are positive", "Invertible", "Determinant is negative", "Diagonal elements are positive"},
                {"0", "1", "-1", "Undefined", "π/2"},
                {"Integration", "Differentiation", "Factorization", "Logarithm", "Power series"},
                {"0", "1", "2", "3", "Undefined"},
                {"1/n", "1/n²", "n/(n+1)", "n/ln(n)", "n"},
                {"y = x² + C", "y = x + C", "y = 2x + C", "y = e^(2x) + C", "y = ln(2x) + C"},
                {"1", "0", "Infinity", "Undefined", "Depends on the matrix"},
                {"Always orthogonal", "Always normalized", "Can be zero vector", "Always linearly dependent", "Change direction under transformation"},
                {"x + C", "ln|x| + C", "e^x + C", "1/(2x²) + C", "tan(x) + C"}
            };
            
            String[] mathAnswers = {"B", "D", "B", "B", "D", "B", "A", "B", "E", "B"};
            
            // Create short answer questions
            String[] shortAnswerQuestions = {
                "Explain the concept of inheritance in object-oriented programming.",
                "Describe how the quicksort algorithm works.",
                "What are the ACID properties in database transactions?",
                "Explain the difference between deep and shallow copying in programming.",
                "Describe the Model-View-Controller (MVC) architectural pattern.",
                "Explain how public key cryptography works.",
                "What is the difference between compilation and interpretation?",
                "Describe the principle of recursion and provide an example.",
                "Explain the concept of lambda calculus and its relevance to functional programming.",
                "What are design patterns and why are they important in software engineering?"
            };
            
            String[] shortAnswerAnswers = {
                "Inheritance is a mechanism where a new class inherits properties and behaviors from an existing class. It promotes code reuse and establishes an is-a relationship between classes.",
                "Quicksort is a divide-and-conquer algorithm that selects a pivot, partitions the array around the pivot, and recursively sorts the sub-arrays. Average time complexity is O(n log n).",
                "ACID stands for Atomicity, Consistency, Isolation, and Durability, which are properties that guarantee database transactions are processed reliably.",
                "Shallow copying creates a new object but copies references to the objects within it, while deep copying creates a completely independent copy of the object and all objects referenced by it.",
                "MVC separates an application into Model (data and business logic), View (user interface), and Controller (mediates between Model and View) components, promoting modular design.",
                "Public key cryptography uses a pair of keys: a public key for encryption and a private key for decryption. Information encrypted with one key can only be decrypted with the other key.",
                "Compilation translates source code into machine code before execution, while interpretation executes source code directly without prior translation to machine code.",
                "Recursion is a technique where a function calls itself to solve smaller instances of the same problem. Example: factorial calculation where n! = n * (n-1)!",
                "Lambda calculus is a formal system for function definition, application and recursion, forming the theoretical foundation for functional programming languages by treating functions as first-class citizens.",
                "Design patterns are reusable solutions to common software design problems. They provide tested, proven development paradigms that improve code readability, maintainability, and scalability."
            };
            
            // Create MCQ questions
            for (int i = 0; i < csQuestionTexts.length; i++) {
                Question question = new Question();
                question.setQuestionText(csQuestionTexts[i]);
                question.setType("MCQ");
                question.setScore(2 + (i % 3)); // Scores between 2-4
                question.setAnswer(csAnswers[i]);
                
                // Set options based on number available
                int optionCount = csOptions[i].length;
                question.setOptionCount(optionCount);
                
                // Set individual options
                if (optionCount >= 1) question.setA(csOptions[i][0]);
                if (optionCount >= 2) question.setB(csOptions[i][1]);
                if (optionCount >= 3) question.setC(csOptions[i][2]);
                if (optionCount >= 4) question.setD(csOptions[i][3]);
                if (optionCount >= 5) question.setE(csOptions[i][4]);
                
                questionDB.add(question);
            }
            
            // Create more MCQ questions (Math)
            for (int i = 0; i < mathQuestionTexts.length; i++) {
                Question question = new Question();
                question.setQuestionText(mathQuestionTexts[i]);
                question.setType("MCQ");
                question.setScore(2 + (i % 3)); // Scores between 2-4
                question.setAnswer(mathAnswers[i]);
                
                // Set options based on number available
                int optionCount = mathOptions[i].length;
                question.setOptionCount(optionCount);
                
                // Set individual options
                if (optionCount >= 1) question.setA(mathOptions[i][0]);
                if (optionCount >= 2) question.setB(mathOptions[i][1]);
                if (optionCount >= 3) question.setC(mathOptions[i][2]);
                if (optionCount >= 4) question.setD(mathOptions[i][3]);
                if (optionCount >= 5) question.setE(mathOptions[i][4]);
                
                questionDB.add(question);
            }
            
            // Create Short Answer questions
            for (int i = 0; i < shortAnswerQuestions.length; i++) {
                Question question = new Question();
                question.setQuestionText(shortAnswerQuestions[i]);
                question.setType("Short Answer");
                question.setScore(5); // Short answers worth more points
                question.setAnswer(shortAnswerAnswers[i]);
                question.setOptionCount(0); // No options for short answer
                
                questionDB.add(question);
            }
            
        } catch (Exception e) {
            System.err.println("Error creating mock questions: " + e.getMessage());
            e.printStackTrace();
        }
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
