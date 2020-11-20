package com.elmakers.mine.bukkit.magic.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.event.CraftWandEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.MagicRecipe;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class CraftingController implements Listener {
    private final MagicController controller;
    private boolean craftingEnabled = false;
    private boolean allowWandsAsIngredients = true;
    private Map<Material, List<MagicRecipe>> recipes = new HashMap<>();
    private Set<String> recipeKeys = new HashSet<>();
    private Set<String> autoDiscoverRecipeKeys = new HashSet<>();

    public CraftingController(MagicController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection configuration) {
        recipes.clear();
        recipeKeys.clear();
        autoDiscoverRecipeKeys.clear();
        if (!craftingEnabled) {
            return;
        }
        Set<String> recipeKeys = configuration.getKeys(false);
        for (String key : recipeKeys)
        {
            ConfigurationSection parameters = configuration.getConfigurationSection(key);
            if (!ConfigurationUtils.isEnabled(parameters)) continue;

            MagicRecipe recipe = new MagicRecipe(key, controller);
            try {
                if (!recipe.load(parameters)) {
                    controller.getLogger().warning("Failed to create crafting recipe: " + key);
                    continue;
                }
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "An error occurred creating crafting recipe: " + key, ex);
                continue;
            }
            Material outputType = recipe.getOutputType();
            List<MagicRecipe> similar = recipes.get(outputType);
            if (similar == null) {
                similar = new ArrayList<>();
                recipes.put(outputType, similar);
            }
            similar.add(recipe);
            this.recipeKeys.add(recipe.getKey());
            if (recipe.isAutoDiscover()) {
                autoDiscoverRecipeKeys.add(recipe.getKey());
            }
        }
    }

    public void loadMainConfiguration(ConfigurationSection configuration) {
        craftingEnabled = configuration.getBoolean("enable_crafting", craftingEnabled);
        allowWandsAsIngredients = craftingEnabled && configuration.getBoolean("allow_wands_as_ingredients", allowWandsAsIngredients);
    }

    public boolean hasCraftPermission(Player player, MagicRecipe recipe)
    {
        if (player == null) return false;

        if (controller.hasBypassPermission(player)) {
            return true;
        }
        if (!controller.hasPermission(player, "Magic.wand.craft")) {
            return false;
        }
        return controller.hasPermission(player, "Magic.craft." + recipe.getKey(), true);
    }

    public void register(MagicController controller, Plugin plugin) {
        if (!craftingEnabled) {
            return;
        }
        for (List<MagicRecipe> list : recipes.values()) {
            for (MagicRecipe recipe : list) {
                recipe.register(controller, plugin);
            }
        }
    }

    @EventHandler
    public void onPrepareCraftItem(PrepareItemCraftEvent event)
    {
        CraftingInventory inventory = event.getInventory();
        ItemStack[] contents = inventory.getMatrix();

        // Check for wands glitched into the crafting inventory
        if (!allowWandsAsIngredients) {
            for (int i = 0; i < 9 && i < contents.length; i++) {
                ItemStack item = contents[i];
                if (Wand.isSpecial(item)) {
                    inventory.setResult(new ItemStack(Material.AIR));
                    return;
                }
            }
        }

        if (!craftingEnabled) return;

        Recipe recipe = event.getRecipe();
        if (recipe == null) return;
        ItemStack result = recipe.getResult();
        if (result == null) return;
        Material resultType = result.getType();
        List<MagicRecipe> candidates = recipes.get(resultType);
        if (candidates == null || candidates.size() == 0) return;

        MagicRecipe.MatchType matchType = MagicRecipe.MatchType.NONE;
        for (MagicRecipe candidate : candidates) {
            matchType = candidate.getMatchType(contents);
            Material substitute = candidate.getSubstitute();
            if (matchType != MagicRecipe.MatchType.NONE) {
                for (HumanEntity human : event.getViewers()) {
                    if (human instanceof Player && !hasCraftPermission((Player) human, candidate)) {
                        matchType = MagicRecipe.MatchType.PARTIAL;
                        break;
                    }
                }

                if (matchType == MagicRecipe.MatchType.PARTIAL) {
                    continue;
                } else if (matchType == MagicRecipe.MatchType.MATCH) {
                    ItemStack crafted = candidate.craft();
                    inventory.setResult(crafted);
                    for (HumanEntity human : event.getViewers()) {
                        candidate.crafted(human, controller);
                    }
                }
                break;
            } else if (substitute != null) {
                inventory.setResult(new ItemStack(substitute, 1));
            }
        }

        // Force-prevent crafting if we got a partial match or no permission
        if (matchType == MagicRecipe.MatchType.PARTIAL) {
            inventory.setResult(new ItemStack(Material.AIR));
            return;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // The only purpose of this handler is to prevent crafting with wands, so skip if we're allowed to do that.
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.isCancelled()) return;

        InventoryType inventoryType = event.getInventory().getType();
        SlotType slotType = event.getSlotType();
        // Check for wand clicks to prevent grinding them to dust, or whatever.
        if (slotType == SlotType.CRAFTING && (inventoryType == InventoryType.CRAFTING || inventoryType == InventoryType.WORKBENCH)) {
            ItemStack cursor = event.getCursor();
            if (Wand.isSpecial(cursor) && !allowWandsAsIngredients) {
                event.setCancelled(true);
            } else if (InventoryUtils.getMetaBoolean(cursor, "undroppable", false)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player)) return;

        Player player = (Player)human;
        Mage mage = controller.getMage(player);

        // Don't allow crafting in the wand inventory.
        if (mage.hasStoredInventory()) {
            event.setCancelled(true);
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        if (Wand.isWand(currentItem)) {
            currentItem = currentItem.clone();
            CraftWandEvent castEvent = new CraftWandEvent(mage, controller.getWand(currentItem));
            Bukkit.getPluginManager().callEvent(castEvent);
        }
    }

    public boolean isEnabled()
    {
        return craftingEnabled;
    }

    public int getCount() {
        return recipeKeys.size();
    }

    public List<String> getRecipeKeys() {
        return new ArrayList<>(recipeKeys);
    }

    public List<String> getAutoDiscoverRecipeKeys() {
        return new ArrayList<>(autoDiscoverRecipeKeys);
    }
}
