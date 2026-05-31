package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Database Information
    private static final String URL= "jdbc:mysql://localhost:3306/ghads";
    private static final String USERNAME= "root";
    private static final String PASSWORD= "";

    // Connection Object
    private static Connection connection;

    // Private Constructor (Singleton)
    private DBConnection() {
    }

    // Get Connection Method
    public static Connection getConnection() {

        try {

            // Check if connection is null or closed
            if (connection == null || connection.isClosed()) {

                // Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Create Connection
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Database Connected Successfully");

            }

        } catch (ClassNotFoundException e) {

            System.out.println("MySQL JDBC Driver Not Found");
            e.printStackTrace();

        } catch (SQLException e) {

            System.out.println("Database Connection Failed");
            e.printStackTrace();
        }

        return connection;
    }

    // Close Connection Method
    public static void closeConnection() {

        try {

            if (connection != null && !connection.isClosed()) {

                connection.close();

                System.out.println("Database Connection Closed");
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }
}
