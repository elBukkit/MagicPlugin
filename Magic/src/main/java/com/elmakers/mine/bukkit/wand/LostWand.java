package com.elmakers.mine.bukkit.wand;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class LostWand implements com.elmakers.mine.bukkit.api.wand.LostWand {
	private Location location;
	private String id;
	private String name;
	private String description;
	private String owner;
    private String ownerId;

	public LostWand(String id, ConfigurationSection config) {
		this.id = id;
		load(config);
	}
	
	public LostWand(Wand wand, Location location) {
		update(wand, location);
	}
	
	public void update(Wand wand, Location location) {
		this.location = location;
		this.id = wand.getId();
		this.name = wand.getName();
		this.owner = wand.getOwner();
        this.ownerId = wand.getOwnerId();
		this.description = wand.getHTMLDescription();
	}
	
	public void update(LostWand other) {
		this.location = other.location;
		this.name = other.getName();
		this.owner = other.getOwner();
        this.ownerId = other.getOwnerId();
		String description = other.getDescription();
		if (description != null && description.length() > 0) {
			this.description = description;
		}
	}
	
	public boolean isValid() {
		return location != null && id != null && id.length() > 0;
	}
	
	public void save(ConfigurationSection configNode)
	{
		try {
			configNode.set("location", ConfigurationUtils.fromLocation(location));
			configNode.set("name", name);
			configNode.set("description", description);
			configNode.set("owner", owner);
            configNode.set("owner_id", ownerId);
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}
	
	public void load(ConfigurationSection configNode)
	{
		try {
			if (configNode == null) return;

			location = ConfigurationUtils.getLocation(configNode, "location");
            name = configNode.getString("name");
			description = configNode.getString("description");
			owner = configNode.getString("owner");
            ownerId = configNode.getString("owner_id");
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (!(obj instanceof LostWand)) {
	        return false;
	    }
	    
        LostWand other = (LostWand)obj;
        return other.id.equals(id);
	}

	@Override
	public int hashCode() {
	    return id.hashCode();
	}
	
	@Override
    public String getId() {
		return id;
	}
	
	@Override
    public String getOwner() {
		return owner;
	}

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name =  name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description =  description;
	}
	
	@Override
    public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
}
