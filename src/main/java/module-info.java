module comp3111.examsystem {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires jdk.jshell;

    opens comp3111.examsystem to javafx.fxml, javafx.base, javafx.graphics, javafx.controls, org.junit.jupiter.api, org.apiguardian.api;
    exports comp3111.examsystem;
    opens comp3111.examsystem.controller to javafx.fxml, javafx.base, javafx.graphics, javafx.controls;
    exports comp3111.examsystem.controller;
    opens comp3111.examsystem.entity to javafx.fxml, javafx.base, javafx.graphics, javafx.controls;
    exports comp3111.examsystem.entity;
    opens comp3111.examsystem.data to javafx.fxml, javafx.base, javafx.graphics, javafx.controls;
    exports comp3111.examsystem.data;
}