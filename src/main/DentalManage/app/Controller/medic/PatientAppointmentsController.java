package app.Controller.medic;

import app.AppManager;
import app.Controller.BaseController;
import app.Controller.Panes;
import app.UxUtils;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Entity.Appointment;
import model.Entity.Patient;
import model.Repository.AppointmentRepository;
import model.Repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;

public class PatientAppointmentsController extends BaseController implements Initializable {

    @FXML
    public TableView<Appointment> appointmentsTable;
    @FXML
    public TableColumn<Appointment, Integer> appointmentID;
    @FXML
    public TableColumn<Appointment, String> userName;
    @FXML
    public TableColumn<Appointment, Timestamp> startDate;
    @FXML
    public TableColumn<Appointment, Timestamp> endDate;
    @FXML
    public TableColumn<Appointment, Integer> duration;
    @FXML
    public TableColumn<Appointment, Integer> interventions;
    @FXML
    public TableColumn<Appointment, String> isDeleted;
    @FXML
    public JFXTextArea resultConsole;
    @FXML
    public JFXTextField idField;
    @FXML
    public JFXDatePicker startDatePicker;
    @FXML
    public Text patientName;
    @FXML
    public JFXTimePicker startTimePicker;
    private Stage stage = new Stage();
    private Executor exec ;
    private static final Logger logger = LoggerFactory.getLogger(PatientAppointmentsController.class);
    private EntityManager entityManager = AppManager.getAppManager().getEntityManager();
    private AppointmentRepository appointmentRepository = new AppointmentRepository(entityManager);
    private PatientRepository patientRepository = new PatientRepository(entityManager);
    private Patient selectedPatient;
    private PatientsController patientsController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientsController = (PatientsController) AppManager.getAppManager().getCachedController(PatientsController.class);
        AppManager.getAppManager().cacheController(PatientAppointmentsController.class, this);
        exec = AppManager.getAppManager().getExecutor();
        selectedPatient = (Patient) AppManager.getAppManager().getCachedEntity(Patient.class);
        patientName.setText(selectedPatient.getPatientName());
        appointmentID.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
        userName.setCellValueFactory(new PropertyValueFactory<>("medicName"));
        startDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        interventions.setCellValueFactory(new PropertyValueFactory<>("interventionsNo"));
        isDeleted.setCellValueFactory(cellData -> {
            String isActiveAppointment = !cellData.getValue().isDeleted() && cellData.getValue().isActive() ? "Yes" : "No";
            return new ReadOnlyStringWrapper(isActiveAppointment);
        });
        HashMap<String, Object> whereClause = new HashMap<>();
        fillTable(whereClause);
    }

    @Override
    public void insertAction(ActionEvent actionEvent) {
        if(selectedPatient.isDeleted()) {
            UxUtils.info("Can't insert appointment on a deleted patient", null, "Error");
            return;
        }
        HashMap<String, Object> input = getInput();
        Timestamp appointmentStartDate = (Timestamp)input.get("startDate");
        if(appointmentStartDate.before(new Timestamp(System.currentTimeMillis()))){
            UxUtils.info("Can't insert appointment before current date", null, "Error");
            return;
        }
        Appointment appointment = null;
        try{
            input.put("patient",selectedPatient);
            appointment = appointmentRepository.generateFromInput(input);
        }catch (Exception e){
            UxUtils.errorBox(e.getMessage(), null,"INSERT ERROR");
            return;
        }
        if(appointment != null){
            if(appointmentRepository.save(appointment)) {
                resultConsole.setText("Appointment inserted");
                AppManager.getAppManager().updateUI();
            }
        }
        clearFields();
    }

    @Override
    public void updateAction(ActionEvent actionEvent) {
        if(selectedPatient.isDeleted()) {
            UxUtils.info("Can't update appointment on a deleted patient", null, "Error");
            return;
        }
        Appointment appointment = appointmentsTable.getSelectionModel().getSelectedItem();
        HashMap<String, Object> appointmentsInput = getInput();
        if(appointment != null && appointmentsInput.size() > 0){
            try {
                appointmentRepository.updateFromInput(appointment,appointmentsInput);
            } catch (Exception e){
                UxUtils.errorBox(e.getMessage(), null,"UPDATE ERROR");
                return;
            }
            resultConsole.setText("Appointment updated");
            AppManager.getAppManager().updateUI();
        }
    }

    @Override
    public void deleteAction(ActionEvent actionEvent) {
        if(selectedPatient.isDeleted()) {
            UxUtils.info("Can't delete appointment on a deleted patient", null, "Error");
            return;
        }
        ArrayList<Appointment> appointments = getAppointments();
        if(appointments != null) {
            appointments.forEach(i -> {
                i.setDeleted(true);
                i.getInterventions().forEach(j -> j.setActive(false));
                appointmentRepository.save(i);
                resultConsole.setText("Appointment with ID " + i.getAppointmentID() + "has been deleted");
            });

            AppManager.getAppManager().updateUI();
        }
    }

    @Override
    public void reactivateAction(ActionEvent actionEvent) {
        ArrayList<Appointment> appointments = getAppointments();
        if(appointments != null) {
            appointments.forEach(i -> {
                if(!i.isActive()) {
                    UxUtils.info("Can not reactivated expired appointment", null, "error");
                    return;
                }
                if(i.getInterventionsNo() > 0) {
                    i.setDeleted(false);
                    i.getInterventions().forEach(j -> {
                        if(!j.isDeleted())
                            j.setActive(true);
                        appointmentRepository.save(i);
                        resultConsole.setText("Appointment with ID " + i.getAppointmentID() + " reactivated");
                    });
                }else
                    resultConsole.setText("Can not activate an appointment with no interventions");
            });
            AppManager.getAppManager().updateUI();
        }
    }

    @Override
    public void searchAction(ActionEvent actionEvent) {
        resultConsole.clear();
        HashMap<String,Object> appointmentData = getInput();
        if(appointmentData.keySet().toArray().length > 0){
            fillTable(appointmentData);
        }else
            fillTable(null);
        clearFields();
    }


    @Override
    public void fillTable(HashMap<String, Object> whereClause) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("patientID",selectedPatient.getPatientID());
        if(whereClause != null && !whereClause.isEmpty())
            params.putAll(whereClause);
        Task<List<Appointment>> task = new Task<List<Appointment>>() {
            protected List<Appointment> call() throws Exception {
                logger.info("Fill patient appointments table executed");
                return appointmentRepository.getAll(params);
            }
        };
        exec.execute(task);
        task.setOnFailed(e -> logger.error(task.getException().getMessage()));
        task.setOnSucceeded(e -> appointmentsTable.getItems().setAll(task.getValue()));
    }

    @Override
    public HashMap<String, Object> getInput() {
        HashMap<String, Object> input = new HashMap<>();
        Timestamp startDate = null;
        if(!idField.getText().isEmpty())
            input.put("appointmentID",Integer.valueOf(idField.getText()));
        if(startDatePicker.getValue() != null && startTimePicker.getValue() != null){
            LocalDate time = startDatePicker.getValue();
            LocalDateTime localDateTime = LocalDateTime.of(startDatePicker.getValue(), startTimePicker.getValue());
            startDate = Timestamp.valueOf(localDateTime);
            input.put("startDate",startDate);
        }
        return input;
    }

    public void showIntAction(ActionEvent actionEvent) {
        if(appointmentsTable.getSelectionModel().getSelectedItem() != null){
            Appointment appointment = appointmentsTable.getSelectionModel().getSelectedItem();
            if(appointment == null)
                appointment = (Appointment) AppManager.getAppManager().getCachedEntity(Appointment.class);
            AppManager.getAppManager().cacheEntity(Appointment.class,appointment);
            Parent root;
            try {
                if(!stage.isShowing()) {
                    root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Panes.APPOINTMENT_INTERVENTIONS_WINDOW.getPane())));
                    stage.setTitle("Interventions");
                    stage.setScene(new Scene(root));
                    Text appointmentID = (Text) stage.getScene().lookup("#appointmentID");
                    Text patientName = (Text) stage.getScene().lookup("#patientName");
                    appointmentID.setText(appointment.getAppointmentID().toString());
                    patientName.setText(appointment.getPatient().getPatientName());
                    stage.setResizable(false);
                    stage.show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            resultConsole.setText("Please select a patient to list its appointments");
        }
    }

    public void clearFields(){
        idField.clear();
        startDatePicker.getEditor().clear();
        startDatePicker.setValue(null);
        startTimePicker.getEditor().clear();
        startDatePicker.setValue(null);
    }

    private ArrayList<Appointment> getAppointments() {
        HashMap<String, Object> appointmentData = getInput();
        ArrayList<Appointment> appointments = new ArrayList<>();
        if(appointmentData.containsKey("appointmentID"))
            appointmentRepository.findById((Integer)appointmentData.get("appointmentID"));
        else
            appointments.addAll(appointmentsTable.getSelectionModel().getSelectedItems());
        return appointments;
    }

    public void setSelectedPatient(Patient selectedPatient) {
        this.selectedPatient = selectedPatient;
    }

    @Override
    public void updateView() {
        fillTable(null);
    }
}
