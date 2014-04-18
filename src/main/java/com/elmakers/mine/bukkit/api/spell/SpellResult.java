package com.elmakers.mine.bukkit.api.spell;

public enum SpellResult {
	CAST, 
	AREA,
	FIZZLE,
	BACKFIRE,
	FAIL,
	INSUFFICIENT_RESOURCES, 
	INSUFFICIENT_PERMISSION, 
	COOLDOWN, 
	NO_TARGET,
	RESTRICTED,
	TARGET_SELECTED,
	PLAYER_REQUIRED,
	WORLD_REQUIRED,
	INVALID_WORLD,
	COST_FREE;
	
	public boolean isSuccess() {
		return this == CAST || this == AREA || this == FIZZLE || this == BACKFIRE;
	}
}
