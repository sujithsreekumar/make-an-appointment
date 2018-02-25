package org.echs.database;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    @Singleton
    public static Connection getConnection() throws Exception {
        try {
            String connectionURL = "jdbc:postgresql://aa6m98aky906p3.czmgcbbphsmm.ap-south-1.rds.amazonaws.com:5432/echs";
            Connection connection = null;
            Class.forName("org.postgresql.Driver").newInstance();
            connection = DriverManager.getConnection(connectionURL, "sujithsreekumar", "kannan1234");
            return connection;
        } catch (Exception e) {
            throw e;
        }
    }
}
