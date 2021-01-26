package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.automata.AutomatonLevel;
import com.elmakers.mine.bukkit.batch.SimulateBatch;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.boss.BossBarConfiguration;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class SimulateSpell extends BlockSpell {

    public static final String[] SIMULATE_PARAMETERS = {
        "radius", "yradius", "material", "omx", "omy", "omz", "death_material",
        "olcx", "olcy", "olcz", "obcx", "obcy", "obcz", "live_rules", "birth_rules",
        "target_mode", "target_types", "move", "target_min_range", "target_max_range",
        "cast", "death_cast", "cast_probability", "diagonal_live_rules", "diagonal_birth_rules",
        "boss_bar"
    };

    private static final int DEFAULT_RADIUS = 32;

    private TreeMap<Integer, AutomatonLevel> levelMap = null;

    @Override
    public SpellResult onCast(ConfigurationSection parameters) {
        Target t = getTarget();
        if (t == null) {
            return SpellResult.NO_TARGET;
        }

        Block target = t.getBlock();
        if (target == null) {
            return SpellResult.NO_TARGET;
        }

        if (!hasBuildPermission(target)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        int radius = parameters.getInt("radius", DEFAULT_RADIUS);
        radius = parameters.getInt("r", radius);
        int yRadius = parameters.getInt("yradius", 0);

        MaterialAndData birthMaterial = new MaterialAndData(target);
        birthMaterial = ConfigurationUtils.getMaterialAndData(parameters, "material", birthMaterial);
        birthMaterial = ConfigurationUtils.getMaterialAndData(parameters, "m", birthMaterial);

        Double dmxValue = ConfigurationUtils.getDouble(parameters, "obx", null);
        Double dmyValue = ConfigurationUtils.getDouble(parameters, "oby", null);
        Double dmzValue = ConfigurationUtils.getDouble(parameters, "obz", null);
        if (dmxValue != null || dmyValue != null || dmzValue != null) {
            Vector offset = new Vector(
                    dmxValue == null ? 0 : dmxValue,
                    dmyValue == null ? 0 : dmyValue,
                    dmzValue == null ? 0 : dmzValue);
            Location targetLocation = target.getLocation().add(offset);
            if (!CompatibilityUtils.isChunkLoaded(targetLocation)) {
                return SpellResult.FAIL;
            }
            birthMaterial = new MaterialAndData(targetLocation.getBlock());
        }

        Material deathMaterial = ConfigurationUtils.getMaterial(parameters, "death_material", Material.AIR);

        // use the target location with the player's facing
        Location location = getLocation();
        Location targetLocation = target.getLocation();
        targetLocation.setPitch(location.getPitch());
        targetLocation.setYaw(location.getYaw());

        // Look for relative-positioned chests that define the rulesets.

        Set<Integer> birthCounts = new HashSet<>();
        Set<Integer> liveCounts = new HashSet<>();

        Double dlcxValue = ConfigurationUtils.getDouble(parameters, "olcx", null);
        Double dlcyValue = ConfigurationUtils.getDouble(parameters, "olcy", null);
        Double dlczValue = ConfigurationUtils.getDouble(parameters, "olcz", null);
        if (dlcxValue != null || dlcyValue != null || dlczValue != null) {
            Location liveChestLocation = targetLocation.clone().add(new Vector(dlcxValue == null ? 0 : dlcxValue, dlcyValue == null ? 0 :
                dlcyValue, dlczValue == null ? 0 : dlczValue));
            Block chestBlock = liveChestLocation.getBlock();
            BlockState chestState = chestBlock.getState();
            if (chestState instanceof InventoryHolder) {
                ItemStack[] items = ((InventoryHolder)chestState).getInventory().getContents();
                for (int index = 0; index < items.length; index++) {
                    if (items[index] != null && items[index].getType() != Material.AIR) {
                        liveCounts.add(index + 1);
                        // controller.getLogger().info("SimulateSpell: Added live rules for index " + (index + 1)  + " from chest at " + liveChestLocation.toVector());
                    }
                }
            } else {
                controller.getLogger().warning("SimulateSpell: Chest for live rules not found at " + liveChestLocation.toVector());
            }
        } else if (parameters.contains("live_rules")) {
            liveCounts.addAll(ConfigurationUtils.getIntegerList(parameters, "live_rules"));
        } else {
            liveCounts.add(2);
            liveCounts.add(3);
        }

        Double dbcxValue = ConfigurationUtils.getDouble(parameters, "obcx", null);
        Double dbcyValue = ConfigurationUtils.getDouble(parameters, "obcy", null);
        Double dbczValue = ConfigurationUtils.getDouble(parameters, "obcz", null);
        if (dbcxValue != null || dbcyValue != null || dbczValue != null) {
            Location birthChestLocation = targetLocation.clone().add(new Vector(dbcxValue == null ? 0 : dbcxValue, dbcyValue == null ? 0 :
                dbcyValue, dbczValue == null ? 0 : dbczValue));
            Block chestBlock = birthChestLocation.getBlock();
            BlockState chestState = chestBlock.getState();
            if (chestState instanceof InventoryHolder) {
                ItemStack[] items = ((InventoryHolder)chestState).getInventory().getContents();
                for (int index = 0; index < items.length; index++) {
                    if (items[index] != null && items[index].getType() != Material.AIR) {
                        birthCounts.add(index + 1);
                        // controller.getLogger().info("SimulateSpell: Added birth rules for index " + (index + 1) + " from chest at " + birthChestLocation.toVector());
                    }
                }
            } else {
                controller.getLogger().warning("SimulateSpell: Chest for birth rules not found at " + birthChestLocation.toVector());
            }
        } else if (parameters.contains("birth_rules")) {
            birthCounts.addAll(ConfigurationUtils.getIntegerList(parameters, "birth_rules"));
        } else {
            birthCounts.add(3);
        }

        if (liveCounts.size() == 0 || birthCounts.size() == 0) {
            return SpellResult.FAIL;
        }

        String automataName = parameters.getString("animate", null);
        boolean isAutomata = automataName != null;
        final SimulateBatch batch = new SimulateBatch(this, targetLocation, radius, yRadius, birthMaterial, deathMaterial, liveCounts, birthCounts, automataName);

        if (parameters.contains("diagonal_live_rules")) {
            batch.setDiagonalLiveRules(ConfigurationUtils.getIntegerList(parameters, "diagonal_live_rules"));
        }

        if (parameters.contains("diagonal_birth_rules")) {
            batch.setDiagonalBirthRules(ConfigurationUtils.getIntegerList(parameters, "diagonal_birth_rules"));
        }

        batch.setBossBar(BossBarConfiguration.parse(controller, parameters, "$p"));
        batch.setReflectChange(parameters.getDouble("reflect_chance", 0));
        batch.setBirthRange(parameters.getInt("birth_range", 0));
        batch.setLiveRange(parameters.getInt("live_range", 0));
        batch.setConcurrent(parameters.getBoolean("concurrent", false));
        batch.setCastRange(parameters.getInt("cast_range", 16));
        int delay = parameters.getInt("delay", 0);

        if (isAutomata) {
            SimulateBatch.TargetMode targetMode = null;
            String targetModeString = parameters.getString("target_mode", "");
            if (targetModeString.length() > 0) {
                try {
                    targetMode = SimulateBatch.TargetMode.valueOf(targetModeString.toUpperCase());
                } catch (Exception ex) {
                    controller.getLogger().warning(ex.getMessage());
                }
            }
            SimulateBatch.TargetMode backupTargetMode = null;
            String backupTargetModeString = parameters.getString("backup_target_mode", "");
            if (backupTargetModeString.length() > 0) {
                try {
                    backupTargetMode = SimulateBatch.TargetMode.valueOf(backupTargetModeString.toUpperCase());
                } catch (Exception ex) {
                    controller.getLogger().warning(ex.getMessage());
                }
            }
            batch.setMoveRange(parameters.getInt("move", 3));

            SimulateBatch.TargetType targetType = null;
            String targetTypeString = parameters.getString("targets", "");
            if (targetTypeString.length() > 0) {
                try {
                    targetType = SimulateBatch.TargetType.valueOf(targetTypeString.toUpperCase());
                } catch (Exception ex) {
                    controller.getLogger().warning(ex.getMessage());
                }
            }
            batch.setTargetType(targetType);
            batch.setMinHuntRange(parameters.getInt("target_min_range", 4));
            batch.setMaxHuntRange(parameters.getInt("target_max_range", 128));
            batch.setDrop(parameters.getString("drop"), parameters.getInt("drop_xp", 0), ConfigurationUtils.getStringList(parameters, "drops"));
            int maxBlocks = parameters.getInt("max_blocks");
            batch.setMaxBlocks(maxBlocks);
            batch.setMinBlocks(parameters.getInt("min_blocks", maxBlocks));

            int level = parameters.getInt("level", 1);
            if (level < 1) level = 1;
            if (levelMap != null) {
                AutomatonLevel automatonLevel = levelMap.get(level);
                batch.setLevel(automatonLevel);
                delay = automatonLevel.getDelay(delay);
            }
            batch.setDelay(delay);
            if (targetMode != null) {
                batch.setTargetMode(targetMode);
            }
            if (backupTargetMode != null) {
                batch.setBackupTargetMode(backupTargetMode);
            }
        }
        boolean success = mage.addBatch(batch);
        return success ? SpellResult.CAST : SpellResult.FAIL;
    }

    @Override
    protected boolean isBatched() {
        return true;
    }

    @Override
    public void getParameters(Collection<String> parameters)
    {
        super.getParameters(parameters);
        parameters.addAll(Arrays.asList(SIMULATE_PARAMETERS));
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("material")) {
            examples.addAll(controller.getBrushKeys());
        } else if (parameterKey.equals("radius") || parameterKey.equals("yradis")) {
            examples.addAll(Arrays.asList(EXAMPLE_SIZES));
        } else if (parameterKey.equals("target_mode")) {
            SimulateBatch.TargetMode[] targetModes = SimulateBatch.TargetMode.values();
            for (SimulateBatch.TargetMode targetMode : targetModes) {
                examples.add(targetMode.name().toLowerCase());
            }
        } else if (parameterKey.equals("target_types")) {
            SimulateBatch.TargetType[] targetTypes = SimulateBatch.TargetType.values();
            for (SimulateBatch.TargetType targetType : targetTypes) {
                examples.add(targetType.name().toLowerCase());
            }
        } else if (parameterKey.equals("cast_probability")) {
            examples.addAll(Arrays.asList(EXAMPLE_PERCENTAGES));
        }
    }

    static final Integer[] emptyList = {};

    @Override
    protected void loadTemplate(ConfigurationSection template)
    {
        super.loadTemplate(template);

        if (template.contains("levels")) {
            ConfigurationSection levelTemplate = template.getConfigurationSection("levels");
            Collection<String> levelKeys = levelTemplate.getKeys(false);
            List<Integer> levels = new ArrayList<>(levelKeys.size());
            for (String levelString : levelKeys) {
                levels.add(Integer.parseInt(levelString));
            }

            if (levels.size() == 0) return;

            levelMap = new TreeMap<>();
            Collections.sort(levels);

            Integer[] levelsArray = levels.toArray(emptyList);
            for (int level = 1; level <= levelsArray[levelsArray.length - 1]; level++) {
                levelMap.put(level, new AutomatonLevel(level, levelsArray, template));
            }
        } else {
            levelMap = new TreeMap<>();
            levelMap.put(1, new AutomatonLevel(1, null, template));
        }
    }
}
