package app;

import app.Controller.BaseController;
import app.Controller.ControllerInterface;
import javafx.fxml.FXMLLoader;
import model.Entity.Appointment;
import model.Entity.BaseEntity;
import model.Repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AppManager {

    private static AppManager AppManagerInstance = null;
    private Executor executor;
    private volatile TimerTask task;
    private EntityManager entityManager;
    private static final Logger logger = LoggerFactory.getLogger(AppManager.class);
    private int loggedUser;
    private volatile HashMap<Class<?>, BaseController> controllerCache;
    private volatile HashMap<Class<?>, BaseEntity> entityCache;

    private AppManager() {
        initAppManager();
    }

    public static AppManager getAppManager(){
        if (AppManagerInstance == null) {
            AppManagerInstance = new AppManager();
        }
        return AppManagerInstance;
    }

    private void initAppManager(){
        controllerCache = new HashMap<>();
        entityCache = new HashMap<>();
        executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread (runnable);
            t.setDaemon(true);
            return t;
        });
        try {
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("Persistence");
            entityManager = entityManagerFactory.createEntityManager();
        } catch (Exception e){
            logger.error(e.getMessage());
            UxUtils.errorBox("Could not load persistence driver", null, "error");
        }
        synchronized (this) {
            if(task == null)
                task = new TimerTask() {
                    @Override
                    public void run() {
                        Timestamp now = new Timestamp(System.currentTimeMillis());
                        AppointmentRepository appointmentRepository = new AppointmentRepository(entityManager);
                        List<Appointment> appointments = new ArrayList<>();
                        try {
                            appointments = appointmentRepository.getAll(new HashMap<String, Object>() {{
                                put("isDeleted", false);
                                put("isActive", true);
                            }}).stream().filter(appointment -> appointment.getEndDate().before(now)).collect(Collectors.toList());
                            appointments.forEach(appointment -> {
                                appointment.setActive(false);
                                appointmentRepository.save(appointment);
                            });
                            updateUI();
                        } catch (Exception e) {
                            logger.error("Error updating task");
                        }
                        logger.info("Update task executed");
                    }
                };
        }
        Timer timer = new Timer();
        timer.schedule(task, 500L, 300000L);
    }

    public void cacheController(Class clazz, BaseController controller){
        if(clazz.getName().equals(controller.getClass().getName())) {
            if (controllerCache.isEmpty())
                controllerCache.put(clazz, controller);
            else if (!controllerCache.containsKey(clazz))
                controllerCache.put(clazz, controller);
            else {
                controllerCache.remove(clazz);
                controllerCache.put(clazz, controller);
            }
        }else
            logger.error("");
    }

    public void cacheEntity(Class clazz, BaseEntity entity){
        String entityClassName = entity.getClass().getName().split("\\$")[0];
        if(clazz.getName().equals(entityClassName)) {
            if (entityCache.isEmpty())
                entityCache.put(clazz, entity);
            else if (!controllerCache.containsKey(clazz))
                entityCache.put(clazz, entity);
            else {
                entityCache.remove(clazz);
                entityCache.put(clazz, entity);
            }
        }else
            logger.error("Key class must be the same as given entity value");
    }

    public void updateUI(){
        for(BaseController controller : controllerCache.values()){
            try{
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.updateView();
        }
    }

    public static void clearCache(){
        AppManagerInstance.controllerCache = null;
        AppManagerInstance.entityCache = null;
        AppManagerInstance.task = null;
        AppManagerInstance = null;
        System.gc();
    }


    public ControllerInterface getCachedController(Class clazz){
        return controllerCache.get(clazz);
    }

    public BaseEntity getCachedEntity(Class clazz){
        return entityCache.get(clazz);
    }

    public Executor getExecutor() {
        return executor;
    }

    public EntityManager getEntityManager(){
        return entityManager;
    }

    public int getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(int loggedUser) {
        this.loggedUser = loggedUser;
    }
}
