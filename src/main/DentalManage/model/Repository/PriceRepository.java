package model.Repository;

import model.Entity.Intervention;
import model.Entity.Price;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class PriceRepository extends AbstractRepository<Price> {

    private final String tableName = "Price";
    private EntityManager entityManager;

    public PriceRepository(EntityManager entityManager) {
        super.setTableName(tableName);
        super.setClazz(Price.class);
        this.entityManager = entityManager;
    }

    @Override
    public Price generateFromInput(HashMap<String, Object> entityInput) throws Exception {
        return null;
    }

    @Override
    public void updateFromInput(Price entity, HashMap<String, Object> entityInput) throws Exception {

    }

    public Optional<Price> getLastPrice(Intervention intervention) {
        Price price = entityManager.createNamedQuery("Price.findByInterventionID", Price.class)
                .setParameter("id", intervention)
                .setMaxResults(1).getSingleResult();
        return price != null ? Optional.of(price) : Optional.empty();
    }

    public List<Price> getPrices(Integer interventionID){
        HashMap<String, Object> key = new HashMap<>();
        key.put("interventionID", interventionID);
        List<Price> prices = getAll(new HashMap<String, Object>(){{
            put("interventionID", interventionID);
            }
        });
        return prices;
    }
}
