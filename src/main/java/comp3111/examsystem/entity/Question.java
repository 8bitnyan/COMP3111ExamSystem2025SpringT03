package comp3111.examsystem.entity;

import java.util.ArrayList;
import java.util.List;

public class Question extends Entity {

    private Long teacherId; // To isolate questions per teacher
    private String type; // e.g., "MCQ", "Short Answer"
    private String questionText;
    // Keep these for backward compatibility
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String optionE;
    private String answer;
    private Integer score;
    private Integer isPublished = 0; // For confirmation of deletion
    private int optionCount = 4; // Default is 4 options (A, B, C, D)

    private Long id;
    private List<String> options = new ArrayList<>(); // For adaptable MCQ options
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
        
        // Initialize options list
        if (A != null) options.add(A);
        if (B != null) options.add(B);
        if (C != null) options.add(C);
        if (D != null) options.add(D);
        this.optionCount = options.size();
    }

    // Method to get an option by index (0-based)
    public String getOption(int index) {
        if (index < 0 || index >= options.size()) {
            return null;
        }
        return options.get(index);
    }
    
    // Method to set an option by index (0-based)
    public void setOption(int index, String value) {
        while (options.size() <= index) {
            options.add("");
        }
        options.set(index, value);
        updateLegacyFields();
    }
    
    // Method to add a new option
    public void addOption(String value) {
        if (options.size() < 5) { // Max 5 options
            options.add(value);
            optionCount = options.size();
            updateLegacyFields();
        }
    }
    
    // Method to remove an option by index
    public boolean removeOption(int index) {
        if (index < 0 || index >= options.size() || options.size() <= 2) {
            return false; // Can't remove if fewer than 2 options would remain
        }
        options.remove(index);
        optionCount = options.size();
        updateLegacyFields();
        return true;
    }
    
    // Update legacy fields from options list
    private void updateLegacyFields() {
        optionA = options.size() > 0 ? options.get(0) : "";
        optionB = options.size() > 1 ? options.get(1) : "";
        optionC = options.size() > 2 ? options.get(2) : "";
        optionD = options.size() > 3 ? options.get(3) : "";
        optionE = options.size() > 4 ? options.get(4) : "";
    }
    
    // Update options list from legacy fields
    private void updateOptionsList() {
        options.clear();
        if (optionA != null && !optionA.isEmpty()) options.add(optionA);
        if (optionB != null && !optionB.isEmpty()) options.add(optionB);
        if (optionC != null && !optionC.isEmpty()) options.add(optionC);
        if (optionD != null && !optionD.isEmpty()) options.add(optionD);
        if (optionE != null && !optionE.isEmpty()) options.add(optionE);
        optionCount = options.size();
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
        if (options.isEmpty() && (optionA != null || optionB != null || optionC != null || optionD != null || optionE != null)) {
            updateOptionsList();
        }
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
        this.optionCount = options.size();
        updateLegacyFields();
    }
    
    public int getOptionCount() {
        return optionCount;
    }
    
    public void setOptionCount(int count) {
        this.optionCount = count;
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
    public String getOptionE() {
        return optionE;
    }

    public void setA(String A) {
        this.optionA = A;
        setOption(0, A);
    }
    public void setB(String B) {
        this.optionB = B;
        if (options.size() > 1) setOption(1, B);
        else if (B != null && !B.isEmpty()) addOption(B);
    }
    public void setC(String C) {
        this.optionC = C;
        if (options.size() > 2) setOption(2, C);
        else if (C != null && !C.isEmpty()) addOption(C);
    }
    public void setD(String D) {
        this.optionD = D;
        if (options.size() > 3) setOption(3, D);
        else if (D != null && !D.isEmpty()) addOption(D);
    }
    public void setE(String E) {
        this.optionE = E;
        if (options.size() > 4) setOption(4, E);
        else if (E != null && !E.isEmpty()) addOption(E);
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