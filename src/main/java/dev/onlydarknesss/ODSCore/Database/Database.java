package dev.onlydarknesss.ODSCore.Database;

import dev.onlydarknesss.ODSCore.Main;
import dev.onlydarknesss.ODSCore.Utils.PasswordUtils;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.sql.*;
import java.util.Set;
import java.util.UUID;

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

            DatabaseMetaData dbMeta = con.getMetaData();
            String dbName = con.getCatalog();

            // USERS tablosu kontrol ve oluşturma
            try (ResultSet rs = dbMeta.getTables(dbName, null, "users", new String[]{"TABLE"})) {
                if (rs.next()) {
                    pl.getLogger().info("Table 'users' already exists.");
                } else {
                    String sql = """
                    CREATE TABLE IF NOT EXISTS users (
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
                    try (Statement stmt = con.createStatement()) {
                        stmt.execute(sql);
                        pl.getLogger().info("Table 'users' created successfully.");
                    }
                }
            }

            try (ResultSet rs = dbMeta.getTables(dbName, null, "players", new String[]{"TABLE"})) {
                if (rs.next()) {
                    pl.getLogger().info("Table 'players' already exists.");
                } else {
                    String sql2 = """
                    CREATE TABLE IF NOT EXISTS players (
                        ID INT(11) NOT NULL AUTO_INCREMENT,
                        username VARCHAR(30) NOT NULL,
                        uuid VARCHAR(50) NOT NULL,
                        perm VARCHAR(30) NOT NULL,
                        PRIMARY KEY (ID),
                        UNIQUE (username),
                        UNIQUE (uuid)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                """;
                    try (Statement stmt = con.createStatement()) {
                        stmt.execute(sql2);
                        pl.getLogger().info("Table 'players' created successfully.");
                    }
                }
            }

            try (ResultSet rs = dbMeta.getTables(dbName, null, "player_permissions", new String[]{"TABLE"})) {
                if (rs.next()) {
                    pl.getLogger().info("Table 'player_permissions' already exists.");
                } else {
                    String sql3 = """
                    CREATE TABLE IF NOT EXISTS player_permissions (
                        ID INT(11) NOT NULL AUTO_INCREMENT,
                        player_uuid VARCHAR(50) NOT NULL,
                        permission VARCHAR(30) NOT NULL,
                        PRIMARY KEY (ID)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                """;
                    try (Statement stmt = con.createStatement()) {
                        stmt.execute(sql3);
                        pl.getLogger().info("Table 'player_permissions' created successfully.");
                    }
                }
            }

            // Root hesap oluşturma
            createRootAcc();

        } catch (SQLException e) {
            pl.getLogger().severe("Error while creating tables: " + e.getMessage());
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

    public void createPlayerIFNotExist(Player player){
        try {
            connect();
            UUID ID = player.getUniqueId();
            String username = player.getName();
            String perm;
            String mail;
            int permLevel;

            PreparedStatement check = con.prepareStatement("SELECT * FROM players WHERE uuid = ?");
            check.setString(1, ID.toString());
            ResultSet rs = check.executeQuery();

            if (!rs.next()){
                perm = "default";
                mail = "null";
                permLevel = 0;
                PreparedStatement insert = con.prepareStatement("INSERT INTO players(username, uuid, perm, email, permlevel) VALUES (?, ?, ?, ?, ?)");
                insert.setString(1, username);
                insert.setString(2, ID.toString());
                insert.setString(3, perm);
                insert.setString(4, mail);
                insert.setInt(5, permLevel);

                insert.executeUpdate();

                Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();
                for (PermissionAttachmentInfo perm1 : perms){
                    String perm2 = perm1.getPermission();

                    PreparedStatement insertPerm = con.prepareStatement(
                            "INSERT INTO player_permissions(player_uuid, permission) VALUES (?, ?)"
                    );
                    insertPerm.setString(1, ID.toString());
                    insertPerm.setString(2, perm2);
                    insertPerm.executeUpdate();
                }
            }
        } catch (SQLException e){
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
