package app.Controller.medic;

import app.AppManager;
import app.Controller.BaseController;

import app.Controller.Panes;
import app.UxUtils;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Entity.Patient;
import model.Repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class PatientsController extends BaseController implements Initializable {

    @FXML
    public AnchorPane patientsPane;
    @FXML
    public TableView<Patient> patientsTable;
    @FXML
    public JFXTextField idField;
    @FXML
    public JFXTextField patientNameField;
    @FXML
    public JFXTextField phoneField;
    @FXML
    public JFXTextField emailField;
    @FXML
    public TableColumn<Patient,Integer> patientID;
    @FXML
    public TableColumn<Patient,String> patientName;
    @FXML
    public TableColumn<Patient,String> patientMail;
    @FXML
    public TableColumn<Patient,String> patientPhone;
    @FXML
    public TableColumn<Patient, String> interventionCount;
    @FXML
    public TableColumn<Patient,String> isDeleted;
    @FXML
    public JFXTextArea resultConsole;

    private Stage stage = new Stage();
    private Executor exec;
    private static final Logger logger = LoggerFactory.getLogger(PatientsController.class);
    private EntityManager entityManager = AppManager.getAppManager().getEntityManager();
    private PatientRepository patientRepository = new PatientRepository(entityManager);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        resultConsole.setEditable(false);
        AppManager.getAppManager().cacheController(PatientsController.class, this);
        exec = AppManager.getAppManager().getExecutor();
        patientID.setCellValueFactory(new PropertyValueFactory<>("patientID"));
        patientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        patientMail.setCellValueFactory(new PropertyValueFactory<>("patientMail"));
        patientPhone.setCellValueFactory(new PropertyValueFactory<>("patientPhone"));
        interventionCount.setCellValueFactory(cellData -> {
            String interventionsCount = cellData.getValue().getInterventionCount() != null ? cellData.getValue().getInterventionCount().toString() : "0";
            return new ReadOnlyStringWrapper(interventionsCount);
        });
        isDeleted.setCellValueFactory(cellData -> {
            String isActiveUser = cellData.getValue().isDeleted() ? "No" : "Yes";
            return new ReadOnlyStringWrapper(isActiveUser);
        });
        fillTable(null);
    }

    @Override
    public void insertAction(ActionEvent actionEvent) {
        HashMap<String,Object> patientInput = getInput();
        Patient patient = null;
        try{
            patient = patientRepository.generateFromInput(patientInput);
        }catch (Exception e){
            UxUtils.errorBox(e.getMessage(), null,"INSERT ERROR");
        }
        if(patient != null) {
            if(patientRepository.save(patient)) {
                resultConsole.setText("Patient inserted");
                AppManager.getAppManager().updateUI();
            }
        }
        clearFields();
    }

    @Override
    public void updateAction(ActionEvent actionEvent) {
        Patient patient = patientsTable.getSelectionModel().getSelectedItem();
        HashMap<String, Object> patientInput = getInput();
        if(patient != null && patientInput.size() > 0){
            patientInput.remove("patientID");
            try{
                patientRepository.updateFromInput(patient,patientInput);
            }catch (Exception e){
                UxUtils.errorBox(e.getMessage(), null,"UPDATE ERROR");
            }
            resultConsole.setText("Patient updated");
            AppManager.getAppManager().updateUI();
        }
        clearFields();
    }

    public void deleteAction(ActionEvent actionEvent){
        ArrayList<Patient> patients = getPatients();
        if(patients != null) {
            patients.forEach(i -> {
                patientRepository.deletePatient(i);
                //patientRepository.save(i);
                resultConsole.setText("Patient with ID " + i.getPatientID() + " deleted");
            });
            AppManager.getAppManager().updateUI();
        }
    }

    public void reactivateAction(ActionEvent actionEvent){
        ArrayList<Patient> patients = getPatients();
        if(patients != null) {
            patients.forEach(i -> {
                //patientRepository.
                patientRepository.reactivatePatient(i);
                resultConsole.setText("Patient with ID " + i.getPatientID() + " reactivated");
            });
            AppManager.getAppManager().updateUI();
        }
    }

    public void searchAction(ActionEvent actionEvent){
        HashMap<String, Object> patientData = getInput();
        if(patientData.keySet().toArray().length > 0) {
            if(!"userPass".equals(String.valueOf(patientData.keySet().toArray()[0]))) {
                fillTable(patientData);
            }
        }else {
            fillTable(null);
        }
        clearFields();
    }

    private ArrayList<Patient> getPatients(){
        HashMap<String,Object> patientData = getInput();
        ArrayList<Patient> patients = new ArrayList<>();
        if(patientData.containsKey("patientID"))
            patientRepository.findById((Integer)patientData.get("patientID")).ifPresent(patients::add);
        else if(patientData.containsKey("patientPhone"))
            patientRepository.findByColumn("patientPhone",String.valueOf(patientData.get("patientPhone")),true)
                    .forEach(opt -> opt
                            .ifPresent(patients::add));
        else if(patientData.containsKey("userMail"))
            patientRepository.findByColumn("patientMail",String.valueOf(patientData.get("patientMail")),true)
                    .forEach(opt -> opt
                            .ifPresent(patients::add));
        else if(patientData.containsKey("userName"))
            patientRepository.findByName(String.valueOf(patientData.get("patientName"))).ifPresent(patients::add);
        if(!patients.isEmpty()){
            return patients;
        }else
            patients.addAll(patientsTable.getSelectionModel().getSelectedItems());
        if(patients.isEmpty()){
            UxUtils.info("Could not find patient",null, "Patient not found");
        } return patients;
    }

    public void fillTable(HashMap<String, Object> whereClause) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("medicID", AppManager.getAppManager().getLoggedUser());
        if(whereClause != null && !whereClause.isEmpty())
            params.putAll(whereClause);
        Task<List<Patient>> task = new Task<List<Patient>>(){
            protected List<Patient> call(){
                logger.info("Fill patients table Task executed");
                return patientRepository.getAll(params);
            }
        };
        exec.execute(task);
        task.setOnFailed(e -> logger.error(task.getException().getMessage()));
        task.setOnSucceeded(e -> patientsTable.getItems().setAll( task.getValue()));
    }

    public void showAppAction(ActionEvent actionEvent) {
        if(patientsTable.getSelectionModel().getSelectedItems() != null){
            if(patientsTable.getSelectionModel().getSelectedItems().size() > 1)
                resultConsole.setText("Please select a single patient");
            else {
                Patient patient = patientsTable.getSelectionModel().getSelectedItem();
                AppManager.getAppManager().cacheEntity(Patient.class, patient);
                Parent root;
                try {
                    root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.PATIENT_APPOINTMENTS_WINDOW.getPane())));
                    if (!stage.isShowing()) {
                        stage.setTitle("Appointments");
                        stage.setScene(new Scene(root, 930, 580));
                        stage.setResizable(false);
                        stage.show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            resultConsole.setText("Please select a patient to list its appointments");
        }

    }
    @Override
    public HashMap<String, Object> getInput() {
        HashMap<String,Object> input = new HashMap<>();
        if(!idField.getText().trim().isEmpty())
            input.put("patientID",Integer.valueOf(idField.getText()));
        if(!patientNameField.getText().trim().isEmpty())
            input.put("patientName",patientNameField.getText());
        if(!phoneField.getText().trim().isEmpty())
            input.put("patientPhone", phoneField.getText());
        if(!emailField.getText().trim().isEmpty())
            input.put("patientMail",emailField.getText());
        return input;
    }

    @Override
    public void updateView() {
        fillTable(null);
    }

    private void clearFields(){
        idField.clear();
        patientNameField.clear();
        phoneField.clear();
        emailField.clear();
    }

    public void getRevenueAction(ActionEvent actionEvent) {
        ArrayList<Patient> patients = getPatients();
        AtomicInteger total = new AtomicInteger(0);
        StringBuilder stringBuilder = new StringBuilder("Total cashed in");
        stringBuilder.append("\r\n-----------------");
        patients.forEach(patient -> {
            if(!patient.isDeleted()){
                patient.getAppointments()
                        .forEach(appointment -> {
                            if(!appointment.isDeleted() && appointment.getEndDate().before(new Timestamp(System.currentTimeMillis())))
                                appointment.getInterventions()
                                    .forEach(intervention -> {
                                        if(!intervention.isDeleted())
                                            total.addAndGet(intervention.getPrice());
                                    });
                        });
                }
                stringBuilder.append("\r\n").append(patient.getPatientName()).append(": ").append(total);
                total.set(0);
            });
        resultConsole.setText(stringBuilder.toString());
    }
}
