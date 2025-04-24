package comp3111.examsystem.controller;

import comp3111.examsystem.entity.*;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for student main page.
 */
public class StudentMainController implements Initializable {
    private Student student;

    /**
     * Initializes the student main page UI.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * Sets the student object and initializes the UI.
     * @param student The student that is operating the page.
     */
    public void preSetController(Student student) {
        this.student = student;
    }

}
