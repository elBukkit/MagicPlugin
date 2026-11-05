package com.elmakers.mine.bukkit.utility.platform.base_v26_3;

import com.elmakers.mine.bukkit.utility.SchematicUtilsBase;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public class SchematicUtils extends SchematicUtilsBase {
    public SchematicUtils(Platform platform) {
        super(platform);
    }

    protected String getBlock(com.elmakers.mine.bukkit.utility.platform.NBTUtils nbtUtils, Object nbtCompound) {
        // Migration for older structure files
        String id = nbtUtils.getString(nbtCompound, "Name");
        if (id != null) {
            nbtUtils.setString(nbtCompound, "id", id);
            nbtUtils.removeMeta(nbtCompound, "Name");
        }
        Object properties = nbtUtils.getTag(nbtCompound, "Properties");
        if (properties != null) {
            nbtUtils.setTag(nbtCompound, "properties", properties);
            nbtUtils.removeMeta(nbtCompound, "Properties");
        }
        return super.getBlock(nbtUtils, nbtCompound);
    }
}
