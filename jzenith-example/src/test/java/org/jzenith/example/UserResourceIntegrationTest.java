package org.jzenith.example;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;

import io.restassured.RestAssured;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jzenith.example.helloworld.ExampleApp;
import org.jzenith.example.helloworld.resources.response.UserResponse;
import org.postgresql.ds.PGSimpleDataSource;

import java.util.UUID;

public class UserResourceIntegrationTest {

    private final static UUID USER_UUID = UUID.fromString("71aebc48-78e2-4298-8d13-7a2bfb58c555");
    private final static String USER_NAME = "jzenith_user";

    private JdbcDatabaseTester databaseTester;

    @BeforeClass
    public static void startup() throws Exception {
        ExampleApp.main();
    }

    @Before
    public void setupData() throws Exception {
        databaseTester = new JdbcDatabaseTester(PGSimpleDataSource.class.getName(),
                "jdbc:postgresql:test", "test", "test");

        final DatabaseConfig config = databaseTester.getConnection().getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());

        final IDataSet dataSet = new FlatXmlDataSet(UserResourceIntegrationTest.class.getResourceAsStream("/user.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.onSetup();
    }

    @After
    public void tearDown() throws Exception {
        databaseTester.onTearDown();
    }

    @Test
    public void testGetUser() {
        when()
                .get("/user/{id}", USER_UUID)
                .then()
                .statusCode(200)
                .body(is(new UserResponse(USER_UUID, USER_NAME)));
    }


}
