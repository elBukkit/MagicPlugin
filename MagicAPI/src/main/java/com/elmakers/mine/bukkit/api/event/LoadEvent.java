package com.elmakers.mine.bukkit.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.entity.TeamProvider;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.RequirementsProcessor;

/**
 * A custom event that fires whenever Magic loads or reloads configurations.
 */
public class LoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private MageController controller;

    public LoadEvent(MageController controller) {
        this.controller = controller;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public MageController getController() {
        return controller;
    }

    @Deprecated
    public void registerAttributeProvider(AttributeProvider provider) {
        controller.getLogger().warning("Attempt to register an attribute provider at load time: " + provider.getAllAttributes() + ", this is no longer supported and will not work. Please use PreLoadEvent instead.");
    }

    @Deprecated
    public void registerTeamProvider(TeamProvider provider) {
        controller.getLogger().warning("Attempt to register a team provider at load time, this is no longer supported and will not work. Please use PreLoadEvent instead.");
    }

    @Deprecated
    public void registerRequirementsProcessor(String requirementType, RequirementsProcessor processor) {
        controller.getLogger().warning("Attempt to register a requirement at load time: " + requirementType + ", this is no longer supported and will not work. Please use PreLoadEvent instead.");
    }
}
