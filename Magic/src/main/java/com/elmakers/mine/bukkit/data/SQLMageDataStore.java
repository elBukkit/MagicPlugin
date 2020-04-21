package com.elmakers.mine.bukkit.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataCallback;

public abstract class SQLMageDataStore extends ConfigurationMageDataStore {
    private Connection connection;
    private Object locks = new Object();

    protected abstract @Nonnull Connection createConnection() throws SQLException;

    protected @Nonnull Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = createConnection();
            checkSchema();
        }
        return connection;
    }

    public String getTextFieldType() {
        return "TEXT";
    }

    protected void checkSchema() throws SQLException {
        if (!tableExists("mage")) {
            controller.getLogger().info("Creating table: mage");
            String sql = "CREATE TABLE IF NOT EXISTS `mage` "
                    + "(`id` varchar(64) NOT NULL,"
                    + "`data` " + getTextFieldType() + ","
                    + "`locked` tinyint default 0,"
                    + "`migrated` tinyint default 0,"
                    + "PRIMARY KEY  (`id`)) ;";
            execute(sql);
        }
    }

    public void execute(String query) throws SQLException {
        Statement statement = getConnection().createStatement();
        try {
            statement.execute(query);
        } finally {
            statement.close();
        }
    }

    public boolean tableExists(String table) throws SQLException {
        ResultSet tableData = getConnection().getMetaData().getTables(null, null, table, null);
        return tableData.next();
    }

    public boolean columnExists(String table, String column) throws SQLException {
        ResultSet columnData = getConnection().getMetaData().getColumns(null, null, table, column);
        return columnData.next();
    }

    @Override
    @Deprecated
    public void save(MageData mage, MageDataCallback callback) {
        save(mage, callback, false);
    }

    @Override
    public void save(MageData mage, MageDataCallback callback, boolean releaseLock) {
        YamlConfiguration serialized = new YamlConfiguration();
        save(mage, serialized);

        try {
            PreparedStatement insert = getConnection().prepareStatement("REPLACE INTO mage (id, data) VALUES (?, ?)");
            insert.setString(1, mage.getId());
            insert.setString(2, serialized.saveToString());
            insert.execute();
        } catch (Exception ex) {
            controller.getLogger().log(Level.SEVERE, "Error saving player " + mage.getId(), ex);
        }

        if (releaseLock) {
            releaseLock(mage);
        }

        if (callback != null) {
            callback.run(mage);
        }
    }

    @Override
    public void releaseLock(MageData mage) {
        synchronized (locks) {
            try {
                // lock.release();
                controller.info("Released lock for " + mage.getId() + " at " + System.currentTimeMillis());
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Unable to release lock for " + mage.getId(), ex);
            }
        }
    }

    protected void obtainLock(String id) {
        synchronized (locks) {
            // TODO
        }
    }

    @Override
    public void load(String id, MageDataCallback callback) {
        obtainLock(id);
        MageData data = null;

        try {
            PreparedStatement loadQuery = getConnection().prepareStatement("SELECT data FROM mage WHERE id = ?");
            loadQuery.setString(1, id);
            ResultSet results = loadQuery.executeQuery();
            if (results != null && results.next()) {
                YamlConfiguration saveFile = new YamlConfiguration();
                saveFile.loadFromString(results.getString(1));
                data = load(id, saveFile);
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.SEVERE, "Error loading player " + id, ex);
        }

        if (callback != null) {
            callback.run(data);
        }
    }

    @Override
    public void delete(String id) {
        // TODO
    }

    @Override
    public Collection<String> getAllIds() {
        List<String> ids = new ArrayList<>();
        // TODO
        return ids;
    }

    @Override
    public void migrate(String id) {
        // TODO
    }
}
