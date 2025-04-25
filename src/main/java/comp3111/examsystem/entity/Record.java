package comp3111.examsystem.entity;

public class Record extends Entity{
    private Long questionID;
    private Long studentID;
    private Long examID;
    private String response;
    private Integer score;

    public Record() { super(); }

    public Record(Long questionID, Long studentID, Long examID, String response, Integer score) {
        this.questionID = questionID;
        this.studentID = studentID;
        this.examID = examID;
        this.response = response;
        this.score = score;
    }

    public Long getQuestionID() {
        return questionID;
    }

    public void setQuestionID(Long questionID) {
        this.questionID = questionID;
    }

    public Long getStudentID() {
        return studentID;
    }

    public void setStudent(Long studentID) {
        this.studentID = studentID;
    }

    public Long getExamID() {
        return examID;
    }

    public void setExamID(Long examID) {
        this.examID = examID;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}