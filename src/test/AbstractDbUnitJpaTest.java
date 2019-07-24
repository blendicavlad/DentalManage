
import java.io.InputStream;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import app.AppManager;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.HibernateException;
import org.hibernate.internal.SessionImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 *
 */
public abstract class AbstractDbUnitJpaTest {

    private static IDatabaseConnection connection;
    private static IDataSet dataset;
    protected static EntityManager entityManager = AppManager.getAppManager().getEntityManager();

    /**
     * @throws DatabaseUnitException
     * @throws HibernateException
     */
    @BeforeClass
    public static void initEntityManager() throws HibernateException, DatabaseUnitException {
        connection = new DatabaseConnection(((SessionImpl) (entityManager.getDelegate())).connection());
        connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());

        FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
        flatXmlDataSetBuilder.setColumnSensing(true);
        InputStream dataSet = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data.xml");
        dataset = flatXmlDataSetBuilder.build(dataSet);
    }

    @AfterClass
    public static void closeEntityManager() {
        entityManager.close();

    }

    /**
     * Will clean the dataBase before each test
     *
     * @throws SQLException
     * @throws DatabaseUnitException
     */
    @Before
    public void cleanDB() throws DatabaseUnitException, SQLException {
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataset);
    }
}