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
import javax.annotation.Nullable;

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
    private boolean hasIsValid = true;

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

    protected boolean isValid(Connection connection) {
        if (connection == null) return false;
        if (!hasIsValid) return true;
        try {
            return connection.isValid(5000);
        } catch (AbstractMethodError old) {
            // SQLLite does not have this method on older spigot versions
            hasIsValid = false;
            return true;
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Error checking database connection", ex);
        }
        return false;
    }

    protected @Nonnull Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = createConnection();
            checkSchema();
        }
        int retries = 3;
        boolean isValid = isValid(connection);
        while (!isValid && retries >= 0) {
            try {
                retries--;
                Thread.sleep(1000);
                isValid = isValid(connection);
            } catch (InterruptedException ex) {
                break;
            }
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
                    + "PRIMARY KEY  (`id`))";
            sql += getTableEncoding() + ';';
            execute(sql);
        }
    }

    protected String getTableEncoding() {
        return "";
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
        ResultSet tableData = null;
        boolean exists = false;
        try {
            tableData = getConnection().getMetaData().getTables(null, null, table, null);
            exists = tableData.next();
        } finally {
            close(tableData);
        }
        return exists;
    }

    public boolean columnExists(String table, String column) throws SQLException {
        ResultSet columnData = null;
        boolean exists = false;
        try {
            columnData = getConnection().getMetaData().getColumns(null, null, table, column);
            exists = columnData.next();
        } finally {
            close(columnData);
        }
        return exists;
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

        PreparedStatement insert = null;
        try {
            String sql;
            if (releaseLock) {
                sql = "REPLACE INTO mage (id, data, locked) VALUES (?, ?, ?)";
            } else {
                sql = "REPLACE INTO mage (id, data) VALUES (?, ?)";
            }
            insert = getConnection().prepareStatement(sql);
            insert.setString(1, mage.getId());
            insert.setString(2, serialized.saveToString());
            if (releaseLock) {
                insert.setInt(3, 0);
            }
            insert.execute();
        } catch (Exception ex) {
            controller.getLogger().log(Level.SEVERE, "Error saving player " + mage.getId(), ex);
            close();
        } finally {
            close(insert);
        }
        controller.info("Finished saving data for " + mage.getId() + (releaseLock ? " and released lock " : ""  + " at " + System.currentTimeMillis()), 10);

        if (callback != null) {
            callback.run(mage);

        }
    }

    @Override
    public void releaseLock(MageData mage) {
        synchronized (lockingLock) {
            PreparedStatement release = null;
            try {
                release = getConnection().prepareStatement("UPDATE mage SET locked = 0 WHERE id = ?");
                release.setString(1, mage.getId());
                release.execute();
                controller.info("Released lock for " + mage.getId() + " at " + System.currentTimeMillis());
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Unable to release lock for " + mage.getId(), ex);
                close();
            } finally {
                close(release);
            }
        }
    }

    @Override
    public void obtainLock(MageData mage) {
        obtainLock(mage.getId());
    }

    protected void obtainLock(String id) {
        synchronized (lockingLock) {
            boolean hasLock = false;
            long start = System.currentTimeMillis();
            controller.info("Obtaining lock for player " + id + " at " + start, 10);

            PreparedStatement lockLookup = null;
            PreparedStatement lock = null;
            ResultSet results = null;
            try {
                lockLookup = getConnection().prepareStatement("SELECT locked FROM mage WHERE id = ?");
                while (!hasLock) {
                    lockLookup.setString(1, id);
                    results = lockLookup.executeQuery();
                    if (results.next()) {
                        hasLock = !results.getBoolean(1);
                    } else {
                        hasLock = true;
                    }
                    if (!hasLock) {
                        // I am hoping this is only called on separate load threads!
                        long now = System.currentTimeMillis();
                        if (now > start + lockTimeout) {
                            controller.info("Lock timeout of " + lockTimeout + "ms expired at " + now + " while waiting for mage " + id + ", claiming lock");
                            break;
                        }
                        Thread.sleep(lockRetry);
                    }
                    close(results);
                    results = null;
                }

                lock = getConnection().prepareStatement("UPDATE mage SET locked = 1 WHERE id = ?");
                lock.setString(1, id);
                lock.execute();
                long now = System.currentTimeMillis();
                long duration = (now - start);
                controller.info("Obtained lock for player " + id + " at " + now + " in " + duration + "ms", 10);
            } catch (Exception ex) {
                controller.info("Could not obtain lock for mage " + id);
                close();
            } finally {
                close(lockLookup);
                close(results);
                close(lock);
            }
        }
    }

    protected boolean isLocked(String id) {
        boolean isLocked = false;
        synchronized (lockingLock) {
            long start = System.currentTimeMillis();
            controller.info("Checking lock for player " + id + " at " + start, 10);

            PreparedStatement lockLookup = null;
            PreparedStatement lock = null;
            ResultSet results = null;
            try {
                lockLookup = getConnection().prepareStatement("SELECT locked FROM mage WHERE id = ?");
                lockLookup.setString(1, id);
                results = lockLookup.executeQuery();
                if (results.next()) {
                    isLocked = results.getBoolean(1);
                } else {
                    isLocked = false;
                }
                close(results);
                results = null;
            } catch (Exception ex) {
                controller.info("Could not check lock for mage " + id);
                close();
            } finally {
                close(lockLookup);
                close(results);
                close(lock);
            }
        }
        return isLocked;
    }

    @Override
    @Deprecated
    public void load(String id, MageDataCallback callback) {
        load(id, callback, true);
    }

    @Override
    public void load(String id, MageDataCallback callback, boolean lock) {
        if (lock) {
            obtainLock(id);
        } else if (isLocked(id)) {
            controller.info("Skipping locked data preload", 10);
            if (callback != null) {
                callback.run(null);
            }
            return;
        }
        MageData data = null;

        PreparedStatement loadQuery = null;
        ResultSet results = null;
        try {
            loadQuery = getConnection().prepareStatement("SELECT data FROM mage WHERE id = ?");
            loadQuery.setString(1, id);
            results = loadQuery.executeQuery();
            if (results.next()) {
                YamlConfiguration saveFile = new YamlConfiguration();
                saveFile.loadFromString(results.getString(1));
                data = load(id, saveFile);
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.SEVERE, "Error loading player " + id, ex);
            close();
        } finally {
            close(results);
            close(loadQuery);
        }

        if (callback != null) {
            callback.run(data);
        }
    }

    @Override
    public void delete(String id) {
        PreparedStatement delete = null;
        try {
            delete = getConnection().prepareStatement("DELETE FROM mage WHERE id = ?");
            delete.setString(1, id);
            delete.execute();
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Unable to delete mage " + id, ex);
            close();
        } finally {
            close(delete);
        }
    }

    @Override
    public Collection<String> getAllIds() {
        PreparedStatement idsQuery = null;
        ResultSet idResults = null;
        List<String> ids = new ArrayList<>();
        try {
            idsQuery = getConnection().prepareStatement("SELECT id FROM mage WHERE migrated = 0");
            idResults = idsQuery.executeQuery();
            while (idResults.next()) {
                ids.add(idResults.getString(1));
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Unable to lookup all mage ids", ex);
            close();
        } finally {
            close(idsQuery);
            close(idResults);
        }
        return ids;
    }

    @Override
    public void migrate(String id) {
        PreparedStatement migrate = null;
        try {
            migrate = getConnection().prepareStatement("UPDATE mage SET migrated = 1 WHERE id = ?");
            migrate.setString(1, id);
            migrate.execute();
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Could not set mage " + id + " as migrated", ex);
            close();
        } finally {
            close(migrate);
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

    private void close(@Nullable Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ex) {
                controller.getLogger().log(Level.WARNING, "Error closing statement", ex);
            }
        }
    }

    private void close(@Nullable ResultSet results) {
        if (results != null) {
            try {
                results.close();
            } catch (SQLException ex) {
                controller.getLogger().log(Level.WARNING, "Error closing result set", ex);
            }
        }
    }
}
