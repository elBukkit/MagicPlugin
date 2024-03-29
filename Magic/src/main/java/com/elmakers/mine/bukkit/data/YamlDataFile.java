package com.elmakers.mine.bukkit.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class YamlDataFile extends YamlConfiguration {
    protected final File file;
    protected final Logger logger;
    protected final boolean checkBackupSize;

    public YamlDataFile(Logger logger, File file) {
        this(logger, file, true);
    }

    public YamlDataFile(Logger logger, File file, boolean checkBackupSize) {
        this.file = file;
        this.logger = logger;
        this.checkBackupSize = checkBackupSize;
    }

    public YamlDataFile(Logger logger, String filename) {
        file = new File(filename);
        this.logger = logger;
        checkBackupSize = true;
    }

    public void load() {
        try {
            load(file);
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
            logger.warning("Error loading data file " + file.getName());
        }
    }

    public void save() {
        try {
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            if (tempFile.exists()) {
                logger.warning("Temp file for " + file.getName() + "exists, deleting");
                tempFile.delete();
            }
            File backupFile = new File(file.getAbsolutePath() + ".bak");
            save(tempFile);

            if (checkBackupSize && backupFile.exists() && backupFile.length() > tempFile.length()) {
                logger.info("Backup file " + backupFile.getName() + " is larger than " + tempFile.getName());
                int index = 1;
                File saveBackup = new File(backupFile.getAbsolutePath() + "." + index);
                while (saveBackup.exists()) {
                    index++;
                    saveBackup = new File(backupFile.getAbsolutePath() + "." + index);
                }
                logger.info("This may be normal, but just in case the backup file will be saved permanently as " + saveBackup.getName());
                logger.info("If data seems missing, please restore the backup file while the server is shut down");
                backupFile.renameTo(saveBackup);
            }

            if (backupFile.exists()) {
                backupFile.delete();
            }
            File targetFile = new File(file.getAbsolutePath());
            if (targetFile.exists()) {
                targetFile.renameTo(backupFile);
            }
            tempFile.renameTo(file);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error saving data file " + file.getName(), ex);
        }
    }

    public File getFile() {
        return file;
    }

    @SuppressWarnings("unchecked")
    private Object convert(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>)value;
            if (!list.isEmpty()) {
                List<Object> newList = new ArrayList<>();
                for (Object o : list) {
                    newList.add(convert(o));
                }
                value = newList;
            }
        } else if (value instanceof ConfigurationSection) {
            value = ConfigurationUtils.toMap((ConfigurationSection)value);
        }
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>)value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                entry.setValue(convert(entry.getValue()));
            }
        }
        return value;
    }

    public void set(String path, Object value) {
        // Stop screwing up the config code Spigot :(
        // Specifically lists no longer get serialized since the "we need to save comments"
        // refactor apocalypse in 1.18
        value = convert(value);
        super.set(path, value);
    }
}
