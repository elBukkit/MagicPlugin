package com.elmakers.mine.bukkit.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class SQLiteMageDataStore extends SQLMageDataStore {
    private String connectionString;

    @Override
    public void initialize(MageController controller, ConfigurationSection configuration) {
        super.initialize(controller, configuration);
        String database = configuration.getString("database");

        if (database.contains("/") || database.contains("\\") || database.endsWith(".db")) {
            controller.getLogger().severe("The database name can not contain: /, \\, or .db");
            return;
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            controller.getLogger().severe("Sqlite Library not found: " + ex);
        }

        File dbFile = new File(controller.getDataFolder(), database + ".sqlite");
        connectionString = "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    @Override
    protected Connection createConnection() throws SQLException {
        return DriverManager.getConnection(connectionString);
    }

}
