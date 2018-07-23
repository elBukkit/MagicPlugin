package com.elmakers.mine.bukkit.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.PrerequisiteSpell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.effect.SoundEffect;
import com.elmakers.mine.bukkit.magic.MageParameters;
import com.elmakers.mine.bukkit.magic.ParameterizedConfigurationSection;
import com.elmakers.mine.bukkit.magic.SpellParameters;

import de.slikey.effectlib.util.ConfigUtils;

public class ConfigurationUtils extends ConfigUtils {

    public static Random random = new Random();

    @Nullable
    public static Location getLocation(ConfigurationSection node, String path) {
        String stringData = node.getString(path);
        if (stringData == null) {
            return null;
        }

        return toLocation(stringData);
    }

    @Nullable
    public static BlockFace toBlockFace(String s) {
        BlockFace face = null;
        try {
            face = BlockFace.valueOf(s.toUpperCase());
        } catch (Exception ex) {
            face = null;
        }
        return face;
    }

    public static String fromBlockFace(BlockFace face) {
        return face.name().toLowerCase();
    }

    public static String fromVector(Vector vector) {
        if (vector == null) return "";
        return vector.getX() + "," + vector.getY() + "," + vector.getZ();
    }

    @Nullable
    public static Vector getVector(ConfigurationSection node, String path) {
       return getVector(node, path, null);
    }

    @Nullable
    public static Vector getVector(ConfigurationSection node, String path, Vector def) {
        String stringData = node.getString(path, null);
        if (stringData == null) {
            return def;
        }

        return toVector(stringData);
    }

    @Nullable
    public static Material getMaterial(ConfigurationSection node, String path, Material def) {
        String stringData = node.getString(path);
        if (stringData == null || stringData.isEmpty()) {
            return def;
        }

        return toMaterial(stringData);
    }

    @Nullable
    public static Material getMaterial(ConfigurationSection node, String path) {
        return getMaterial(node, path, null);
    }

    @Nullable
    public static MaterialAndData getMaterialAndData(ConfigurationSection node, String path) {
        return getMaterialAndData(node, path, null);
    }

    @Nullable
    public static MaterialAndData getMaterialAndData(ConfigurationSection node, String path, MaterialAndData def) {
        String stringData = node.getString(path);
        if (stringData == null) {
            return def;
        }

        return toMaterialAndData(stringData);
    }

    public static String fromLocation(Location location) {
        if (location == null) return "";
        if (location.getWorld() == null) return "";
        return location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorld().getName()
                + "," + location.getYaw() + "," + location.getPitch();
    }

    public static String fromMaterial(Material material)
    {
        if (material == null) return "";
        return material.name().toLowerCase();
    }

