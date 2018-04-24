package com.elmakers.mine.bukkit.magic;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

public class ConfigurationLoadTask implements Runnable {
    private final MagicController controller;
    private final CommandSender sender;
    private static final Object loadLock = new Object();

    protected ConfigurationSection configuration;
    protected ConfigurationSection messages;
    protected ConfigurationSection materials;
    protected ConfigurationSection wands;
    protected ConfigurationSection paths;
    protected ConfigurationSection crafting;
    protected ConfigurationSection mobs;
    protected ConfigurationSection items;
    protected ConfigurationSection classes;
    protected ConfigurationSection attributes;
    protected ConfigurationSection automata;
    protected ConfigurationSection effects;
    protected ConfigurationSection spells;

    protected boolean success;

    public ConfigurationLoadTask(MagicController controller, CommandSender sender) {
        this.controller = controller;
        this.sender = sender;
    }

    public void runNow() {
        synchronized (loadLock) {
            run(true);
        }
    }

    @Override
    public void run() {
        synchronized (loadLock) {
            run(false);
        }
    }

    private void run(boolean synchronous) {
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
            messages = controller.loadMessageConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading messages.yml", ex);
            success = false;
        }

        // Load materials configuration
        try {
            materials = controller.loadMaterialsConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading material.yml", ex);
            success = false;
        }

        // Load spells, and map their inherited configs
        try {
            spells = controller.loadAndMapSpells(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading spells.yml", ex);
            success = false;
        }

        // Load enchanting paths
        try {
            paths = controller.loadPathConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading paths.yml", ex);
            success = false;
        }

        // Load wand templates
        try {
            wands = controller.loadWandConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading wands.yml", ex);
            success = false;
        }

        // Load crafting recipes
        try {
            crafting = controller.loadCraftingConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading crafting.yml", ex);
            success = false;
        }


        // Load classes
        try {
            classes = controller.loadClassConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading classes.yml", ex);
            success = false;
        }

        // Load mobs
        try {
            mobs = controller.loadMobsConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading mobs.yml", ex);
            success = false;
        }

        // Load items
        try {
            items = controller.loadItemsConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading items.yml", ex);
            success = false;
        }

        // Load attributes
        try {
            attributes = controller.loadAttributesConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading attributes.yml", ex);
            success = false;
        }

        // Load automata
        try {
            automata = controller.loadAutomataConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading automata.yml", ex);
            success = false;
        }

        // Load effects
        try {
            effects = controller.loadEffectConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading effects.yml", ex);
            success = false;
        }

        // Finalize configuration load
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
