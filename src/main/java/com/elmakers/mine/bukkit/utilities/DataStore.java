package com.elmakers.mine.bukkit.utilities;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;

public class DataStore extends YamlConfiguration {
	protected final File file;
	protected final Logger logger;
	
	public DataStore(Logger logger, File file) {
		this.file = file;
		this.logger = logger;
	}
	
	public DataStore(Logger logger, String filename) {
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
                logger.warning("Not saving file " + file.getName() + ", temp file exists");
                tempFile.deleteOnExit();
                return;
            }
            File backupFile = new File(file.getAbsolutePath() + ".bak");
			save(tempFile);
            if (backupFile.exists()) {
                backupFile.delete();
            }
            File targetFile = new File(file.getAbsolutePath());
            if (targetFile.exists()) {
                targetFile.renameTo(backupFile);
            }
            tempFile.renameTo(file);
		} catch (Exception ex) {
			logger.warning(ex.getMessage());
			logger.warning("Error saving data file " + file.getName());
		}
	}

    public File getFile() {
        return file;
    }
}
