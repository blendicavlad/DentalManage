package app.Controller.medic;

import app.AppManager;
import app.Controller.BaseController;
import app.UxUtils;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Entity.Appointment;
import model.Entity.Intervention;
import model.Entity.InterventionLink;
import model.Repository.AppointmentRepository;
import model.Repository.InterventionLinkRepository;
import model.Repository.InterventionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executor;

public class AppointmentInterventionsController extends BaseController implements Initializable{

    @FXML
    public TableView<InterventionLink> interventionsTable;
    @FXML
    public TableColumn<InterventionLink, Integer> interventionID;
    @FXML
    public TableColumn<InterventionLink, String> interventionName;
    @FXML
    public TableColumn<InterventionLink, Integer> interventionPrice;
    @FXML
    public TableColumn<InterventionLink, Integer> interventionDuration;
    @FXML
    public JFXTextArea resultConsole;
    @FXML
    public JFXTextField idField;
    @FXML
    public TableView<Intervention> availableInterventionsTable;
    @FXML
    public TableColumn<Intervention, Integer> availableInterventionID;
    @FXML
    public TableColumn<Intervention, String> availableInterventionName;
    @FXML
    public TableColumn<Intervention, Integer> availableInterventionPrice;
    @FXML
    public TableColumn<Intervention, Integer> availableInterventionDuration;
    private Executor exec;
    private static final Logger logger = LoggerFactory.getLogger(AppointmentInterventionsController.class);
    private EntityManager entityManager = AppManager.getAppManager().getEntityManager();
    private InterventionLinkRepository interventionLinkRepository  = new InterventionLinkRepository(entityManager);
    private InterventionRepository InterventionRepository = new InterventionRepository(entityManager);
    private AppointmentRepository appointmentRepository = new AppointmentRepository(entityManager);
    private Appointment selectedAppointment;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exec = AppManager.getAppManager().getExecutor();
        AppManager.getAppManager().cacheController(AppointmentInterventionsController.class,this);
        selectedAppointment = (Appointment) AppManager.getAppManager().getCachedEntity(Appointment.class);
        interventionID.setCellValueFactory(new PropertyValueFactory<>("interventionLinkID"));
        interventionName.setCellValueFactory(new PropertyValueFactory<>("interventionName"));
        interventionPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        interventionDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        availableInterventionID.setCellValueFactory(new PropertyValueFactory<>("interventionID"));
        availableInterventionName.setCellValueFactory(new PropertyValueFactory<>("interventionName"));
        availableInterventionDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        availableInterventionPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        if(availableInterventionsTable.getItems().isEmpty()) {
            Task<List<Intervention>> task2 = new Task<List<Intervention>>() {
                @Override
                protected List<Intervention> call() {
                    logger.info("Fill Intervention Table Task executed");
                    return InterventionRepository.getAll(new HashMap<String, Object>() {
                        {
                            put("isDeleted", false);
                        }
                    });
                }
            };
            exec.execute(task2);
            task2.setOnFailed(e -> logger.error(task2.getException().getMessage()));
            task2.setOnFailed(e -> task2.getException().printStackTrace());
            task2.setOnSucceeded(e -> availableInterventionsTable.getItems().setAll(task2.getValue()));
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fillTable(null);
    }

    @Override
    public void insertAction(ActionEvent actionEvent) {
        if(!selectedAppointment.isActive()) {
            UxUtils.info("Can't insert intervention on a deleted appointment",null,"error");
            return;
        }
        Intervention intervention = availableInterventionsTable.getSelectionModel().getSelectedItem();
        if(intervention == null) {
            UxUtils.errorBox("Please select an intervention from the lower table",null,"INSERT ERROR");
            return;
        }
        Appointment appointment = appointmentRepository.findById(selectedAppointment.getAppointmentID()).orElse(null);
        try {
            interventionLinkRepository.createLink(intervention, appointment);
        } catch (Exception e){
            UxUtils.errorBox(e.getMessage(),null,"INSERT ERROR");
            e.printStackTrace();
            return;
        }
        AppManager.getAppManager().updateUI();
    }


    @Override
    public void deleteAction(ActionEvent actionEvent) {
        if(!selectedAppointment.isActive()) {
            UxUtils.info("Can't delete intervention of a deleted appointment",null,"error");
            return;
        }
        ArrayList<InterventionLink> interventions = new ArrayList<>(interventionsTable.getSelectionModel().getSelectedItems());
        if (interventions.isEmpty())
            resultConsole.setText("Please select an intervention");
        else {
            for (InterventionLink interventionLink : interventions) {
                try {
                    InterventionLink persistentInterventionLink = interventionLinkRepository.findByID(interventionLink.getInterventionLinkID());
                    interventionLinkRepository.deleteLink(persistentInterventionLink);
                } catch (Exception e) {
                    UxUtils.errorBox(e.getMessage(), null, "DELETE ERROR");
                    e.printStackTrace();
                    return;
                }
            }
            AppManager.getAppManager().updateUI();
        }
    }

    @Override
    public void fillTable(HashMap<String, Object> whereClause) {
        Task<List<InterventionLink>> task = new Task<List<InterventionLink>>(){
            @Override
            protected List<InterventionLink> call(){
                logger.info("Fill Intervention Table Task executed");
                return interventionLinkRepository.findLinksByAppointment(selectedAppointment);
            }
        };
        exec.execute(task);
        task.setOnFailed(e -> logger.error(task.getException().getMessage()));
        task.setOnFailed(e -> task.getException().printStackTrace());
        task.setOnSucceeded(e -> interventionsTable.getItems().setAll(task.getValue()));
    }

    @Override
    public void updateView() {
        fillTable(null);
    }

    @Override
    public HashMap<String, Object> getInput() {
        return null;
    }

}
