package model.Repository;

import app.AppManager;
import model.Entity.*;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AppointmentRepository extends AbstractRepository<Appointment> {


    private final String tableName = "Appointment";
    private EntityManager entityManager;

    public AppointmentRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        super.setTableName(tableName);
        super.setClazz(Appointment.class);
    }

    public List<Intervention> getAvailableInterventions(){
        List<Intervention> interventions = entityManager.createNamedQuery("Appointment.findAll", Intervention.class)
                .getResultList();
        return interventions != null ? interventions : new ArrayList<>();
    }

    public List<Appointment> getAppointmentsByDate(User user, Timestamp date){
        List<Appointment> appointments = entityManager.createQuery("select a from Appointment a " +
                    "where ?1 between a.startDate and a.endDate and a.user = ?2 or date(?1) = date(a.startDate) and isDeleted = false", Appointment.class)
                    .setParameter(1, date)
                    .setParameter(2, user)
                    .getResultList();
        return appointments != null ? appointments : new ArrayList<>();
    }

    public List<Appointment> getAppointmentsBetween(User user,Timestamp startDate, Timestamp endDate){
        List<Appointment> appointments = entityManager.createQuery("select a from Appointment a " +
                " where a.startDate >= date(?1) and date(a.endDate) <= ?2 and a.isDeleted = false and a.user = ?3",Appointment.class)
                .setParameter(1, startDate)
                .setParameter(2,endDate)
                .setParameter(3,user)
                .getResultList();
        return appointments != null ? appointments : new ArrayList<>();
    }

    @Override
    public Appointment generateFromInput(HashMap<String, Object> entityInput) throws Exception {
        if(!entityInput.containsKey("startDate"))
            throw new Exception("The appointment must have a date and time to be inserted");
        else{
            Appointment appointment = new Appointment();
            UserRepository userRepository = new UserRepository(entityManager);
            User medic = userRepository.findByID(AppManager.getAppManager().getLoggedUser()).get();
            appointment.setUser(medic);
            appointment.setPatient((Patient) entityInput.get("patient"));
            appointment.setMedicName(medic.getUserName());
            appointment.setStartDate((Timestamp) entityInput.get("startDate"));
            appointment.setEndDate(null);
            appointment.setDuration(null);
            appointment.setInterventionsNo(0);
            appointment.setDeleted(true);
            checkIfValidAppointmentDates(appointment);
            return appointment;
        }

    }

    @Override
    public void updateFromInput(Appointment entity, HashMap<String, Object> entityInput) throws Exception {
        if(entityInput.containsKey("appointmentID"))
            throw new Exception("ID cannot be updated");
        if(entityInput.containsKey("startDate"))
            entity.setStartDate((Timestamp) entityInput.get("startDate"));
        else
            throw new Exception("Cannot update: Please set start date and start time");
        checkIfValidAppointmentDates(entity);
        this.save(entity);
    }

    private void checkIfValidAppointmentDates(Appointment appointment) throws Exception{
        if(appointment.getStartDate() != null){
            verifyOverlappingDate(appointment,appointment.getStartDate());
        }
        if(appointment.getEndDate() != null){
            verifyOverlappingDate(appointment,appointment.getEndDate());
        }
    }

    private void verifyOverlappingDate(Appointment appointment, Timestamp date) throws Exception{
        List<Appointment> appointments = getAppointmentsByDate(appointment.getUser(),date)
                .stream().filter(app -> !Objects.equals(app.getAppointmentID(), appointment.getAppointmentID()))
                .collect(Collectors.toList());
        if(!appointments.isEmpty()){
            StringBuilder overlappedAppointments = new StringBuilder();
            appointments.forEach(i -> overlappedAppointments.append("\n").append(i.getAppointmentID()).append(" - ")
                    .append("Date: ")
                    .append(i.getStartDate().toString())
                    .append(" - ")
                    .append(i.getEndDate().toString()));
            throw new Exception("Appointment date is overlapping appointments: " + overlappedAppointments.toString());
        }
    }


    //TODO refactoring save o singura data, in metoda apelant
    void synchroniseAppointment(Appointment appointment) throws Exception{
        InterventionLinkRepository interventionLinkRepository = new InterventionLinkRepository(entityManager);
        Appointment transientAppointment = this.findById(appointment.getAppointmentID()).get();
        PatientRepository patientRepository = new PatientRepository(entityManager);
        LocalDateTime startDate = transientAppointment.getStartDate().toLocalDateTime();
        LocalDateTime endDate;
        long totalHours;
        int interventionsNo;
        AtomicInteger durationSum = new AtomicInteger(0);
        AtomicInteger interventionsNr = new AtomicInteger(0);
        //TODO total price;
        List<InterventionLink> interventionLinks = interventionLinkRepository.getAll(new HashMap<String, Object>(){
            {
                put("appointmentID",transientAppointment.getAppointmentID());
                put("isDeleted",false);
                put("isActive",true);
            }
        });
        if(interventionLinks != null && !interventionLinks.isEmpty()) {
            interventionLinks.forEach(i -> {
                interventionsNr.incrementAndGet();
                durationSum.updateAndGet(v -> v + i.getDuration());
            });
            totalHours = durationSum.get();
            interventionsNo = interventionsNr.get();
            endDate = startDate.plusHours(totalHours);
            transientAppointment.setDuration(durationSum.get());
            transientAppointment.setDeleted(false);
            transientAppointment.setInterventionsNo(interventionsNo);
            transientAppointment.setEndDate(Timestamp.valueOf(endDate));
        }
        else {
            transientAppointment.setDuration(0);
            transientAppointment.setInterventionsNo(0);
            transientAppointment.setEndDate(null);
            transientAppointment.setDeleted(true);
            return;
        }
        checkIfValidAppointmentDates(appointment);
        if(!this.save(transientAppointment))
            throw new Exception("Could not synchronise appointment");
        patientRepository.synchronisePatient(transientAppointment.getPatient());
    }

    public List<Appointment> getTodayAppointments(){
        Timestamp today = new Timestamp(System.currentTimeMillis());
        User user = entityManager.find(User.class, AppManager.getAppManager().getLoggedUser());
        return getAppointmentsByDate(user,today);
    }
}
