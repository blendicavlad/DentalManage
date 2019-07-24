//package model;
//
//import app.AppManager;
//import model.BaseEntity.User;
//import model.Repository.UserRepository;
//import org.junit.Assert;
//import org.junit.jupiter.api.Test;
//
//import javax.persistence.EntityManager;
//import java.util.List;
//
//class UserRepositoryTest extends com.geowarin.hibernate.jpa.standalone.AbstractDbUnitJpaTest {
//
//    private EntityManager entityManager = AppManager.getAppManager().getEntityManager();
//    private User user = new User("test", "test", true, false, "0000", "test@test" );
//    private UserRepository userRepository = new UserRepository(entityManager);
//
//    @Test
//    void findByColumn() {
////        UserRepository userRepository = new UserRepository(entityManager);
////        Optional<User> medic = userRepository.findByColumn("userName","test");
////        Assert.assertTrue(medic.isPresent());
//    }
//
//    @Test
//    void save() {
//        Assert.assertFalse(userRepository.save(user));
//    }
//
//    @Test
//    void findById() {
//        Assert.assertTrue(userRepository.findById(user.getUserID()).isPresent());
//    }
//
//    @Test
//    void delete() {
//        //Assert.assertTrue(userRepository.delete(medic));
//    }
//
//    @Test
//    void getAll() {
//        List<User> allUsers = entityManager.createQuery("from User").getResultList();
//        Assert.assertNotNull(allUsers);
//    }
//}