package com.elmakers.mine.bukkit.entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityVillagerData extends EntityExtraData {
    protected Villager.Profession profession;
    protected Integer riches;
    protected List<MerchantRecipe> recipes;

    public EntityVillagerData() {

    }

    public EntityVillagerData(ConfigurationSection parameters, MageController controller) {
        if (parameters.contains("villager_profession")) {
            profession = Villager.Profession.valueOf(parameters.getString("villager_profession").toUpperCase());
        }
        if (parameters.contains("villager_riches")) {
            riches = parameters.getInt("villager_riches");
        }
        if (parameters.contains("villager_trades")) {
            recipes = new ArrayList<>();
            Collection<ConfigurationSection> tradeList = ConfigurationUtils.getNodeList(parameters, "villager_trades");
            for (ConfigurationSection tradeConfiguration : tradeList) {
                String outputKey = tradeConfiguration.getString("output");
                ItemStack output = controller.createItem(outputKey);
                if (output == null || output.getType() == Material.AIR)
                {
                    controller.getLogger().warning("Invalid output specified in villager trade: " + outputKey);
                    continue;
                }
                MerchantRecipe recipe = new MerchantRecipe(output, tradeConfiguration.getInt("max_uses", 1));
                recipe.setExperienceReward(tradeConfiguration.getBoolean("experience_reward", true));
                List<String> ingredientConfiguration = tradeConfiguration.getStringList("ingredients");
                for (String ingredientKey : ingredientConfiguration) {
                    ItemStack ingredient = controller.createItem(ingredientKey);
                    if (ingredient == null || ingredient.getType() == Material.AIR)
                    {
                        controller.getLogger().warning("Invalid ingredient specified in villager trade: " + ingredientKey);
                        continue;
                    }
                    recipe.addIngredient(ingredient);
                }
                recipes.add(recipe);
            }
        }
    }

    public EntityVillagerData(Villager villager) {
        profession = villager.getProfession();
        riches = villager.getRiches();
        recipes = villager.getRecipes();
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Villager)) return;
        Villager villager = (Villager) entity;
        if (profession != null) {
            villager.setProfession(profession);
        }
        if (recipes != null) {
            villager.setRecipes(recipes);
        }
        if (riches != null) {
            villager.setRiches(riches);
        }
    }

    @Override
    public EntityExtraData clone() {
        EntityVillagerData copy = new EntityVillagerData();
        copy.profession = profession;
        copy.recipes = recipes;
        
        return copy;
    }
}