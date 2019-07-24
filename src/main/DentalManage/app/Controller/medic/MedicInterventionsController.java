package app.Controller.medic;

import app.AppManager;
import app.Controller.BaseController;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Entity.Intervention;
import model.Entity.Price;
import model.Repository.InterventionRepository;
import model.Repository.PriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executor;

public class MedicInterventionsController extends BaseController implements Initializable
{

    @FXML
    public TableView<Intervention> interventionsTable;
    @FXML
    public TableColumn<Intervention,Integer> interventionID;
    @FXML
    public TableColumn<Intervention,String> interventionName;
    @FXML
    public TableColumn<Intervention,Integer> price;
    @FXML
    public TableColumn<Intervention,Integer> duration;
    @FXML
    public TableColumn<Intervention,String> isDeleted;
    @FXML
    public JFXTextArea resultConsole;
    @FXML
    public JFXTextField idField;
    @FXML
    public JFXTextField interventionNameField;
    @FXML
    public JFXTextField priceField;
    @FXML
    public JFXDatePicker olderThan;
    @FXML
    public JFXTextField durationField;

    private Executor exec ;
    private static final Logger logger = LoggerFactory.getLogger(MedicInterventionsController.class);
    private EntityManager entityManager = AppManager.getAppManager().getEntityManager();
    private InterventionRepository interventionRepository = new InterventionRepository(entityManager);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resultConsole.setEditable(false);
        AppManager.getAppManager().cacheController(MedicInterventionsController.class, this);
        exec = AppManager.getAppManager().getExecutor();
        interventionID.setCellValueFactory(new PropertyValueFactory<>("interventionID"));
        interventionName.setCellValueFactory(new PropertyValueFactory<>("interventionName"));
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));
        isDeleted.setCellValueFactory(cellData -> {
            String isActiveIntervention = cellData.getValue().isDeleted() ? "No" : "Yes";
            return new ReadOnlyStringWrapper(isActiveIntervention);
        });
        fillTable(null);
    }

    @Override
    public void searchAction(ActionEvent actionEvent) {
        HashMap<String, Object> interventionData = getInput();
        if(interventionData.keySet().toArray().length > 0) {
            fillTable(interventionData);
        }else {
            fillTable(null);
        }
        clearFields();
    }

    @Override
    public void fillTable(HashMap<String, Object> whereClause) {
        Task<List<Intervention>> task = new Task<List<Intervention>>(){
            @Override
            protected List<Intervention> call(){
                logger.info("Fill Intervention Table Task executed");
                return interventionRepository.getAll(whereClause);
            }
        };
        exec.execute(task);
        task.setOnFailed(e -> logger.error(task.getException().getMessage()));
        task.setOnSucceeded(e -> interventionsTable.getItems().setAll( task.getValue()));
    }

    @Override
    public void updateView() {
        fillTable(null);
    }

    @Override
    public HashMap<String, Object> getInput() {
        HashMap<String,Object> input = new HashMap<>();
        if(!interventionNameField.getText().trim().isEmpty())
            input.put("interventionName",interventionNameField.getText());
        if(!durationField.getText().trim().isEmpty())
            input.put("duration",Integer.parseInt(durationField.getText()));
        if(!priceField.getText().trim().isEmpty()) {
            input.put("price", Integer.parseInt(priceField.getText()));
        }
        return input;
    }

    public void showPricesAction(ActionEvent actionEvent) {
        olderThan.getValue();
        StringBuilder result = new StringBuilder();
        Intervention intervention = interventionsTable.getSelectionModel().getSelectedItem();
        PriceRepository priceRepository = new PriceRepository(null);
        List<Price> prices = priceRepository.getPrices(intervention.getInterventionID());
        Collections.reverse(prices);
        prices.forEach(e -> result.append("Price: ")
                .append(e.getPrice())
                .append("\nViability: ")
                .append(e.getStartDate())
                .append("-")
                .append(e.getEndDate().isPresent() ? e.getEndDate().get() : " undetermined").append("\r\n-----------"));
        resultConsole.setText(result.toString());
    }

    private void clearFields() {
        interventionNameField.clear();
        priceField.clear();
        durationField.clear();
        idField.clear();
        olderThan.getEditor().clear();
        olderThan.setValue(null);
    }
}
