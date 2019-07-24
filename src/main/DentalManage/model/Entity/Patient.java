package model.Entity;

import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Patient",schema = "sys",
        indexes = {@Index(name = "IDX_Patient",columnList = "patientID")})
@NamedQueries({
        @NamedQuery(name = "Patient.findByName",
                query = "SELECT i FROM Patient i WHERE i.patientName = :name"),
        @NamedQuery(name = "Patient.findAll",
                query = "SELECT i FROM Patient i")
})
public class Patient extends BaseEntity {

    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Integer patientID;
    @Column
    private Integer medicID;
    @Column
    private String patientName;
    @Column
    private String patientPhone;
    @Column
    private String patientMail;
    @Column
    private Integer interventionCount;
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> appointments;
    @Column
    private boolean isDeleted;

    public Patient(){}

    public Patient(Integer medicID, String patientName, String patientPhone, String patientMail, Integer interventionCount, List<Appointment> appointments, boolean isDeleted) {
        this.medicID = medicID;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.patientMail = patientMail;
        this.interventionCount = interventionCount;
        this.appointments = appointments;
        this.isDeleted = isDeleted;
    }

    public Integer getPatientID() {
        return patientID;
    }

    public void setPatientID(Integer patientID) {
        this.patientID = patientID;
    }

    public Integer getMedicID() {
        return medicID;
    }

    public void setMedicID(Integer medicID) {
        this.medicID = medicID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientMail() {
        return patientMail;
    }

    public void setPatientMail(String patientMail) {
        this.patientMail = patientMail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public Integer getInterventionCount() {
        return interventionCount;
    }

    public void setInterventionCount(Integer interventionCount) {
        this.interventionCount = interventionCount;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public List<Appointment> getAppointments() {
        Hibernate.initialize(this.appointments);
        if(this.appointments == null){
            this.appointments = new ArrayList<>();
        }
        return appointments;
    }

    public void getAppointments(Appointment appointment) {
        getAppointments().add(appointment);
        appointment.setPatient(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return isDeleted == patient.isDeleted &&
                Objects.equals(patientID, patient.patientID) &&
                Objects.equals(medicID, patient.medicID) &&
                Objects.equals(patientName, patient.patientName) &&
                Objects.equals(patientPhone, patient.patientPhone) &&
                Objects.equals(patientMail, patient.patientMail) &&
                Objects.equals(interventionCount, patient.interventionCount) &&
                Objects.equals(appointments, patient.appointments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientID, medicID, patientName, patientPhone, patientMail, interventionCount, appointments, isDeleted);
    }
}
