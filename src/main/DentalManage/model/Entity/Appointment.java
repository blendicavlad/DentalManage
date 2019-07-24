package model.Entity;


import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Appointment",schema = "sys",
        indexes = {@Index(name = "IDX_Appointment",columnList = "appointmentID")})
@NamedQueries({
        @NamedQuery(name = "Appointment.findByUserName",
                query = "SELECT i FROM Appointment i WHERE i.user.userName = :name"),
        @NamedQuery(name = "Appointment.findAll",
                query = "SELECT i FROM Appointment i"),
        @NamedQuery(name = "Appointment.findAvailableInterventions",
                    query = "SELECT i from Intervention i where i.interventionID not in " +
                            "(select intervention.interventionID from InterventionLink where appointment = :appointment) and i.isDeleted = false")
})
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Integer appointmentID;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientID")
    private Patient patient;
    @Column(nullable = false)
    private String medicName;
    @Column(nullable = false)
    private Timestamp startDate;
    @Column
    private Timestamp endDate;
    @Column
    private Integer duration;
    @Column
    private Integer interventionsNo;
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterventionLink> interventions;
    @Column
    private boolean isDeleted;
    @Column
    private boolean isActive = true;

    public Appointment(User user, Patient patient, String medicName, Timestamp startDate, Timestamp endDate, Integer duration, Integer interventionsNo, List<InterventionLink> interventions, boolean isDeleted) {
        this.user = user;
        this.patient = patient;
        this.medicName = medicName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.interventionsNo = interventionsNo;
        this.interventions = interventions;
        this.isDeleted = isDeleted;
    }

    public Appointment(){

    }

    public Integer getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(Integer appointmentID) {
        this.appointmentID = appointmentID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getMedicName() {
        return medicName;
    }

    public void setMedicName(String medicName) {
        this.medicName = medicName;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getInterventionsNo() {
        return interventionsNo;
    }

    public void setInterventionsNo(Integer interventionsNo) {
        this.interventionsNo = interventionsNo;
    }

    public List<InterventionLink> getInterventions() {
        Hibernate.initialize(this.interventions);
        if(this.interventions == null){
            this.interventions = new ArrayList<>();
        }
        return interventions;
    }

    public void setInterventions(List<InterventionLink> interventions) {
        this.interventions = interventions;
    }

    public void addIntervention(InterventionLink intervention){
        getInterventions().add(intervention);
        intervention.setAppointment(this);
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return isDeleted == that.isDeleted &&
                isActive == that.isActive &&
                Objects.equals(appointmentID, that.appointmentID) &&
                Objects.equals(user, that.user) &&
                Objects.equals(patient, that.patient) &&
                Objects.equals(medicName, that.medicName) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate) &&
                Objects.equals(duration, that.duration) &&
                Objects.equals(interventionsNo, that.interventionsNo) &&
                Objects.equals(interventions, that.interventions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentID, user, patient, medicName, startDate, endDate, duration, interventionsNo, interventions, isDeleted, isActive);
    }
}
