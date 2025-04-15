package com.restassured.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static Connection connection;
    
    static {
        try {
            String driver = ConfigUtil.getProperty("db.driver");
            String url = ConfigUtil.getProperty("db.url");
            String user = ConfigUtil.getProperty("db.user");
            String password = ConfigUtil.getProperty("db.password");
            
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            logger.error("Error initializing database connection", e);
        }
    }
    
    public static List<Map<String, Object>> executeQuery(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            int columnCount = rs.getMetaData().getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
        } catch (SQLException e) {
            logger.error("Error executing query", e);
        }
        
        return results;
    }
    
    public static int executeUpdate(String query) {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(query);
        } catch (SQLException e) {
            logger.error("Error executing update", e);
            return -1;
        }
    }
    
    public static int executeUpdate(String query, Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error executing prepared update", e);
            return -1;
        }
    }
    
    public static Object executeScalar(String query) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getObject(1);
            }
        } catch (SQLException e) {
            logger.error("Error executing scalar query", e);
        }
        
        return null;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }
    
    public static void beginTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            logger.error("Error beginning transaction", e);
        }
    }
    
    public static void commitTransaction() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("Error committing transaction", e);
        }
    }
    
    public static void rollbackTransaction() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("Error rolling back transaction", e);
        }
    }
} 