package zunayedhassan.EmailTemplateMaker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Zunayed Hassan
 */
public class EmailTemplateMakerApp extends Application {
    public static final String ICON = "icons/icon.png";
    
    @Override
    public void start(Stage primaryStage) {
        EmailTemplateMakerProgram program = new EmailTemplateMakerProgram(primaryStage);
        Scene scene = new Scene(program, 400, 200);
        
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream(ICON)));
        primaryStage.setTitle("Email Templates Generator");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
