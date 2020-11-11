package com.elmakers.mine.bukkit.entity;

import java.util.Arrays;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.RandomUtils;

public class EntityVillagerData extends EntityExtraData {
    protected Villager.Profession profession;
    protected boolean randomProfession;

    public EntityVillagerData() {

    }

    public EntityVillagerData(ConfigurationSection parameters, MageController controller) {
        if (parameters.contains("villager_profession")) {
            String professionKey = parameters.getString("villager_profession").toUpperCase();
            if (professionKey.startsWith("RAND")) {
                randomProfession = true;
            } else {
                try {
                    profession = Villager.Profession.valueOf(professionKey);
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid villager_profession: " + professionKey);
                }
            }
        }
    }

    public EntityVillagerData(Villager villager) {
        profession = villager.getProfession();
        randomProfession = false;
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Villager)) return;
        Villager villager = (Villager) entity;
        if (randomProfession) {
            Villager.Profession profession = RandomUtils.getRandom(Arrays.asList(Villager.Profession.values()), 1);
            villager.setProfession(profession);
        } else if (profession != null) {
            villager.setProfession(profession);
        }
    }

    @Override
    public EntityExtraData clone() {
        EntityVillagerData copy = new EntityVillagerData();
        copy.profession = profession;
        copy.randomProfession = randomProfession;

        return copy;
    }
}
