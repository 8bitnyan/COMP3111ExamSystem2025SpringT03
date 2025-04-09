package comp3111.examsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import comp3111.examsystem.tools.UIhelper;

/**
 * Main class for the Exam System application.
 * This class extends the JavaFX Application class and serves as the entry point for the application.
 */
public class Main extends Application {
	/**
	 * Starts the JavaFX application by setting up the primary stage.
	 *
	 * @param primaryStage the primary stage for this application, onto which
	 *                     the application scene can be set.
	 */
	public void start(Stage primaryStage) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("LoginUI.fxml"));
			Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
			UIhelper.expandToFullScreen(primaryStage);

			primaryStage.setTitle("HKUST Examination System");
			primaryStage.setScene(scene);

			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The main method that launches the JavaFX application.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
