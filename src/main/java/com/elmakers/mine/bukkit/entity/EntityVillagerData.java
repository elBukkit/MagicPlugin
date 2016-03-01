package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public class EntityVillagerData extends EntityExtraData {
    protected Villager.Profession profession;
    protected Integer riches;
    protected List<MerchantRecipe> recipes;

    public EntityVillagerData() {

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
    }

    @Override
    public EntityExtraData clone() {
        EntityVillagerData copy = new EntityVillagerData();
        copy.profession = profession;
        copy.recipes = recipes;
        
        return copy;
    }
}