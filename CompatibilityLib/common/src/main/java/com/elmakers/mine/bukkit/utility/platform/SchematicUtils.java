package com.elmakers.mine.bukkit.utility.platform;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.elmakers.mine.bukkit.utility.schematic.LoadableSchematic;

public interface SchematicUtils {
    boolean loadSchematic(InputStream input, LoadableSchematic schematic, Logger log);

    boolean saveSchematic(OutputStream output, String[][][] blockData);

    boolean loadLegacySchematic(InputStream input, LoadableSchematic schematic);
}
