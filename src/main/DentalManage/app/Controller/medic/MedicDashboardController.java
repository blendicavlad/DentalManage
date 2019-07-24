package app.Controller.medic;

import app.AppManager;
import app.Controller.BaseController;
import app.Utils;
import app.UxUtils;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import model.Entity.Appointment;
import model.Entity.InterventionLink;
import model.Repository.AppointmentRepository;
import model.Repository.PatientRepository;
import model.Repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MedicDashboardController extends BaseController implements Initializable {


    @FXML
    public JFXDatePicker StartDatePicker;
    @FXML
    public JFXDatePicker EndDatePicker;
    @FXML
    public Label percentageText;
    @FXML
    public JFXTextArea nextAppointmentTextArea;
    @FXML
    public Text totalPatientsNo;
    @FXML
    public Text todayAppointmentsNo;

    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private EntityManager entityManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppManager.getAppManager().cacheController(MedicDashboardController.class, this);
        entityManager = AppManager.getAppManager().getEntityManager();
        patientRepository = new PatientRepository(entityManager);
        appointmentRepository = new AppointmentRepository(entityManager);
        updateDashboard(null);
    }

    private void updateTotalPatients(){
        totalPatientsNo.setText(String.valueOf(patientRepository.getCount("isDeleted=false and medicID= " + AppManager.getAppManager().getLoggedUser())));
    }

    private void updateTodayAppointments(){
        todayAppointmentsNo.setText(String.valueOf(appointmentRepository.getTodayAppointments().size()));
    }

    private void updateNextAppointment(){
        StringBuilder nextAppointment = new StringBuilder();
        Appointment appointment = null;
        try {
             appointment = (Appointment) entityManager.createNativeQuery("select * from Appointment " +
                    "where startDate > now() and userID = ?1 and isDeleted = false order by startDate limit 1", Appointment.class)
                    .setParameter(1, AppManager.getAppManager().getLoggedUser())
                    .getSingleResult();
        } catch (NoResultException e){
            nextAppointmentTextArea.setText("No appointments");
        }
        if(appointment != null) {
            List<InterventionLink> interventions = appointment.getInterventions()
                    .stream()
                    .filter(interventionLink -> !interventionLink.isDeleted())
                    .collect(Collectors.toList());
            int totalPrice = interventions.stream()
                    .filter(interventionLink -> interventionLink.getPrice() != null)
                    .mapToInt(InterventionLink::getPrice)
                    .sum();
            nextAppointment.append(" Patient name: ").append(appointment.getPatient().getPatientName())
                    .append("\n Start Date: ").append(appointment.getStartDate())
                    .append("\n End Date: ").append(appointment.getEndDate())
                    .append("\n Duration: ").append(appointment.getDuration()).append(" hrs")
                    .append("\n Number of interventions: ").append(appointment.getInterventionsNo())
                    .append("\n\n Interventions ")
                    .append("\n----------------------------\n");
            AtomicInteger interventionNo = new AtomicInteger(0);
            interventions.forEach(interventionLink -> {
                String stringBuilder = interventionNo.incrementAndGet() + "." + " Name: " + interventionLink.getIntervention().getInterventionName() +
                        "\n     Duration: " + interventionLink.getIntervention().getDuration() +
                        "\n     Price: " + interventionLink.getIntervention().getPrice() + "\n";
                nextAppointment.append(stringBuilder);
            });
            nextAppointment.append("----------------------------").append("\n").append("TOTAL PRICE: ").append(totalPrice);
            nextAppointmentTextArea.setText(nextAppointment.toString());
        }
        nextAppointmentTextArea.setEditable(false);
    }

    @FXML
    private void getWorkload(ActionEvent actionEvent) {
        if(StartDatePicker.getValue() == null || EndDatePicker.getValue() == null) {
            UxUtils.info("Please set all fields", null, "Warning");
            return;
        }
        LocalDate startDate = StartDatePicker.getValue();
        LocalDate endDate = EndDatePicker.getValue();
        int workingHours = Utils.calculateWorkdays(startDate,endDate) * 8;
        AtomicInteger workedHours = new AtomicInteger(0);
        UserRepository userRepository = new UserRepository(entityManager);
        List<Appointment> appointments = appointmentRepository.getAppointmentsBetween(
                userRepository.findByID(AppManager.getAppManager().getLoggedUser()).get(),
                Timestamp.valueOf(startDate.atStartOfDay()),
                Timestamp.valueOf(endDate.atStartOfDay()));

        appointments.forEach(i -> workedHours.addAndGet(i.getDuration()));

        float workload = ((float) workedHours.get() / (float) workingHours) * 100;
        String workloadString = String.format("%2.02f",workload) + "%";
        percentageText.setText(workloadString);
    }

    private void updateDashboard(ActionEvent actionEvent){
        updateTotalPatients();
        updateTodayAppointments();
        updateNextAppointment();
    }

    @Override
    public void updateView() {
        updateDashboard(null);
    }
}
