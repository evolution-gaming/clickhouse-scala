package integration;

import chdriver.Decoder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import chdriver.Client;
import chdriver.ClickhouseProperties;

public class Sample {
    private static final String DB_URL = "jdbc:clickhouse://localhost:";
    private static final String USER = "default";
    private static final String PASS = "";

    @Rule
    public GenericContainer chServer = new GenericContainer("yandex/clickhouse-server:latest")
            .withExposedPorts(8123, 9000);
    private java.sql.Connection conn;
    private Statement stmt;
    private Integer http;
    private Integer tcp;
    private Client scalaClient;
    private ClickhouseProperties scalaClickhouseProperties;
    private Decoder<TestArray> scalaDecoder;

    @Before
    public void setUp() throws Exception {
        http = chServer.getMappedPort(8123);

        Class.forName("ru.yandex.clickhouse.ClickHouseDriver");
        conn = DriverManager.getConnection(DB_URL + http, USER, PASS);
        stmt = conn.createStatement();

        String forCreate = "create table test_array(x Array(Int32)) engine = Memory;";
        stmt.executeUpdate(forCreate);

        conn.setAutoCommit(false);
        String forInsert = "insert into test_array(x) values (?)";
        PreparedStatement ps = conn.prepareStatement(forInsert);
        for (int i = 0; i < 1_000_000; i++) {
            Integer[] ints = {1, 2, 3};
            ps.setArray(1, conn.createArrayOf("Int32", ints));
            ps.addBatch();
        }
        ps.executeBatch();
        conn.commit();
        conn.setAutoCommit(true);

        tcp = chServer.getMappedPort(9000);
        scalaClient = TestArray.client(tcp);
        scalaClickhouseProperties = TestArray.clickhouseProperties();
        scalaDecoder = TestArray.testArrayDecoder();
    }

    @Test
    public void test() throws Exception {
        String sql = "SELECT * FROM test_array limit 1000000";
        int times = 100;

        long javaTime = 0;
        for (int i = 0; i < times; i++) {
            long now = System.currentTimeMillis();

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                rs.getArray("x");
            }
            rs.close();
            javaTime += System.currentTimeMillis() - now;
        }
        stmt.close();
        conn.close();
        System.out.println("jdbc = " + javaTime);

        long scalaTime = 0;
        for (int i = 0; i < times; i++) {
            long now = System.currentTimeMillis();
            scala.collection.Iterator<TestArray> it = scalaClient.execute(sql, scalaClickhouseProperties, scalaDecoder);
            while (it.hasNext()) it.next();
            scalaTime += System.currentTimeMillis() - now;

        }
        System.out.println("scala = " + scalaTime);
    }
}