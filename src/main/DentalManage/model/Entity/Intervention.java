package model.Entity;


import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Intervention",schema = "sys",
        indexes = {@Index(name = "IDX_Intervention", columnList = "interventionID")})
@NamedQueries({
        @NamedQuery(name = "Intervention.findByName",
                query = "SELECT i FROM Intervention i WHERE i.interventionName = :name"),
        @NamedQuery(name = "Intervention.findAll",
                query = "SELECT i FROM Intervention i")
})
public class Intervention{

    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Integer interventionID;
    @Column(nullable = false, unique = true)
    private String interventionName;
    @Column(nullable = false)
    private Integer price;
    private boolean isDeleted;
    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL)
    private List<Price> prices;
    @Column(nullable = false)
    private Integer duration;

    public Intervention(String interventionName, Integer price, boolean isDeleted, List<Price> prices, Integer duration) {
        this.interventionName = interventionName;
        this.price = price;
        this.isDeleted = isDeleted;
        this.prices = prices;
        this.duration = duration;
    }

    public Intervention(){}

    public Integer getInterventionID() {
        return interventionID;
    }

    public void setInterventionID(Integer interventionID) {
        this.interventionID = interventionID;
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

    public List<Price> getPrices() {
        Hibernate.initialize(this.prices);
        if(this.prices == null){
            this.prices = new ArrayList<>();
        }
        return prices;
    }

    public void setPrices(List<Price> prices) {
        this.prices = prices;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public void addPrice(Price price){
        getPrices().add(price);
        price.setIntervention(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Intervention that = (Intervention) o;
        return isDeleted == that.isDeleted &&
                Objects.equals(interventionID, that.interventionID) &&
                Objects.equals(interventionName, that.interventionName) &&
                Objects.equals(price, that.price) &&
                Objects.equals(prices, that.prices) &&
                Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interventionID, interventionName, price, isDeleted, prices, duration);
    }
}
