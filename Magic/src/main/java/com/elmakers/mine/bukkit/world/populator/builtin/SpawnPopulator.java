package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.random.DistanceWeightedValue;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

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
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final World targetWorld = Bukkit.getWorld(world.getName());
        if (targetWorld == null) return;
        final int chunkBaseX = (chunkX << 4);
        final int chunkBaseZ = (chunkZ << 4);
        final int baseY = world.getGroundLevel();
        EntityData entityData = RandomUtils.getDistanceWeighted(spawns, worldInfo.getSeed(), chunkX, chunkZ);
        if (entityData == null) return;

        boolean spawned = false;
        final int position = this.position.getRandom(random);
        final int baseX = chunkBaseX + position;
        final int baseZ = chunkBaseZ + position;
        for (int x = baseX - searchX; x < baseX + searchX && !spawned; x++) {
            for (int z = baseZ - searchZ; z < baseZ + searchZ && !spawned; z++) {
                for (int y = baseY - searchY; y < baseY + searchY && !spawned; y++) {
                    if (!region.isInRegion(x, y, z) || !region.isInRegion(x, y + 1, z) || !region.isInRegion(x, y - 1, z)) continue;
                    if (region.getType(x, y, z).isAir() && region.getType(x, y + 1, z).isAir() && !region.getType(x, y - 1, z).isAir()) {
                        Entity entity = entityData.create(new Location(targetWorld, x, y, z));
                        if (entity != null) {
                            region.addEntity(entity);
                        }
                        spawned = true;
                    }
                }
            }
        }
    }
}
