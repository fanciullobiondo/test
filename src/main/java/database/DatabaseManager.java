/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nicolo.boschi
 */
public class DatabaseManager {

    private final static String databaseName = "testmydatabase.db";
    private static String databasePath;

    private static Connection openConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new SQLException(e);
        }
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath + databaseName);
    }

    public static void initDatabase(String dbPath) throws SQLException {
        databasePath = dbPath;
        File file = new File(databasePath + databaseName);

        if (file.exists()) {
            //System.out.print("Database already exists!");
            //return;
            file.delete();
        }

        try (Connection c = openConnection();) {

            if (c.getAutoCommit()) {
                c.setAutoCommit(false);
            }
            try {
                initTables(c);
            } catch (SQLException e) {
                c.rollback();
                throw new SQLException(e);
            }
            c.commit();
            c.setAutoCommit(true);
        }
        System.out.println("OK!");

    }

    private static void initTables(Connection c) throws SQLException {
        PreparedStatement stat = c.prepareStatement("CREATE TABLE COMPANY "
            + "(ID INT PRIMARY KEY     NOT NULL,"
            + " NAME           TEXT    NOT NULL)");
        stat.execute();
    }

    public static void insertCompany(String name) throws SQLException {
        try (Connection c = openConnection();
            PreparedStatement p = c.prepareStatement("insert into company(id, name) values(?,?)");) {
            p.setInt(1, 1);
            p.setString(2, name);
            p.executeUpdate();
        }
    }

    public static String findCompany() throws SQLException {
        try (Connection c = openConnection();
            PreparedStatement p = c.prepareStatement("select name from company where id=?");) {
            p.setInt(1, 1);
            try (ResultSet res = p.executeQuery();) {
                if (res.next()) {
                    return res.getString(1);
                }
            }
        }
        return null;
    }

}
