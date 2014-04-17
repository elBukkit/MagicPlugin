package com.elmakers.mine.bukkit.block;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utilities.Messages;

public class Automaton extends BlockData implements com.elmakers.mine.bukkit.api.magic.Automaton {
	private String message;
	private String name;
	private long createdAt;
	
	public Automaton(ConfigurationSection node) {
		super(node);
		name = node.getString("name");
		message = node.getString("message");
		createdAt = node.getLong("created", 0);
	}
	
	public Automaton(Block block, String name, String message) {
		super(block);
		
		this.name = name;
		this.message = message;
		this.createdAt = System.currentTimeMillis();
	}
	
	@Override
	public void save(ConfigurationSection node) {
		super.save(node);
		node.set("name", name);
		node.set("message", message);
		node.set("created", createdAt);
	}
	
	public String getMessage() {
		if (message == null || message.length() == 0 || name == null || name.length() == 0) return "";
		String contents = Messages.get(message);
		if (contents == null || contents.length() == 0) return "";
		return contents.replace("$name", name);
	}
	
	public String getName() {
		return name;
	}
	
	public long getCreatedTime() {
		return createdAt;
	}
}
