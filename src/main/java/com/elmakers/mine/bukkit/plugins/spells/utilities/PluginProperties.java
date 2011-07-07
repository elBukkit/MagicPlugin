package com.elmakers.mine.bukkit.plugins.spells.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;

public class PluginProperties extends Properties 
{
	static final long serialVersionUID = 0;
	static final Logger log = Logger.getLogger("minecraft");
	private String fileName;
	
	public PluginProperties(String file)
	{
		fileName = file;
	}
	
	public void load()
	{
		File file = new File(fileName);
		if (file.exists())
		{
			try 
			{
				load(new FileInputStream(fileName));
			} 
			catch (IOException ex)
			{
			    log.log(Level.SEVERE, "Unable to load " + fileName, ex);
			}
		}
	}
	
	public void save()
	{
		try 
		{
		    store(new FileOutputStream(fileName), "Minecraft Properties File");
		} 
		catch (IOException ex) 
		{
		    log.log(Level.SEVERE, "Unable to save " + fileName, ex);
		}
	}
	
	public int getInteger(String key, int value)
	{
		if (containsKey(key)) 
		{
            return Integer.parseInt(getProperty(key));
        }

		put(key, String.valueOf(value));
        return value;
	}

	public double getDouble(String key, double value)
	{
		if (containsKey(key)) 
		{
            return Double.parseDouble(getProperty(key));
        }

		put(key, String.valueOf(value));
        return value;
	}
	
	public String getString(String key, String value)
	{
		if (containsKey(key)) 
		{
            return getProperty(key);
        }

		put(key, value);
        return value;
	}
	
	public boolean getBoolean(String key, boolean value)
	{
		if (containsKey(key)) 
		{
            String boolString = getProperty(key);
            return (boolString.length() > 0 && boolString.toLowerCase().charAt(0) == 't');
        }
		put(key, value ? "true" : "false");
        return value;
	}
	
	public Material getMaterial(String key, Material material)
	{
		return Material.getMaterial(getInteger(key, material.getId()));
	}
	
	public static List<Material> parseMaterials(String csvList)
	{
		List<Material> materials = new ArrayList<Material>();
		
		String[] matIds = csvList.split(",");
		for (String matId : matIds)
		{
			try
			{
				int typeId = Integer.parseInt(matId.trim());
				materials.add(Material.getMaterial(typeId));
			}
			catch (NumberFormatException ex)
			{
				
			}
		}
		return materials;
	}
	
	public static List<Integer> parseIntegers(String csvList)
	{
		List<Integer> ints = new ArrayList<Integer>();
		
		String[] intStrings = csvList.split(",");
		for (String s : intStrings)
		{
			try
			{
				int thisInt = Integer.parseInt(s.trim());
				ints.add(thisInt);
			}
			catch (NumberFormatException ex)
			{
				
			}
		}
		return ints;
	}
	
	public List<Material> getMaterials(String key, String csvList)
	{
		if (containsKey(key)) 
		{
			csvList = getProperty(key);
		}
		
		put(key, csvList);
		
		return parseMaterials(csvList);
	}
	
	public List<String> getStringList(String key, String csvList)
	{
		if (containsKey(key)) 
		{
			csvList = getProperty(key);
		}
		List<String> strings = new ArrayList<String>();
		
		String[] defaultStrings = csvList.split(",");
		for (String defaultString : defaultStrings)
		{
			strings.add(defaultString);
		}
		put(key, csvList);
		
		return strings;
	}
	
	public List<Integer> getIntegers(String key, String csvList)
	{
		if (containsKey(key)) 
		{
			csvList = getProperty(key);
		}
		
		put(key, csvList);
		
		return parseIntegers(csvList);
	}
}
