package app.Controller;



import javafx.event.ActionEvent;

import java.util.HashMap;

public interface ControllerInterface {

    void insertAction(ActionEvent actionEvent);
    void updateAction(ActionEvent actionEvent);
    void deleteAction(ActionEvent actionEvent);
    void reactivateAction(ActionEvent actionEvent);
    void searchAction(ActionEvent actionEvent);
    void fillTable(HashMap<String,Object> whereClause);
    void updateView();
    HashMap<String, Object> getInput();
}
