package comp3111.examsystem.controller;

import comp3111.examsystem.entity.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for teacher question bank management page
 */
public class TeacherQuestionBankMgmtController implements Initializable {
    private Teacher teacher;

    /**
     * Initializes the UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * Sets the teacher object and initializes the UI
     * @param teacher The teacher object that is operating this page.
     */
    public void presetController(Teacher teacher) {
        this.teacher = teacher;
    }
}
