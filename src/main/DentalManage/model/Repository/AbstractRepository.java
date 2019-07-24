package model.Repository;


import app.AppManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractRepository<T> {

    private EntityManager entityManager = AppManager.getAppManager().getEntityManager();
    private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);
    private String tableName;
    private Class<T> clazz;


    void setTableName(String tableName) {
        this.tableName = tableName;
    }

    void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public abstract T generateFromInput(HashMap<String, Object> entityInput) throws Exception;
    public abstract void updateFromInput(T entity, HashMap<String, Object> entityInput) throws Exception;

    public List<T> getAll(HashMap<String, Object> queryFilters){
        String select = "from " + tableName;
        Query query;
        if(queryFilters != null && !queryFilters.isEmpty()) {
            StringBuilder where = new StringBuilder(" where ");
            AtomicInteger atomicCounter = new AtomicInteger(0);
            queryFilters.entrySet().iterator().forEachRemaining(filter -> {
                if (atomicCounter.get() == 0)
                    where.append(filter.getKey()).append(" =:").append(filter.getKey());
                else{
                    where.append(" AND ").append(filter.getKey()).append(" =:").append(filter.getKey());
                }
                atomicCounter.incrementAndGet();
            });
            query = entityManager.createQuery(select + where);
            Iterator it = queryFilters.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry pair = (Map.Entry) it.next();
                query.setParameter(String.valueOf(pair.getKey()),pair.getValue());
                it.remove();
            }
            return query.getResultList();
        }
        return entityManager.createQuery(select).getResultList();
    }

    public List getAll(String where){
        String select = "from " + tableName + " where " + where;
        return entityManager.createNativeQuery(select,clazz).getResultList();
    }

    public int getCount(String where){
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + where;
        BigInteger count = (BigInteger) entityManager.createNativeQuery(sql).getSingleResult();
        return count.intValue();
    }

    public Optional<T> findById(Integer id){
        T entity = entityManager.find(this.clazz, id);
        return entity != null ? Optional.of(entity) : Optional.empty();
    }

    public List<Optional<T>> findByColumn(String columnName, Object value, boolean limitOne) {
        String entityName = tableName;
        ArrayList<Optional<T>> result = new ArrayList<>();
        if(limitOne) {
            result.add(Optional.of(entityManager.createQuery("SELECT e FROM " + entityName + " e WHERE e." + columnName + " = :param" + " LIMIT 1", this.clazz)
                    .setParameter("param", value)
                    .getSingleResult()));
        } else {
           entityManager.createQuery("SELECT e FROM " + entityName + " e WHERE e." + columnName + " = :param", this.clazz)
                    .setParameter("param", value)
                    .getResultList().forEach(e -> result.add(Optional.of(e)));
        }
        return result;
    }

    public boolean save(T entity) {
        try {
            if(!entityManager.getTransaction().isActive())
                entityManager.getTransaction().begin();
            entityManager.persist(entity);
            entityManager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            entityManager.clear();
        }
        return false;
    }

    public boolean delete(T entity) {
        try {
            if(!entityManager.getTransaction().isActive())
                entityManager.getTransaction().begin();
            entityManager.remove(entity);
            entityManager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            entityManager.clear();
        }
        return false;
    }

}
