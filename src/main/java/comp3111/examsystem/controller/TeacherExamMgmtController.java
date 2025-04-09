package comp3111.examsystem.controller;
import comp3111.examsystem.entity.*;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.*;

/**
 * The controller for teacher exam management page.
 */
public class TeacherExamMgmtController implements Initializable {
    private Teacher teacher;

    /**
     * Initializes the teacher exam management page UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * Sets the teacher object and initializes the UI.
     * @param teacher The teacher object that is operating the page.
     */
    public void presetController(Teacher teacher) {
        this.teacher = teacher;
    }
}
