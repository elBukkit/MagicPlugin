package com.elmakers.mine.bukkit.magic;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationLoadTask implements Runnable {
    private final MagicController controller;
    private final CommandSender sender;

    protected ConfigurationSection configuration;
    protected ConfigurationSection messages;
    protected ConfigurationSection materials;
    protected ConfigurationSection wands;
    protected ConfigurationSection enchanting;
    protected ConfigurationSection crafting;
    protected Map<String, ConfigurationSection> spells;

    protected boolean success;

    public ConfigurationLoadTask(MagicController controller, CommandSender sender) {
        this.controller = controller;
        this.sender = sender;
    }

    public void runNow() {
        run(true);
    }

    @Override
    public void run() {
        run(false);
    }

    public void run(boolean synchronous) {
        success = true;
        Logger logger = controller.getLogger();

        // Load main configuration
        try {
            configuration = controller.loadMainConfiguration();
            configuration = controller.loadExamples(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading config.yml", ex);
            success = false;
        }

        // Load messages
        try {
            messages = controller.loadMessageConfiguration();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading messages.yml", ex);
            success = false;
        }

        // Load materials configuration
        try {
            materials = controller.loadMaterialsConfiguration();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading material.yml", ex);
            success = false;
        }

        // Load spells, and map their inherited configs
        try {
            spells = controller.loadAndMapSpells();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading spells.yml", ex);
            success = false;
        }

        // Load enchanting paths
        try {
            enchanting = controller.loadEnchantingConfiguration();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading enchanting.yml", ex);
            success = false;
        }

        // Load wand templates
        try {
            wands = controller.loadWandConfiguration();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading wands.yml", ex);
            success = false;
        }

        // Load crafting recipes
        try {
            crafting = controller.loadCraftingConfiguration();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading crafting.yml", ex);
            success = false;
        }

        if (synchronous) {
            controller.finalizeLoad(this, sender);
        } else {
            Plugin plugin = controller.getPlugin();
            final ConfigurationLoadTask result = this;
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                controller.finalizeLoad(result, sender);
                }
            });
        }
    }
}
