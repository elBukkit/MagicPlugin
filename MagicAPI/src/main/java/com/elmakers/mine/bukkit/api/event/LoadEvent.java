package com.elmakers.mine.bukkit.api.event;

import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.RequirementsProcessor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A custom event that fires whenever Magic loads or reloads configurations.
 */
public class LoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private MageController controller;
    private List<AttributeProvider> attributeProviders = new ArrayList<>();
    private Map<String, RequirementsProcessor> requirementProcessors = new HashMap<>();

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

    public void registerAttributeProvider(AttributeProvider provider) {
        attributeProviders.add(provider);
    }

    public Collection<AttributeProvider> getAttributeProviders() {
        return attributeProviders;
    }

    /**
     * Register a RequirementsProcessor for handling a specific type of requirement.
     * 
     * Requirement types are 1:1 with processors, each type may only have one processor associated with it.
     * 
     * Processors must be re-registered with each load.
     * 
     * Example requirement block, which might appear in a spell, Selector or other config:
     * 
     * requirements:
     * - type: skillapi
     *   skill: enchanting
     * - type: avengers
     *   power: hulkout
     *   character: Hulk
     * 
     * @param requirementType The type of requirements this processor handles
     * @param processor The processor to register
     */
    public void registerRequirementsProcessor(String requirementType, RequirementsProcessor processor) {
        if (requirementProcessors.containsKey(requirementType)) {
            controller.getLogger().warning("Tried to register RequiremensProcessor twice for same type: " + requirementType);
        }
        requirementProcessors.put(requirementType, processor);
    }
    
    public Map<String, RequirementsProcessor> getRequirementProcessors() {
        return requirementProcessors;
    }
}
