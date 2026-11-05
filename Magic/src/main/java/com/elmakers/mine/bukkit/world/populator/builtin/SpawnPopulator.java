package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.random.DistanceWeightedValue;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class SpawnPopulator extends BaseBlockPopulator {
    private List<DistanceWeightedValue<EntityData>> spawns = new ArrayList<>();
    private IntegerRange position;
    private int searchY = 16;
    private int searchX = 2;
    private int searchZ = 2;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        spawns.clear();

        position = IntegerRange.fromConfig(getLogger(), config, "position", 0, 15);
        searchX = config.getInt("search_x", searchX);
        searchY = config.getInt("search_y", searchY);
        searchZ = config.getInt("search_z", searchZ);

        MagicController controller = world.getController();
        ConfigurationSection typeConfigs = config.getConfigurationSection("types");
        if (typeConfigs != null) {
            for (String typeId : typeConfigs.getKeys(false)) {
                if (typeConfigs.isConfigurationSection(typeId)) {
                    ConfigurationSection generatorConfig = typeConfigs.getConfigurationSection(typeId);
                    typeId = generatorConfig.getString("type", typeId);
                    EntityData entityData = controller.getMob(typeId);
                    DistanceWeightedValue<EntityData> entry = DistanceWeightedValue.fromConfig(world.getLogger(), entityData, generatorConfig);
                    spawns.add(entry);
                } else {
                    EntityData entityData = controller.getMob(typeId);
                    DistanceWeightedValue<EntityData> entry = DistanceWeightedValue.fromString(world.getLogger(), entityData, typeConfigs.getString(typeId));
                    spawns.add(entry);
                }
            }
        } else {
            List<String> typeList = ConfigUtils.getStringList(config, "types");
            if (typeList != null && !typeList.isEmpty()) {
                for (String typeId : typeList) {
                    EntityData entityData = controller.getMob(typeId);
                    DistanceWeightedValue<EntityData> entry = DistanceWeightedValue.fromString(world.getLogger(), entityData, "1");
                    spawns.add(entry);
                }
            } else {
                controller.getLogger().warning("Spawn populator missing 'types' section");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final int baseY = world.getGroundLevel();
        EntityData entityData = RandomUtils.getDistanceWeighted(spawns, context.getSeed(), context.getChunkX(), context.getChunkZ());
        if (entityData == null) return;

        boolean spawned = false;
        final int positionX = this.position.getRandom(random);
        final int positionZ = this.position.getRandom(random);
        for (int x = positionX - searchX; x < positionX + searchX && !spawned; x++) {
            for (int z = positionZ - searchZ; z < positionZ + searchZ && !spawned; z++) {
                for (int y = baseY - searchY; y < baseY + searchY && !spawned; y++) {
                    if (!context.isInRegion(x, y, z) || !context.isInRegion(x, y + 1, z) || !context.isInRegion(x, y - 1, z)) continue;
                    if (context.getType(x, y, z).isAir() && context.getType(x, y + 1, z).isAir() && !context.getType(x, y - 1, z).isAir()) {
                        context.spawn(entityData, x, y, z);
                        spawned = true;
                    }
                }
            }
        }
    }
}
