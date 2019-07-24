package app.Controller.admin;

import app.AppManager;
import app.Controller.BaseController;
import app.Controller.Panes;
import app.UxUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdminController extends BaseController implements Initializable {

    @FXML
    private Pane dashboard;
    @FXML
    private Pane mainPane;
    @FXML
    private Pane userPane;
    @FXML
    private Pane interventionsPane;
    @FXML
    private Pane medicsPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPanes();
    }


    public void homeAction(ActionEvent actionEvent) {
        setPane(dashboard);
    }

    public void usersAction(ActionEvent actionEvent){
        setPane(userPane);
    }

    public void interventionsAction(ActionEvent actionEvent){
        setPane(interventionsPane);
    }

    public void medicsAction(ActionEvent actionEvent){
        setPane(medicsPane);
    }

    public void logOutAction(ActionEvent actionEvent){
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
        scene = new Scene(parent);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private void setPane(Pane newPane){
        mainPane.setManaged(false);
        mainPane.getChildren().clear();
        mainPane.getChildren().add(newPane);
    }

    private void loadPanes(){
        try {
            if(dashboard == null)
                dashboard = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.ADMIN_DASHBOARD.getPane())));
            if(userPane == null)
                userPane = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.USER_PANE.getPane())));
            if(interventionsPane == null)
                interventionsPane = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.INTERVENTIONS_PANE.getPane())));
            if(medicsPane == null)
                medicsPane = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.MEDICS_PANE.getPane())));
            mainPane.getChildren().add(dashboard);
        } catch (IOException | NullPointerException e){
            e.printStackTrace();
            UxUtils.errorBox("Could not load the UI",null,"INITIALISATION ERROR");
        }
    }
}
