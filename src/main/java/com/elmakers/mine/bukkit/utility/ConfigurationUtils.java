package com.elmakers.mine.bukkit.utility;

import java.util.*;
import java.util.Map.Entry;

import de.slikey.effectlib.util.ParticleEffect;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.block.MaterialAndData;

public class ConfigurationUtils {

    public static Random random = new Random();

    public static Location getLocation(ConfigurationSection node, String path) {
        String stringData = node.getString(path);
        if (stringData == null) {
            return null;
        }

        return toLocation(stringData);
    }

    public static BlockFace toBlockFace(String s) {
        BlockFace face = null;
        try {
            face = BlockFace.valueOf(s.toUpperCase());
        } catch(Exception ex) {
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

    public static Vector getVector(ConfigurationSection node, String path) {
       return getVector(node, path, null);
    }

    public static Vector getVector(ConfigurationSection node, String path, Vector def) {
        String stringData = node.getString(path, null);
        if (stringData == null) {
            return def;
        }

        return toVector(stringData);
    }

    public static Material getMaterial(ConfigurationSection node, String path, Material def) {
        String stringData = node.getString(path);
        if (stringData == null) {
            return def;
        }

        return toMaterial(stringData);
    }

    public static MaterialAndData getMaterialAndData(ConfigurationSection node, String path) {
        return getMaterialAndData(node, path, null);
    }

    public static MaterialAndData getMaterialAndData(ConfigurationSection node, String path, MaterialAndData def) {
        String stringData = node.getString(path);
        if (stringData == null) {
            return def;
        }

        return toMaterialAndData(stringData);
    }

    public static Material getMaterial(ConfigurationSection node, String path) {
        return getMaterial(node, path, null);
    }

    public static Collection<ConfigurationSection> getNodeList(ConfigurationSection node, String path) {
        Collection<ConfigurationSection> results = new ArrayList<ConfigurationSection>();
        List<Map<?, ?>> mapList = node.getMapList(path);
        for (Map<?, ?> map : mapList) {
            results.add(toNodeList(map));
        }

        return results;
    }

    public static ConfigurationSection toNodeList(Map<?, ?> nodeMap) {
        ConfigurationSection newSection = new MemoryConfiguration();
        for (Entry<?, ?> entry : nodeMap.entrySet()) {
            set(newSection, entry.getKey().toString(), entry.getValue());
        }

        return newSection;
    }

    public static String fromLocation(Location location) {
        if (location == null) return "";
        if (location.getWorld() == null) return "";
        return location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorld().getName()
                + "," + location.getYaw() + "," + location.getPitch();
    }

    public static String fromMaterial(Material material)
    {
        return material.name().toLowerCase();
    }

    @SuppressWarnings("deprecation")
    public static String fromBlock(Block block)
    {
        return fromLocation(block.getLocation()) + "|" + block.getTypeId() + ":" + block.getData();
    }

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
            } catch(Exception ex) {
                return null;
            }
        }
        return null;
    }

    public static Vector toVector(Object o) {
        if (o instanceof Vector) {
            return (Vector)o;
        }
        if (o instanceof String) {
            try {
                String[] pieces = StringUtils.split((String)o, ',');
                double x = Double.parseDouble(pieces[0]);
                double y = Double.parseDouble(pieces[1]);
                double z = Double.parseDouble(pieces[2]);
                return new Vector(x, y, z);
            } catch(Exception ex) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static Material toMaterial(Object o)
    {
        if (o instanceof Material) {
            return (Material)o;
        }
        if (o instanceof Integer) {
            return Material.values()[(Integer)o];
        }
        if (o instanceof String) {
            String matName = (String)o;
            try
            {
                Integer value = Integer.parseInt(matName);
                if (value != null)
                {
                    return Material.getMaterial(value);
                }
            }
            catch(NumberFormatException ex)
            {

            }
            return Material.getMaterial(matName.toUpperCase());
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public static MaterialAndData toMaterialAndData(Object o)
    {
        if (o instanceof MaterialAndData) {
            return (MaterialAndData)o;
        }
        if (o instanceof String) {
            String matName = (String)o;
            return new MaterialAndData(matName);
        }

        return null;
    }


    public static Set<Material> getMaterials(ConfigurationSection node, String key)
    {
         List<String> materialData = node.getStringList(key);
         if (materialData == null) {
             return null;
         }

         Set<Material> materials = new HashSet<Material>();
         for (String matName : materialData)
         {
             Material material = toMaterial(matName);
             if (material != null) {
                 materials.add(material);
             }
         }

         return materials;
    }

    public static Set<Material> parseMaterials(String csv)
    {
         String[] nameList = StringUtils.split(csv, ',');
         Set<Material> materials = new HashSet<Material>();

         for (String matName : nameList)
         {
             Material material = toMaterial(matName);
             if (material != null) {
                 materials.add(material);
             }
         }

         return materials;
     }

    @SuppressWarnings("unchecked")
    protected void combine(Map<Object, Object> to, Map<? extends Object, Object> from) {
         for (Entry<? extends Object, Object> entry : from.entrySet()) {
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

    public static ConfigurationSection cloneConfiguration(ConfigurationSection section)
    {
        return addConfigurations(new MemoryConfiguration(), section);
    }


    public static ConfigurationSection addConfigurations(ConfigurationSection first, ConfigurationSection second)
    {
        return addConfigurations(first, second, true);
    }

    public static ConfigurationSection addConfigurations(ConfigurationSection first, ConfigurationSection second, boolean override)
    {
        if (second == null) return first;
        Set<String> keys = second.getKeys(false);
        for (String key : keys) {
            Object value = second.get(key);
            if (value == null) continue;

            Object existingValue = first.get(key);
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
        Set<String> keys = second.getKeys(false);
        for (String key : keys) {
            Object value = second.get(key);
            first.set(key, value);
        }

        return first;
    }

    protected static double parseDouble(String s)
    {
        char firstChar = s.charAt(0);
        if (firstChar == 'r' || firstChar == 'R')
        {
            String[] pieces = StringUtils.split(s, "(,)");
            try {
                double min = Double.parseDouble(pieces[1]);
                double max = Double.parseDouble(pieces[2]);
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

    protected static double overrideDouble(ConfigurationSection node, double value, String nodeName)
    {
        String override = node.getString(nodeName);
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

    public static World overrideWorld(ConfigurationSection node, String path, World world, boolean canCreateWorlds) {
        return overrideWorld(node.getString(path), world, canCreateWorlds);
    }

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

    @SuppressWarnings("deprecation")
    public static Location overrideLocation(ConfigurationSection node, String basePath, Location location, boolean canCreateWorlds)
    {
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
        if (node.contains(path)) return node.getInt(path);
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

    public static void set(ConfigurationSection node, String path, Object value)
    {
        // This is a bunch of hackery... I suppose I ought to change the NBT
        // types to match and make this smarter?
        boolean isTrue = value.equals("true");
        boolean isFalse = value.equals("false");
        if (isTrue || isFalse) {
            node.set(path, isTrue);
        } else {
            try {
                Double d = (value instanceof Double) ? (Double)value : (
                        (value instanceof Float) ? (Float)value :
                        Double.parseDouble(value.toString())
                    );
                node.set(path, d);
            } catch (Exception ex) {
                try {
                    Integer i = (value instanceof Integer) ? (Integer)value : Integer.parseInt(value.toString());
                    node.set(path, i);
                } catch (Exception ex2) {
                    node.set(path, value);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(ConfigurationSection section, String path) {
        Object o = section.get(path);
        if (o == null) {
            return null;
        } else if (o instanceof List) {
            return (List<Object>) o;
        } else if (o instanceof String) {
            return new ArrayList<Object>(Arrays.asList(StringUtils.split((String) o, ',')));
        } else {
            List<Object> single = new ArrayList<Object>();
            single.add(o);
            return single;
        }
    }

    public static List<String> getStringList(ConfigurationSection section, String path, List<String> def) {
        List<String> list = getStringList(section, path);
        return list == null ? (def == null ? new ArrayList<String>() : def) : list;
    }

    public static List<String> getStringList(ConfigurationSection section, String path) {
        List<Object> raw = getList(section, path);

        if (raw == null) {
            return null;
        }

        List<String> list = new ArrayList<String>();

        for (Object o : raw) {
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
             return new ArrayList<Integer>();
         }
         List<Integer> list = new ArrayList<Integer>();

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
      * @param o
      * @return
      */
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
         } else if (o instanceof String ) {
             try
             {
                 return Integer.parseInt((String)o);
             }
             catch(NumberFormatException ex)
             {
                 return null;
             }
         } else {
             return null;
         }
     }

    static public void addParameters(String[] extraParameters, ConfigurationSection parameters)
    {
        if (extraParameters != null)
        {
            for (int i = 0; i < extraParameters.length - 1; i += 2)
            {
                ConfigurationUtils.set(parameters, extraParameters[i], extraParameters[i + 1]);
            }
        }
    }

    static public String getParameters(ConfigurationSection parameters) {
        Collection<String> parameterStrings = new ArrayList<String>();
        Collection<String> keys = parameters.getKeys(false);
        for (String key : keys) {
            parameterStrings.add(key);
            parameterStrings.add(parameters.getString(key));
        }
        return StringUtils.join(parameterStrings, ' ');
    }

    public static SoundEffect toSoundEffect(String soundConfig) {
        return new SoundEffect(soundConfig);
    }

    public static ParticleEffect toParticleEffect(String effectParticleName) {
        ParticleEffect effectParticle = null;
        if (effectParticleName.length() > 0) {
            String particleName = effectParticleName.toUpperCase();
            try {
                effectParticle = ParticleEffect.valueOf(particleName);
            } catch (Exception ex) {
            }
        }

        return effectParticle;
    }

}
