package com.elmakers.mine.bukkit.arena;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class ArenaMobSpawner {
    private EntityData entity;
    private int count;

    public ArenaMobSpawner(EntityData entity, int count) {
        this.entity = entity;
        this.count = count;
    }

    public ArenaMobSpawner(MageController controller, ConfigurationSection configuration) {
        String mobType = configuration.getString("type");
        entity = controller.getMob(mobType);
        if (entity == null) {
            controller.getLogger().warning("Invalid mob type in arena config: " + mobType);
        }
        count = configuration.getInt("count", 1);
    }

    public void save(ConfigurationSection configuration) {
        if (entity != null) {
            configuration.set("type", entity.getKey());
        }
        configuration.set("count", count);
    }

    public EntityData getEntity() {
        return entity;
    }

    public void setEntity(EntityData entity) {
        this.entity = entity;
    }

    public boolean isValid() {
        return entity != null;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
