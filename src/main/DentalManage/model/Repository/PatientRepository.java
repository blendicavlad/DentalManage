package model.Repository;

import app.AppManager;
import model.Entity.Patient;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;

public class PatientRepository extends AbstractRepository<Patient> {

    private final String tableName = "Patient";
    private EntityManager entityManager;

    public PatientRepository(EntityManager entityManager){
        this.entityManager = entityManager;
        super.setTableName(tableName);
        super.setClazz(Patient.class);
    }

    public Optional<Patient> findByName(String name) {
        Patient patient = entityManager.createNamedQuery("Patient.findByName", Patient.class)
                .setParameter("name", name)
                .getSingleResult();
        return patient != null ? Optional.of(patient) : Optional.empty();
    }

    @Override
    public Patient generateFromInput(HashMap<String, Object> patientInput) throws Exception {
        if (!patientInput.containsKey("patientName") || !patientInput.containsKey("patientPhone"))
            throw new Exception("Patient must have a name and phone to be inserted");
        else {
            Patient user = new Patient();
            user.setPatientName((String) patientInput.get("patientName"));
            user.setPatientPhone((String) patientInput.get("patientPhone"));
            user.setMedicID(AppManager.getAppManager().getLoggedUser());
            user.setDeleted(false);
            if(patientInput.containsKey("patientMail"))
                user.setPatientMail((String)patientInput.get("patientMail"));
            if(patientInput.containsKey("interventionCount"))
                user.setInterventionCount((Integer) patientInput.get("interventionCount"));
            return user;
        }
    }

    @Override
    public void updateFromInput(Patient patient, HashMap<String, Object> patientInput) throws Exception {
        if(patientInput.containsKey("patientID"))
            throw new Exception( "ID cannot be updated");
        if(patientInput.containsKey("patientName"))
            patient.setPatientName(patientInput.get("patientName").toString());
        if(patientInput.containsKey("patientMail"))
            patient.setPatientName(patientInput.get("patientMail").toString());
        if(patientInput.containsKey("interventionCount"))
            patient.setInterventionCount((Integer) patientInput.get("interventionCount"));
        if(patientInput.containsKey("patientPhone"))
            patient.setPatientPhone(patientInput.get("patientPhone").toString());
        if(patientInput.containsKey("isDeleted"))
            patient.setDeleted((boolean)patientInput.get("isDeleted"));
        this.save(patient);
    }

    public void synchronisePatient(Patient patient){
        PatientRepository patientRepository = new PatientRepository(entityManager);
        Object totalInterventionsNo = entityManager.createNativeQuery("SELECT SUM(interventionsNo) " +
                "FROM Appointment WHERE patientID = " + patient.getPatientID() + " AND isDeleted = false AND isActive = true")
                .getSingleResult();
        Patient transientPatient = patientRepository.findById(patient.getPatientID()).get();
        transientPatient.setInterventionCount(((BigDecimal) totalInterventionsNo).intValue());
    }

    public void deletePatient(Patient patient){
        if(patient.getAppointments() != null && !patient.getAppointments().isEmpty()) {
            patient.getAppointments().forEach(appointment -> {
                appointment.setActive(false);
                appointment.getInterventions().forEach(intervention -> intervention.setActive(false));
            });
            patient.setDeleted(true);
            this.save(patient);
        }
    }

    public void reactivatePatient(Patient patient){
        if(patient.getAppointments() != null && !patient.getAppointments().isEmpty()) {
            patient.getAppointments().forEach(appointment -> {
                appointment.setActive(true);
                appointment.getInterventions().forEach(intervention -> intervention.setActive(true));
            });
            patient.setDeleted(false);
            this.save(patient);
        }
    }

}
