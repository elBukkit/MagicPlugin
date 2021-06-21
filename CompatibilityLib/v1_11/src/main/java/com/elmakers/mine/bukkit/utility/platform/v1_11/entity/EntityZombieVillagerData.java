package com.elmakers.mine.bukkit.utility.platform.v1_11.entity;

import java.util.Arrays;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;

public class EntityZombieVillagerData extends EntityExtraData {
    protected Villager.Profession profession;
    protected boolean randomProfession;

    public EntityZombieVillagerData() {

    }

    public EntityZombieVillagerData(ConfigurationSection parameters, MageController controller) {
        String professionKey = parameters.getString("villager_profession");
        if (professionKey != null && !professionKey.isEmpty()) {
            professionKey = professionKey.toUpperCase();
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

    public EntityZombieVillagerData(Entity entity) {
        if (entity instanceof ZombieVillager) {
            ZombieVillager villager = (ZombieVillager)entity;
            profession = villager.getVillagerProfession();
        }
        randomProfession = false;
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof ZombieVillager)) return;
        ZombieVillager villager = (ZombieVillager) entity;
        if (randomProfession) {
            Villager.Profession profession = RandomUtils.getRandom(Arrays.asList(Villager.Profession.values()), 1);
            villager.setVillagerProfession(profession);
        } else if (profession != null) {
            villager.setVillagerProfession(profession);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }

        ZombieVillager villager = (ZombieVillager)entity;
        Villager.Profession profession = villager.getVillagerProfession();
        Villager.Profession[] professionValues = Villager.Profession.values();
        int villagerOrdinal = (profession.ordinal() + 1) % professionValues.length;
        profession = professionValues[villagerOrdinal];
        villager.setVillagerProfession(profession);
        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof ZombieVillager;
    }
}
