package comp3111.examsystem.entity;

public class Stats extends Entity{
    private String studentName;
    private String courseCode;
    private String examName;
    private Integer score;
    private Integer maxScore;
    private String timeSpent;

    public Stats() { };

    public Stats(String studentName, String courseCode, String examName, Integer score, Integer maxScore, String timeSpent) {
        this.studentName = studentName;
        this.courseCode = courseCode;
        this.examName = examName;
        this.score = score;
        this.maxScore = maxScore;
        this.timeSpent = timeSpent;
    }

    public String getStudentName() { return this.studentName; }
    public void setStudentName (String name) { this.studentName = name; }

    public String getCourseCode() { return this.courseCode; }
    public void setCourseCode (String code) { this.courseCode = code; }

    public String getExamName() { return this.examName; }
    public void setExamName (String name) { this.examName = name; }

    public Integer getScore() { return this.score; }
    public void setScore (Integer score) { this.score = score; }

    public Integer getMaxScore() { return this.maxScore; }
    public void setMaxScore (Integer maxScore) { this.maxScore = maxScore; }

    public String getTimeSpent() { return this.timeSpent; }
    public void setTimeSpent (String timeSpent) { this.timeSpent = timeSpent; }
}
