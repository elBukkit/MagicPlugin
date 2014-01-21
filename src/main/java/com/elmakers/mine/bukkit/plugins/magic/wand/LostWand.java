package com.elmakers.mine.bukkit.plugins.magic.wand;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class LostWand {
	private Location location;
	private String id;
	private String name;
	private String description;
	private String owner;
	
	public LostWand(String id, ConfigurationNode config) {
		this.id = id;
		load(config);
	}
	
	public LostWand(Wand wand, Location location) {
		this.location = location;
		this.id = wand.getId();
		this.name = wand.getName();
		this.owner = wand.getOwner();
		this.description = wand.getHTMLDescription();
	}
	
	public boolean isValid() {
		return location != null && id != null && id.length() > 0;
	}
	
	public void save(ConfigurationNode configNode)
	{
		try {
			configNode.setProperty("location", location);
			configNode.setProperty("name", name);
			configNode.setProperty("description", description);
			configNode.setProperty("owner", owner);
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}
	
	public void load(ConfigurationNode configNode)
	{
		try {
			if (configNode == null) return;

			location = configNode.getLocation("location");
			name = configNode.getString("name");
			description = configNode.getString("description");
			owner = configNode.getString("owner");
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public boolean equals(Object obj) {
	    if (!(obj instanceof LostWand)) {
	        return false;
	    }
	    
        LostWand other = (LostWand)obj;
        return other.id.equals(id);
	}

	public int hashCode() {
	    return id.hashCode();
	}
	
	public String getId() {
		return id;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
}
