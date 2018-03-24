package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class AnimateAction extends CompoundAction {

	@Override
	public SpellResult step(CastContext context) {
		return SpellResult.NO_ACTION;
	}

	public enum TargetMode {
		STABILIZE, WANDER, GLIDE, HUNT, FLEE, DIRECTED
	}

	public enum TargetType {
		PLAYER, MAGE, MOB, AUTOMATON, ANY
	}

	public static boolean DEBUG = false;
}
