package model.Entity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "InterventionLink",schema = "sys",
        indexes = {@Index(name = "IDX_InterventionLink", columnList = "interventionLinkID")})
@NamedQueries({
        @NamedQuery(name = "InterventionLink.findByAppointment",
                query = "SELECT i FROM InterventionLink i WHERE i.appointment = :appointment AND i.isDeleted = false"),
        @NamedQuery(name = "InterventionLink.findAll",
                query = "SELECT i FROM InterventionLink i"),
        @NamedQuery(name = "InterventionLink.findByID",
                query =  "SELECT i FROM InterventionLink  i WHERE i.interventionLinkID = :interventionLinkID")
})
public class InterventionLink {

    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Integer interventionLinkID;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interventionID")
    private Intervention intervention;
    @ManyToOne
    @JoinColumn(name = "appointmentID")
    private Appointment appointment;
    @Column(nullable = false)
    private String interventionName;
    @Column(nullable = false)
    private Integer price;
    @Column(nullable = false)
    private Integer duration;
    @Column
    private boolean isDeleted;
    @Column
    private boolean isActive = true;

    public InterventionLink() {

    }

    public InterventionLink(Intervention intervention, Appointment appointment) {
        this.intervention = intervention;
        this.appointment = appointment;
        this.interventionName = intervention.getInterventionName();
        this.price = intervention.getPrice();
        this.duration = intervention.getDuration();
        this.isDeleted = false;
    }

    public Integer getInterventionLinkID() {
        return interventionLinkID;
    }

    public void setInterventionLinkID(Integer interventionLinkID) {
        this.interventionLinkID = interventionLinkID;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public void setIntervention(Intervention interventionID) {
        this.intervention = interventionID;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointmentID) {
        this.appointment = appointmentID;
    }

    public String getInterventionName() {
        return interventionName;
    }

    public void setInterventionName(String interventionName) {
        this.interventionName = interventionName;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
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
        InterventionLink that = (InterventionLink) o;
        return isDeleted == that.isDeleted &&
                isActive == that.isActive &&
                Objects.equals(interventionLinkID, that.interventionLinkID) &&
                Objects.equals(intervention, that.intervention) &&
                Objects.equals(appointment, that.appointment) &&
                Objects.equals(interventionName, that.interventionName) &&
                Objects.equals(price, that.price) &&
                Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interventionLinkID, intervention, appointment, interventionName, price, duration, isDeleted, isActive);
    }
}
