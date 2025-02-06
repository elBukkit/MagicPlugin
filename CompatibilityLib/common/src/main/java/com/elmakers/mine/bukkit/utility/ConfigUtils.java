package com.elmakers.mine.bukkit.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockVector;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.data.SerializedLocation;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.configuration.TranslatingConfigurationSection;

/**
 * This was originally part of EffectLib, but I wanted to make an independent copy to use for Magic.
 */
public class ConfigUtils {

    public static Random random = new Random();

    public static List<ConfigurationSection> getNodeList(ConfigurationSection node, String path) {
        List<ConfigurationSection> results = new ArrayList<>();
        List<Map<?, ?>> mapList = node.getMapList(path);
        for (Map<?, ?> map : mapList) {
            results.add(toConfigurationSection(node, map));
        }

        return results;
    }

    public static ConfigurationSection newSection(ConfigurationSection parent) {
        return newSection(parent, "");
    }

    public static ConfigurationSection newSection(ConfigurationSection parent, String path) {
        if (parent instanceof TranslatingConfigurationSection) {
            return ((TranslatingConfigurationSection)parent).newSection(path);
        }
        return new TranslatingConfigurationSection(parent, path);
    }

    public static ConfigurationSection toConfigurationSection(ConfigurationSection parent, Map<?, ?> nodeMap) {
        return toConfigurationSection(parent, "", nodeMap);
    }

    public static ConfigurationSection toConfigurationSection(ConfigurationSection parent, String path, Map<?, ?> nodeMap) {
        ConfigurationSection newSection = newSection(parent, path);
        for (Map.Entry<?, ?> entry : nodeMap.entrySet()) {
            newSection.set(entry.getKey().toString(), entry.getValue());
        }
        return newSection;
    }

    public static ConfigurationSection convertConfigurationSection(Map<?, ?> nodeMap) {
        ConfigurationSection newSection = new MemoryConfiguration();
        for (Map.Entry<?, ?> entry : nodeMap.entrySet()) {
            set(newSection, entry.getKey().toString(), entry.getValue());
        }

        return newSection;
    }

    public static ConfigurationSection toStringConfiguration(Map<String, String> stringMap) {
        if (stringMap == null) return null;

        ConfigurationSection configMap = new MemoryConfiguration();
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            configMap.set(entry.getKey(), entry.getValue());
        }

