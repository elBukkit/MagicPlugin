package com.elmakers.mine.bukkit.api.spell;

public interface CastingCost {
	public boolean hasCosts(CostReducer reducer);
	public String getDescription(CostReducer reducer);
	public String getFullDescription(CostReducer reducer);
	public int getXP();
}
