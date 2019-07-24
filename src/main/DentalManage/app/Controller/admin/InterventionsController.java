package app.Controller.admin;

import app.AppManager;
import app.Controller.BaseController;
import app.UxUtils;
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
import javafx.scene.layout.AnchorPane;
import model.Entity.Price;
import model.Repository.InterventionRepository;
import model.Entity.Intervention;
import model.Repository.PriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class InterventionsController extends BaseController implements Initializable {

    @FXML
    public TableView<Intervention> interventionsTable;
    @FXML
    public TableColumn<Intervention, Integer> interventionID;
    @FXML
    public TableColumn<Intervention, String> interventionName;
    @FXML
    public TableColumn<Intervention, String> price;
    @FXML
    public TableColumn<Intervention, String> duration;
    @FXML
    public TableColumn<Intervention, String> isDeleted;
    @FXML
    public JFXTextArea resultConsole;
    @FXML
    public JFXTextField interventionNameField;
    @FXML
    public JFXTextField priceField;
    @FXML
    public JFXDatePicker olderThan;
    @FXML
    public JFXTextField durationField;
    @FXML
    public JFXTextField idField;
    @FXML
    public AnchorPane interventionsPane;
    private static final Logger logger = LoggerFactory.getLogger(InterventionsController.class);
    private Executor exec;
    private EntityManager entityManager = AppManager.getAppManager().getEntityManager();
    private InterventionRepository interventionsRepository = new InterventionRepository(entityManager);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exec = AppManager.getAppManager().getExecutor();
        fillTable(null);
        interventionID.setCellValueFactory(new PropertyValueFactory<>("interventionID"));
        interventionName.setCellValueFactory(new PropertyValueFactory<>("interventionName"));
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));
        isDeleted.setCellValueFactory(cellData -> {
            String isActiveIntervention = cellData.getValue().isDeleted() ? "No" : "Yes";
            return new ReadOnlyStringWrapper(isActiveIntervention);
        });
    }

    @Override
    public void insertAction(ActionEvent actionEvent) {
        HashMap<String,Object> interventionInput = getInput();
        Intervention intervention = null;
        try{
            intervention = interventionsRepository.generateFromInput(interventionInput);
        }catch (Exception e){
            resultConsole.setText(e.getMessage());
        }
        if(intervention != null) {
            if(interventionsRepository.save(intervention)) {
                resultConsole.setText("Intervention inserted");
                fillTable(null);
                clearFields();
            }
        }
    }

    @Override
    public void updateAction(ActionEvent actionEvent) {
        Intervention intervention = interventionsTable.getSelectionModel().getSelectedItem();
        HashMap<String, Object> interventionInput = getInput();
        if(intervention != null && interventionInput.size() > 0){
            interventionInput.remove("interventionID");
            try{
                interventionsRepository.updateFromInput(intervention,interventionInput);
            }catch (Exception e){
                logger.error(e.getMessage());
                e.printStackTrace();
                resultConsole.setText("Could not update intervention");
            }
            resultConsole.setText("Intervention updated");
            fillTable(null);
        }
        clearFields();
    }

    @Override
    public void deleteAction(ActionEvent actionEvent) {
        ArrayList<Intervention> interventions = getInterventions();
        if(interventions != null) {
            interventions.forEach(i -> {
                i.setDeleted(true);
                i.getPrices().forEach(p -> p.setDeleted(true));
                interventionsRepository.save(i);
                resultConsole.setText("Intervention with ID " + i.getInterventionID() + " deleted");
            });
            fillTable(null);
        }
    }

    @Override
    public void reactivateAction(ActionEvent actionEvent) {
        ArrayList<Intervention> interventions = getInterventions();
        if(interventions != null) {
            interventions.forEach(i -> {
                i.setDeleted(false);
                i.getPrices().forEach(p -> p.setDeleted(false));
                interventionsRepository.save(i);
                resultConsole.setText("Intervention with ID " + i.getInterventionID() + " reactivated");
            });
            fillTable(null);
        }
    }

    private ArrayList<Intervention> getInterventions(){
        HashMap<String,Object> interventionData = getInput();
        ArrayList<Intervention> interventions = new ArrayList<>();
        if(interventionData.containsKey("interventionID"))
            interventionsRepository.findById((Integer.valueOf(interventionData.get("interventionID").toString()))).ifPresent(interventions::add);
        else if(interventionData.containsKey("duration"))
            interventionsRepository.findByColumn("duration",Integer.parseInt(interventionData.get("duration").toString()),false)
                    .forEach(opt -> opt
                            .ifPresent(interventions::add));
        else if(interventionData.containsKey("price"))
            interventionsRepository.findByColumn("price",Integer.parseInt(interventionData.get("price").toString()),false)
                    .forEach(opt -> opt
                            .ifPresent(interventions::add));
        else if(interventionData.containsKey("interventionName"))
            interventionsRepository.findByName(String.valueOf(interventionData.get("interventionName"))).ifPresent(interventions::add);
        if(!interventions.isEmpty()){
            return interventions;
        }else
            interventions.addAll(interventionsTable.getSelectionModel().getSelectedItems());
        if(interventions.isEmpty()){
            UxUtils.info("Could not find intervention",null, "User not found");
        } return interventions;
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
    public void fillTable(HashMap<String,Object> whereClause) {
        Task<List<Intervention>> task = new Task<List<Intervention>>(){
            @Override
            protected List<Intervention> call(){
                logger.info("Fill Intervention Table Task executed");
                return interventionsRepository.getAll(whereClause);
            }
        };
        exec.execute(task);
        task.setOnFailed(e -> logger.error(task.getException().getMessage()));
        task.setOnSucceeded(e -> interventionsTable.getItems().setAll( task.getValue()));
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
        if(olderThan.getValue() != null){
            LocalDate time = olderThan.getValue();
            Timestamp olderThan = Timestamp.valueOf(time.atStartOfDay());
            input.put("olderThan",olderThan);
        }
        return input;
    }

    private void clearFields() {
        interventionNameField.clear();
        priceField.clear();
        durationField.clear();
        idField.clear();
        olderThan.getEditor().clear();
        olderThan.setValue(null);
    }

    public void showPricesAction(ActionEvent actionEvent) {
        StringBuilder result = new StringBuilder();
        Intervention intervention = interventionsTable.getSelectionModel().getSelectedItem();
        PriceRepository priceRepository = new PriceRepository(null);
        List<Price> prices = priceRepository.getPrices(intervention.getInterventionID());
        List<Price> filteredPrices = new ArrayList<>();
        if(getInput().containsKey("olderThan")){
            Timestamp olderThan = (Timestamp) getInput().get("olderThan");
            filteredPrices = prices.stream().filter(price -> {
                return price.getEndDate().isPresent() && price.getEndDate().get().before(olderThan);
            }).collect(Collectors.toList());
        } else
            filteredPrices = prices;
        Collections.reverse(filteredPrices);
        filteredPrices.forEach(e -> result.append("Price: ")
                .append(e.getPrice())
                .append("\n")
                .append("Viability: ")
                .append(new Date(e.getStartDate().getTime()))
                .append(" - ")
                .append(e.getEndDate().isPresent() ? new Date(e.getEndDate().get().getTime()) : " undetermined").append("\n-------------\n"));
        resultConsole.setText(result.toString());
    }
}
