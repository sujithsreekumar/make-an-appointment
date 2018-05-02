package org.echs.database;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    @Singleton
    public static Connection getConnection() throws Exception {
        try {
            String connectionURL = "jdbc:postgresql://localhost:5432/echs";
//            String connectionURL = "jdbc:postgresql://echs-kochi.czmgcbbphsmm.ap-south-1.rds.amazonaws.com:5432/echs";
            Connection connection = null;
            Class.forName("org.postgresql.Driver").newInstance();
            connection = DriverManager.getConnection(connectionURL, "sujithsreekumar", "");
            return connection;
        } catch (Exception e) {
            throw e;
        }
    }
}
