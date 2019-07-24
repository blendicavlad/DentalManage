package app.Controller.admin;

import app.AppManager;
import app.Controller.BaseController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import model.Repository.AppointmentRepository;
import model.Repository.InterventionRepository;
import model.Repository.PatientRepository;
import model.Repository.UserRepository;

import javax.persistence.EntityManager;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController extends BaseController implements Initializable {

    EntityManager entityManager = AppManager.getAppManager().getEntityManager();

    @FXML
    private Text nrUsers;
    @FXML
    private Text nrPatients;
    @FXML
    public Text nrAppointments;
    @FXML
    public Text nrInterventions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UserRepository userRepository = new UserRepository(entityManager);
        PatientRepository patientRepository = new PatientRepository(entityManager);
        AppointmentRepository appointmentRepository = new AppointmentRepository(entityManager);
        InterventionRepository interventionRepository = new InterventionRepository(entityManager);
        nrUsers.setText(String.valueOf(userRepository.getCount("isDeleted=false")));
        try {
            Thread.sleep(10);
            nrPatients.setText(String.valueOf(patientRepository.getCount("isDeleted=false")));
            Thread.sleep(10);
            nrAppointments.setText(String.valueOf(appointmentRepository.getCount("isDeleted=false and isActive=true")));
            Thread.sleep(10);
            nrInterventions.setText(String.valueOf(interventionRepository.getCount("isDeleted=false")));
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
