package app;

import app.Controller.Panes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception{
        AppManager.getAppManager();
        Parent root = null;
        try {
            root = FXMLLoader.load((Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.ENTRY_PANE.getPane()))));
        } catch (NullPointerException e){
            UxUtils.errorBox("Resources are missing.","Cannot start app.","ERROR");
        }
        if(root != null){
            primaryStage.setTitle("Dental Manage");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
        }
        primaryStage.setOnCloseRequest(event -> System.exit(0));
    }


    public static void main(String[] args) {
        logger.info("Application has been started");
        launch(args);
    }
}
