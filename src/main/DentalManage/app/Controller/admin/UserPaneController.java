package app.Controller.admin;

import app.AppManager;
import app.Controller.BaseController;
import app.UxUtils;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.layout.AnchorPane;
import model.Entity.User;

import javafx.event.ActionEvent;
import model.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.net.URL;

import java.util.*;
import java.util.concurrent.Executor;

public class UserPaneController extends BaseController implements Initializable {

    @FXML
    public AnchorPane usersPane;
    @FXML
    private ComboBox jobCombo;
    @FXML
    private JFXTextField idField;
    @FXML
    private JFXTextField usernameField;
    @FXML
    private JFXPasswordField passwordField;
    @FXML
    private JFXTextField phoneField;
    @FXML
    private JFXTextField emailField;
    @FXML
    private JFXTextArea resultConsole;
    @FXML
    private TableColumn<User, String> userName;
    @FXML
    private TableColumn<User, Integer> userId;
    @FXML
    private TableColumn<User, String> userPhone;
    @FXML
    private TableColumn<User, String> userMail;
    @FXML
    private TableColumn<User, String> isAdmin;
    @FXML
    private TableColumn<User, String> isDeleted;
    @FXML
    private TableView<User> userTable;
    private static final Logger logger = LoggerFactory.getLogger(UserPaneController.class);
    private Executor exec;
    private EntityManager entityManager = AppManager.getAppManager().getEntityManager();
    private UserRepository userRepository = new UserRepository(entityManager);

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        exec = AppManager.getAppManager().getExecutor();
        jobCombo.getItems().removeAll(jobCombo.getItems());
        jobCombo.getItems().addAll("Admin","Medic");
        userId.setCellValueFactory(new PropertyValueFactory<>("userID"));
        userName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userMail.setCellValueFactory(new PropertyValueFactory<>("userMail"));
        userPhone.setCellValueFactory(new PropertyValueFactory<>("userPhone"));
        isAdmin.setCellValueFactory(cellData -> {
            String isAdminAsString = cellData.getValue().isAdmin() ? "Admin" : "Medic";
            return new ReadOnlyStringWrapper(isAdminAsString);
        });
        isDeleted.setCellValueFactory(cellData -> {
            String isActiveUser = cellData.getValue().isDeleted() ? "No" : "Yes";
            return new ReadOnlyStringWrapper(isActiveUser);
        });
        fillTable(null);
    }

    public void insertAction(ActionEvent actionEvent){
          HashMap<String,Object> userInput = getInput();
          User user = null;
          try{
              user = userRepository.generateFromInput(userInput);
          }catch (Exception e){
              resultConsole.setText(e.getMessage());
          }
          if(user != null) {
              if(userRepository.save(user)) {
                  resultConsole.setText("User inserted");
                  fillTable(null);
              }
          }
          clearFields();
    }

    public void updateAction(ActionEvent actionEvent){
        User user = userTable.getSelectionModel().getSelectedItem();
        HashMap<String, Object> userInput = getInput();
        if(user != null && userInput.size() > 0){
            userInput.remove("userID");
            try{
                userRepository.updateFromInput(user,userInput);
            }catch (Exception e){
                resultConsole.setText(e.getMessage());
            }
            resultConsole.setText("User updated");
            fillTable(null);
        }
        clearFields();
    }

    public void deleteAction(ActionEvent actionEvent){
        ArrayList<User> users = getUsers();
        if(users != null) {
            users.forEach(i -> {
                i.setDeleted(true);
                userRepository.save(i);
                resultConsole.setText("User with ID " + i.getUserID() + " reactivated");
            });
            fillTable(null);
        }
    }

    public void reactivateAction(ActionEvent actionEvent){
        ArrayList<User> users = getUsers();
        if(users != null) {
            users.forEach(i -> {
                i.setDeleted(false);
                userRepository.save(i);
                resultConsole.setText("User with ID " + i.getUserID() + " reactivated");
            });
            fillTable(null);
        }
    }

    public void searchAction(ActionEvent actionEvent){
        HashMap<String, Object> userData = getInput();
        if(userData.keySet().toArray().length > 0) {
            if(!"userPass".equals(String.valueOf(userData.keySet().toArray()[0]))) {
                fillTable(userData);
            }
        }else {
            fillTable(null);
        }
        clearFields();
    }


    private ArrayList<User> getUsers(){
        HashMap<String,Object> userData = getInput();
        ArrayList<User> users = new ArrayList<>();
        if(userData.containsKey("userID"))
            userRepository.findById((Integer.valueOf(userData.get("userID").toString()))).ifPresent(users::add);
        else if(userData.containsKey("userPhone"))
            userRepository.findByColumn("userPhone",String.valueOf(userData.get("userPhone")),true)
                    .forEach(opt -> opt
                            .ifPresent(users::add));
        else if(userData.containsKey("userMail"))
            userRepository.findByColumn("userMail",String.valueOf(userData.get("userMail")),true)
                    .forEach(opt -> opt
                            .ifPresent(users::add));
        else if(userData.containsKey("userName"))
            userRepository.findByName(String.valueOf(userData.get("userName"))).ifPresent(users::add);
        if(!users.isEmpty()){
            return users;
        }else
            users.addAll(userTable.getSelectionModel().getSelectedItems());
        if(users.isEmpty()){
            UxUtils.info("Could not find medic",null, "User not found");
        } return users;
    }

    public HashMap<String,Object> getInput(){
        HashMap<String,Object> input = new HashMap<>();
        if(!usernameField.getText().trim().isEmpty())
            input.put("userName",usernameField.getText());
        if(!passwordField.getText().trim().isEmpty())
            input.put("userPass",passwordField.getText());
        if(!phoneField.getText().trim().isEmpty()) {
            if(phoneField.getText().matches("[0-9]+"))
                input.put("userPhone", phoneField.getText());
            else
                resultConsole.setText("Phone number must be numeric!");
        }
        if(!emailField.getText().trim().isEmpty()) {
            input.put("userMail", emailField.getText());
        }
        if(!jobCombo.getSelectionModel().isEmpty()) {
            input.put("isAdmin", getComboValue());
        }
        return input;
    }

    public void fillTable(HashMap<String,Object> whereClause){
        Task<List<User>> task = new Task<List<User>>(){
            @Override
            protected List<User> call(){
                logger.info("Fill medic Table Task executed");
                return userRepository.getAll(whereClause);
            }
        };
        exec.execute(task);
        task.setOnFailed(e -> logger.error(task.getException().getMessage()));
        task.setOnSucceeded(e -> userTable.getItems().setAll( task.getValue()));
    }
    private void clearFields(){
        jobCombo.getSelectionModel().clearSelection();
        idField.clear();
        usernameField.clear();
        passwordField.clear();
        phoneField.clear();
        emailField.clear();
    }

    private boolean getComboValue(){
        return String.valueOf(jobCombo.getValue()).equals("Admin");
    }
}
