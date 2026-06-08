package com.elmakers.mine.bukkit.world.populator;

import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.BlockPopulator;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.world.MagicWorld;

public abstract class MagicBlockPopulator extends BlockPopulator {
    public static final String BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.world.populator.builtin";

    protected MagicWorld world;

    public boolean load(MagicWorld world, ConfigurationSection config) {
        this.world = world;
        return onLoad(config);
    }

    public abstract boolean onLoad(ConfigurationSection config);

    protected void logBlockRule(String message) {
        getController().info(message);
    }

    public MagicController getController() {
        return world.getController();
    }

    @Nullable
    public static MagicBlockPopulator create(MageController controller, String className) {
        if (className == null) return null;

        if (className.indexOf('.') <= 0) {
            className = BUILTIN_CLASSPATH + "." + className;
            if (!className.endsWith("Populator")) {
                className += "Populator";
            }
        }

        Class<?> handlerClass = null;
        try {
            handlerClass = Class.forName(className);
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error loading block populator: " + className, ex);
            return null;
        }

        Object newObject;
        try {
            newObject = handlerClass.getDeclaredConstructor().newInstance();
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error loading block populator: " + className, ex);
            return null;
        }

        if (newObject == null || !(newObject instanceof MagicBlockPopulator)) {
            controller.getLogger().warning("Error loading block populator: " + className + ", does it extend MagicBlockPopulator?");
            return null;
        }

        return (MagicBlockPopulator)newObject;
    }
}
