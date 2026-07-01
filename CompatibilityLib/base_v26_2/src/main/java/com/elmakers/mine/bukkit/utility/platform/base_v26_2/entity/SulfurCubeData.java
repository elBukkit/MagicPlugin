package com.elmakers.mine.bukkit.utility.platform.base_v26_2.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.SulfurCube;

import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.platform.base_v26_1.entity.EntityExtraData;

public class SulfurCubeData extends EntityExtraData {
    public Integer size;
    public boolean splittable;

    public SulfurCubeData(ConfigurationSection parameters) {
        size = ConfigUtils.getOptionalInteger(parameters, "size");
        splittable = parameters.getBoolean("split", true);
    }

    public SulfurCubeData(Entity entity) {
        if (entity instanceof SulfurCube cube) {
            size = cube.getSize();
            splittable = true;
        }
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof SulfurCube)) return;
        SulfurCube cube = (SulfurCube)entity;
        if (size != null) cube.setSize(size);
    }

    @Override
    public boolean isSplittable() {
        return splittable;
    }
}
