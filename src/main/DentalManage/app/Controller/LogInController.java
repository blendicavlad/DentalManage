package app.Controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.ResourceBundle;

import app.AppManager;
import app.UxUtils;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import DB.DBConnect;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;



public class LogInController implements Initializable{

    @FXML
    public ImageView userLogo = new ImageView();
    @FXML
    public ImageView passLogo = new ImageView();
    @FXML
    private JFXTextField textUsername;

    @FXML
    private JFXPasswordField textPassword;

    private Connection connection = null;

    public LogInController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void buttonPressed(KeyEvent event)
    {
        if(event.getCode().toString().equals("ENTER"))
        {
            logInAction(event);
        }
    }

    public void buttonClicked(ActionEvent event){
       logInAction(event);
    }

    private void logInAction(Event event){

        String email = null;
        String password = null;
        Parent parent = null;
        if (textPassword.getText() == null || textPassword.getText() == null)
            UxUtils.errorBox("Please fill in all forms",null, "Failed");
        else{
            email = textUsername.getText();
            password = textPassword.getText();
        }
        String sql = "SELECT * FROM User WHERE userName = ? and userPass = ?";
        boolean connectionError = false;
        try {
            connection = DBConnect.getConnection();
        } catch (RuntimeException ex){
            connectionError = true;
        }
        if(!connectionError) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    UxUtils.errorBox("Incorrect email or password", null, "Failed");
                } else {
                    if (resultSet.getBoolean("isAdmin") || !resultSet.getBoolean("isDeleted")) {
                        Node node = (Node) event.getSource();
                        Stage dialogStage = (Stage) node.getScene().getWindow();
                        dialogStage.close();
                        AppManager.getAppManager().setLoggedUser(resultSet.getInt("userID"));
                        try {
                            AppManager.getAppManager().setLoggedUser(resultSet.getInt("userID"));
                            if (resultSet.getBoolean("isAdmin"))
                                parent = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.ADMIN_PANE.getPane())));
                            else if (!resultSet.getBoolean("isDeleted"))
                                parent = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.MEDIC_PANE.getPane())));
                        } catch (NullPointerException e) {
                            UxUtils.errorBox("Could not load view", null, "Failed");
                        }
                        if (parent != null) {
                            Scene scene = new Scene(parent);
                            dialogStage.setScene(scene);
                            dialogStage.setResizable(false);
                            dialogStage.show();
                        }
                    } else
                        UxUtils.info("The user has been deactivated",null,"Terminated Account");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else
            UxUtils.errorBox("Could not connect to the server",null,"Failed");
    }

}
