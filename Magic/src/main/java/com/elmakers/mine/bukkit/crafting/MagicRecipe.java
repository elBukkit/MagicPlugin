package com.elmakers.mine.bukkit.crafting;

import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

/**
 * Represents a crafting recipe which will make a wand item.
 */
public abstract class MagicRecipe {
    public static boolean FIRST_REGISTER = true;

    protected final MagicController controller;
    protected final String key;
    protected boolean locked = false;
    private boolean disableDefaultRecipe;

    // Output item
    private String outputKey;
    private Material outputType;
    private String outputItemType;

    protected MagicRecipe(String key, MagicController controller) {
        this.key = key;
        this.controller = controller;
    }

    protected abstract boolean load(ConfigurationSection configuration);

    protected ItemStack loadItem(ConfigurationSection configuration) {
        locked = configuration.getBoolean("locked", false);
        disableDefaultRecipe = configuration.getBoolean("disable_default", false);

        outputKey = configuration.getString("output");
        if (outputKey == null || outputKey.isEmpty()) {
            outputKey = this.key;
        }

        outputItemType = configuration.getString("output_type", "item");
        ItemStack item = craft();
        if (item == null) {
            controller.getLogger().warning("Unknown output for recipe " + key + ": " + outputKey);
            return null;
        }
        outputType = item.getType();

        return item;
    }

    public abstract Recipe getRecipe();

    public Material getOutputType() {
        return outputType;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public boolean isAutoDiscover() {
        return false;
    }

    public Material getSubstitute() {
        return null;
    }

    public @Nullable ItemStack craft() {
        if (outputKey == null) {
            return null;
        }

        ItemStack item;
        if (outputItemType.equalsIgnoreCase("wand")) {
            if (outputKey != null && !outputKey.isEmpty()) {
                item = controller.createWand(outputKey).getItem();
            } else {
                item = null;
            }
        } else if (outputItemType.equalsIgnoreCase("spell")) {
            item = controller.createSpellItem(outputKey);
        } else if (outputItemType.equalsIgnoreCase("brush")) {
            item = controller.createBrushItem(outputKey);
        } else if (outputItemType.equalsIgnoreCase("item")) {
            item = controller.createItem(outputKey);
        } else {
            item = null;
        }

        return item;
    }

    public void crafted(HumanEntity entity, MageController controller) {

    }

    public void unregister(Plugin plugin) {
        // Remove this recipe
        Recipe recipe = getRecipe();
        boolean canRemoveRecipes = CompatibilityLib.getCompatibilityUtils().canRemoveRecipes();
        if (recipe != null && !FIRST_REGISTER && canRemoveRecipes) {
            CompatibilityLib.getCompatibilityUtils().removeRecipe(key);
        }
    }

    public void preregister(Plugin plugin) {
        boolean canRemoveRecipes = CompatibilityLib.getCompatibilityUtils().canRemoveRecipes();
        if (disableDefaultRecipe && canRemoveRecipes) {
            int disabled = 0;
            List<Recipe> existing = plugin.getServer().getRecipesFor(new ItemStack(getOutputType()));
            for (Recipe recipe : existing) {
                CompatibilityLib.getCompatibilityUtils().removeRecipe(recipe);
                disabled++;
            }
            if (disabled > 0) {
                plugin.getLogger().info("Disabled " + disabled + " default crafting recipe(s) for " + getOutputType());
            }
        }
    }

    public void register(MagicController controller, Plugin plugin)
    {
        boolean canRemoveRecipes = CompatibilityLib.getCompatibilityUtils().canRemoveRecipes();
        Recipe recipe = getRecipe();
        // Add our custom recipe if crafting is enabled
        if (recipe != null)
        {
            // Recipes can't be removed on older minecraft versions, so we have to skip re-registering if we've already registered this one
            if (!FIRST_REGISTER && !canRemoveRecipes) {
                List<Recipe> existing = plugin.getServer().getRecipesFor(craft());
                if (existing.size() > 0) {
                    return;
                }
            }
            controller.info("Adding crafting recipe for " + getOutputKey());
            try {
                plugin.getServer().addRecipe(recipe);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Failed to add recipe", ex);
            }
        }
    }

    public boolean isSameRecipe(Recipe matchRecipe) {
        return CompatibilityLib.getCompatibilityUtils().isSameKey(controller.getPlugin(), getKey(), matchRecipe);
    }

    public String getKey() {
        return key;
    }

    public boolean isLocked() {
        return locked;
    }

    public static MagicRecipe loadRecipe(MagicController controller, String key, ConfigurationSection configuration) {
        MagicRecipe recipe = null;
        String recipeType = configuration.getString("type", "shaped").toLowerCase();
        try {
            switch (recipeType) {
                case "shaped":
                    recipe = new MagicShapedRecipe(key, controller);
                    break;
                default:
                    controller.getLogger().warning("Unknown recipe type: " + recipeType);
            }
            if (recipe != null) {
                if (!recipe.load(configuration)) {
                    recipe = null;
                }
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "An error occurred creating crafting recipe: " + key, ex);
            recipe = null;
        }
        return recipe;
    }

    public RecipeMatchType getMatchType(Recipe matchRecipe, ItemStack[] matrix) {
        if (!CompatibilityLib.getCompatibilityUtils().isLegacyRecipes()) {
            return isSameRecipe(matchRecipe) ? RecipeMatchType.MATCH : RecipeMatchType.NONE;
        }
        // I .. guess?
        return RecipeMatchType.MATCH;
    }
}
