package com.elmakers.mine.bukkit.data;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.data.BrushData;
import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataStore;
import com.elmakers.mine.bukkit.api.data.SpellData;
import com.elmakers.mine.bukkit.api.data.UndoData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public abstract class ConfigurationMageDataStore implements MageDataStore {
    protected Logger logger;
    protected MageController controller;

    @Override
    public void initialize(MageController controller, ConfigurationSection configuration) {
        this.controller = controller;
        logger = controller.getPlugin().getLogger();
    }

    public void save(MageData mage, ConfigurationSection saveFile) {
        saveFile.set("id", mage.getId());
        saveFile.set("name", mage.getName());
        saveFile.set("last_cast", mage.getLastCast());
        saveFile.set("cooldown_expiration", mage.getCooldownExpiration());
        saveFile.set("last_death_location", ConfigurationUtils.fromLocation(mage.getLastDeathLocation()));
        Location location = mage.getLocation();
        if (location != null) {
            saveFile.set("location", ConfigurationUtils.fromLocation(location));
        }
        saveFile.set("destination_warp", mage.getDestinationWarp());
        saveFile.set("fall_protection_count", mage.getFallProtectionCount());
        saveFile.set("fall_protection", mage.getFallProtectionDuration());

        BrushData brush = mage.getBrushData();
        if (brush != null) {
            ConfigurationSection brushNode = saveFile.createSection("brush");
            try {
                Location cloneSource = brush.getCloneLocation();
                if (cloneSource != null) {
                    brushNode.set("clone_location", ConfigurationUtils.fromLocation(cloneSource));
                }
                Location cloneTarget = brush.getCloneTarget();
                if (cloneTarget != null) {
                    brushNode.set("clone_target", ConfigurationUtils.fromLocation(cloneTarget));
                }
                Location materialTarget = brush.getMaterialTarget();
                if (materialTarget != null) {
                    brushNode.set("material_target", ConfigurationUtils.fromLocation(materialTarget));
                }
                brushNode.set("map_id", brush.getMapId());
                brushNode.set("material", ConfigurationUtils.fromMaterial(brush.getMaterial()));
                brushNode.set("data", brush.getMaterialData());
                brushNode.set("schematic", brush.getSchematicName());
                brushNode.set("scale", brush.getScale());
                brushNode.set("erase", brush.isFillWithAir());
            } catch (Exception ex) {
                logger.warning("Failed to save brush data: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        UndoData undoData = mage.getUndoData();
        if (undoData != null) {
            List<Map<String, Object>> nodeList = new ArrayList<Map<String, Object>>();
            List<UndoList> undoList = undoData.getBlockList();
            for (UndoList list : undoList) {
                MemoryConfiguration listNode = new MemoryConfiguration();
                list.save(listNode);
                nodeList.add(listNode.getValues(true));
            }
            saveFile.set("undo", nodeList);
        }
        ConfigurationSection spellNode = saveFile.createSection("spells");
        for (SpellData spell : mage.getSpellData()) {
            ConfigurationSection node = spellNode.createSection(spell.getKey());
            node.set("cast_count", spell.getCastCount());
            node.set("last_cast", spell.getLastCast());
            node.set("cooldown_expiration", spell.getCooldownExpiration());
            node.set("active", spell.isActive() ? true : null);
            ConfigurationSection extra = spell.getExtraData();
            if (extra != null) {
                ConfigurationUtils.addConfigurations(node, extra);
            }
        }

        Map<String, ItemStack> boundWands = mage.getBoundWands();
        if (boundWands != null && boundWands.size() > 0) {
            ConfigurationSection wandSection = saveFile.createSection("wands");
            for (Map.Entry<String, ItemStack> wandEntry : boundWands.entrySet()) {
                String key = wandEntry.getKey();
                if (key == null || key.isEmpty()) continue;
                controller.serialize(wandSection, key, wandEntry.getValue());
            }
        }
        Map<Integer, ItemStack> respawnArmor = mage.getRespawnArmor();
        if (respawnArmor != null) {
            ConfigurationSection armorSection = saveFile.createSection("respawn_armor");
            for (Map.Entry<Integer, ItemStack> entry : respawnArmor.entrySet())
            {
                controller.serialize(armorSection, Integer.toString(entry.getKey()), entry.getValue());
            }
        }
        Map<Integer, ItemStack> respawnInventory = mage.getRespawnInventory();
        if (respawnInventory != null) {
            ConfigurationSection inventorySection = saveFile.createSection("respawn_inventory");
            for (Map.Entry<Integer, ItemStack> entry : respawnInventory.entrySet())
            {
                controller.serialize(inventorySection, Integer.toString(entry.getKey()), entry.getValue());
            }
        }

        List<ItemStack> storedInventory = mage.getStoredInventory();
        if (storedInventory != null) {
            saveFile.set("inventory", storedInventory);
        }
        saveFile.set("experience", mage.getStoredExperience());
        saveFile.set("level", mage.getStoredLevel());

        Wand soulWand = mage.getSoulWand();
        if (soulWand != null && soulWand instanceof com.elmakers.mine.bukkit.wand.Wand) {
            com.elmakers.mine.bukkit.wand.Wand wand = (com.elmakers.mine.bukkit.wand.Wand)soulWand;
            ConfigurationSection soulNode = saveFile.createSection("soul");
            wand.saveProperties(soulNode);
        }

        ConfigurationSection extraData = mage.getExtraData();
        if (extraData != null) {
            ConfigurationSection dataSection = saveFile.createSection("data");
            ConfigurationUtils.addConfigurations(dataSection, extraData);
        }
    }

    @Override
    public void save(Collection<MageData> mages) {
        for (MageData data : mages) {
            save(data);
        }
    }

    public MageData load(String id, ConfigurationSection saveFile) {
        MageData data = new MageData(id);

        // Load brush data
        ConfigurationSection brushConfig = saveFile.getConfigurationSection("brush");
        if (brushConfig != null) {
            BrushData brushData = new BrushData();
            try {
                brushData.setCloneLocation(ConfigurationUtils.getLocation(brushConfig, "clone_location"));
                brushData.setCloneTarget(ConfigurationUtils.getLocation(brushConfig, "clone_target"));
                brushData.setMaterialTarget(ConfigurationUtils.getLocation(brushConfig, "material_target"));
                brushData.setSchematicName(brushConfig.getString("schematic", ""));
                brushData.setMapId((short) brushConfig.getInt("map_id", -1));
                brushData.setMaterial(ConfigurationUtils.getMaterial(brushConfig, "material", Material.AIR));
                brushData.setMaterialData((short) brushConfig.getInt("data", 0));
                brushData.setScale(brushConfig.getDouble("scale", 1));
                brushData.setFillWithAir(brushConfig.getBoolean("erase", true));
                data.setBrushData(brushData);
            } catch (Exception ex) {
                logger.warning("Failed to load brush data: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // Load bound wand data
        if (saveFile.contains("wands")) {
            HashMap<String, ItemStack> boundWands = new HashMap<String, ItemStack>();
            ConfigurationSection wands = saveFile.getConfigurationSection("wands");
            Set<String> keys = wands.getKeys(false);
            for (String key : keys) {
                ItemStack boundWand = controller.deserialize(wands, key);
                boundWands.put(key, boundWand);
            }
            data.setBoundWands(boundWands);
        }

        // Load extra data
        data.setExtraData(saveFile.getConfigurationSection("data"));

        // Fall protection data
        data.setFallProtectionCount(saveFile.getLong("fall_protection_count", 0));
        data.setFallProtectionDuration(saveFile.getLong("fall_protection", 0));

        // Random data and mage properties
        data.setName(saveFile.getString("name", ""));
        data.setLastDeathLocation(ConfigurationUtils.getLocation(saveFile, "last_death_location"));
        data.setLocation(ConfigurationUtils.getLocation(saveFile, "location"));
        data.setLastCast(saveFile.getLong("last_cast", 0));
        data.setCooldownExpiration(saveFile.getLong("cooldown_expiration", 0));
        data.setDestinationWarp(saveFile.getString("destination_warp"));

        // Load undo queue
        UndoData undoData = new UndoData();
        Collection<ConfigurationSection> nodeList = ConfigurationUtils.getNodeList(saveFile, "undo");
        if (nodeList != null) {
            for (ConfigurationSection listNode : nodeList) {
                // The owner will get set by UndoQueue.load
                // This is .. kind of hacky, but allows us to use UndoList as a data
                // storage mechanism instead of making a separate DAO for it right now.
                UndoList list = new com.elmakers.mine.bukkit.block.UndoList(null);
                list.load(listNode);
                undoData.getBlockList().add(list);
            }
        }
        data.setUndoData(undoData);

        // Load spell data
        ConfigurationSection spellSection = saveFile.getConfigurationSection("spells");
        if (spellSection != null) {
            Set<String> keys = spellSection.getKeys(false);
            List<SpellData> spellDataList = new ArrayList<SpellData>();
            for (String key : keys) {
                ConfigurationSection node = spellSection.getConfigurationSection(key);
                SpellData spellData = new SpellData(key);
                spellData.setCastCount(node.getLong("cast_count", 0));
                spellData.setLastCast(node.getLong("last_cast", 0));
                spellData.setCooldownExpiration(node.getLong("cooldown_expiration", 0));
                spellData.setExtraData(node);
                spellDataList.add(spellData);
            }
            data.setSpellData(spellDataList);
        }

        // Load respawn inventory
        ConfigurationSection respawnData = saveFile.getConfigurationSection("respawn_inventory");
        if (respawnData != null) {
            Collection<String> keys = respawnData.getKeys(false);
            Map<Integer, ItemStack> respawnInventory = new HashMap<Integer, ItemStack>();
            for (String key : keys) {
                try {
                    int index = Integer.parseInt(key);
                    ItemStack item = controller.deserialize(respawnData, key);
                    respawnInventory.put(index, item);
                } catch (Exception ex) {
                }
            }
            data.setRespawnInventory(respawnInventory);
        }

        // Load respawn armor
        ConfigurationSection respawnArmorData = saveFile.getConfigurationSection("respawn_armor");
        if (respawnArmorData != null) {
            Collection<String> keys = respawnArmorData.getKeys(false);
            Map<Integer, ItemStack> respawnArmor = new HashMap<Integer, ItemStack>();
            for (String key : keys) {
                try {
                    int index = Integer.parseInt(key);
                    ItemStack item = controller.deserialize(respawnArmorData, key);
                    respawnArmor.put(index, item);
                } catch (Exception ex) {
                }
            }
            data.setRespawnArmor(respawnArmor);
        }

        // Load brush data
        if (saveFile.contains("brush")) {
            try {
                ConfigurationSection node = saveFile.getConfigurationSection("brush");
                BrushData brushData = new BrushData();
                brushData.setCloneLocation(ConfigurationUtils.getLocation(node, "clone_location"));
                brushData.setCloneTarget(ConfigurationUtils.getLocation(node, "clone_target"));
                brushData.setMaterialTarget(ConfigurationUtils.getLocation(node, "material_target"));
                brushData.setSchematicName(node.getString("schematic"));
                brushData.setMapId((short)node.getInt("map_id"));
                brushData.setMaterial(ConfigurationUtils.getMaterial(node, "material"));
                brushData.setMaterialData((short)node.getInt("data"));
                brushData.setScale(node.getDouble("scale"));
                brushData.setFillWithAir(node.getBoolean("erase"));
                data.setBrushData(brushData);
            } catch (Exception ex) {
                ex.printStackTrace();
                controller.getLogger().warning("Failed to load brush data: " + ex.getMessage());
            }
        }

        // Load stored inventory
        if (saveFile.contains("inventory")) {
            data.setStoredInventory((List<ItemStack>) saveFile.getList("inventory"));
        }
        if (saveFile.contains("experience")) {
            data.setStoredExperience((float)saveFile.getDouble("experience"));
        }
        if (saveFile.contains("level")) {
            data.setStoredLevel(saveFile.getInt("level"));
        }

        if (saveFile.contains("soul") && controller instanceof MagicController) {
            ConfigurationSection soulNode = saveFile.getConfigurationSection("soul");
            Wand wand = new com.elmakers.mine.bukkit.wand.Wand((MagicController)controller, soulNode);
            data.setSoulWand(wand);
        }

        return data;
    }
}
