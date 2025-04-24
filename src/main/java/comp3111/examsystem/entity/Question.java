package comp3111.examsystem.entity;

import java.util.List;

public class Question extends Entity {
    private Long id;
    private Long teacherId; // To isolate questions per teacher
    private String type; // e.g., "MCQ", "Short Answer"
    private String questionText;
    private List<String> options; // For MCQ
    private String answer;
    private float score;
    private boolean isAble = true; // Required for lazy deletion
    private boolean isPublished = false; // For confirmation of deletion

    // Constructors
    public Question() {} // Required for Database.java reflection

    public Question(Long teacherId, String type, String questionText, List<String> options, String answer, int score) {
        this.teacherId = teacherId;
        this.type = type;
        this.questionText = questionText;
        this.options = options;
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

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public boolean isAble() {
        return isAble;
    }

    public void setAble(boolean able) {
        isAble = able;
    }

    public boolean getPublished() {
        return isPublished;
    }

    public void setPublished(boolean publish) {
        isPublished = publish;
    }
}