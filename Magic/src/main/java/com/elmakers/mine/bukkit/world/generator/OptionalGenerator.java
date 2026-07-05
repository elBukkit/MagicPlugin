package com.elmakers.mine.bukkit.world.generator;

import org.bukkit.Location;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.world.LocationContext;
import com.elmakers.mine.bukkit.world.WorldContext;
import com.elmakers.mine.bukkit.world.WorldInfoContext;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public abstract class OptionalGenerator extends BaseChunkGenerator {
    protected abstract BaseChunkGenerator getGenerator(WorldContext context);

    @Override
    public void generate(GeneratorContext context) {
        BaseChunkGenerator generator = getGenerator(context);
        if (generator != null) {
            generator.generate(context);
        }
    }

    @Override
    public void populate(PopulatorContext context) {
        super.populate(context);
        BaseChunkGenerator generator = getGenerator(context);
        if (generator != null) {
            generator.populate(context);
        }
    }

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldContext context) {
        BaseChunkGenerator generator = getGenerator(context);
        return generator != null && generator.shouldGenerateNoise(context);
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldContext context) {
        BaseChunkGenerator generator = getGenerator(context);
        return generator != null && generator.shouldGenerateSurface(context);
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldContext context) {
        BaseChunkGenerator generator = getGenerator(context);
        return generator != null && generator.shouldGenerateCaves(context);
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldContext context) {
        BaseChunkGenerator generator = getGenerator(context);
        return generator != null && generator.shouldGenerateDecorations(context);
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldContext context) {
        BaseChunkGenerator generator = getGenerator(context);
        return generator != null && generator.shouldGenerateMobs(context);
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldContext context) {
        BaseChunkGenerator generator = getGenerator(context);
        return generator != null && generator.shouldGenerateStructures(context);
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        String baseTargetWorld = super.getPortalTargetWorld(location);
        if (baseTargetWorld != null) {
            return baseTargetWorld;
        }
        final LocationContext context = new LocationContext(location);
        BaseChunkGenerator generator = getGenerator(context);
        if (generator == null) {
            return null;
        }
        return generator.getPortalTargetWorld(location);
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        BiomeProvider biomeProvider = super.getDefaultBiomeProvider(worldInfo);
        if (biomeProvider != null) {
            return biomeProvider;
        }
        final WorldInfoContext context = new WorldInfoContext(worldInfo);
        BaseChunkGenerator generator = getGenerator(context);
        return generator != null ? generator.getDefaultBiomeProvider(worldInfo) : null;
    }
}
