package com.elmakers.mine.bukkit.world.generator;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.world.LocationContext;
import com.elmakers.mine.bukkit.world.WorldContext;
import com.elmakers.mine.bukkit.world.WorldInfoContext;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public abstract class CompoundGenerator extends BaseChunkGenerator {
    protected abstract List<BaseChunkGenerator> getGenerators(WorldContext context);

    @Override
    public void generate(GeneratorContext context) {
        for (BaseChunkGenerator generator : getGenerators(context)) {
            generator.generate(context);
        }
    }

    @Override
    public void populate(PopulatorContext context) {
        super.populate(context);
        for (BaseChunkGenerator generator : getGenerators(context)) {
            generator.populate(context);
        }
    }

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldContext context) {
        for (BaseChunkGenerator generator : getGenerators(context)) {
            if (generator.shouldGenerateNoise(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldContext context) {
        for (BaseChunkGenerator generator : getGenerators(context)) {
            if (generator.shouldGenerateNoise(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldContext context) {
        for (BaseChunkGenerator generator : getGenerators(context)) {
            if (generator.shouldGenerateNoise(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldContext context) {
        for (BaseChunkGenerator generator : getGenerators(context)) {
            if (generator.shouldGenerateNoise(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldContext context) {
        for (BaseChunkGenerator generator : getGenerators(context)) {
            if (generator.shouldGenerateNoise(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldContext context) {
        for (BaseChunkGenerator generator : getGenerators(context)) {
            if (generator.shouldGenerateNoise(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        String baseTargetWorld = super.getPortalTargetWorld(location);
        if (baseTargetWorld != null) {
            return baseTargetWorld;
        }
        final LocationContext context = new LocationContext(location);
        for (BaseChunkGenerator generator : getGenerators(context)) {
            String targetWorld = generator.getPortalTargetWorld(location);
            if (targetWorld != null) {
                return targetWorld;
            }
        }
        return null;
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        BiomeProvider biomeProvider = super.getDefaultBiomeProvider(worldInfo);
        if (biomeProvider != null) {
            return biomeProvider;
        }
        final WorldInfoContext context = new WorldInfoContext(worldInfo);
        for (BaseChunkGenerator generator : getGenerators(context)) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
            if (biomeProvider != null) {
                return biomeProvider;
            }
        }
        return null;
    }
}
