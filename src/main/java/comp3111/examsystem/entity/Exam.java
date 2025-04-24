package comp3111.examsystem.entity;


import comp3111.examsystem.tools.Database;
import comp3111.examsystem.tools.MsgSender;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.util.List;
import java.util.stream.Collectors;


public class Exam extends Entity {
    private String courseCode;
    private String examName;
    private long examTime;
    private List<Long> questions;
    private boolean isPublished;


    public Exam() {
        super();
    }

    public Exam(String courseCode, String examName, long publisherId, long examTime, List<Long> questions, boolean isPublished) {
        this.courseCode = courseCode;
        this.examName = examName;
        this.examTime = examTime;
        this.questions = questions;
        this.isPublished = false;
    }

    //Getter Function

    public String getCourseCode() {
        return courseCode;
    }

    public String getExamName() {
        return examName;
    }

    public long getExamTime() {
        return examTime;
    }

    public List<Long> getQuestions() {
        return questions;
    }


    public boolean getIsPublished() {
        return isPublished;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setExamName(String examName) {
        this.courseCode = courseCode;
    }

    public void setExamTime(Long examTime) {
        this.courseCode = courseCode;
    }

    public void setIsPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }

    public void setQuestions(List<Long> newQuestions) {
        this.questions = newQuestions;
    }
}
