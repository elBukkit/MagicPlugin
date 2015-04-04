package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.magic.MagicRecipe;
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

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.wand.Wand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CraftingController implements Listener {
	private final MagicController controller;
	private boolean craftingEnabled = false;
    private Map<Material, List<MagicRecipe>> recipes = new HashMap<Material, List<MagicRecipe>>();
    int recipeCount = 0;

	public CraftingController(MagicController controller) {
		this.controller = controller;
	}
	
	public void load(ConfigurationSection configuration) {
        recipes.clear();
        if (!craftingEnabled) {
            return;
        }
        Set<String> recipeKeys = configuration.getKeys(false);
        for (String key : recipeKeys)
        {
            ConfigurationSection parameters = configuration.getConfigurationSection(key);
            if (!parameters.getBoolean("enabled", true)) continue;

            MagicRecipe recipe = new MagicRecipe(controller);
            if (!recipe.load(parameters)) {
                controller.getLogger().warning("Failed to create crafting recipe: " + key);
                continue;
            }
            Material outputType = recipe.getOutputType();
            List<MagicRecipe> similar = recipes.get(outputType);
            if (similar == null) {
                similar = new ArrayList<MagicRecipe>();
                recipes.put(outputType, similar);
            }
            similar.add(recipe);
            recipeCount++;
        }
	}
	
	public void register(Plugin plugin) {
        if (!craftingEnabled) {
            return;
        }
        for (List<MagicRecipe> list : recipes.values()) {
            for (MagicRecipe recipe : list) {
                recipe.register(plugin);
            }
        }
	}
	
	@EventHandler
	public void onPrepareCraftItem(PrepareItemCraftEvent event) 
	{
        CraftingInventory inventory = event.getInventory();
        ItemStack[] contents = inventory.getMatrix();

        // Check for wands glitched into the crafting inventor
        for (int i = 0; i < 9 && i < contents.length; i++) {
            ItemStack item = contents[i];
            if (Wand.isUpgrade(item) || Wand.isWand(item) || Wand.isBrush(item) || Wand.isSpell(item)) {
                inventory.setResult(new ItemStack(Material.AIR));
                return;
            }
        }

        if (!craftingEnabled) return;

        Recipe recipe = event.getRecipe();
        Material resultType = recipe.getResult().getType();
        List<MagicRecipe> candidates = recipes.get(resultType);
        if (candidates == null || candidates.size() == 0) return;

        for (MagicRecipe candidate : candidates) {
            Set<Material> ingredients = candidate.getIngredients();
            boolean ingredientsMatch = true;
            for (Material ingredient : ingredients) {
                if (!inventory.contains(ingredient)) {
                    ingredientsMatch = false;
                    break;
                }
            }

            Material substitute = candidate.getSubstitute();
            if (ingredientsMatch) {
                for (HumanEntity human : event.getViewers()) {
                    if (human instanceof Player && !controller.hasPermission((Player)human, "Magic.wand.craft")) {
                        inventory.setResult(new ItemStack(Material.AIR));
                        return;
                    }
                }

                ItemStack crafted = candidate.craft();
                inventory.setResult(crafted);
                break;
            } else if (substitute != null) {
                inventory.setResult(new ItemStack(substitute, 1));
            }
        }
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		if (event.isCancelled()) return;
		
		InventoryType inventoryType = event.getInventory().getType();
		SlotType slotType = event.getSlotType();
		// Check for wand clicks to prevent grinding them to dust, or whatever.
		if (slotType == SlotType.CRAFTING && (inventoryType == InventoryType.CRAFTING || inventoryType == InventoryType.WORKBENCH)) {
			ItemStack cursor = event.getCursor();
			if (Wand.isWand(cursor) || Wand.isBrush(cursor) || Wand.isSpell(cursor) || Wand.isUpgrade(cursor)) {
				event.setCancelled(true);
				return;
			}
		}
	}

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player)event.getWhoClicked();
        Mage mage = controller.getMage(player);

        // Don't allow crafting in the wand inventory.
        if (mage.hasStoredInventory()) {
            event.setCancelled(true);
            return;
        }
    }
	
	public boolean isEnabled()
	{
		return craftingEnabled;
	}

    public void setEnabled(boolean enabled)
    {
        this.craftingEnabled = enabled;
    }

    public int getCount() {
        return recipeCount;
    }
}
