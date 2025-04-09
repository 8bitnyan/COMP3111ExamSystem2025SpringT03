package comp3111.examsystem.controller;
import comp3111.examsystem.entity.Teacher;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for teacher grade exam page.
 */
public class TeacherGradeExamController implements Initializable {
    private Teacher teacher;

    /**
     * Initializes the UI and filter options.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * Sets the teacher for this controller.
     * @param teacher The teacher to set.
     */
    public void presetController(Teacher teacher) {
        this.teacher = teacher;
    }
}
