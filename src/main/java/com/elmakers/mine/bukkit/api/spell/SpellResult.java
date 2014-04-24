package com.elmakers.mine.bukkit.api.spell;

/**
 * Every Spell will return a SpellResult when cast. This result
 * will determine the messaging and effects used, as well as whether 
 * or not the Spell cast consumes its CastingCost costs. 
 * 
 * A Spell that fails to cast will not consume costs or register for cooldown.
 */
public enum SpellResult {
	CAST, 
	AREA,
	FIZZLE,
	BACKFIRE,
	FAIL,
	CANCEL,
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
	
	/**
	 * Determine if this result is a success or not.
	 * 
	 * @return True if this cast was a success.
	 */
	public boolean isSuccess() {
		return this == CAST || this == AREA || this == FIZZLE || this == BACKFIRE;
	}
}
