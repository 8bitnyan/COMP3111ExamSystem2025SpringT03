package comp3111.examsystem.entity;

public class TimeSpent extends Entity {
    private Long studentId;
    private Long examId;
    private Integer timeSpent;

    public TimeSpent() { };

    public TimeSpent(Long studentId, Long examId, Integer timeSpent) {
        this.studentId = studentId;
        this.examId = examId;
        this.timeSpent = timeSpent;
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public Integer getTimeSpent() { return timeSpent; }
    public void setTimeSpent(Integer timeSpent) { this.timeSpent = timeSpent; }
}
