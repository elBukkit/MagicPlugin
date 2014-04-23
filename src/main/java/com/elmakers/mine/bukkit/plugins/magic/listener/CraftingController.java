package com.elmakers.mine.bukkit.plugins.magic.listener;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.ConfigurationUtils;

public class CraftingController implements Listener {
	private final MagicController controller;
	private boolean craftingEnabled = false;
	private Material wantItemSubstitute = null;

	private Recipe wandRecipe = null;
	private Material wandRecipeUpperMaterial = Material.DIAMOND;
	private Material wandRecipeLowerMaterial = Material.BLAZE_ROD;
	private String recipeOutputTemplate = "random(1)";

	public CraftingController(MagicController controller) {
		this.controller = controller;
	}
	
	public void load(ConfigurationSection properties) {
		Wand.DefaultWandMaterial = ConfigurationUtils.getMaterial(properties, "wand_item", Wand.DefaultWandMaterial);
		Wand.EnchantableWandMaterial = ConfigurationUtils.getMaterial(properties, "wand_item_enchantable", Wand.EnchantableWandMaterial);
		wantItemSubstitute = ConfigurationUtils.getMaterial(properties, "wand_item_substitute", null);
		
		// Parse crafting recipe settings
		craftingEnabled = properties.getBoolean("enable_crafting", craftingEnabled);
		if (craftingEnabled) {
			recipeOutputTemplate = properties.getString("crafting_output", recipeOutputTemplate);
			wandRecipeUpperMaterial = ConfigurationUtils.getMaterial(properties, "crafting_material_upper", wandRecipeUpperMaterial);
			wandRecipeLowerMaterial = ConfigurationUtils.getMaterial(properties, "crafting_material_lower", wandRecipeLowerMaterial);
		}

		if (craftingEnabled) {
			Wand wand = new Wand(controller);
			ShapedRecipe recipe = new ShapedRecipe(wand.getItem());
			recipe.shape("o", "i").
					setIngredient('o', wandRecipeUpperMaterial).
					setIngredient('i', wandRecipeLowerMaterial);
			wandRecipe = recipe;
			
			controller.getLogger().info("Wand crafting is enabled");
		}
	}
	
	public void enable(Plugin plugin) {
		// Add our custom recipe if crafting is enabled
		if (wandRecipe != null) {
			plugin.getServer().addRecipe(wandRecipe);
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
	
	@EventHandler
	public void onPrepareCraftItem(PrepareItemCraftEvent event) 
	{
		// TODO: Configurable crafting recipes
		Recipe recipe = event.getRecipe();
		if (craftingEnabled && wandRecipe != null && recipe.getResult().getType() == Wand.DefaultWandMaterial) {
			// Verify that this was our recipe
			// Just in case something else can craft our base material (e.g. stick)
			Inventory inventory = event.getInventory();
			if (inventory.contains(wandRecipeLowerMaterial) && !inventory.contains(wandRecipeUpperMaterial)) {
				Wand defaultWand = Wand.createWand(controller, null);
				Wand wand = defaultWand;
				if (recipeOutputTemplate != null && recipeOutputTemplate.length() > 0) {
					Wand templateWand = Wand.createWand(controller, recipeOutputTemplate);
					templateWand.add(defaultWand);
					wand = templateWand;
				}
				event.getInventory().setResult(wand.getItem());
			} else if (wantItemSubstitute != null) {
				event.getInventory().setResult(new ItemStack(wantItemSubstitute, 1));
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
			if (Wand.isWand(event.getCursor())) {
				event.setCancelled(true);
				return;
			}
		}
	}
}
