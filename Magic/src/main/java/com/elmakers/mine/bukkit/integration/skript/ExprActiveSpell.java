package com.elmakers.mine.bukkit.integration.skript;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Active Spell")
@Description({"The active spell for a player"})
@Examples({"active spell of player"})
public class ExprActiveSpell extends SimplePropertyExpression<Player, String> {
    public static void register() {
    	register(ExprActiveSpell.class, String.class, "active spell", "players");
    }

	@Override
	protected String[] get(final Event e, final Player[] source) {
		return super.get(source, new Converter<Player, String>() {
			@SuppressWarnings("null")
			@Override
			public String convert(final Player p) {
				Mage mage = MagicPlugin.getAPI().getController().getRegisteredMage(p);
				if (mage == null) return "";
				Wand wand = mage.getActiveWand();
				if (wand != null) {
					return wand.getActiveSpellKey();
				}
				MageClass mageClass = mage.getActiveClass();
				if (mageClass != null) {
					return mageClass.getProperty("active_spell", "");
				}
				return mage.getProperties().getProperty("active_spell", "");
			}
		});
	}

	@Override
	@Nullable
	public String convert(final Player p) {
		assert false;
		return null;
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		if (mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.REMOVE_ALL)
			return null;
		return new Class<?>[] {String.class};
	}

	@Override
	public void change(final Event e, final @Nullable Object[] delta, final Changer.ChangeMode mode) {
		assert mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.REMOVE_ALL;
		for (final Player p : getExpr().getArray(e)) {
			Mage mage = MagicPlugin.getAPI().getController().getRegisteredMage(p);
			if (mage == null) continue;

			Wand wand = mage.getActiveWand();
			if (wand == null) {
				continue;
			}

			final String newSpell = delta == null ? null : ((String) delta[0]);

			int level;
			if (getTime() > 0 && e instanceof PlayerDeathEvent && ((PlayerDeathEvent) e).getEntity() == p && !Delay.isDelayed(e)) {
				level = ((PlayerDeathEvent) e).getNewLevel();
			} else {
				level = p.getLevel();
			}
			switch (mode) {
				case SET:
					wand.setActiveSpell(newSpell);
					break;
				case REMOVE_ALL:
					wand.setActiveSpell(null);
					break;
				default:
					assert false;
					continue;
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "active spell";
	}
}
