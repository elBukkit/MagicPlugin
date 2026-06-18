package com.elmakers.mine.bukkit.utility.platform.base_v26_2;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.SchematicUtilsBase;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.EntityUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.MobUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;

public abstract class PlatformBase extends com.elmakers.mine.bukkit.utility.platform.base_v26_1.PlatformBase {

    public PlatformBase(MageController controller) {
        super(controller);
    }

    protected EntityMetadataUtils createEntityMetadataUtils() {
        return new PersistentEntityMetadataUtils(this.getPlugin());
    }

    protected EntityUtils createEntityUtils() {
        return new EntityUtilsBase(this);
    }


    protected SkinUtils createSkinUtils() {
        return new SkinUtilsBase(this);
    }

    protected SchematicUtils createSchematicUtils() {
        return new SchematicUtilsBase(this);
    }

    protected NBTUtils createNBTUtils() {
       return new NBTUtilsBase(this);
    }

    protected ItemUtils createItemUtils() {
        return new ItemUtilsBase(this);
    }

    protected InventoryUtils createInventoryUtils() {
        return new InventoryUtilsBase(this);
    }

    protected CompatibilityUtils createCompatibilityUtils() {
        return new CompatibilityUtilsBase(this);
    }

    protected DeprecatedUtils createDeprecatedUtils() {
        return new DeprecatedUtilsBase(this);
    }

    protected MobUtils createMobUtils() {
        return new MobUtilsBase(this);
    }
}
