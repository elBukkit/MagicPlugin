package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.block.Schematic;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.SchematicUtils;

import java.io.InputStream;

public class LoadSchematicTask implements Runnable {
    private final Schematic schematic;
    private final InputStream input;
    private final MagicController controller;

    public LoadSchematicTask(MagicController controller, InputStream input, Schematic schematic) {
        this.controller = controller;
        this.schematic = schematic;
        this.input = input;
    }

    @Override
    public void run() {
        try {
            SchematicUtils.loadSchematic(input, schematic);
            controller.info("Finished loading schematic");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