    @Nullable
    public static Location toLocation(Object o) {
        if (o instanceof Location) {
            return (Location)o;
        }
        if (o instanceof String) {
            try {
                float pitch = 0;
                float yaw = 0;
                String[] pieces = StringUtils.split((String)o, ',');
                double x = Double.parseDouble(pieces[0]);
                double y = Double.parseDouble(pieces[1]);
                double z = Double.parseDouble(pieces[2]);
                World world = null;
                if (pieces.length > 3) {
                    world = Bukkit.getWorld(pieces[3]);
                } else {
                    world = Bukkit.getWorlds().get(0);
                }
                if (pieces.length > 5) {
                    yaw = Float.parseFloat(pieces[4]);
                    pitch = Float.parseFloat(pieces[5]);
                }
                return new Location(world, x, y, z, yaw, pitch);
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    public static Vector toVector(Object o) {
        if (o instanceof Vector) {
            return (Vector)o;
        }
        if (o instanceof String) {
            try {
                String parse = (String)o;
                if (parse.isEmpty()) return null;
                if (!parse.contains(" ")) {
                    parse = parse.replace(",", " ");
                }
                parse = parse.replace("|", " ");
                String[] pieces = StringUtils.split(parse, ' ');
                double x = parseDouble(pieces[0]);
                double y = parseDouble(pieces[1]);
                double z = parseDouble(pieces[2]);
                return new Vector(x, y, z);
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    public static Material toMaterial(Object o) {
        if (o instanceof Material) {
            return (Material)o;
        }
        if (o instanceof Integer) {
            return Material.values()[(Integer)o];
        }
        if (o instanceof String) {
            String matName = (String)o;
            try {
                int value = Integer.parseInt(matName);
                return CompatibilityUtils.getMaterial(value);
            } catch (NumberFormatException ignored) {
            }

            return Material.getMaterial(matName.toUpperCase());
        }

        return null;
    }

    @Nullable
    public static MaterialAndData toMaterialAndData(Object o) {
        if (o instanceof MaterialAndData) {
            return (MaterialAndData)o;
        }
        if (o instanceof String) {
            String matName = (String)o;
            return new MaterialAndData(matName);
        }

        return null;
    }

    public static boolean loadAllTagsFromNBT(ConfigurationSection tags, Object tag)
    {
        try {
            Set<String> keys = InventoryUtils.getTagKeys(tag);
            if (keys == null) return false;

            for (String tagName : keys) {
                Object metaBase = NMSUtils.class_NBTTagCompound_getMethod.invoke(tag, tagName);
                if (metaBase != null) {
                    if (NMSUtils.class_NBTTagCompound.isAssignableFrom(metaBase.getClass())) {
                        ConfigurationSection newSection = tags.createSection(tagName);
                        loadAllTagsFromNBT(newSection, metaBase);
                    } else if (NMSUtils.class_NBTTagString.isAssignableFrom(metaBase.getClass())) {
                        // Special conversion case here... not sure if this is still a good idea
                        // But there would be downstream effects.
                        // TODO: Look closer.
                        set(tags, tagName, NMSUtils.class_NBTTagString_dataField.get(metaBase));
                    } else {
                        tags.set(tagName, InventoryUtils.getTagValue(metaBase));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean loadAllTagsFromNBT(ConfigurationSection tags, ItemStack item)
    {
        if (item == null) {
            return false;
        }
        Object handle = NMSUtils.getHandle(item);
        if (handle == null) return false;
        Object tag = NMSUtils.getTag(handle);
        if (tag == null) return false;

        return loadAllTagsFromNBT(tags, tag);
    }

    @SuppressWarnings("unchecked")
    protected void combine(Map<Object, Object> to, Map<? extends Object, Object> from) {
         for (Entry<?, Object> entry : from.entrySet()) {
             Object value = entry.getValue();
             Object key = entry.getKey();
             if (value instanceof Map && to.containsKey(key)) {
                 Object toValue = to.get(key);
                 if (toValue instanceof Map) {
                     combine((Map<Object, Object>)toValue, (Map<Object, Object>)value);
                     continue;
                 }
             }
             to.put(key, value);
         }
     }

    public static ConfigurationSection cloneEmptyConfiguration(ConfigurationSection section)
    {
        if (section instanceof SpellParameters) {
            return new SpellParameters((SpellParameters)section);
        }
        if (section instanceof MageParameters) {
            return new MageParameters((MageParameters)section);
        }
        if (section instanceof ParameterizedConfigurationSection) {
            return new ParameterizedConfigurationSection((ParameterizedConfigurationSection)section);
        }
        return new MemoryConfiguration();
    }

    public static ConfigurationSection cloneConfiguration(ConfigurationSection section)
    {
        ConfigurationSection copy = cloneEmptyConfiguration(section);
        return addConfigurations(copy, section);
    }

    private static Map<String, Object> replaceParameters(Map<String, Object> configuration, ConfigurationSection parameters)
    {
        if (configuration == null || configuration.isEmpty()) return configuration;

        Map<String, Object> replaced = new HashMap<>();
        for (Map.Entry<String, Object> entry : configuration.entrySet())
        {
            Object entryValue = entry.getValue();
            Object replacement = replaceParameters(entryValue, parameters);
            if (replacement != null) {
                replaced.put(entry.getKey(), replacement);
            }
        }

        return replaced;
    }

    @Nullable
    private static Object replaceParameters(Object value, ConfigurationSection parameters) {
        if (value == null) return null;
        if (value instanceof Map)
        {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)value;
            value = replaceParameters(map, parameters);
        }
        else if (value instanceof ConfigurationSection)
        {
            value = replaceParameters((ConfigurationSection)value, parameters);
        }
        else if (value instanceof List)
        {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>)value;
            value = replaceParameters(list, parameters);
        }
        else if (value instanceof String)
        {
            value = replaceParameter((String)value, parameters);
        }

        return value;
    }

    private static List<Object> replaceParameters(List<Object> configurations, ConfigurationSection parameters)
    {
        if (configurations == null || configurations.size() == 0) return configurations;

        List<Object> replaced = new ArrayList<>();
        for (Object value : configurations)
        {
            Object replacement = replaceParameters(value, parameters);
            if (replacement != null) {
                replaced.add(replacement);
            }
        }

        return replaced;
    }

    @Nullable
    public static ConfigurationSection replaceParameters(ConfigurationSection configuration, ConfigurationSection parameters) {
        if (configuration == null) return null;

        ConfigurationSection replaced = new MemoryConfiguration();
        Map<String, Object> map = NMSUtils.getMap(configuration);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;

            Object replacement = replaceParameters(value, parameters);
            if (replacement != null) {
                replaced.set(entry.getKey(), replacement);
            }
        }

        return replaced;
    }

    private static Object replaceParameter(String value, ConfigurationSection parameters)
    {
        if (value.length() < 2 || value.charAt(0) != '$') return value;
        return parameters.get(value.substring(1));
    }

    public static ConfigurationSection addConfigurations(ConfigurationSection first, ConfigurationSection second)
    {
        return addConfigurations(first, second, true);
    }

    public static ConfigurationSection addConfigurations(ConfigurationSection first, ConfigurationSection second, boolean override)
    {
        if (second == null) return first;
        Map<String, Object> map = NMSUtils.getMap(second);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;
            String key = entry.getKey();

            Object existingValue = first.get(key);
            if (value instanceof Map)
            {
                value = getConfigurationSection(second, key);
            }
            if (existingValue instanceof Map)
            {
                existingValue = getConfigurationSection(first, key);
            }
            if (value instanceof ConfigurationSection && (existingValue == null || existingValue instanceof ConfigurationSection)) {
                ConfigurationSection addChild = (ConfigurationSection)value;
                if (existingValue == null || !addChild.getBoolean("inherit", true)) {
                    ConfigurationSection newChild = first.createSection(key);
                    addConfigurations(newChild, addChild, override);
                } else {
                    addConfigurations((ConfigurationSection)existingValue, addChild, override);
                }
            } else if (override || existingValue == null) {
                first.set(key, value);
            }
        }

        return first;
    }

    public static ConfigurationSection replaceConfigurations(ConfigurationSection first, ConfigurationSection second)
    {
        if (second == null) return first;
        Map<String, Object> map = NMSUtils.getMap(second);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            first.set(entry.getKey(), entry.getValue());
        }

        return first;
    }

    public static ConfigurationSection overlayConfigurations(ConfigurationSection first, ConfigurationSection second)
    {
        if (second == null) return first;
        Map<String, Object> map = NMSUtils.getMap(second);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first.contains(entry.getKey())) {
                first.set(entry.getKey(), entry.getValue());
            }
        }

        return first;
    }

    protected static double parseDouble(String s)
    {
        if (s == null || s.isEmpty()) return 0;
        char firstChar = s.charAt(0);
        if (firstChar == 'r' || firstChar == 'R')
        {
            String[] pieces = StringUtils.split(s, "(,)");
            try {
                double min = Double.parseDouble(pieces[1].trim());
                double max = Double.parseDouble(pieces[2].trim());
                return random.nextDouble() * (max - min) + min;
            } catch (Exception ex) {
                Bukkit.getLogger().warning("Failed to parse: " + s);
                ex.printStackTrace();
            }
        }

        try {
            return Double.parseDouble(s);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("Failed to parse as double: " + s);
            ex.printStackTrace();
        }

        return 0;
    }

    protected static int parseInt(String s)
    {
        if (s == null || s.isEmpty()) return 0;
        char firstChar = s.charAt(0);
        if (firstChar == 'r' || firstChar == 'R')
        {
            String[] pieces = StringUtils.split(s, "(,)");
            try {
                double min = Double.parseDouble(pieces[1].trim());
                double max = Double.parseDouble(pieces[2].trim());
                return (int)Math.floor(random.nextDouble() * (max - min) + min);
            } catch (Exception ex) {
                Bukkit.getLogger().warning("Failed to parse: " + s);
                ex.printStackTrace();
            }
        }

        try {
            return (int)Math.floor(Double.parseDouble(s));
        } catch (Exception ex) {
            Bukkit.getLogger().warning("Failed to parse as int: " + s);
            ex.printStackTrace();
        }

        return 0;
    }

    public static double overrideDouble(String override, double value)
    {
        if (override == null || override.length() == 0) return value;
        try {
            if (override.startsWith("~")) {
                override = override.substring(1);
                value = value + parseDouble(override);
            } else if (override.startsWith("*")) {
                override = override.substring(1);
                value = value * parseDouble(override);
            } else {
                value = parseDouble(override);
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }

        return value;
    }

    protected static double overrideDouble(ConfigurationSection node, double value, String nodeName)
    {
        String override = node.getString(nodeName);
        return overrideDouble(override, value);
    }

    @Nullable
    public static World overrideWorld(ConfigurationSection node, String path, World world, boolean canCreateWorlds) {
        return overrideWorld(node.getString(path), world, canCreateWorlds);
    }

    @Nullable
    public static World overrideWorld(String worldName, World world, boolean canCreateWorlds) {
        if (worldName == null || worldName.length() == 0) return null;

        if (worldName.charAt(0) == '~') {
            if (world == null) return null;

            String baseWorld = world.getName();
            worldName = worldName.substring(1);
            worldName = worldName.trim();
            if (worldName.charAt(0) == '-') {
                worldName = worldName.substring(1);
                worldName = worldName.trim();
                worldName = baseWorld.replace(worldName, "");
            } else {
                worldName = baseWorld + worldName;
            }
        }

        World worldOverride = Bukkit.getWorld(worldName);
        if (worldOverride == null) {
            if (canCreateWorlds && world != null) {
                Bukkit.getLogger().info("Creating/Loading world: " + worldName);
                worldOverride = Bukkit.createWorld(new WorldCreator(worldName).copy(world));
                if (worldOverride == null) {
                    Bukkit.getLogger().warning("Failed to load world: " + worldName);
                    return null;
                }
            } else {
                Bukkit.getLogger().warning("Could not load world: " + worldName);
                return null;
            }
        }

        return worldOverride;
    }

    @Nullable
    public static Location overrideLocation(ConfigurationSection node, String basePath, Location location, boolean canCreateWorlds) {
        String xName = basePath + "x";
        String yName = basePath + "y";
        String zName = basePath + "z";
        String dxName = basePath + "dx";
        String dyName = basePath + "dy";
        String dzName = basePath + "dz";
        boolean hasPosition = node.contains(xName) || node.contains(yName) || node.contains(zName);
        boolean hasDirection = node.contains(dxName) || node.contains(dyName) || node.contains(dzName);

        World baseWorld = location == null ? null : location.getWorld();
        World worldOverride = overrideWorld(node, basePath + "world", baseWorld, canCreateWorlds);

        if (!hasPosition && !hasDirection && worldOverride == null) return null;

        if (location == null) {
            if (worldOverride == null) return null;
            location = new Location(worldOverride, 0, 0, 0);
        } else {
            location = location.clone();
            if (worldOverride != null) {
                location.setWorld(worldOverride);
            }
        }
        if (hasPosition) {
            location.setX(overrideDouble(node, location.getX(), xName));
            location.setY(overrideDouble(node, location.getY(), yName));
            location.setZ(overrideDouble(node, location.getZ(), zName));
        }

        if (hasDirection) {
            Vector direction = location.getDirection();
            direction.setX(overrideDouble(node, direction.getX(), dxName));
            direction.setY(overrideDouble(node, direction.getY(), dyName));
            direction.setZ(overrideDouble(node, direction.getZ(), dzName));

            location.setDirection(direction);
        }

        return location;
    }

    public static Color getColor(ConfigurationSection node, String path, Color def) {
        Color o = toColor(node.get(path));
        return o == null ? def : o;
    }

    @Nullable
    public static Color toColor(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Byte) {
            return Color.fromRGB((Byte) o);
        } else if (o instanceof Integer) {
            return Color.fromRGB((Integer) o);
        } else if (o instanceof Double) {
            return Color.fromRGB((int) (double) (Double) o);
        } else if (o instanceof Float) {
            return Color.fromRGB((int) (float) (Float) o);
        } else if (o instanceof Long) {
            return Color.fromRGB((int) (long) (Long) o);
        } else if (o instanceof String) {
            try {
                String s = (String)o;
                if (s.length() == 0) return null;
                if (s.charAt(0) == '#') {
                    s = s.substring(1, s.length());
                }
                Integer rgb = Integer.parseInt(s, 16);
                return Color.fromRGB(rgb);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        return null;
    }

    public static Integer getInteger(ConfigurationSection node, String path, Integer def)
    {
        if (node.contains(path)) {
            return parseInt(node.getString(path));
        }
        return def;
    }

    public static Double getDouble(ConfigurationSection node, String path, Double def)
    {
        if (node.contains(path)) {
            return parseDouble(node.getString(path));
        }
        return def;
    }

    public static Boolean getBoolean(ConfigurationSection node, String path, Boolean def)
    {
        if (node.contains(path)) return node.getBoolean(path);
        return def;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static List<Object> getList(ConfigurationSection section, String path) {
        List<Object> list = (List<Object>)section.getList(path);
        if (list != null) {
            return list;
        }
        Object o = section.get(path);
        return getList(o);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static List<Object> getList(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof List) {
            return (List<Object>) o;
        } else if (o instanceof String) {
            return new ArrayList<>(Arrays.asList(StringUtils.split((String) o, ',')));
        } else {
            List<Object> single = new ArrayList<>();
            single.add(o);
            return single;
        }
    }

    public static void addIfNotEmpty(String message, Collection<String> list) {
        if (message != null && !message.isEmpty()) {
            list.add(message);
        }
    }

    public static List<String> getStringList(ConfigurationSection section, String path, List<String> def) {
        List<String> list = getStringList(section, path);
        return list == null ? (def == null ? new ArrayList<>() : def) : list;
    }

    @Nullable
    public static List<String> getStringList(ConfigurationSection section, String path) {
        List<Object> raw = getList(section, path);
        return getStringList(raw);
    }

    @Nullable
    public static List<String> getStringList(Object o) {
        List<Object> raw = getList(o);
        return getStringList(raw);
    }

    @Nullable
    public static List<String> getStringList(List<Object> rawList) {
        if (rawList == null) {
            return null;
        }

        List<String> list = new ArrayList<>();

        for (Object o : rawList) {
            if (o == null) {
                continue;
            }

            list.add(o.toString());
        }

        return list;
    }

     /**
      * Gets a list of integers. Non-valid entries will not be in the list.
      * There will be no null slots. If the list is not defined, the
      * default will be returned. 'null' can be passed for the default
      * and an empty list will be returned instead. The node must be
      * an actual list and not just an integer.
      *
      * @param section the ConfigurationSection to load the list from
      * @param path path to node (dot notation)
      */
     public static List<Integer> getIntegerList(ConfigurationSection section, String path) {
         List<Object> raw = getList(section, path);

         if (raw == null) {
             return new ArrayList<>();
         }
         List<Integer> list = new ArrayList<>();

         for (Object o : raw) {
             Integer i = castInt(o);
             if (i != null) {
                 list.add(i);
             }
         }

         return list;
     }

     /**
      * Casts a value to an integer. May return null.
      *
      * @param o the object to cast
      * @return an Integer, or null on failure
      */
     @Nullable
     private static Integer castInt(Object o) {
         if (o == null) {
             return null;
         } else if (o instanceof Byte) {
             return (int) (Byte) o;
         } else if (o instanceof Integer) {
             return (Integer) o;
         } else if (o instanceof Double) {
             return (int) (double) (Double) o;
         } else if (o instanceof Float) {
             return (int) (float) (Float) o;
         } else if (o instanceof Long) {
             return (int) (long) (Long) o;
         } else if (o instanceof String) {
             try {
                 return Integer.parseInt((String)o);
             } catch (NumberFormatException ex) {
                 return null;
             }
         } else {
             return null;
         }
     }

    public static void addParameters(String[] extraParameters, ConfigurationSection parameters)
    {
        if (extraParameters != null)
        {
            for (int i = 0; i < extraParameters.length - 1; i += 2)
            {
                if (extraParameters[i] == null || extraParameters[i].isEmpty()) continue;
                set(parameters, extraParameters[i], extraParameters[i + 1]);
            }
        }
    }

    public static String getParameters(ConfigurationSection parameters) {
        Collection<String> parameterStrings = new ArrayList<>();
        Map<String, Object> map = NMSUtils.getMap(parameters);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            parameterStrings.add(entry.getKey());
            parameterStrings.add(entry.getValue().toString());
        }
        return StringUtils.join(parameterStrings, ' ');
    }

    public static SoundEffect toSoundEffect(String soundConfig) {
        return new SoundEffect(soundConfig);
    }

    @Nullable
    public static Particle toParticleEffect(String effectParticleName) {
        Particle effectParticle = null;
        if (effectParticleName.length() > 0) {
            String particleName = effectParticleName.toUpperCase();
            try {
                effectParticle = Particle.valueOf(particleName);
            } catch (Exception ignored) {
            }
        }

        return effectParticle;
    }

    public static Collection<String> getKeysOrList(@Nonnull ConfigurationSection node, @Nonnull String key) {
        Collection<String> values = null;
        if (node.isString(key)) {
            values = ConfigurationUtils.getStringList(node, key);
        } else if (node.isConfigurationSection(key)) {
            ConfigurationSection spellSection = node.getConfigurationSection(key);
            if (spellSection != null) {
                values = spellSection.getKeys(false);
            }
        }
        if (values == null) {
            values = new ArrayList<>(0);
        }
        return values;
    }


    public static Collection<PrerequisiteSpell> getPrerequisiteSpells(MageController controller, ConfigurationSection node, String key, String loadContext, boolean removeMissing) {
        if (node == null || key == null) {
            return new ArrayList<>(0);
        }

        Collection<?> spells = null;
        if (node.isString(key)) {
            spells = ConfigurationUtils.getStringList(node, key);
        } else if (node.isConfigurationSection(key)) {
            ConfigurationSection spellSection = node.getConfigurationSection(key);
            if (spellSection != null) {
                spells = spellSection.getKeys(false);
            }
        } else {
            spells = node.getList(key);
        }
        if (spells == null) {
            spells = new ArrayList<>(0);
        }

        List<PrerequisiteSpell> requiredSpells = new ArrayList<>(spells.size());
        for (Object o : spells) {
            PrerequisiteSpell prerequisiteSpell = null;
            if (o instanceof String) {
                prerequisiteSpell = new PrerequisiteSpell(new SpellKey((String) o), 0);
            } else if (o instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) o;
                String spell = section.getString("spell");
                long progressLevel = section.getLong("progress_level");
                prerequisiteSpell = new PrerequisiteSpell(new SpellKey(spell), progressLevel);
            } else if (o instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) o;
                String spell = map.get("spell").toString();
                String progressLevelString = map.containsKey("progress_level") ? map.get("progress_level").toString() : "0";
                if (spell != null && StringUtils.isNumeric(progressLevelString)) {
                    long progressLevel = 0;
                    try {
                        progressLevel = Long.parseLong(progressLevelString);
                    } catch (NumberFormatException ignore) { }
                    prerequisiteSpell = new PrerequisiteSpell(new SpellKey(spell), progressLevel);
                }
            }

            if (prerequisiteSpell != null) {
                if (controller.getSpellTemplate(prerequisiteSpell.getSpellKey().getKey()) != null) {
                    requiredSpells.add(prerequisiteSpell);
                } else {
                    if (!removeMissing) {
                        requiredSpells.add(prerequisiteSpell);
                        controller.getLogger().warning("Unknown or disabled spell requirement " + prerequisiteSpell.getSpellKey().getKey() + " in " + loadContext + ", upgrade will be disabled");
                    } else {
                        controller.getLogger().warning("Unknown or disabled spell prerequisite " + prerequisiteSpell.getSpellKey().getKey() + " in " + loadContext + ", ignoring");
                    }
                }
            }
        }

        return requiredSpells;
    }

    @Nullable
    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection effectConfig) {
        return getPotionEffects(effectConfig, null);
    }

    @Nullable
    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection effectConfig, Integer duration) {
        return getPotionEffects(effectConfig, duration, true, true);
    }

    @Nullable
    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection effectConfig, Integer duration, boolean ambient, boolean particles) {
        if (effectConfig == null) return null;
        List<PotionEffect> effects = new ArrayList<>();
        Set<String> keys = effectConfig.getKeys(false);
        if (keys.isEmpty()) return null;

        for (String key : keys) {
            String value = effectConfig.getString(key);
            try {
                PotionEffectType effectType = PotionEffectType.getByName(key.toUpperCase());

                int ticks = 10;
                int power = 1;
                if (value.contains(",")) {
                    String[] pieces = StringUtils.split(value, ',');
                    ticks = (int)Float.parseFloat(pieces[0]);
                    power = (int)Float.parseFloat(pieces[1]);
                } else {
                    power = (int)Float.parseFloat(value);
                    if (duration != null) {
                        ticks = duration / 50;
                    }
                }

                PotionEffect effect = new PotionEffect(effectType, ticks, power, ambient, particles);
                effects.add(effect);

            } catch (Exception ex) {
                Bukkit.getLogger().warning("Error parsing potion effect for " + key + ": " + value);
            }
        }
        return effects;
    }

    @Nullable
    public static List<PotionEffect> getPotionEffectObjects(ConfigurationSection baseConfig, String key, Logger log) {
        List<PotionEffect> potionEffects = null;
        Collection<ConfigurationSection> potionEffectList = getNodeList(baseConfig, key);
        if (potionEffectList != null) {
            potionEffects = new ArrayList<>();
            for (ConfigurationSection potionEffectSection : potionEffectList) {
                try {
                    PotionEffectType effectType = PotionEffectType.getByName(potionEffectSection.getString("type").toUpperCase());
                    if (effectType == null) {
                        log.log(Level.WARNING, "Invalid potion effect type: " + potionEffectSection.getString("type", "(null)"));
                        continue;
                    }
                    int ticks = (int) (potionEffectSection.getLong("duration", 3600000) / 50);
                    ticks = potionEffectSection.getInt("ticks", ticks);
                    int amplifier = potionEffectSection.getInt("amplifier", 0);
                    boolean ambient = potionEffectSection.getBoolean("ambient", true);
                    boolean particles = potionEffectSection.getBoolean("particles", true);

                    potionEffects.add(new PotionEffect(effectType, ticks, amplifier, ambient, particles));
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Invalid potion effect type: " + potionEffectSection.getString("type", "(null)"), ex);
                }
            }
        }
        return potionEffects;
    }

    public static ConfigurationSection subtractConfiguration(ConfigurationSection base, ConfigurationSection subtract) {
         Set<String> keys = subtract.getKeys(false);
         for (String key : keys) {
             Object baseObject = base.get(key);
             if (baseObject == null) continue;
             Object subtractObject = subtract.get(key);
             if (subtractObject == null) continue;
             if (subtractObject instanceof ConfigurationSection && baseObject instanceof ConfigurationSection) {
                 ConfigurationSection baseConfig = (ConfigurationSection)baseObject;
                 ConfigurationSection subtractConfig = (ConfigurationSection)subtractObject;
                 baseConfig = subtractConfiguration(baseConfig, subtractConfig);
                 if (!baseConfig.getKeys(false).isEmpty()) continue;
             } else if (!subtractObject.equals(baseObject)) continue;
             base.set(key, null);
         }

         return base;
    }
}
