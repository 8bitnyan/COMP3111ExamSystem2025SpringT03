package comp3111.examsystem.entity;

import java.util.List;

public class Question extends Entity {

    private Long teacherId; // To isolate questions per teacher
    private String type; // e.g., "MCQ", "Short Answer"
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private Integer score;
    private Integer isPublished = 0; // For confirmation of deletion

    private Long id;
    private List<String> options; // For MCQ
    private boolean isAble = true; // Required for lazy deletion


    // Constructors
    public Question() {} // Required for Database.java reflection

    public Question(Long teacherId, String type, String questionText, String A, String B, String C, String D, String answer, int score) {
        this.teacherId = teacherId;
        this.type = type;
        this.questionText = questionText;
        this.optionA = A;
        this.optionB = B;
        this.optionC = C;
        this.optionD = D;

        this.answer = answer;
        this.score = score;
    }

    // Getters and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }
    public String getOptionB() {
        return optionB;
    }
    public String getOptionC() {
        return optionC;
    }
    public String getOptionD() {
        return optionD;
    }

    public void setA(String A) {
        this.optionA = A;
    }
    public void setB(String B) {
        this.optionB = B;
    }
    public void setC(String C) {
        this.optionC = C;
    }
    public void setD(String D) {
        this.optionD = D;
    }


    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public int getPublished() {
        return isPublished;
    }

    public void setPublished(int publish) {
        isPublished = publish;
    }
}