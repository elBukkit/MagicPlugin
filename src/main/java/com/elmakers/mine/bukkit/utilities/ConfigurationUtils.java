package com.elmakers.mine.bukkit.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.RandomUtils;

public class ConfigurationUtils {
	
	public static Location getLocation(ConfigurationSection node, String path) {
		String stringData = node.getString(path);
		if (stringData == null) {
			return null;
		}

		return toLocation(stringData);
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
			Material material = null;
			byte data = 0;
			String[] pieces = StringUtils.split(matName, ':');
			if (pieces.length > 0) {
				if (pieces.length > 1) {
					try {
						data = Byte.parseByte(pieces[1]);
					}
					catch(NumberFormatException ex)
					{
						data = 0;
					}
				}
				try
				{
					Integer value = Integer.parseInt(pieces[0]);
					if (value != null)
					{
						material = Material.getMaterial(value);
					}
				}
				catch(NumberFormatException ex)
				{
					material = Material.getMaterial(pieces[0].toUpperCase());
				}
			}
			
			if (material == null) return null;
			return new MaterialAndData(material, data);
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
	
	public static ConfigurationSection addConfigurations(ConfigurationSection first, ConfigurationSection second)
	{
		Set<String> keys = second.getKeys(false);
		for (String key : keys) {
			first.set(key, second.get(key));
		}
		
		return first;
	}
	
	protected static double overrideDouble(ConfigurationSection node, double value, String nodeName)
	{
		String override = node.getString(nodeName);
		if (override == null || override.length() == 0) return value;
		try {
			if (override.startsWith("~")) {
				override = override.substring(1);
				value = value + Double.parseDouble(override);
			} else {
				value = Double.parseDouble(override);
			}	
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
		
		return value;
	}
	
	public static Location getLocationOverride(ConfigurationSection node, String basePath, Location location) {
		String worldName = basePath + "world";
		String xName = basePath + "x";
		String yName = basePath + "y";
		String zName = basePath + "z";
		String dxName = basePath + "dx";
		String dyName = basePath + "dy";
		String dzName = basePath + "dz";
		boolean hasPosition = node.contains(xName) || node.contains(yName) || node.contains(zName);
		boolean hasDirection = node.contains(dxName) || node.contains(dyName) || node.contains(dzName);
		String worldOverride = node.getString(worldName);
		boolean hasWorld = worldOverride != null && worldOverride.length() > 0;
		
		if (!hasPosition && !hasDirection && !hasWorld) return null;

		if (location == null) {
			if (!hasWorld || !hasPosition) return null;
			location = new Location(Bukkit.getWorld(worldOverride), 0, 0, 0);
		} else {
			location = location.clone();
			if (hasWorld) {
				location.setWorld(Bukkit.getWorld(worldOverride));
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
			RandomUtils.setDirection(location, direction);
		}
		
		return location;
	}

	public static Color getColor(ConfigurationSection node, String path, Color def) {
		Color o = castColor(node.get(path));
		return o == null ? def : o;
	}

	private static Color castColor(Object o) {
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
				Integer rgb = Integer.parseInt((String) o, 16);
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
		if (node.contains(path)) return node.getDouble(path);
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
}
