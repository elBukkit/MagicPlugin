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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataCallback;
import com.elmakers.mine.bukkit.api.magic.MageController;

public abstract class SQLMageDataStore extends ConfigurationMageDataStore {
    private Connection connection;
    private final Object lockingLock = new Object();
    private int lockTimeout = 0;
    private int lockRetry = 0;

    protected abstract @Nonnull Connection createConnection() throws SQLException;

    @Override
    public void initialize(MageController controller, ConfigurationSection configuration) {
        super.initialize(controller, configuration);
        lockTimeout = configuration.getInt("lock_timeout", 5000);
        lockRetry = configuration.getInt("lock_retry", 100);
        if (lockRetry < 2) {
            lockRetry = 2;
        }
    }

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
        synchronized (lockingLock) {
            try {
                PreparedStatement release = getConnection().prepareStatement("UPDATE mage SET locked = 0 WHERE id = ?");
                release.setString(1, mage.getId());
                release.execute();
                controller.info("Released lock for " + mage.getId() + " at " + System.currentTimeMillis());
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Unable to release lock for " + mage.getId(), ex);
            }
        }
    }

    protected void obtainLock(String id) {
        synchronized (lockingLock) {
            boolean hasLock = false;
            long start = System.currentTimeMillis();

            try {
                PreparedStatement lockLookup = getConnection().prepareStatement("SELECT locked FROM mage WHERE id = ?");
                while (!hasLock) {
                    lockLookup.setString(1, id);
                    ResultSet results = lockLookup.executeQuery();
                    if (results.next()) {
                        hasLock = !results.getBoolean(1);
                    } else {
                        hasLock = true;
                    }
                    if (!hasLock) {
                        // I am hoping this is only called on separate load threads!
                        if (System.currentTimeMillis() > start + lockTimeout) {
                            controller.getLogger().log(Level.WARNING, "Lock timeout while waiting for mage " + id + ", claiming lock");
                            break;
                        }
                        Thread.sleep(lockRetry);
                    }
                }

                PreparedStatement lock = getConnection().prepareStatement("UPDATE mage SET locked = 1 WHERE id = ?");
                lock.setString(1, id);
                lock.execute();
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Could not obtain lock for mage " + id, ex);
            }
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
            if (results.next()) {
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
        try {
            PreparedStatement delete = getConnection().prepareStatement("DELETE FROM mage WHERE id = ?");
            delete.setString(1, id);
            delete.execute();
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Unable to delete mage " + id, ex);
        }
    }

    @Override
    public Collection<String> getAllIds() {
        List<String> ids = new ArrayList<>();
        try {
            PreparedStatement idsQuery = getConnection().prepareStatement("SELECT id FROM mage WHERE migrated = 0");
            ResultSet idResults = idsQuery.executeQuery();
            while (idResults.next()) {
                ids.add(idResults.getString(1));
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Unable to lookup all mage ids", ex);
        }
        return ids;
    }

    @Override
    public void migrate(String id) {
        try {
            PreparedStatement migrate = getConnection().prepareStatement("UPDATE mage SET migrated = 1 WHERE id = ?");
            migrate.setString(1, id);
            migrate.execute();
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Could not set mage " + id + " as migrated", ex);
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Error closing player data connection", ex);
            }
            connection = null;
        }
    }
}
