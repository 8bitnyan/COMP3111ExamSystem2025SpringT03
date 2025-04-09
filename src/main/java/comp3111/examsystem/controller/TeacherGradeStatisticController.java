package comp3111.examsystem.controller;
import comp3111.examsystem.entity.*;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.*;

/**
 * The controller for teacher grade statistic page.
 */
public class TeacherGradeStatisticController implements Initializable {
    private Teacher teacher;

    /**
     * Initializes the page and loads the data into the table and charts.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * Sets the teacher object and initializes the UI.
     * @param teacher The teacher object that is operating this page.
     */
    public void presetController(Teacher teacher) {
        this.teacher = teacher;
    }
}
