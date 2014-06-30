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
			save(file);
		} catch (Exception ex) {
			logger.warning(ex.getMessage());
			logger.warning("Error saving data file " + file.getName());
		}
	}

    public File getFile() {
        return file;
    }
}
