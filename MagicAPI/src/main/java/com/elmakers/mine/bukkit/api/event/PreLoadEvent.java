package com.elmakers.mine.bukkit.api.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.entity.TeamProvider;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicProvider;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.api.protection.CastPermissionManager;
import com.elmakers.mine.bukkit.api.protection.EntityTargetingManager;
import com.elmakers.mine.bukkit.api.protection.PVPManager;
import com.elmakers.mine.bukkit.api.protection.PlayerWarpManager;
import com.elmakers.mine.bukkit.api.protection.PlayerWarpProvider;
import com.elmakers.mine.bukkit.api.requirements.RequirementsProcessor;
import com.elmakers.mine.bukkit.api.requirements.RequirementsProvider;

/**
 * A custom event that fires whenever Magic loads or reloads configurations.
 */
public class PreLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private MageController controller;
    private List<Currency> currencies = new ArrayList<>();
    private List<AttributeProvider> attributeProviders = new ArrayList<>();
    private List<TeamProvider> teamProviders = new ArrayList<>();
    private List<BlockBreakManager> blockBreakManagers = new ArrayList<>();
    private List<BlockBuildManager> blockBuildManager = new ArrayList<>();
    private List<PVPManager> pvpManagers = new ArrayList<>();
    private List<CastPermissionManager> castManagers = new ArrayList<>();
    private List<EntityTargetingManager> targetingManagers = new ArrayList<>();
    private Map<String, RequirementsProcessor> requirementProcessors = new HashMap<>();
    private Map<String, PlayerWarpManager> warpManagers = new HashMap<>();

    public PreLoadEvent(MageController controller) {
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

    /**
     * Register any MagicProvider. This replaces most of the below methods.
     * @param provider The provider to add
     * @return true if registration was successful, false for an unknown provider type.
     */
    public boolean register(MagicProvider provider) {
        if (provider instanceof EntityTargetingManager) {
            targetingManagers.add((EntityTargetingManager)provider);
        } else if (provider instanceof AttributeProvider) {
            attributeProviders.add((AttributeProvider)provider);
        } else if (provider instanceof TeamProvider) {
            teamProviders.add((TeamProvider)provider);
        } else if (provider instanceof Currency) {
            currencies.add((Currency)provider);
        } else if (provider instanceof RequirementsProvider) {
            RequirementsProvider requirements = (RequirementsProvider)provider;
            registerRequirementsProcessor(requirements.getKey(), requirements);
        } else if (provider instanceof PlayerWarpProvider) {
            PlayerWarpProvider warp = (PlayerWarpProvider)provider;
            registerPlayerWarpManager(warp.getKey(), warp);
        } else if (provider instanceof BlockBreakManager) {
            blockBreakManagers.add((BlockBreakManager)provider);
        } else if (provider instanceof PVPManager) {
            pvpManagers.add((PVPManager)provider);
        } else if (provider instanceof BlockBuildManager) {
            blockBuildManager.add((BlockBuildManager)provider);
        } else if (provider instanceof CastPermissionManager) {
            castManagers.add((CastPermissionManager)provider);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Register an AttributeProvider, for adding custom attribute support to spells and mages.
     *
     * @param provider The provider to add.
     */
    public void registerAttributeProvider(AttributeProvider provider) {
        attributeProviders.add(provider);
    }

    /**
     * Register a TeamProvider, to be able to make decisions about who players and mobs can target.
     *
     * @param provider The provider to add.
     */
    public void registerTeamProvider(TeamProvider provider) {
        teamProviders.add(provider);
    }

    /**
     * Register a custom currency, which can be used in shops, spell worth/earns and casting costs.
     *
     * @param currency A currency instance to register
     */
    public void registerCurrency(Currency currency) {
        currencies.add(currency);
    }

    /**
     * Register a RequirementsProcessor for handling a specific type of requirement.
     *
     * <p>Requirement types are 1:1 with processors, each type may only have one processor associated with it.
     *
     * <p>Processors must be re-registered with each load.
     *
     * <p>Example requirement block, which might appear in a spell, Selector or other config:
     *
     * <code>
     * requirements:
     * - type: skillapi
     *   skill: enchanting
     * - type: avengers
     *   power: hulkout
     *   character: Hulk
     * </code>
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

    /**
     * Register a BlockBreakManager, for controlling whether or not players can break blocks with magic.
     *
     * @param manager The manager to add.
     */
    public void registerBlockBlockManager(BlockBreakManager manager) {
        blockBreakManagers.add(manager);
    }

    /**
     * Register a PVPManager, for controlling whether or not players can harm other players with magic.
     *
     * @param manager The manager to add.
     */
    public void registerPVPManager(PVPManager manager) {
        pvpManagers.add(manager);
    }

    /**
     * Register a BlockBuildManager, for controlling whether or not players can place blocks with magic.
     *
     * @param manager The manager to add.
     */
    public void registerBlockBuildManager(BlockBuildManager manager) {
        blockBuildManager.add(manager);
    }

    /**
     * Register a CastPermissionManager, for controlling whether or not players can cast spells in
     * specific regions.
     *
     * @param manager The manager to add.
     */
    public void registerCastPermissionManager(CastPermissionManager manager) {
        castManagers.add(manager);
    }

    /**
     * Register a PlayerWarpManager, for providing warps to be used in the Recall menu.
     * The name of the manager as registered corresponds with the "allow_player_warps" map in the recall
     * spell configuration.
     *
     * @param key The name of the manager
     * @param manager The manager to add
     */
    public void registerPlayerWarpManager(String key, PlayerWarpManager manager) {
        warpManagers.put(key, manager);
    }

    public Map<String, PlayerWarpManager> getWarpManagers() {
        return warpManagers;
    }

    public Map<String, RequirementsProcessor> getRequirementProcessors() {
        return requirementProcessors;
    }

    public Collection<AttributeProvider> getAttributeProviders() {
        return attributeProviders;
    }

    public Collection<TeamProvider> getTeamProviders() {
        return teamProviders;
    }

    public Collection<Currency> getCurrencies() {
        return currencies;
    }

    public Collection<BlockBreakManager> getBlockBreakManagers() {
        return blockBreakManagers;
    }

    public Collection<BlockBuildManager> getBlockBuildManagers() {
        return blockBuildManager;
    }

    public Collection<PVPManager> getPVPManagers() {
        return pvpManagers;
    }

    public Collection<CastPermissionManager> getCastManagers() {
        return castManagers;
    }

    public Collection<EntityTargetingManager> getTargetingManagers() {
        return targetingManagers;
    }
}