        return configMap;
    }


    public static void set(ConfigurationSection node, String path, Object value) {
        if (value == null) {
            node.set(path, null);
            return;
        }

        boolean isTrue = value.equals("true");
        boolean isFalse = value.equals("false");
        if (isTrue || isFalse) {
            node.set(path, isTrue);
            return;
        }
        try {
            Integer i = (value instanceof Integer) ? (Integer) value : Integer.parseInt(value.toString());
            node.set(path, i);
        } catch (Exception ex) {
            try {
                double d;
                if (value instanceof Double) d = (Double) value;
                else if (value instanceof Float) d = (Float) value;
                else d = Double.parseDouble(value.toString());
                node.set(path, d);
            } catch (Exception ex2) {
                node.set(path, value);
            }
        }
    }

    public static ConfigurationSection getConfigurationSection(ConfigurationSection base, String key) {
        ConfigurationSection section = base.getConfigurationSection(key);
        if (section != null) return section;

        Object value = base.get(key);
        if (value == null) return null;

        if (value instanceof ConfigurationSection) return (ConfigurationSection)value;

        if (value instanceof Map) {
            ConfigurationSection newChild = base.createSection(key);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                newChild.set(entry.getKey(), entry.getValue());
            }
            base.set(key, newChild);
            return newChild;
        }

        return null;
    }

    public static boolean isMaxValue(String stringValue) {
        if (stringValue == null) return false;
        return stringValue.equalsIgnoreCase("infinite")
                || stringValue.equalsIgnoreCase("forever")
                || stringValue.equalsIgnoreCase("infinity")
                || stringValue.equalsIgnoreCase("max");
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
    public static List<String> getStringList(ConfigurationSection section, String path, String delimiter) {
        if (section.isList(path)) {
            List<Object> raw = getList(section, path);
            return getStringList(raw);
        }
        String value = section.getString(path);
        if (value == null) {
            return null;
        }
        String[] pieces = StringUtils.split(value, delimiter);
        return Arrays.asList(pieces);
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
            // This prevents weird behaviors when we're expecting a list of strings but given
            // a list of ConfigurationSections, Lists or other complex types
            if (o == null || o instanceof ConfigurationSection || o instanceof List || o instanceof Map) {
                continue;
            }
            list.add(o.toString());
        }

        return list;
    }

    @Nullable
    public static List<PotionEffect> getPotionEffectObjects(ConfigurationSection baseConfig, String key, Logger log) {
        return getPotionEffectObjects(baseConfig, key, log, Integer.MAX_VALUE, 0, true, false);
    }

    @Nullable
    public static List<PotionEffect> getPotionEffectObjects(ConfigurationSection baseConfig, String key, Logger log, int defaultDuration) {
        return getPotionEffectObjects(baseConfig, key, log, defaultDuration, 0, true, true);
    }

    @Nullable
    public static List<PotionEffect> getPotionEffectObjects(ConfigurationSection baseConfig, String key, Logger log, int defaultDuration, int defaultAmplifier, boolean defaultAmbient, boolean defaultParticles) {
        List<PotionEffect> potionEffects = null;
        List<Object> genericList = getList(baseConfig, key);
        if (genericList != null && !genericList.isEmpty()) {
            potionEffects = new ArrayList<>();
            for (Object genericEntry : genericList) {
                if (genericEntry instanceof String) {
                    String typeString = (String)genericEntry;
                    PotionEffectType effectType = PotionEffectType.getByName(typeString.toUpperCase());
                    if (effectType == null) {
                        log.log(Level.WARNING, "Invalid potion effect type: " + typeString);
                        continue;
                    }
                    int ticks = defaultDuration / 50;
                    potionEffects.add(new PotionEffect(effectType, effectType.isInstant() ? 1 : ticks, defaultAmplifier, defaultAmbient, defaultParticles));
                } else {
                    ConfigurationSection potionEffectSection = genericEntry instanceof ConfigurationSection ? (ConfigurationSection)genericEntry : null;
                    if (potionEffectSection == null && genericEntry instanceof Map) {
                        potionEffectSection = toConfigurationSection(baseConfig, (Map<?, ?>)genericEntry);
                    }
                    if (potionEffectSection != null) {
                        if (potionEffectSection.contains("type")) {
                            PotionEffectType effectType = PotionEffectType.getByName(potionEffectSection.getString("type").toUpperCase());
                            if (effectType == null) {
                                log.log(Level.WARNING, "Invalid potion effect type: " + potionEffectSection.getString("type", "(null)"));
                                continue;
                            }
                            int ticks = Integer.MAX_VALUE;
                            String duration = potionEffectSection.getString("duration");
                            if (duration == null || (!duration.equals("forever") && !duration.equals("infinite") && !duration.equals("infinity"))) {
                                ticks = (int) (potionEffectSection.getLong("duration", defaultDuration) / 50);
                                ticks = potionEffectSection.getInt("ticks", ticks);
                            }
                            int amplifier = potionEffectSection.getInt("amplifier", defaultAmplifier);
                            boolean ambient = potionEffectSection.getBoolean("ambient", defaultAmbient);
                            boolean particles = potionEffectSection.getBoolean("particles", defaultParticles);

                            potionEffects.add(new PotionEffect(effectType, effectType.isInstant() ? 1 : ticks, amplifier, ambient, particles));
                        } else {
                            Collection<String> types = potionEffectSection.getKeys(false);
                            for (String type : types) {
                                PotionEffectType effectType = PotionEffectType.getByName(type.toUpperCase());
                                if (effectType == null) {
                                    log.log(Level.WARNING, "Invalid potion effect type: " + type);
                                    continue;
                                }
                                int amplifier = potionEffectSection.getInt(type, defaultAmplifier);
                                int ticks = defaultDuration / 50;
                                potionEffects.add(new PotionEffect(effectType, effectType.isInstant() ? 1 : ticks, amplifier, defaultAmbient, defaultParticles));
                            }
                        }
                    }
                }
            }
        }
        return potionEffects;
    }

    @Nullable
    public static EulerAngle getEulerAngle(ConfigurationSection node, String path) {
        Vector vector = getVector(node, path, null);
        return vector == null ? null : new EulerAngle(vector.getX(), vector.getY(), vector.getZ());
    }

    @Nullable
    public static Vector getVector(ConfigurationSection node, String path) {
        return getVector(node, path, null);
    }

    @Nullable
    public static Vector getVector(ConfigurationSection node, String path, Vector def) {
        return getVector(node, path, def, null, null);
    }

    @Nullable
    public static Vector getVector(ConfigurationSection node, String path, Vector def, Logger logger, String logContext) {
        Object o = node.get(path, null);
        if (o == null) {
            return def;
        }

        Vector result = toVector(o);
        if (result == null && logger != null) {
            if (logContext == null) {
                logContext = "unknown";
            }
            logger.warning("Invalid vector in " + logContext + ": " + o);
        }
        return result;
    }

    @Nullable
    public static Vector toVector(Object o) {
        if (o instanceof Vector) {
            return (Vector)o;
        }
        if (o instanceof ConfigurationSection) {
            ConfigurationSection config = (ConfigurationSection)o;
            return new Vector(config.getDouble("x"), config.getDouble("y"), config.getDouble("z"));
        }
        if (o instanceof String) {
            try {
                String parse = (String)o;
                if (parse.isEmpty()) return null;
                // rand() coordinates can not use commas as the delimiter
                if (!parse.contains("r") && !parse.contains("R") && parse.contains(",")) {
                    parse = parse.replace(" ", "");
                    parse = parse.replace(",", " ");
                }
                if (parse.contains("|")) {
                    parse = parse.replace(" ", "");
                    parse = parse.replace("|", " ");
                }
                String[] pieces = StringUtils.split(parse, ' ');
                if (pieces.length < 3) return null;
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

    protected static double parseDouble(String s) throws NumberFormatException
    {
        if (s == null || s.isEmpty()) return 0;
        char firstChar = s.charAt(0);
        if (firstChar == 'r' || firstChar == 'R')
        {
            String[] pieces = StringUtils.split(s, "(,)");
            double min = Double.parseDouble(pieces[1].trim());
            double max = Double.parseDouble(pieces[2].trim());
            return random.nextDouble() * (max - min) + min;
        }

        return Double.parseDouble(s);
    }

    @Nullable
    public static Location getLocation(ConfigurationSection node, String path) {
        String stringData = node.getString(path);
        if (stringData == null) {
            return null;
        }

        return toLocation(stringData);
    }

    @Nullable
    public static SerializedLocation getSerializedLocation(ConfigurationSection node, String path) {
        String stringData = node.getString(path);
        if (stringData == null) {
            return null;
        }

        return toSerializedLocation(stringData);
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

    /**
     * Serialises a location as a relative offset to another location.
     *
     * <p>One of the following formats may be used, depending on the
     * availability of the locations, whether or not they are in the same
     * world and whether or not the pitch and the yaw are the same.
     *
     * <ul>
     * <li>{@code ""}, the empty string, when no location was specified, or the
     * center location was not valid.
     * <li>{@code "x,y,z"}, offsets relative to the center, with the same
     * orientation.
     * <li>{@code "x,y,z,yaw,pitch"}, offset relative to the center, with
     * absolute rotations.
     * <li>{@code "x,y,z,world,yaw,pitch"}, an absolute location an orientation
     * </ul>
     *
     * @param location The location to serialise.
     * @param relativeTo The center to make the location relative to.
     * @return The string representation.
     */
    @Nonnull
    public static String fromLocation(
            @Nullable Location location,
            @Nullable Location relativeTo) {
        if (location == null || location.getWorld() == null) {
            // Invalid location
            return "";
        } else if (relativeTo != null && relativeTo.getWorld() == null) {
            // Invalid center
            // FIXME: Shouldn't we just return a non-relative location?
            return "";
        } else if (relativeTo == null
                || !relativeTo.getWorld().equals(location.getWorld())) {
            // No relative, or they are not in the same world
            return fromLocation(location);
        }

        // Make location relative to relativeTo
        location = location.clone();
        location.subtract(relativeTo);

        String serialized = location.getX() + "," + location.getY() + "," + location.getZ();
        if (location.getPitch() != relativeTo.getPitch() || location.getYaw() != relativeTo.getYaw()) {
            serialized += "," + location.getYaw() + "," + location.getPitch();
        }
        return serialized;
    }

    @Nonnull
    public static String fromLocation(@Nullable Location location) {
        if (location == null) return "";
        if (location.getWorld() == null) return "";
        return location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorld().getName()
                + "," + location.getYaw() + "," + location.getPitch();
    }

    public static String fromSerializedLocation(SerializedLocation location) {
        if (location == null) return "";
        if (location.getWorld() == null) return "";
        return location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorldName()
                + "," + location.getYaw() + "," + location.getPitch();
    }

    @Nullable
    public static String getWorldName(String descriptor) {
        if (descriptor == null) return null;
        String[] pieces = StringUtils.split(descriptor, ',');
        return pieces.length > 3 ? pieces[3] : null;
    }

    @Nullable
    public static Location toLocation(Object o) {
        return toLocation(o, null);
    }

    @Nullable
    public static Location toLocation(Object o, Location relativeTo) {
        if (o instanceof Location) {
            return (Location)o;
        }
        SerializedLocation location = toSerializedLocation(o, relativeTo);
        return location == null ? null : location.asLocation();
    }

    @Nullable
    public static SerializedLocation toSerializedLocation(Object o) {
        return toSerializedLocation(o, null);
    }

    @Nullable
    public static SerializedLocation toSerializedLocation(Object o, Location relativeTo) {
        if (o instanceof SerializedLocation) {
            return (SerializedLocation)o;
        }
        if (o instanceof String) {
            try {
                float pitch = 0;
                float yaw = 0;
                String[] pieces = StringUtils.split((String)o, ',');
                double x = parseDouble(pieces[0]);
                double y = parseDouble(pieces[1]);
                double z = parseDouble(pieces[2]);
                String world;
                if (relativeTo != null && (pieces.length == 3 || pieces.length == 5)) {
                    world = relativeTo.getWorld().getName();
                    x += relativeTo.getX();
                    y += relativeTo.getY();
                    z += relativeTo.getZ();
                    if (pieces.length == 5) {
                        yaw = Float.parseFloat(pieces[3]);
                        pitch = Float.parseFloat(pieces[4]);
                    } else {
                        yaw = relativeTo.getYaw();
                        pitch = relativeTo.getPitch();
                    }
                } else {
                    if (pieces.length > 3) {
                        world = pieces[3];
                    } else {
                        world = Bukkit.getWorlds().get(0).getName();
                    }
                    if (pieces.length > 5) {
                        yaw = Float.parseFloat(pieces[4]);
                        pitch = Float.parseFloat(pieces[5]);
                    }
                }
                return new SerializedLocation(world, new BlockVector(x, y, z), yaw, pitch);
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    protected static int parseInt(String s) throws NumberFormatException
    {
        if (s == null || s.isEmpty()) return 0;
        char firstChar = s.charAt(0);
        if (firstChar == 'r' || firstChar == 'R')
        {
            String[] pieces = StringUtils.split(s, "(,)");
            double min = Double.parseDouble(pieces[1].trim());
            double max = Double.parseDouble(pieces[2].trim());
            return (int)Math.floor(random.nextDouble() * (max - min) + min);
        }

        return (int)Math.floor(Double.parseDouble(s));
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
    public static World overrideWorld(MageController controller, ConfigurationSection node, String path, World world, boolean canCreateWorlds) {
        return overrideWorld(controller, node.getString(path), world, canCreateWorlds);
    }

    @Nullable
    public static World overrideWorld(MageController controller, String worldName, World world, boolean canCreateWorlds) {
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
                worldOverride = controller.copyWorld(worldName, world);
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
    public static Location overrideLocation(MageController controller, ConfigurationSection node, String basePath, Location location, boolean canCreateWorlds) {
        String xName = basePath + "x";
        String yName = basePath + "y";
        String zName = basePath + "z";
        String dxName = basePath + "dx";
        String dyName = basePath + "dy";
        String dzName = basePath + "dz";
        boolean hasPosition = node.contains(xName) || node.contains(yName) || node.contains(zName);
        boolean hasDirection = node.contains(dxName) || node.contains(dyName) || node.contains(dzName);

        World baseWorld = location == null ? null : location.getWorld();
        World worldOverride = overrideWorld(controller, node, basePath + "world", baseWorld, canCreateWorlds);

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
                    s = s.substring(1);
                }
                if (s.startsWith("rand")) {
                    return Color.fromRGB(random.nextInt(16777216));
                }
                int rgb = Integer.parseInt(s, 16);
                return Color.fromRGB(rgb);
            } catch (NumberFormatException ex) {
                return null;
            }
        } else if (o instanceof ConfigurationSection) {
            ConfigurationSection config = (ConfigurationSection)o;
            ColorHD color = new ColorHD(config);
            return color.getColor();
        }

        return null;
    }

    public static Integer getInteger(ConfigurationSection node, String path, Integer def)
    {
        if (node.contains(path)) {
            String strVal = node.getString(path);
            try {
                return parseInt(strVal);
            } catch (NumberFormatException ex) {
                Bukkit.getLogger().warning("Failed to parse as integer: " + strVal);
            }
        }
        return def;
    }

    public static Double getDouble(ConfigurationSection node, String path, Double def)
    {
        if (node.contains(path)) {
            String strVal = node.getString(path);
            try {
                return parseDouble(strVal);
            } catch (NumberFormatException ex) {
                Bukkit.getLogger().warning("Failed to parse as number: " + strVal);
            }
        }
        return def;
    }

    public static Boolean getBoolean(ConfigurationSection node, String path, Boolean def)
    {
        if (node.contains(path)) return node.getBoolean(path);
        return def;
    }

    public static void addIfNotEmpty(String message, Collection<String> list) {
        if (message != null && !message.isEmpty()) {
            list.add(message);
        }
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

    @Nullable
    public static Boolean getOptionalBoolean(ConfigurationSection configuration, String key, Boolean def) {
        return configuration.contains(key) ? (Boolean)configuration.getBoolean(key) : def;
    }

    @Nullable
    public static Boolean getOptionalBoolean(ConfigurationSection configuration, String key) {
        return getOptionalBoolean(configuration, key, null);
    }

    @Nullable
    public static Integer getOptionalInteger(ConfigurationSection configuration, String key, Integer def) {
        return configuration.contains(key) ? (Integer)configuration.getInt(key) : def;
    }

    @Nullable
    public static Integer getOptionalInteger(ConfigurationSection configuration, String key) {
        return getOptionalInteger(configuration, key, null);
    }

    @Nullable
    public static Double getOptionalDouble(ConfigurationSection configuration, String key) {
        return configuration.contains(key) ? configuration.getDouble(key) : null;
    }

    @Nullable
    public static Float getOptionalFloat(ConfigurationSection configuration, String key) {
        return configuration.contains(key) ? (float)configuration.getDouble(key) : null;
    }

    // This is here to replace the more efficient but broken by 1.18 CompatibilityUtils.getMap
    @Nonnull
    public static Map<String, Object> toMap(ConfigurationSection section) {
        return toTypedMap(section);
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> toTypedMap(ConfigurationSection section) {
        Map<String, T> map = new LinkedHashMap<>();
        Set<String> keys = section.getKeys(false);
        for (String key : keys) {
            map.put(key, (T)section.get(key));
        }

        return map;
    }
}
