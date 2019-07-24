package app.Controller.medic;

import app.AppManager;
import app.Controller.BaseController;
import app.Controller.Panes;
import app.UxUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MedicController extends BaseController implements Initializable {

    @FXML
    private Pane homePane;
    @FXML
    private Pane patientsPane;
    @FXML
    private Pane interventionsPane;
    @FXML
    private Pane medicDashboard;

    private static final Logger logger = LoggerFactory.getLogger(MedicController.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getAppManager().cacheController(MedicController.class, this);
        loadPanes();
    }

    public void homeAction(ActionEvent actionEvent) {
        setPane(medicDashboard);
    }

    public void patientsAction(ActionEvent actionEvent) {
        setPane(patientsPane);
    }

    public void appointmentsAction(ActionEvent actionEvent) {
    }

    public void interventionsAction(ActionEvent actionEvent) {
        setPane(interventionsPane);
    }

    private void setPane(Pane newPane){
        homePane.setManaged(false);
        homePane.getChildren().clear();
        homePane.getChildren().add(newPane);
    }

    public void logOutAction(ActionEvent actionEvent) {
        Node node = (Node) actionEvent.getSource();
        Parent parent = null;
        Scene scene;
        Stage dialogStage;
        dialogStage = (Stage) node.getScene().getWindow();
        dialogStage.close();
        try{
            parent = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("main.fxml")));
        } catch (IOException e){
            e.printStackTrace();
        }
        AppManager.clearCache();
        scene = new Scene(Objects.requireNonNull(parent));
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
        dialogStage.show();
    }

    private void loadPanes(){
        try {
            if(medicDashboard == null)
                medicDashboard = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.MEDIC_DASHBOARD.getPane())));
            if(patientsPane == null)
                patientsPane = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.PATIENTS_PANE.getPane())));
            if(interventionsPane == null)
                interventionsPane = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.MEDIC_INTERVENTIONS_PANE.getPane())));
            homePane.getChildren().add(medicDashboard);
        } catch (IOException | NullPointerException e){
            e.printStackTrace();
            logger.error(e.getMessage());
            UxUtils.errorBox("Could not load the UI",null,"INITIALISATION ERROR");
        }
    }
}
