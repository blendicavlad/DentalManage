package model.Repository;

import model.Entity.Appointment;
import model.Entity.Intervention;
import model.Entity.InterventionLink;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;

public class InterventionLinkRepository extends AbstractRepository<InterventionLink> {

    private final String tableName = "InterventionLink";
    private EntityManager entityManager;
    AppointmentRepository appointmentRepository;

    public InterventionLinkRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.appointmentRepository = new AppointmentRepository(entityManager);
        super.setTableName(tableName);
        super.setClazz(InterventionLink.class);
    }

    public List<InterventionLink> findLinksByAppointment(Appointment appointment) {
       List<InterventionLink> interventions = entityManager.createNamedQuery("InterventionLink.findByAppointment", InterventionLink.class)
                .setParameter("appointment", appointment)
                .getResultList();
        return interventions != null ? interventions : new ArrayList<InterventionLink>();
    }

    public InterventionLink findByID(Integer id){
        return  entityManager.find(InterventionLink.class, id);
    }

    @Override
    public InterventionLink generateFromInput(HashMap<String, Object> entityInput) throws Exception {
        return null;
    }

    @Override
    public void updateFromInput(InterventionLink entity, HashMap<String, Object> entityInput) throws Exception {

    }

    public void createLink(Intervention intervention, Appointment appointment) throws Exception{
        InterventionLink interventionLink = new InterventionLink(intervention,appointment);
        if(!this.save(interventionLink))
            throw new Exception("Could not insert entity");
        else
            appointmentRepository.synchroniseAppointment(appointment);
    }

    public void deleteLink(InterventionLink interventionLink) throws Exception{
        interventionLink.setDeleted(true);
        if(!this.save(interventionLink))
            throw new Exception("Could not delete entity");
        else
            appointmentRepository.synchroniseAppointment(interventionLink.getAppointment());
    }

}
