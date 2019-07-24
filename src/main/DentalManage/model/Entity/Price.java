package model.Entity;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "Price",schema = "sys",
        indexes = {@Index(name = "IDX_Price",columnList = "priceID,interventionID")})
@NamedQueries({
        @NamedQuery(name = "Price.findByInterventionID",
                query = "SELECT p FROM Price p WHERE p.intervention = :id AND p.isDeleted = false order by p.priceID desc"),
        @NamedQuery(name = "Price.findAll",
                query = "SELECT i FROM Price i")
})
public class Price {

    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Integer priceID;
    @Column(nullable = false)
    private Integer price;
    @Column(nullable = false)
    private Timestamp startDate;
    private Timestamp endDate;
    @ManyToOne
    @JoinColumn(name = "interventionID")
    private Intervention intervention;
    private boolean isDeleted;

    public Price(Integer price, Timestamp startDate, Timestamp endDate, Intervention intervention, boolean isDeleted) {
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.intervention = intervention;
        this.isDeleted = isDeleted;
    }

    public Price(){

    }

    public Integer getPriceID() {
        return priceID;
    }

    public void setPriceID(Integer priceID) {
        this.priceID = priceID;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Optional<Timestamp> getEndDate() {
        return this.endDate != null ? Optional.of(endDate) : Optional.empty();
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public void setIntervention(Intervention intervention) {
        this.intervention = intervention;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price1 = (Price) o;
        return isDeleted == price1.isDeleted &&
                Objects.equals(priceID, price1.priceID) &&
                Objects.equals(price, price1.price) &&
                Objects.equals(startDate, price1.startDate) &&
                Objects.equals(endDate, price1.endDate) &&
                Objects.equals(intervention, price1.intervention);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priceID, price, startDate, endDate, intervention, isDeleted);
    }
}
