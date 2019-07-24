package model.Repository;

import model.Entity.User;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Optional;

public class UserRepository extends AbstractRepository<User> {

    private final String tableName = "User";
    private EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        super.setTableName(this.tableName);
        super.setClazz(User.class);
    }

    public Optional<User> findByName(String name) {
        User user = entityManager.createNamedQuery("User.findByName", User.class)
                .setParameter("name", name)
                .getSingleResult();
        return user != null ? Optional.of(user) : Optional.empty();
    }

    public User findByNameAndPassword(String name, String password){
        User user = entityManager.createNamedQuery("User.findByNameAndPassword", User.class)
                .setParameter("name", name)
                .setParameter("password",password)
                .getSingleResult();
        return user;
    }

    public Optional<User> findByID(Integer ID) {
        User user = entityManager.createNamedQuery("User.findByID", User.class)
                .setParameter("ID",ID)
                .getSingleResult();
        return user != null ? Optional.of(user) : Optional.empty();
    }

    @Override
    public User generateFromInput(HashMap<String, Object> userData) throws Exception {
        if (!userData.containsKey("userName") || !userData.containsKey("userPass"))
            throw new Exception("User must have a name and password to be inserted");
        else {
            User user = new User();
            user.setUserName((String) userData.get("userName"));
            user.setUserPass((String) userData.get("userPass"));
            user.setAdmin((boolean) userData.get("isAdmin"));
            user.setDeleted(false);
            if(userData.containsKey("userPhone"))
                user.setUserPhone((String)userData.get("userPhone"));
            if(userData.containsKey("userMail"))
                user.setUserMail((String)userData.get("userMail"));
            return user;
        }
    }

    @Override
    public void updateFromInput(User user, HashMap<String, Object> userData) throws Exception{
        if(userData.containsKey("userID"))
            throw new Exception( "ID cannot be updated");
        if(userData.containsKey("userName"))
            user.setUserName(userData.get("userName").toString());
        if(userData.containsKey("userPass"))
            user.setUserPass(userData.get("userPass").toString());
        if(userData.containsKey("userMail"))
            user.setUserMail(userData.get("userMail").toString());
        if(userData.containsKey("userPhone"))
            user.setUserPhone(userData.get("userPhone").toString());
        if(userData.containsKey("isAdmin"))
            user.setAdmin((boolean)userData.get("isAdmin"));
        if(userData.containsKey("isDeleted"))
            user.setDeleted((boolean)userData.get("isDeleted"));
        this.save(user);
    }
}

