package model.Repository;

import app.AppManager;
import model.Entity.Intervention;
import model.Entity.Price;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Optional;

public class InterventionRepository extends AbstractRepository<Intervention> {

    private final String tableName = "Intervention";
    private EntityManager entityManager;

    public InterventionRepository(EntityManager entityManager){
        this.entityManager = entityManager;
        super.setTableName(tableName);
        super.setClazz(Intervention.class);
    }

    public Optional<Intervention> findByName(String name) {
        Intervention intervention = entityManager.createNamedQuery("Intervention.findByName", Intervention.class)
                .setParameter("name", name)
                .getSingleResult();
        return intervention != null ? Optional.of(intervention) : Optional.empty();
    }

    @Override
    public Intervention generateFromInput(HashMap<String, Object> interventionData) throws Exception {
        if (!interventionData.containsKey("interventionName") || !interventionData.containsKey("price") || !interventionData.containsKey("duration"))
            throw new Exception("Intervention must have a name,duration and price to be inserted");
        else {
            Intervention intervention = new Intervention();
            intervention.setInterventionName((String) interventionData.get("interventionName"));
            intervention.setDuration((Integer) interventionData.get("duration"));
            Price price = new Price((Integer)interventionData.get("price"),new Timestamp(System.currentTimeMillis()),null,intervention,false);
            intervention.setPrice(price.getPrice());
            intervention.addPrice(price);
            return intervention;
        }
    }

    @Override
    public void updateFromInput(Intervention intervention, HashMap<String, Object> interventionData) throws Exception {
        if(interventionData.containsKey("interventionID"))
            throw new Exception( "ID cannot be updated");
        if(interventionData.containsKey("interventionName"))
            intervention.setInterventionName(interventionData.get("interventionName").toString());
        if(interventionData.containsKey("duration"))
            intervention.setDuration(Integer.parseInt(interventionData.get("duration").toString()));
        if(interventionData.containsKey("price")) {
            PriceRepository priceRepository = new PriceRepository(null);
            intervention.setPrice(Integer.parseInt(interventionData.get("price").toString()));
            Timestamp currentDate = new Timestamp(System.currentTimeMillis());
            Price newPrice = new Price(Integer.parseInt(interventionData.get("price").toString()), currentDate,null, intervention, false);
            Price oldPrice = getCurrentPrice(intervention);
            oldPrice.setEndDate(currentDate);
            intervention.addPrice(newPrice);
            //priceRepository.save(oldPrice);
        }
        if(interventionData.containsKey("isDeleted"))
            intervention.setDeleted((boolean)interventionData.get("isDeleted"));
        this.save(intervention);
    }

    static public Price getCurrentPrice(Intervention intervention){
        PriceRepository priceRepository = new PriceRepository(AppManager.getAppManager().getEntityManager());
        Optional<Price> price = priceRepository.getLastPrice(intervention);
        return price.orElse(null);
    }
}
