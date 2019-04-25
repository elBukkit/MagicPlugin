package com.elmakers.mine.bukkit.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityVillagerData extends EntityExtraData {
    protected Villager.Profession profession;

    public EntityVillagerData() {

    }

    public EntityVillagerData(ConfigurationSection parameters, MageController controller) {
        if (parameters.contains("villager_profession")) {
            profession = Villager.Profession.valueOf(parameters.getString("villager_profession").toUpperCase());
        }
    }

    public EntityVillagerData(Villager villager) {
        profession = villager.getProfession();
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Villager)) return;
        Villager villager = (Villager) entity;
        if (profession != null) {
            villager.setProfession(profession);
        }
    }

    @Override
    public EntityExtraData clone() {
        EntityVillagerData copy = new EntityVillagerData();
        copy.profession = profession;

        return copy;
    }
}