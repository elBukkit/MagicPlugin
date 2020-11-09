package com.elmakers.mine.bukkit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.data.BrushData;
import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataCallback;
import com.elmakers.mine.bukkit.api.data.MageDataStore;
import com.elmakers.mine.bukkit.api.data.SpellData;
import com.elmakers.mine.bukkit.api.data.UndoData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class ConfigurationMageDataStore implements MageDataStore {
    protected MageController controller;

    @Override
    public void initialize(MageController controller, ConfigurationSection configuration) {
        this.controller = controller;
    }

    public void save(MageData mage, ConfigurationSection saveFile) {
        save(controller, mage, saveFile);
    }

    public static void save(MageController controller, MageData mage, ConfigurationSection saveFile) {
        saveFile.set("id", mage.getId());
        saveFile.set("name", mage.getName());
        saveFile.set("created", mage.getCreatedTime());
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
                controller.getLogger().warning("Failed to save brush data: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        Map<String, List<Map<String, Object>>> allUndoLists = new HashMap<>();
        UndoData undoData = mage.getUndoData();
        if (undoData != null) {
            List<World> worlds = controller.getPlugin().getServer().getWorlds();
            String serverId = worlds == null || worlds.isEmpty() ? "common" : worlds.get(0).getUID().toString();
            allUndoLists.put(serverId, saveUndoData(undoData));
        }
        Map<String, UndoData> externalUndo = mage.getExternalUndoData();
        if (externalUndo != null && !externalUndo.isEmpty()) {
            for (Map.Entry<String, UndoData> entry : externalUndo.entrySet()) {
                allUndoLists.put(entry.getKey(), saveUndoData(entry.getValue()));
            }
        }

        if (!allUndoLists.isEmpty()) {
            saveFile.set("undo", allUndoLists);
        }

        ConfigurationSection spellNode = saveFile.createSection("spells");
        Collection<SpellData> spellData = mage.getSpellData();
        if (spellData != null) {
            for (SpellData spell : spellData) {
                ConfigurationSection node = spellNode.createSection(spell.getKey().getKey());
                ConfigurationSection variables = spell.getVariables();
                if (variables != null) {
                    ConfigurationUtils.addConfigurations(node, variables);
                }
                node.set("cast_count", spell.getCastCount());
                node.set("last_cast", spell.getLastCast());
                node.set("last_earn", spell.getLastEarn());
                node.set("cooldown_expiration", spell.getCooldownExpiration());
                node.set("active", spell.isActive() ? true : null);
                node.set("enabled", spell.isEnabled() ? null : false);
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
        saveFile.set("open_wand", mage.isOpenWand());
        saveFile.set("gave_welcome_wand", mage.getGaveWelcomeWand());
        saveFile.set("resource_pack_preference", mage.getResourcePackEnabled());

        ConfigurationSection extraData = mage.getExtraData();
        if (extraData != null) {
            ConfigurationSection dataSection = saveFile.createSection("data");
            ConfigurationUtils.addConfigurations(dataSection, extraData);
        }

        ConfigurationSection properties = mage.getProperties();
        if (properties != null) {
            ConfigurationSection propertiesSection = saveFile.createSection("properties");
            ConfigurationUtils.addConfigurations(propertiesSection, properties);
        }

        ConfigurationSection variables = mage.getVariables();
        if (variables != null && !variables.getKeys(false).isEmpty()) {
            saveFile.set("variables", variables);
        }

        Map<String, ConfigurationSection> classProperties = mage.getClassProperties();
        if (classProperties != null) {
            ConfigurationSection classesSection = saveFile.createSection("classes");
            for (Map.Entry<String, ConfigurationSection> entry : classProperties.entrySet()) {
                ConfigurationSection classSection = classesSection.createSection(entry.getKey());
                ConfigurationUtils.addConfigurations(classSection, entry.getValue());
            }
        }

        Map<String, ConfigurationSection> modifierProperties = mage.getModifierProperties();
        if (modifierProperties != null) {
            ConfigurationSection modifiersSection = saveFile.createSection("modifiers");
            for (Map.Entry<String, ConfigurationSection> entry : modifierProperties.entrySet()) {
                ConfigurationSection modifierSection = modifiersSection.createSection(entry.getKey());
                ConfigurationUtils.addConfigurations(modifierSection, entry.getValue());
            }
        }
        saveFile.set("active_class", mage.getActiveClass());
        saveFile.set("health", mage.getHealth());
    }

    @Override
    public void save(Collection<MageData> mages) {
        for (MageData data : mages) {
            save(data, (MageDataCallback)null, false);
        }
    }

    private static List<Map<String, Object>> saveUndoData(UndoData undoData) {
        List<Map<String, Object>> nodeList = new ArrayList<>();
        List<UndoList> undoList = undoData.getBlockList();
        for (UndoList list : undoList) {
            MemoryConfiguration listNode = new MemoryConfiguration();
            list.save(listNode);
            nodeList.add(listNode.getValues(true));
        }
        return nodeList;
    }

    public MageData load(String id, ConfigurationSection saveFile) {
        return load(controller, id, saveFile);
    }

    public static MageData load(MageController controller, String id, ConfigurationSection saveFile) {
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
                controller.getLogger().warning("Failed to load brush data: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // Load bound wand data
        if (saveFile.contains("wands")) {
            HashMap<String, ItemStack> boundWands = new HashMap<>();
            ConfigurationSection wands = saveFile.getConfigurationSection("wands");
            Set<String> keys = wands.getKeys(false);
            for (String key : keys) {
                ItemStack boundWand = controller.deserialize(wands, key);
                if (boundWand == null) {
                    controller.getLogger().warning("Error loading bound wand: " + key);
                } else {
                    boundWands.put(key, boundWand);
                }
            }
            data.setBoundWands(boundWands);
        }

        // Load properties
        data.setProperties(saveFile.getConfigurationSection("properties"));
        data.setVariables(saveFile.getConfigurationSection("variables"));

        // Load classes
        Map<String, ConfigurationSection> classProperties = new HashMap<>();
        ConfigurationSection classes = saveFile.getConfigurationSection("classes");
        if (classes != null) {
            Set<String> classKeys = classes.getKeys(false);
            for (String classKey : classKeys) {
                classProperties.put(classKey, classes.getConfigurationSection(classKey));
            }
        }
        data.setClassProperties(classProperties);
        data.setActiveClass(saveFile.getString("active_class"));

        // Load modifiers
        Map<String, ConfigurationSection> modifierProperties = new HashMap<>();
        ConfigurationSection modifiers = saveFile.getConfigurationSection("modifiers");
        if (modifiers != null) {
            Set<String> modifierKeys = modifiers.getKeys(false);
            for (String modifierKey : modifierKeys) {
                modifierProperties.put(modifierKey, modifiers.getConfigurationSection(modifierKey));
            }
        }
        data.setModifierProperties(modifierProperties);

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
        data.setCreatedTime(saveFile.getLong("created", 0));
        data.setCooldownExpiration(saveFile.getLong("cooldown_expiration", 0));
        data.setDestinationWarp(saveFile.getString("destination_warp"));

        // Load undo queue
        Map<String, UndoData> externalUndoData = null;
        UndoData undoData = new UndoData();
        ConfigurationSection undoMap = ConfigurationUtils.getConfigurationSection(saveFile, "undo");
        if (undoMap == null) {
            // Try legacy list format
            Collection<ConfigurationSection> nodeList = ConfigurationUtils.getNodeList(saveFile, "undo");
            loadUndoData(controller, undoData, nodeList);
        } else {
            List<World> worlds = controller.getPlugin().getServer().getWorlds();
            String serverId = worlds == null || worlds.isEmpty() ? "common" : worlds.get(0).getUID().toString();
            for (String key : undoMap.getKeys(false)) {
                Collection<ConfigurationSection> nodeList = ConfigurationUtils.getNodeList(undoMap, key);
                if (key.equals(serverId)) {
                    loadUndoData(controller, undoData, nodeList);
                } else {
                    if (externalUndoData == null) {
                        externalUndoData = new HashMap<>();
                    }
                    UndoData external = new UndoData();
                    loadUndoData(controller, external, nodeList);
                    externalUndoData.put(key, external);
                }
            }

        }
        data.setExternalUndoData(externalUndoData);
        data.setUndoData(undoData);

        // Load spell data
        ConfigurationSection spellSection = saveFile.getConfigurationSection("spells");
        if (spellSection != null) {
            Set<String> keys = spellSection.getKeys(false);
            Map<String, SpellData> spellDataMap = new HashMap<>();
            for (String key : keys) {
                ConfigurationSection node = spellSection.getConfigurationSection(key);
                SpellData spellData = spellDataMap.get(key);
                if (spellData == null) {
                    spellData = new SpellData(key);
                    spellDataMap.put(key, spellData);
                }
                spellData.setIsEnabled(node.getBoolean("enabled", true));
                spellData.setCastCount(spellData.getCastCount() + node.getLong("cast_count", 0));
                spellData.setLastCast(Math.max(spellData.getLastCast(), node.getLong("last_cast", 0)));
                spellData.setLastEarn(Math.max(spellData.getLastEarn(), node.getLong("last_earn", 0)));
                spellData.setCooldownExpiration(Math.max(spellData.getCooldownExpiration(), node.getLong("cooldown_expiration", 0)));
                spellData.setIsActive(node.getBoolean("active"));
                node.set("cast_count", null);
                node.set("last_cast", null);
                node.set("last_earn", null);
                node.set("cooldown_expiration", null);
                node.set("active", null);
                node.set("enabled", null);
                spellData.setVariables(node);
            }
            data.setSpellData(spellDataMap.values());
        }

        // Load respawn inventory
        ConfigurationSection respawnData = saveFile.getConfigurationSection("respawn_inventory");
        if (respawnData != null) {
            Collection<String> keys = respawnData.getKeys(false);
            Map<Integer, ItemStack> respawnInventory = new HashMap<>();
            for (String key : keys) {
                try {
                    int index = Integer.parseInt(key);
                    ItemStack item = controller.deserialize(respawnData, key);
                    respawnInventory.put(index, item);
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Error loading respawn inventory for " + id, ex);
                }
            }
            data.setRespawnInventory(respawnInventory);
        }

        // Load respawn armor
        ConfigurationSection respawnArmorData = saveFile.getConfigurationSection("respawn_armor");
        if (respawnArmorData != null) {
            Collection<String> keys = respawnArmorData.getKeys(false);
            Map<Integer, ItemStack> respawnArmor = new HashMap<>();
            for (String key : keys) {
                try {
                    int index = Integer.parseInt(key);
                    ItemStack item = controller.deserialize(respawnArmorData, key);
                    respawnArmor.put(index, item);
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Error loading respawn armor inventory for " + id, ex);
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
            @SuppressWarnings("unchecked")
            List<ItemStack> inventory = (List<ItemStack>) saveFile.getList("inventory");
            data.setStoredInventory(inventory);
        }
        if (saveFile.contains("experience")) {
            data.setStoredExperience((float)saveFile.getDouble("experience"));
        }
        if (saveFile.contains("level")) {
            data.setStoredLevel(saveFile.getInt("level"));
        }
        data.setOpenWand(saveFile.getBoolean("open_wand", false));
        data.setGaveWelcomeWand(saveFile.getBoolean("gave_welcome_wand", false));
        if (saveFile.contains("resource_pack_preference")) {
            data.setResourcePackEnabled(saveFile.getBoolean("resource_pack_preference"));
        }
        data.setHealth(saveFile.getDouble("health"));

        return data;
    }

    private static void loadUndoData(MageController controller, UndoData undoData, Collection<ConfigurationSection> nodeList) {
        if (nodeList == null) return;
        for (ConfigurationSection listNode : nodeList) {
            // The owner will get set by UndoQueue.load
            UndoList list = new com.elmakers.mine.bukkit.block.UndoList(controller);
            list.load(listNode);
            undoData.getBlockList().add(list);
        }
    }
}
