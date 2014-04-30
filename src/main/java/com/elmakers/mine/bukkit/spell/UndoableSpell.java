package com.elmakers.mine.bukkit.spell;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.block.UndoList;

public abstract class UndoableSpell extends TargetingSpell {
	private UndoList 		modifiedBlocks 			= null;
	private boolean 		bypassUndo				= false;
	private int	 			autoUndo				= 0;
	
	@Override
	protected void processParameters(ConfigurationSection parameters) 
	{
		super.processParameters(parameters);
		bypassUndo = parameters.getBoolean("bypass_undo", false);
		bypassUndo = parameters.getBoolean("bu", bypassUndo);
		autoUndo = parameters.getInt("undo", 0);
		autoUndo = parameters.getInt("u", autoUndo);
	}
	
	@Override
	protected void preCast()
	{
		super.preCast();
		modifiedBlocks = null;
	}
	
	public int getModifiedCount() 
	{
		return modifiedBlocks == null ? 0 : modifiedBlocks.size();
	}

	public void registerForUndo()
	{
		if (modifiedBlocks == null || modifiedBlocks.isEmpty()) return;
		
		mage.registerForUndo(modifiedBlocks);	
	}
	
	public void registerForUndo(Block block)
	{
		getUndoList().add(block);
	}
	
	public void registerForUndo(Entity entity)
	{
		if (entity instanceof Player) return;
		getUndoList().add(entity);
	}
	
	public void registerRemoved(Entity entity)
	{
		if (entity instanceof Player) return;
		getUndoList().addRemoved(entity);
	}
	
	public void watch(Entity entity)
	{
		if (entity == null) return;
		getUndoList().watch(entity);
	}
	
	public UndoList getUndoList()
	{
		if (modifiedBlocks == null) {
			modifiedBlocks = new UndoList(controller.getPlugin());
			modifiedBlocks.setBypass(bypassUndo);
			modifiedBlocks.setScheduleUndo(autoUndo);
		}
		return modifiedBlocks;
	}
	
	public boolean contains(Block block)
	{
		return modifiedBlocks.contains(block);
	}
	
	public int getScheduledUndo()
	{
		return autoUndo;
	}
}
