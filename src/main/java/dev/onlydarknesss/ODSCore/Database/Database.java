package dev.onlydarknesss.ODSCore.Database;

import dev.onlydarknesss.ODSCore.Main;
import dev.onlydarknesss.ODSCore.Utils.PasswordUtils;

import java.sql.*;

public class Database {
    private Connection con;
    private final Main pl;

    public Database(Main pl) {
        this.pl = pl;
    }

    public void connect() throws SQLException {
        if (con == null || con.isClosed()) {
            con = DriverManager.getConnection(
                    "jdbc:mysql://" + Main.getHost() + ":" + Main.getPort() + "/"
                            + Main.getDatabase() + "?useSSL=" + Main.getSSL(),
                    Main.getUsername(),
                    Main.getPassword()
            );
            pl.getLogger().info("Database connection established.");
        }
    }

    public boolean isConnected() {
        try {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getCon() {
        return con;
    }

    public void createUserTable() {
        try {
            connect();

            String dbName = con.getCatalog();

            DatabaseMetaData dbMeta = con.getMetaData();
            try (ResultSet tables = dbMeta.getTables(dbName, null, "users", new String[]{"TABLE"})) {
                if (tables.next()) {
                    pl.getLogger().info("Table 'users' already exists.");
                } else {
                    String sql = """
                        CREATE TABLE users (
                            ID INT(11) NOT NULL AUTO_INCREMENT,
                            username VARCHAR(30) NOT NULL,
                            email VARCHAR(50) NOT NULL,
                            password VARCHAR(80) NOT NULL,
                            permlevel INT(11) NOT NULL,
                            PRIMARY KEY (ID),
                            UNIQUE (username),
                            UNIQUE (email)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                    """;

                    try (Statement statement = con.createStatement()) {
                        statement.execute(sql);
                        pl.getLogger().info("Table 'users' created successfully.");
                    }
                }
            }


            createRootAcc();

        } catch (SQLException e) {
            pl.getLogger().severe("Error while creating users table: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void createRootAcc() {
        try {
            if (!isConnected()) {
                connect();
            }

            String username = "root";
            String email = "root@ods-core.com";
            String rawPassword = "root123";
            int permLevel = 10;


            String checkSql = "SELECT username FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        pl.getLogger().info("Root account already exists: " + rs.getString("username"));
                        return;
                    }
                }
            }

            String hashedPassword = PasswordUtils.hashPassword(rawPassword);

            String insertSql = "INSERT INTO users(username, email, password, permlevel) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, email);
                insertStmt.setString(3, hashedPassword);
                insertStmt.setInt(4, permLevel);
                int affectedRows = insertStmt.executeUpdate();
                if (affectedRows > 0) {
                    pl.getLogger().info("Root account created successfully.");
                } else {
                    pl.getLogger().warning("Root account insertion affected 0 rows.");
                }
            }

        } catch (Exception e) {
            pl.getLogger().severe("Failed to create root account: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                con.close();
                pl.getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                pl.getLogger().severe("Error while closing database connection: " + e.getMessage());
            }
        }
    }
}
