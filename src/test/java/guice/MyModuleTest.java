package guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import entity.MyTable;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MyModuleTest {
    private static final String MASTER_URL = "jdbc:derby:memory:masterDB;create=true";
    private static final String SLAVE_URL = "jdbc:derby:memory:slaveDB;create=true";

    @BeforeClass
    public static void initClass() throws Exception {
        createSchema();
    }

    private static void createSchema() throws SQLException {
        for (final String url : Arrays.asList(MASTER_URL, SLAVE_URL)) {
            createTable(url);
        }
    }

    private static void createTable(final String url) throws SQLException {
        final String ddl = "create table mytable (mycol varchar (255), primary key (mycol))";
        try (final Connection cn = DriverManager.getConnection(url);
             final Statement st = cn.createStatement()) {
            st.executeUpdate(ddl);
        }
    }

    private static void cleanInsert(final String url, final String value) throws SQLException {
        try (final Connection cn = DriverManager.getConnection(url);
             final Statement st = cn.createStatement()) {
            st.executeUpdate("delete from mytable");
        }

        final String sql = "insert into mytable (mycol) values (?)";
        try (final Connection cn = DriverManager.getConnection(url);
             final PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.executeUpdate();
        }
    }

    @Before
    public void setUp() throws Exception {
        cleanInsert(MASTER_URL, "master");
        cleanInsert(SLAVE_URL, "slave");

        injector = Guice.createInjector(new MyModule());
        injector.getInstance(Key.get(PersistService.class, MasterPu.class)).start();
        injector.getInstance(Key.get(PersistService.class, SlavePu.class)).start();
        injector.getInstance(Key.get(UnitOfWork.class, MasterPu.class)).begin();
        injector.getInstance(Key.get(UnitOfWork.class, SlavePu.class)).begin();
    }

    private Injector injector;

    @Test
    public void masterPuFetchesFromMaster() throws Exception {
        final EntityManager sut = injector.getInstance(Key.get(EntityManager.class, MasterPu.class));

        final MyTable actual = sut.find(MyTable.class, "master");

        assertThat(actual, is(not(nullValue())));
    }

    @Test
    public void masterMyTableServiceFetchesFromMaster() throws Exception {
        final MyTableService sut = injector.getInstance(Key.get(MyTableService.class, MasterPu.class));

        final String actual = sut.fetch();

        assertThat(actual, is("master"));
    }

    @Test
    public void slavePuFetchesFromMaster() throws Exception {
        final EntityManager sut = injector.getInstance(Key.get(EntityManager.class, SlavePu.class));

        final MyTable actual = sut.find(MyTable.class, "slave");

        assertThat(actual, is(not(nullValue())));
    }

    @Test
    public void slaveMyTableServiceFetchesFromSlave() throws Exception {
        final MyTableService sut = injector.getInstance(Key.get(MyTableService.class, SlavePu.class));

        final String actual = sut.fetch();

        assertThat(actual, is("slave"));
    }

    @After
    public void tearDown() throws Exception {
        injector.getInstance(Key.get(UnitOfWork.class, MasterPu.class)).end();
        injector.getInstance(Key.get(UnitOfWork.class, SlavePu.class)).end();
    }
}
