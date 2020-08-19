package com.elmakers.mine.bukkit.data;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;

public class YamlDataFile extends YamlConfiguration {
    protected final File file;
    protected final Logger logger;

    public YamlDataFile(Logger logger, File file) {
        this.file = file;
        this.logger = logger;
    }

    public YamlDataFile(Logger logger, String filename) {
        file = new File(filename);
        this.logger = logger;
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

            if (backupFile.exists() && backupFile.length() > tempFile.length()) {
                logger.warning("Backup file " + backupFile.getName() + " is larger than " + tempFile.getName());
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
}
