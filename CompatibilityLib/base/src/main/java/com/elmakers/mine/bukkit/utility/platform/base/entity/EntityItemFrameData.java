package com.elmakers.mine.bukkit.utility.platform.base.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.entity.SpawnedEntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;

public class EntityItemFrameData extends EntityExtraData {
    protected Rotation rotation;
    protected ItemStack item;
    protected BlockFace facing;

    public EntityItemFrameData(ItemStack item, BlockFace facing, Rotation rotation) {
        this.item = item;
        this.facing = facing;
        this.rotation = rotation;
    }

    public EntityItemFrameData(ConfigurationSection parameters, MageController controller) {
        Logger log = controller.getLogger();
        String facingString = parameters.getString("facing");
        if (facingString != null && !facingString.isEmpty()) {
            try {
                facing = BlockFace.valueOf(facingString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid facing: " + facingString, ex);
            }
        }
        String rotationString = parameters.getString("rotation");
        if (rotationString != null && !rotationString.isEmpty()) {
            try {
                rotation = Rotation.valueOf(rotationString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid rotation: " + rotationString, ex);
            }
        }
        String itemKey = parameters.getString("item");
        if (itemKey != null && !itemKey.isEmpty()) {
            ItemData itemData = controller.getOrCreateItem(itemKey);
            if (itemData == null) {
                log.warning("Invalid item in item frame config: " + itemKey);
            } else {
                item = itemData.getItemStack();
            }
        }
    }

    public EntityItemFrameData(Entity entity) {
        if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame)entity;
            item = PlatformInterpreter.getPlatform().getItemUtils().getCopy(itemFrame.getItem());
            rotation = itemFrame.getRotation();
            facing = itemFrame.getFacing();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame)entity;
            if (!PlatformInterpreter.getPlatform().getItemUtils().isEmpty(item)) {
                itemFrame.setItem(item);
            }
            if (facing != null) {
                itemFrame.setFacingDirection(facing, true);
            }
            itemFrame.setRotation(rotation);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public SpawnedEntityExtraData spawn(Location location) {
        Entity newEntity = PlatformInterpreter.getPlatform().getCompatibilityUtils().createItemFrame(location, facing, rotation, item);
        return new SpawnedEntityExtraData(newEntity, false);
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }
        ItemFrame itemFrame = (ItemFrame)entity;
        ItemStack frameItem = itemFrame.getItem();
        InventoryUtils inventoryUtils = PlatformInterpreter.getPlatform().getInventoryUtils();
        DeprecatedUtils deprecatedUtils = PlatformInterpreter.getPlatform().getDeprecatedUtils();
        int mapId = inventoryUtils.getMapId(frameItem);
        mapId++;
        MapView mapView = deprecatedUtils.getMap(mapId);
        if (mapView == null) {
            mapId = 0;
            mapView = deprecatedUtils.getMap(mapId);
            if (mapView == null) {
                return false;
            }
        }
        inventoryUtils.setMapId(frameItem, mapId);
        itemFrame.setItem(frameItem);
        return true;
    }

    @Override
    public boolean canCycle(Entity entity) {
        if (!(entity instanceof ItemFrame)) {
            return false;
        }

        ItemFrame itemFrame = (ItemFrame)entity;
        ItemStack frameItem = itemFrame.getItem();
        CompatibilityUtils compatibilityUtils = PlatformInterpreter.getPlatform().getCompatibilityUtils();;
        if (frameItem == null || !compatibilityUtils.isFilledMap(frameItem.getType())) {
            return false;
        }
        return true;
    }
}
