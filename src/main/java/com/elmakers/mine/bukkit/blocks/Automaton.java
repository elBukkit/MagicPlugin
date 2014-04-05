package com.elmakers.mine.bukkit.blocks;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class Automaton extends BlockData {
	private String message;
	private long createdAt;
	
	public Automaton(ConfigurationNode node) {
		super(node);
		message = node.getString("message");
		createdAt = node.getLong("created", 0);
	}
	
	public Automaton(Block block, String message) {
		super(block);
		
		this.message = message;
		this.createdAt = System.currentTimeMillis();
	}
	
	@Override
	public void save(ConfigurationNode node) {
		super.save(node);
		node.setProperty("message", message);
		node.setProperty("created", createdAt);
	}
	
	public String getMessage() {
		return message;
	}
	
	public long getCreatedTime() {
		return createdAt;
	}
}
