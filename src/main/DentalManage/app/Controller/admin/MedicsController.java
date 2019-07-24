package app.Controller.admin;

import app.AppManager;
import app.Controller.BaseController;
import app.Utils;
import app.UxUtils;
import com.jfoenix.controls.JFXDatePicker;
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
import model.Entity.InterventionLink;
import model.Entity.User;
import model.Repository.AppointmentRepository;
import model.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class MedicsController extends BaseController implements Initializable {

    @FXML
    public JFXDatePicker startDatePicker;
    @FXML
    public JFXDatePicker endDatePicker;
    @FXML
    public JFXTextField usernameField;
    @FXML
    public JFXTextField phoneField;
    @FXML
    public JFXTextField idField;
    @FXML
    public TableView<User> medicTable;
    @FXML
    public TableColumn<User,Integer> userId;
    @FXML
    public TableColumn<User,String> userName;
    @FXML
    public TableColumn<User,String> userPhone;
    @FXML
    public TableColumn<User,String> userMail;
    @FXML
    public JFXTextArea resultConsole;
    private static final Logger logger = LoggerFactory.getLogger(UserPaneController.class);
    private Executor exec;
    private EntityManager entityManager = AppManager.getAppManager().getEntityManager();
    private UserRepository userRepository = new UserRepository(entityManager);
    private AppointmentRepository appointmentRepository = new AppointmentRepository(entityManager);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exec = AppManager.getAppManager().getExecutor();
        userId.setCellValueFactory(new PropertyValueFactory<>("userID"));
        userName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userMail.setCellValueFactory(new PropertyValueFactory<>("userMail"));
        userPhone.setCellValueFactory(new PropertyValueFactory<>("userPhone"));
        fillTable(null);
    }

    @Override
    public void searchAction(ActionEvent actionEvent) {
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

    @Override
    public void fillTable(HashMap<String, Object> whereClause) {
        Task<List<User>> task = new Task<List<User>>(){
            @Override
            protected List<User> call(){
                logger.info("Fill medic Table Task executed");
                return userRepository.getAll(whereClause);
            }
        };
        exec.execute(task);
        task.setOnFailed(e -> logger.error(task.getException().getMessage()));
        task.setOnSucceeded(e -> medicTable.getItems().setAll( task.getValue()));
    }

    public void getEarningsAction(ActionEvent actionEvent) {
        User medic = getMedic();
        LinkedHashMap<String, Integer> monthsAndRevenue = new LinkedHashMap<>();
        StringBuilder stringBuilder = new StringBuilder("Monthly revenue for current year");
        stringBuilder.append("\r\n---------------------------");
        monthsAndRevenue.put("january",0);
        monthsAndRevenue.put("february",0);
        monthsAndRevenue.put("march",0);
        monthsAndRevenue.put("april",0);
        monthsAndRevenue.put("may",0);
        monthsAndRevenue.put("june",0);
        monthsAndRevenue.put("july",0);
        monthsAndRevenue.put("august",0);
        monthsAndRevenue.put("september",0);
        monthsAndRevenue.put("october",0);
        monthsAndRevenue.put("november",0);
        monthsAndRevenue.put("december",0);
        List<Appointment> appointments = appointmentRepository.getAll(new HashMap<String, Object>(){{
            put("isDeleted",false);
            put("userID",medic.getUserID());
        }});
        for(Appointment appointment : appointments){
            Calendar cal = Calendar.getInstance();
            cal.setTime(appointment.getStartDate());
            if(cal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                switch (cal.get(Calendar.MONTH)) {
                    case 0:
                        addToRevenueHasmap(monthsAndRevenue,"january",getAppointmentRevenue(appointment));
                        break;
                    case 1:
                        addToRevenueHasmap(monthsAndRevenue,"february",getAppointmentRevenue(appointment));
                        break;
                    case 2:
                        addToRevenueHasmap(monthsAndRevenue,"march",getAppointmentRevenue(appointment));
                        break;
                    case 3:
                        addToRevenueHasmap(monthsAndRevenue,"april",getAppointmentRevenue(appointment));
                        break;
                    case 4:
                        addToRevenueHasmap(monthsAndRevenue,"may",getAppointmentRevenue(appointment));
                        break;
                    case 5:
                        addToRevenueHasmap(monthsAndRevenue,"june",getAppointmentRevenue(appointment));
                        break;
                    case 6:
                        addToRevenueHasmap(monthsAndRevenue,"july",getAppointmentRevenue(appointment));
                        break;
                    case 7:
                        addToRevenueHasmap(monthsAndRevenue,"august",getAppointmentRevenue(appointment));
                        break;
                    case 8:
                        addToRevenueHasmap(monthsAndRevenue,"september",getAppointmentRevenue(appointment));
                        break;
                    case 9:
                        addToRevenueHasmap(monthsAndRevenue,"october",getAppointmentRevenue(appointment));
                        break;
                    case 10:
                        addToRevenueHasmap(monthsAndRevenue,"november",getAppointmentRevenue(appointment));
                        break;
                    case 11:
                        addToRevenueHasmap(monthsAndRevenue,"december",getAppointmentRevenue(appointment));
                        break;
                }
            }
        }
        for(String key : monthsAndRevenue.keySet()){
            stringBuilder.append("\n").append(key).append(": ").append(monthsAndRevenue.get(key));
            resultConsole.setText(stringBuilder.toString());
        }
    }

    private int getAppointmentRevenue(Appointment appointment){
        return appointment.getInterventions()
                .stream()
                .filter(interventionLink -> !interventionLink.isDeleted())
                .mapToInt(InterventionLink::getPrice)
                .sum();
    }

    private void addToRevenueHasmap(LinkedHashMap<String,Integer> hashMap, String key, Integer value){
        int val = hashMap.get(key);
        val += value;
        hashMap.replace(key,val);
    }

    public void getWorkload(ActionEvent actionEvent) {
        if(startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            UxUtils.info("Please set all fields", null, "Warning");
            return;
        }
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        int workingHours = Utils.calculateWorkdays(startDate,endDate) * 8;
        AtomicInteger workedHours = new AtomicInteger(0);
        UserRepository userRepository = new UserRepository(entityManager);
        List<Appointment> appointments = appointmentRepository.getAppointmentsBetween(
                getMedic(),
                Timestamp.valueOf(startDate.atStartOfDay()),
                Timestamp.valueOf(endDate.atStartOfDay()));

        appointments.forEach(i -> workedHours.addAndGet(i.getDuration()));

        float workload = ((float) workedHours.get() / (float) workingHours) * 100;
        String workloadString = String.format("%2.02f",workload) + "%";
        resultConsole.setText("Workload: " + workloadString);
    }

    private User getMedic(){
        HashMap<String,Object> userData = getInput();
        User user = null;
        if(userData.containsKey("userID"))
            user = userRepository.findById((Integer.valueOf(userData.get("userID").toString()))).get();
        else if(userData.containsKey("userPhone"))
            userRepository.findByColumn("userPhone",String.valueOf(userData.get("userPhone")),true).get(0);
        else if(userData.containsKey("userName"))
            userRepository.findByName(String.valueOf(userData.get("userName"))).get();
        if(user != null){
            return user;
        }else
            user = medicTable.getSelectionModel().getSelectedItem();
        if(user == null){
            UxUtils.info("Could not find medic",null, "Medic not found");
        } return user;
    }

    public HashMap<String,Object> getInput(){
        HashMap<String,Object> input = new HashMap<>();
        if(!usernameField.getText().trim().isEmpty())
            input.put("userName",usernameField.getText());
        if(!phoneField.getText().trim().isEmpty()) {
            if(phoneField.getText().matches("[0-9]+"))
                input.put("userPhone", phoneField.getText());
            else
                resultConsole.setText("Phone number must be numeric!");
        }
        return input;
    }

    private void clearFields(){
        idField.clear();
        usernameField.clear();
        phoneField.clear();
    }

}
