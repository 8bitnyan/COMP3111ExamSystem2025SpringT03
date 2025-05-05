package comp3111.examsystem.entity;

import java.util.ArrayList;
import java.util.List;

public class Exam extends Entity {
    
    private Long id;
    private Long teacherId; // To isolate exams per teacher
    private String name;
    private String examName; // Added for database compatibility
    private String courseCode; // Changed from courseId to courseCode
    private Integer duration; // in minutes
    private String examTime; // Added for database compatibility
    private String isPublished = "0"; // Changed from Integer to String
    private Integer isPublishedInt = 0; // Added for API compatibility
    private Boolean published = false; // Added for database compatibility
    private Boolean enabled = true; // Added for compatibility with Entity.isAble
    private List<Long> questionIds = new ArrayList<>(); // IDs of questions included in this exam
    private String questions; // Added for database compatibility
    
    // Default constructor required for Database.java reflection
    public Exam() {}
    
    /**
     * Full constructor for creating a new exam
     */
    public Exam(Long teacherId, String name, String courseCode, Integer duration, Integer isPublished, List<Long> questionIds) {
        this.teacherId = teacherId;
        this.name = name;
        this.examName = name; // Set examName to match name
        this.courseCode = courseCode;
        this.duration = duration;
        this.examTime = duration != null ? duration.toString() + " minutes" : null; // Initialize examTime
        this.isPublishedInt = isPublished;
        this.isPublished = isPublished != null ? isPublished.toString() : "0"; // Convert to String
        this.published = isPublished != null && isPublished > 0; // Set published boolean based on isPublished integer
        this.enabled = true; // Default to enabled
        // Always copy input list to ensure mutability
        this.questionIds = questionIds != null ? new ArrayList<>(questionIds) : new ArrayList<>();
        updateQuestionsField(); // Initialize questions field
    }
    
    /**
     * Updates the questions field based on questionIds
     */
    private void updateQuestionsField() {
        if (questionIds != null && !questionIds.isEmpty()) {
            this.questions = String.join(",", questionIds.stream().map(Object::toString).toArray(String[]::new));
        } else {
            this.questions = "";
        }
    }
    
    /**
     * Add a question to this exam
     * @param questionId The ID of the question to add
     * @return true if added, false if already exists
     */
    public boolean addQuestion(Long questionId) {
        if (!questionIds.contains(questionId)) {
            questionIds.add(questionId);
            updateQuestionsField();
            return true;
        }
        return false;
    }
    
    /**
     * Remove a question from this exam
     * @param questionId The ID of the question to remove
     * @return true if removed, false if not found
     */
    public boolean removeQuestion(Long questionId) {
        boolean removed = questionIds.remove(questionId);
        if (removed) {
            updateQuestionsField();
        }
        return removed;
    }
    
    /**
     * Get the number of questions in this exam
     * @return The question count
     */
    public int getQuestionCount() {
        return questionIds.size();
    }
    
    // Getters and Setters
    
    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        this.examName = name; // Keep examName in sync with name
    }
    
    public String getExamName() {
        return examName;
    }
    
    public void setExamName(String examName) {
        this.examName = examName;
        this.name = examName; // Keep name in sync with examName
    }
    
    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    
    // For backwards compatibility in case any code still uses the old method names
    @Deprecated
    public String getCourseId() {
        return courseCode;
    }
    
    @Deprecated
    public void setCourseId(String courseCode) {
        this.courseCode = courseCode;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
        this.examTime = duration != null ? duration.toString() + " minutes" : null; // Update examTime when duration changes
    }
    
    public String getExamTime() {
        return examTime;
    }
    
    public void setExamTime(String examTime) {
        this.examTime = examTime;
        // Try to parse duration from examTime if possible
        if (examTime != null && examTime.contains(" minutes")) {
            try {
                String durationStr = examTime.replace(" minutes", "").trim();
                this.duration = Integer.parseInt(durationStr);
            } catch (NumberFormatException e) {
                // If parsing fails, keep the existing duration
            }
        }
    }
    
    public String getIsPublished() {
        return isPublished;
    }
    
    public void setIsPublished(String isPublished) {
        this.isPublished = isPublished;
        // Try to update isPublishedInt
        try {
            this.isPublishedInt = Integer.parseInt(isPublished);
        } catch (NumberFormatException e) {
            // If parsing fails, default to 0
            this.isPublishedInt = 0;
        }
        // Update published boolean
        this.published = this.isPublishedInt > 0;
    }
    
    // For API compatibility with existing code
    public Integer getIsPublishedInt() {
        return isPublishedInt;
    }
    
    public void setIsPublishedInt(Integer isPublishedInt) {
        this.isPublishedInt = isPublishedInt;
        this.isPublished = isPublishedInt != null ? isPublishedInt.toString() : "0";
        this.published = isPublishedInt != null && isPublishedInt > 0;
    }
    
    public Boolean getPublished() {
        return published;
    }
    
    public void setPublished(Boolean published) {
        this.published = published;
        this.isPublishedInt = published != null && published ? 1 : 0;
        this.isPublished = this.isPublishedInt.toString();
    }
    
    @Override
    public Boolean getIsAble() {
        return enabled;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
        super.isAble = enabled; // Keep base class isAble in sync
    }
    
    public List<Long> getQuestionIds() {
        return questionIds;
    }
    
    public void setQuestionIds(List<?> questionIds) {
        if (questionIds == null) {
            this.questionIds = new ArrayList<>();
        } else {
            this.questionIds = new ArrayList<>();
            for (Object id : questionIds) {
                if (id instanceof Long) {
                    this.questionIds.add((Long) id);
                } else if (id instanceof String) {
                    try {
                        this.questionIds.add(Long.parseLong((String) id));
                    } catch (NumberFormatException e) {
                        // Skip invalid string IDs
                    }
                }
            }
        }
        updateQuestionsField(); // Update questions field when questionIds changes
    }
    
    public String getQuestions() {
        return questions;
    }
    
    public void setQuestions(String questions) {
        this.questions = questions;
        // Try to parse and update questionIds from questions string
        if (questions != null && !questions.isEmpty()) {
            try {
                String[] idStrings = questions.split(",");
                List<Long> ids = new ArrayList<>();
                for (String idStr : idStrings) {
                    if (!idStr.trim().isEmpty()) {
                        ids.add(Long.parseLong(idStr.trim()));
                    }
                }
                // Only update questionIds if we parsed at least one valid ID
                if (!ids.isEmpty()) {
                    this.questionIds = ids;
                }
            } catch (NumberFormatException e) {
                // If parsing fails, keep the existing questionIds (does NOT clear the list)
            }
        }
    }
}
