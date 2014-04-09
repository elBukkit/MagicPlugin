package com.elmakers.mine.bukkit.block;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.utilities.Messages;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class Automaton extends BlockData {
	private String message;
	private String name;
	private long createdAt;
	
	public Automaton(ConfigurationNode node) {
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
	public void save(ConfigurationNode node) {
		super.save(node);
		node.setProperty("name", name);
		node.setProperty("message", message);
		node.setProperty("created", createdAt);
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
